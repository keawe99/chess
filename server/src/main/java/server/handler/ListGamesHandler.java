package server.handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.ErrorResponse;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ListGamesHandler implements Route {
    private final GameService gameService;
    private final AuthDAO authDAO;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService, AuthDAO authDAO) {
        this.gameService = gameService;
        this.authDAO = authDAO;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        try {
            String authToken = req.headers("Authorization");

            Collection<GameData> games = gameService.listGames(authToken);

            Map<String, Object> result = new HashMap<>();
            result.put("games", games);

            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}

