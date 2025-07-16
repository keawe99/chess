package dataaccess;

import model.AuthData;
import java.util.HashMap;

public class AuthDAO {
    private final HashMap<String, AuthData> tokens = new HashMap<>();

    // ✅ Explicit no-argument constructor
    public AuthDAO() {
        // No special setup needed
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

    // Optional: You may want to remove or implement this method
    public void createAuth(AuthData authData) {
        insertAuth(authData);
    }

    public Object read(String authToken) {
        return tokens.get(authToken);
    }
}
