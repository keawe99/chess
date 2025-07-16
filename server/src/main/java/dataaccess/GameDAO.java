package dataaccess;

import model.GameData;
import chess.ChessGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private final MemoryDataAccess dataAccess = MemoryDataAccess.getInstance();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

    public Collection<GameData> listGames() {
        return games.values();
    }

    public void clear() {
        games.clear();
        nextGameId = 1;
    }

    public GameData createGame(String gameName) {
        int id = nextGameId++;
        GameData game = new GameData(id, null, null, gameName, new ChessGame());
        games.put(id, game);
        return game;
    }

    public GameData getGame(int gameId) {
        return games.get(gameId);
    }

    public void updateGame(GameData updatedGame) {
        games.put(updatedGame.gameID(), updatedGame);
    }
}
