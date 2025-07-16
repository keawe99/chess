// File: server/src/main/java/dataaccess/MemoryDataAccess.java
package dataaccess;

import model.*;

import java.util.*;

public class MemoryDataAccess implements DataAccessInterface {
    private static MemoryDataAccess instance;

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

    private MemoryDataAccess() {}

    public static MemoryDataAccess getInstance() {
        if (instance == null) {
            instance = new MemoryDataAccess();
        }
        return instance;
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameId = 1;
    }

    @Override
    public void insertUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public int createGame(String gameName) {
        int id = nextGameId++;
        GameData game = new GameData(id, null, null, gameName, new chess.ChessGame());
        games.put(id, game);
        return id;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public void insertAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }
}
