package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {

    private final UserService userService;
    private final AuthDAO authDAO;

    public RegisterHandler(UserService userService, AuthDAO authDAO) {
        this.userService = userService;
        this.authDAO = authDAO;
    }

    @Override
    public Object handle(Request req, Response res) {
        Gson gson = new Gson();
        try {
            UserData user = gson.fromJson(req.body(), UserData.class);
            AuthData auth = userService.register(user);

            // Store auth token
            authDAO.insertAuth(auth);

            res.status(200);
            return gson.toJson(auth);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("already taken")) {
                res.status(403);
            } else {
                res.status(400);
            }
            return gson.toJson(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Internal server error"));
        }
    }

    private record ErrorResponse(String message) {}
}
