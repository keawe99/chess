package server.handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
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

    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");

            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

            CreateGameResponse result = gameService.createGame(request, authToken);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
