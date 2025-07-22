package dataaccess;

import model.UserData;
import java.util.HashMap;

public class UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    // ✅ Explicit no-argument constructor
    public UserDAO() {
        // No special setup needed
    }

    public void insertUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.getUsername())) {
            throw new DataAccessException("Error: already taken", 409);
        }
        users.put(user.getUsername(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }


}
