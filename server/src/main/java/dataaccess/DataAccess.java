// File: server/src/main/java/dataaccess/DataAccessInterface.java
package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
    void clear() throws DataAccessException;

    void insertUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;

    void insertAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
}
