package server;

import dataaccess.*;
import server.handler.CreateGameHandler;
import server.handler.JoinGameHandler;
import server.handler.ListGamesHandler;
import server.handler.LoginHandler;
import server.handler.LogoutHandler;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.Spark;

public class Server {

    public int run(int desiredPort) throws Exception {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // 🔧 Make sure to initialize database BEFORE anything else uses it
        DatabaseManager.initializeDatabase();

        // Instantiate DAOs
        UserDAOInterface userDAO = DAOFactory.getUserDAO();
        MemoryGameDAO memoryGameDAO = new MemoryGameDAO();
        AuthDAO authDAO = AuthDAO.getInstance();

        // Services
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(memoryGameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, memoryGameDAO, authDAO);

        // Route registration
        Spark.post("/user", new RegisterHandler(userService, authDAO));
        Spark.post("/session", new LoginHandler(userService, authDAO));
        Spark.delete("/session", new LogoutHandler(userService));
        Spark.post("/game", new CreateGameHandler(gameService, authDAO));
        Spark.put("/game", new JoinGameHandler(gameService, authDAO));
        Spark.get("/game", new ListGamesHandler(gameService, authDAO));
        Spark.delete("/db", new ClearHandler(clearService));

        // Only call init AFTER everything is ready
        Spark.init();
        Spark.awaitInitialization();

        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
