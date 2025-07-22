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
    public Object handle(Request req, Response res) throws Exception {
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
        } catch (DataAccessException e) {
            int status = e.statusCode();
            if (status == 0) {
                // fallback if statusCode() not set properly
                status = 400;
            }
            res.status(status);
            return gson.toJson(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            // For bad requests like invalid join color or missing fields
            res.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        } catch (Exception e) {
            res.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
    }


}

