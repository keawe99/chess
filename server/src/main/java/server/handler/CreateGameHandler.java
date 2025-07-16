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
    private final AuthDAO authDAO;  // Need this to validate token
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService, AuthDAO authDAO) {
        this.gameService = gameService;
        this.authDAO = authDAO;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            // Step 1: Validate auth token
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank() || authDAO.read(authToken) == null) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            // Step 2: Parse and validate request body
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);
            if (request == null || request.gameName() == null || request.gameName().isBlank()) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            // Step 3: Call service to create game
            CreateGameResponse result = gameService.createGame(request, authToken);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            res.status(400); // Bad input or auth error from service layer
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500); // Server-side error
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
