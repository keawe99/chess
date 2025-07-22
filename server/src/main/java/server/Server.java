package server;

import dataaccess.DatabaseManager;
import dataaccess.dao.AuthDAO;
import dataaccess.dao.DAOFactory;
import dataaccess.dao.GameDAOInterface;
import dataaccess.dao.UserDAOInterface;
import server.handler.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.Spark;

public class Server {

    public int run(int desiredPort) {
        try {
            Spark.port(desiredPort);
            Spark.staticFiles.location("web");

            // 🔧 Initialize database
            DatabaseManager.createDatabase();
            DatabaseManager.initializeDatabase();

            // Instantiate DAOs
            UserDAOInterface userDAO = DAOFactory.getUserDAO();
            GameDAOInterface gameDAO = DAOFactory.getGameDAO(); // ✅ use GameDAOInterface here
            AuthDAO authDAO = AuthDAO.getInstance();

            // Services
            UserService userService = new UserService(userDAO, authDAO);
            GameService gameService = new GameService(gameDAO, authDAO);     // ✅ use GameDAOInterface
            ClearService clearService = new ClearService(userDAO, gameDAO, authDAO); // ✅ use GameDAOInterface

            // Route registration
            Spark.post("/user", new RegisterHandler(userService, authDAO));
            Spark.post("/session", new LoginHandler(userService, authDAO));
            Spark.delete("/session", new LogoutHandler(userService));
            Spark.post("/game", new CreateGameHandler(gameService, authDAO));
            Spark.put("/game", new JoinGameHandler(gameService, authDAO));
            Spark.get("/game", new ListGamesHandler(gameService, authDAO));
            Spark.delete("/db", new ClearHandler(clearService));

            Spark.init();
            Spark.awaitInitialization();

            return Spark.port();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
