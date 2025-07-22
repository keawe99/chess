package dataaccess.memory;

import dataaccess.DataAccessException;
import dataaccess.dao.UserDAOInterface;
import model.UserData;

import java.util.HashMap;

public class InMemoryUserDAO implements UserDAOInterface {
    private final HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Error: already taken", 409);
        }
        users.put(user.username(), user);
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
