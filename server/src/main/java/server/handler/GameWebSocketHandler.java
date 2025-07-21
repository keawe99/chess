package server.handler;

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

        // Parse message, e.g. {"type": "joinGame", "gameId": 123}
        Map<?, ?> msg = gson.fromJson(message, Map.class);
        String type = (String) msg.get("type");

        switch (type) {
            case "joinGame":
                int gameId = ((Double) msg.get("gameId")).intValue();
                joinGame(session, gameId);
                break;

            case "makeMove":
                // Implement move logic here
                // You'll want to validate moves, update game state, broadcast updates
                break;

            case "resign":
                // Handle resign logic
                break;

            default:
                session.getRemote().sendString(gson.toJson(Map.of("type", "error", "message", "Unknown message type")));
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
