package dataaccess.dao;

import dataaccess.DataAccessException;
import model.GameData;

import java.util.Collection;

public interface GameDAOInterface {
    Collection<GameData> listGames() throws DataAccessException;
    void clear() throws DataAccessException;
    GameData createGame(String gameName) throws DataAccessException;

    int createGame(GameData game) throws DataAccessException;

    GameData getGame(int gameId) throws DataAccessException;
    void updateGame(GameData updatedGame) throws DataAccessException;
}
