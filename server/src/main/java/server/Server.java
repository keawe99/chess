package server;

import dataaccess.*;
import server.handler.CreateGameHandler;
import server.handler.JoinGameHandler;
import server.handler.ListGamesHandler;
import server.handler.LogoutHandler;
import server.RegisterHandler;
import server.handler.LoginHandler;
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
        MemoryDataAccess memoryDataAccess = MemoryDataAccess.getInstance();

        // Services that depend on DAOs
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

        // Register routes and handlers (pass required dependencies)
        Spark.post("/user", new RegisterHandler(userService, authDAO));
        Spark.post("/session", new LoginHandler(userService, authDAO));
        Spark.delete("/session", new LogoutHandler(userService));

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
