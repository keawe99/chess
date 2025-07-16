package server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import server.handler.ListGamesHandler;
import server.handler.LogoutHandler;
import service.GameService;
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

        // In Server.java
        GameDAO gameDAO = new GameDAO(); // implement this if not done yet
        GameService gameService = new GameService(authDAO, gameDAO);
        Spark.get("/game", new ListGamesHandler(gameService));


        Spark.post("/user", new RegisterHandler(userService));

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.post("/session", new server.handler.LoginHandler(userService));

        Spark.delete("/session", new LogoutHandler(userService));

        Spark.delete("/db", new ClearHandler());

        Spark.awaitInitialization();

        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
