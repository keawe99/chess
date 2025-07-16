package server;

import com.google.gson.Gson;
import model.UserData;
import model.AuthData;
import service.UserService;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
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
        try {
            UserData user = new Gson().fromJson(req.body(), UserData.class);
            AuthData auth = userService.register(user);

            // 💥 Store auth token
            authDAO.insertAuth(auth);

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
