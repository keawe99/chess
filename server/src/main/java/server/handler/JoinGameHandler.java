package server.handler;

import com.google.gson.Gson;
import model.JoinGameRequest;
import model.ErrorResponse;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            if (joinRequest == null || joinRequest.gameID() <= 0) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            gameService.joinGame(joinRequest, authToken);
            res.status(200);
            return "{}"; // Return empty JSON object on success

        } catch (Exception e) {
            res.status(400); // Or 500 if it's a server error
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
