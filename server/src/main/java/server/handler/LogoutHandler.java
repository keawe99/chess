package server.handler;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import com.google.gson.Gson;
import model.ErrorResponse;

public class LogoutHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String authToken = req.headers("Authorization");

        try {
            userService.logout(authToken);
            res.status(200);
            return "{}";
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

