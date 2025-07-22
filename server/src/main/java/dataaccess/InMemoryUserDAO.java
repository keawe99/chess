package dataaccess;

import model.UserData;

import java.util.HashMap;

public class InMemoryUserDAO implements UserDAOInterface {
    private final HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.getUsername())) {
            throw new DataAccessException("Error: already taken", 409);
        }
        users.put(user.getUsername(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void clear() {
        users.clear();
    }
}
