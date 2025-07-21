package server;

import dataaccess.AuthDAO;
import dataaccess.DAOFactory;
import dataaccess.GameDAO;
import dataaccess.UserDAOInterface;
import server.handler.CreateGameHandler;
import server.handler.JoinGameHandler;
import server.handler.ListGamesHandler;
import server.handler.LoginHandler;
import server.handler.LogoutHandler;
import server.RegisterHandler;
import server.ClearHandler;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.Spark;

public class Server {

    public int run(int desiredPort) {
        // Set the port and static files location
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Instantiate shared singletons and DAOs
        UserDAOInterface userDAO = DAOFactory.getUserDAO();
        GameDAO gameDAO = new GameDAO();
        AuthDAO authDAO = AuthDAO.getInstance();

        // Services that depend on DAOs
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

        // Register routes and handlers (pass required dependencies)
        Spark.post("/user", new RegisterHandler(userService, authDAO));    // Register user
        Spark.post("/session", new LoginHandler(userService, authDAO));    // Login user
        Spark.delete("/session", new LogoutHandler(userService));           // Logout user

        Spark.post("/game", new CreateGameHandler(gameService, authDAO));
        Spark.put("/game", new JoinGameHandler(gameService, authDAO));
        Spark.get("/game", new ListGamesHandler(gameService, authDAO));

        Spark.delete("/db", new ClearHandler(clearService));

        // Initialize Spark AFTER all routes are registered
        Spark.init();
        Spark.awaitInitialization();

        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
