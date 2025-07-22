package service;

import dataaccess.*;

import dataaccess.dao.AuthDAO;
import dataaccess.dao.GameDAOInterface;
import model.AuthData;
import model.GameData;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.CreateGameResponse;

import java.util.Collection;


public class GameService {
    private final GameDAOInterface gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAOInterface gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }


    /**
     * Lists all games for the authenticated user.
     */
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);

        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        return gameDAO.listGames();
    }

    /**
     * Creates a new game and returns its ID.
     */
    public CreateGameResponse createGame(CreateGameRequest request, String authToken) throws DataAccessException {
        if (authToken == null || request == null || request.gameName() == null || request.gameName().isEmpty()) {
            throw new DataAccessException("Error: bad request", 400);
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        int newGameID = gameDAO.createGame(request.gameName()).gameID();
        return new CreateGameResponse(newGameID);
    }

    public GameData getGameById(int gameId) throws DataAccessException {
        GameData game = gameDAO.getGame(gameId);
        if (game == null) {
            throw new DataAccessException("Error: game not found", 404);
        }
        return game;
    }


    /**
     * Allows a user to join a game as WHITE or BLACK if the spot is available.
     */
    public void joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        if (request == null || authToken == null || request.playerColor() == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        String username = auth.username();

        switch (request.playerColor().toUpperCase()) {
            case "WHITE" -> {
                if (game.whiteUsername() != null) {
                    throw new DataAccessException("Error: already taken", 403);
                }
                game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.gameData());
            }
            case "BLACK" -> {
                if (game.blackUsername() != null) {
                    throw new DataAccessException("Error: already taken", 403);
                }
                game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.gameData());
            }
            default -> throw new DataAccessException("Error: bad request", 400);
        }



        gameDAO.updateGame(game);
    }

    public void updateGame(int gameId, String updatedGame) throws DataAccessException {
        GameData oldGame = gameDAO.getGame(gameId);
        if (oldGame == null) {
            throw new DataAccessException("Game not found", 404);
        }
        GameData updated = new GameData(gameId, oldGame.whiteUsername(), oldGame.blackUsername(), oldGame.gameName(), updatedGame);
        gameDAO.updateGame(updated);
    }

}
