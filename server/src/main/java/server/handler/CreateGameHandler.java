package server.handler;

import com.google.gson.Gson;
import dataaccess.dao.AuthDAO;
import dataaccess.DataAccessException;
import model.CreateGameRequest;
import model.CreateGameResponse;
import model.ErrorResponse;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService, AuthDAO authDAO) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);
            if (request == null || request.gameName() == null || request.gameName().isBlank()) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            CreateGameResponse result = gameService.createGame(request, authToken);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(500); // Database failure = internal error
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: internal server error"));
        }
    }
}
