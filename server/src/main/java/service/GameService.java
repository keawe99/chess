package service;

import dataaccess.*;
import model.*;

import java.util.List;
import java.util.stream.Collectors;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
        return gameDAO.createGame(gameName);
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        List<GameData> list = gameDAO.listGames().stream().toList();
        return list;
    }

    public void joinGame(String authToken, int gameID, String color) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData game = gameDAO.getGame(gameID);
        if (game == null) throw new DataAccessException("Error: bad request");

        if (color.equalsIgnoreCase("WHITE")) {
            if (game.whiteUsername() != null) throw new DataAccessException("Error: already taken");
            game = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
        } else if (color.equalsIgnoreCase("BLACK")) {
            if (game.blackUsername() != null) throw new DataAccessException("Error: already taken");
            game = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
        } else {
            throw new DataAccessException("Error: bad request");
        }

        gameDAO.updateGame(game);
    }
}
