package dataaccess;

import model.AuthData;

public interface AuthDAOInterface {
    void insertAuth(AuthData auth) throws DataAccessException;

    AuthData getAuth(String token) throws DataAccessException;

    void deleteAuth(String token) throws DataAccessException;

    void clear() throws DataAccessException;
}
