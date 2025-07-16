package server;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.UserService;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        UserDAO userDAO = new UserDAO();
        AuthDAO authDAO = new AuthDAO();
        UserService userService = new UserService(userDAO, authDAO);

        Spark.post("/user", new RegisterHandler(userService));

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.delete("/db", new ClearHandler());

        Spark.awaitInitialization();

        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
