package server.handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private ChessPosition parseChessPosition(String pos) {
        if (pos == null || pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format: " + pos);
        }

        char colChar = Character.toLowerCase(pos.charAt(0));
        char rowChar = pos.charAt(1);

        int column = colChar - 'a' + 1;
        int row = rowChar - '0';

        if (column < 1 || column > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Invalid position range: " + pos);
        }

        return new ChessPosition(row, column);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        String token = session.getUpgradeRequest().getParameterMap().getOrDefault("token", null) != null ?
                session.getUpgradeRequest().getParameterMap().get("token").get(0) : null;

        if (token == null || authDAO.getAuth(token) == null) {
            session.close(401, "Unauthorized: Invalid token");
            return;
        }

        String username = authDAO.getAuth(token).getUsername();
        sessionUserMap.put(session, username);

        session.getRemote().sendString(gson.toJson(Map.of("type", "connected", "username", username)));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        String username = sessionUserMap.get(session);
        if (username == null) {
            session.close(401, "Unauthorized: No username found");
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
                int moveGameId = ((Double) msg.get("gameId")).intValue();
                Map<?, ?> moveMap = (Map<?, ?>) msg.get("move");
                handleMove(session, moveGameId, moveMap, username);
            }
            case "resign" -> {
                int resignGameId = ((Double) msg.get("gameId")).intValue();
                handleResign(session, resignGameId, username);
            }
            default -> {
                session.getRemote().sendString(gson.toJson(Map.of(
                        "type", "error",
                        "message", "Unknown message type"
                )));
            }
        }
    }

    private void handleResign(Session session, int gameId, String username) throws Exception {
        GameData game = gameService.getGameById(gameId);
        if (game == null) {
            session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Game not found")));
            return;
        }

        boolean isPlayer = username.equals(game.whiteUsername()) || username.equals(game.blackUsername());
        if (!isPlayer) {
            session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Must be a player to resign")));
            return;
        }

        var resignMsg = Map.of(
                "type", "playerResigned",
                "gameId", gameId,
                "resignedBy", username
        );

        for (Session s : gameSessions.getOrDefault(gameId, ConcurrentHashMap.newKeySet())) {
            try {
                s.getRemote().sendString(gson.toJson(resignMsg));
            } catch (Exception ignored) {}
        }

        // Optional: update game state in DB
    }

    private void handleMove(Session session, int gameId, Map<?, ?> moveMap, String username) {
        try {
            GameData gameData = gameService.getGameById(gameId);
            if (gameData == null) {
                session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Game not found")));
                return;
            }

            ChessGame game = gson.fromJson(gameData.gameData(), ChessGame.class);

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
            var move = new ChessMove(from, to, null); // Extend for promotion if needed

            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Invalid move")));
                return;
            }

            String updatedGameJson = gson.toJson(game);
            gameService.updateGame(gameId, updatedGameJson);

            var updateMsg = Map.of(
                    "type", "moveMade",
                    "gameId", gameId,
                    "move", Map.of("from", from.toString(), "to", to.toString()),
                    "board", game.getBoard().toString()
            );

            for (Session s : gameSessions.getOrDefault(gameId, ConcurrentHashMap.newKeySet())) {
                try {
                    s.getRemote().sendString(gson.toJson(updateMsg));
                } catch (Exception ignored) {}
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

        session.getRemote().sendString(gson.toJson(Map.of("type", "gameState", "game", game)));
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessionUserMap.remove(session);
        for (var sessions : gameSessions.values()) {
            sessions.remove(session);
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}
