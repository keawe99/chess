package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccessInterface {

    // Auth methods
    AuthData getAuth(String authToken);
    void insertAuth(AuthData auth);
    void deleteAuth(String authToken);

    // Game methods
    int createGame(String gameName);
    Collection<GameData> listGames();
    GameData getGame(int gameID);
    void updateGame(GameData game);

    // User methods
    void insertUser(UserData user);
    UserData getUser(String username);

    // Utility
    void clear();
}
