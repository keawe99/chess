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
        if ((request == null)) {
            throw new IllegalArgumentException("Missing game ID");
        }

        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token");
        }

        GameData game = gameDAO.getGame(request.getGameID());
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }

        String color = request.getPlayerColor();
        if (color != null) {
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                throw new IllegalArgumentException("Invalid player color");
            }

            if (color.equals("WHITE") && game.getWhiteUsername() != null) {
                throw new IllegalStateException("White already taken");
            }

            if (color.equals("BLACK") && game.getBlackUsername() != null) {
                throw new IllegalStateException("Black already taken");
            }

            gameDAO.updateGamePlayer(request.getGameID(), color, auth.username());
        }
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
