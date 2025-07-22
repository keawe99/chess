package server.handler;

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
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isBlank()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            userService.logout(authToken);
            res.status(200);
            return "{}";  // empty success response
        } catch (DataAccessException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: internal server error"));
        }
    }
}
