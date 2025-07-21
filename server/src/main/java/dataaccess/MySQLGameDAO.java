package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.List;

public class MySQLGameDAO implements GameDAOInterface {
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {

    }
    // ... your methods here
}
