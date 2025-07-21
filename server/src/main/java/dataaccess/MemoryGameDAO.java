package dataaccess;

import model.GameData;
import chess.ChessGame;

import java.util.*;

public class MemoryGameDAO implements GameDAOInterface {
    private final Map<Integer, GameData> games = new HashMap<>();
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
        GameData game = new GameData(id, null, null, gameName, new ChessGame());
        games.put(id, game);
        return game;
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
