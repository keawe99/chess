package server.handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.JoinGameRequest;
import model.ErrorResponse;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final AuthDAO authDAO;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService, AuthDAO authDAO) {
        this.gameService = gameService;
        this.authDAO = authDAO;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        try {
            String authToken = req.headers("Authorization");
            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);

            gameService.joinGame(joinRequest, authToken);
            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else if (e.getMessage().contains("already taken")) {
                res.status(403);
            } else if (e.getMessage().contains("bad request")) {
                res.status(400);
            } else {
                res.status(500);
            }
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}

