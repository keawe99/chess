package dataaccess.memory;

import dataaccess.DataAccessException;
import dataaccess.dao.GameDAOInterface;
import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;

import java.util.*;

public class MemoryGameDAO implements GameDAOInterface {
    private final Map<Integer, GameData> games = new HashMap<>();
    private final Gson gson = new Gson();
    private int nextGameId = 1;

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void clear() {
        games.clear();
        nextGameId = 1;
    }

    @Override
    public GameData createGame(String gameName) {
        int id = nextGameId++;
        ChessGame gameObj = new ChessGame();
        String gameJson = gson.toJson(gameObj); // serialize
        GameData game = new GameData(id, null, null, gameName, gameJson);
        games.put(id, game);
        return game;
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameId) {
        return games.get(gameId);
    }

    @Override
    public void updateGame(GameData updatedGame) {
        games.put(updatedGame.gameID(), updatedGame);
    }
}
