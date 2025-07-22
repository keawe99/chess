package dataaccess.dao;

import dataaccess.DataAccessException;
import model.UserData;

public interface UserDAOInterface {
    void insertUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
