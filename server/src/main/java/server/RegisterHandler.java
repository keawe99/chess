package server;

import com.google.gson.Gson;
import model.UserData;
import model.AuthData;
import service.UserService;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {

    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            UserData user = new Gson().fromJson(req.body(), UserData.class);
            AuthData auth = userService.register(user);

            res.status(200);
            return new Gson().toJson(auth);
        } catch (DataAccessException e) {
            String message = e.getMessage();
            if (message.contains("already taken")) {
                res.status(403);
            } else {
                res.status(400);
            }
            return new Gson().toJson(new ErrorMessage(message));
        } catch (Exception e) {
            res.status(500);
            return new Gson().toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}
