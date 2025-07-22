package server.handler;

import com.google.gson.Gson;
import dataaccess.dao.AuthDAO;
import dataaccess.DataAccessException;
import model.JoinGameRequest;
import model.ErrorResponse;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService, AuthDAO authDAO) {
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

            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            gameService.joinGame(joinRequest, authToken);

            res.status(200);
            return "{}";

        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        } catch (IllegalStateException e) {
            res.status(403);
            return gson.toJson(new ErrorResponse("Error: already taken"));
        } catch (DataAccessException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: unexpected server error"));
        }
    }

}
