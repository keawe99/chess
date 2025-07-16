package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
    }

    // We'll add more methods here later (e.g., createUser, getGame, etc.)
}
