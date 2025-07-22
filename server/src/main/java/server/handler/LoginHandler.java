package server.handler;

import com.google.gson.Gson;
import dataaccess.dao.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {
    private final UserService userService;
    private final AuthDAO authDAO;

    public LoginHandler(UserService userService, AuthDAO authDAO) {
        this.userService = userService;
        this.authDAO = authDAO;
    }

    @Override
    public Object handle(Request req, Response res) {
        Gson gson = new Gson();
        try {
            LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);
            LoginResult result = userService.login(request);

            // Store auth token in DAO
            authDAO.insertAuth(new AuthData(result.authToken(), result.username()));

            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("bad request")) {
                res.status(400);
            } else if (msg.contains("unauthorized")) {
                res.status(401);
            } else {
                res.status(500);
            }
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: internal server error"));
        }
    }


    private record ErrorResponse(String message) {}
}
