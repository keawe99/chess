package dataaccess;

import model.AuthData;
import java.util.HashMap;

public class AuthDAO {
    private static AuthDAO instance = null;
    private final HashMap<String, AuthData> tokens = new HashMap<>();

    public AuthDAO() {}

    public static synchronized AuthDAO getInstance() {
        if (instance == null) {
            instance = new AuthDAO();
        }
        return instance;
    }

    public void insertAuth(AuthData auth) {
        tokens.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String token) {
        return tokens.get(token);
    }

    public void deleteAuth(String token) {
        tokens.remove(token);
    }

    public void clear() {
        tokens.clear();
    }

    public AuthData read(String authToken) {
        return tokens.get(authToken);
    }

    public void createAuth(AuthData authData) {
    }
}


