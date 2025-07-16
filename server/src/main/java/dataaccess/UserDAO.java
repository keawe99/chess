// UserDAO.java
package dataaccess;

import model.UserData;
import java.util.HashMap;

public class UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    public void insertUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Error: already taken");
        }
        users.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }
}
