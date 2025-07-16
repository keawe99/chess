package server.handler;

import com.google.gson.Gson;
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

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        try {
            String authToken = req.headers("Authorization");
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

            CreateGameResponse result = gameService.createGame(request, authToken);

            res.status(200); // Success
            return gson.toJson(result);
        } catch (Exception e) {
            res.status(400);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
