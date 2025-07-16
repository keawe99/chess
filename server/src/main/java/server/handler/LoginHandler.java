package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.*;
import service.*;

import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {
    private final UserService userService;

    // Correct constructor: assigns the passed-in userService
    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        Gson gson = new Gson();
        LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);

        try {
            LoginResult result = userService.login(request);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("bad request")) {
                res.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
