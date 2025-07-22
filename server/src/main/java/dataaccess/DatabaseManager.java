package dataaccess;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseManager {
    private static final String CONFIG_FILE = "server/src/main/resources/db.properties";

    public static Connection getConnection() throws Exception {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            throw new Exception("Could not load db.properties", e);
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }

    public static void initializeDatabase() throws Exception {
        try (Connection conn = getConnection()) {
            try (var stmt = conn.createStatement()) {
                // Create Users table
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                );
            """);

                // Create Auth Tokens table
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth (
                    token VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                );
            """);

                // Create Games table
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT PRIMARY KEY AUTO_INCREMENT,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255),
                    gameData TEXT
                );
            """);
            }
        }
    }


    public void loadProperties(Properties props) throws IOException {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        }
    }
}
