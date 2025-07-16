package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccessInterface;
import dataaccess.GameDAO;
import dataaccess.MemoryDataAccess;

import model.AuthData;
import model.GameData;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.CreateGameResponse;

import java.util.Collection;


public class GameService {

    private final DataAccessInterface dataAccess;

    public GameService(GameDAO gameDAO) {
        this.dataAccess = MemoryDataAccess.getInstance(); // Adjust if you're injecting or using another DAO
    }

    /**
     * Lists all games for the authenticated user.
     */
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        return dataAccess.listGames();
    }

    /**
     * Creates a new game and returns its ID.
     */
    public CreateGameResponse createGame(CreateGameRequest request, String authToken) throws DataAccessException {
        if (authToken == null || request == null || request.gameName() == null || request.gameName().isEmpty()) {
            throw new DataAccessException("Error: bad request", 400);
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        int newGameID = dataAccess.createGame(request.gameName());
        return new CreateGameResponse(newGameID);
    }

    /**
     * Allows a user to join a game as WHITE or BLACK if the spot is available.
     */
    public void joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        if (request == null || authToken == null || request.playerColor() == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        String username = auth.username();

        switch (request.playerColor().toUpperCase()) {
            case "WHITE" -> {
                if (game.whiteUsername() != null) {
                    throw new DataAccessException("Error: already taken", 403);
                }
                game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            }
            case "BLACK" -> {
                if (game.blackUsername() != null) {
                    throw new DataAccessException("Error: already taken", 403);
                }
                game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            }
            default -> throw new DataAccessException("Error: bad request", 400);
        }

        dataAccess.updateGame(game);
    }
}
