package server.handler;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.GameService;
import chess.ChessGame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class GameWebSocketHandler {

    private static final Gson gson = new Gson();

    // Maps sessions to usernames
    private final Map<Session, String> sessionUserMap = new ConcurrentHashMap<>();

    // Maps game IDs to sets of sessions (players and observers)
    private final Map<Integer, ConcurrentHashMap.KeySetView<Session, Boolean>> gameSessions = new ConcurrentHashMap<>();

    private final AuthDAO authDAO;
    private final GameService gameService;

    public GameWebSocketHandler(AuthDAO authDAO, GameService gameService) {
        this.authDAO = authDAO;
        this.gameService = gameService;
    }

    private chess.ChessPosition parseChessPosition(String pos) {
        if (pos == null || pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format: " + pos);
        }

        char colChar = Character.toLowerCase(pos.charAt(0));
        char rowChar = pos.charAt(1);

        int column = colChar - 'a' + 1;  // 'a' → 1, 'b' → 2, ..., 'h' → 8
        int row = rowChar - '0';         // '1' → 1, ..., '8' → 8

        if (column < 1 || column > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Invalid position range: " + pos);
        }

        return new chess.ChessPosition(row, column);
    }


    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        // Expect token as a query param: ?token=abc123
        String token = session.getUpgradeRequest().getParameterMap().getOrDefault("token", null) != null ?
                session.getUpgradeRequest().getParameterMap().get("token").get(0) : null;

        if (token == null || authDAO.getAuth(token) == null) {
            session.close(4001, "Unauthorized: Invalid token");
            return;
        }

        String username = authDAO.getAuth(token).username();
        sessionUserMap.put(session, username);

        // Optionally send a welcome message or user info
        session.getRemote().sendString(gson.toJson(Map.of("type", "connected", "username", username)));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        String username = sessionUserMap.get(session);
        if (username == null) {
            session.close(4002, "Unauthorized: No username found");
            return;
        }

        Map<?, ?> msg = gson.fromJson(message, Map.class);
        String type = (String) msg.get("type");

        switch (type) {
            case "joinGame" -> {
                int gameId = ((Double) msg.get("gameId")).intValue();
                joinGame(session, gameId);
            }
            case "makeMove" -> {
                int moveGameId = ((Double) msg.get("gameId")).intValue(); // ✅ renamed to avoid conflict
                Map<?, ?> moveMap = (Map<?, ?>) msg.get("move");
                handleMove(session, moveGameId, moveMap, username);
            }
            case "resign" -> {
                // TODO: Implement resign logic later
            }
            default -> {
                session.getRemote().sendString(gson.toJson(Map.of(
                        "type", "error",
                        "message", "Unknown message type"
                )));
            }
        }
    }

    private void handleMove(Session session, int gameId, Map<?, ?> moveMap, String username) {
        try {
            GameData gameData = gameService.getGameById(gameId);
            if (gameData == null) {
                session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Game not found")));
                return;
            }

            ChessGame game = gameData.game();

            // Convert moveMap to ChessMove
            String fromStr = (String) moveMap.get("from");
            String toStr = (String) moveMap.get("to");

            if (fromStr == null || toStr == null) {
                session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Missing move coordinates")));
                return;
            }

            ChessGame.TeamColor playerColor = username.equals(gameData.whiteUsername()) ?
                    ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            var from = parseChessPosition(fromStr);
            var to = parseChessPosition(toStr);

            var move = new chess.ChessMove(from, to, null); // Add promotion piece if needed

            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Invalid move")));
                return;
            }

            // Save updated game state
            gameService.updateGame(gameId, game);

            // Broadcast updated game state
            var updateMsg = Map.of(
                    "type", "moveMade",
                    "gameId", gameId,
                    "move", Map.of("from", from.toString(), "to", to.toString()),
                    "board", game.getBoard().toString()
            );

            for (Session s : gameSessions.getOrDefault(gameId, ConcurrentHashMap.newKeySet())) {
                s.getRemote().sendString(gson.toJson(updateMsg));
            }

        } catch (Exception e) {
            try {
                session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }


    private void joinGame(Session session, int gameId) throws Exception {
        gameSessions.putIfAbsent(gameId, ConcurrentHashMap.newKeySet());
        gameSessions.get(gameId).add(session);

        GameData game = gameService.getGameById(gameId);
        if (game == null) {
            session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Game not found")));
            return;
        }

        // Send the current game state to the user
        session.getRemote().sendString(gson.toJson(Map.of("type", "gameState", "game", game)));
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String username = sessionUserMap.remove(session);
        // Remove session from all game sessions
        for (var sessions : gameSessions.values()) {
            sessions.remove(session);
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}
