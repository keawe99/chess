package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.dao.GameDAOInterface;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDAO implements GameDAOInterface {

    @Override
    public int createGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, gameData) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, game.gameName());
                stmt.setString(4, game.gameData());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    } else {
                        throw new DataAccessException("Failed to get game ID");
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to create game", e, 500);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM games WHERE gameID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, gameID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                rs.getString("gameData")
                        );
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to get game", e, 500);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM games";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    games.add(new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            rs.getString("gameData")
                    ));
                }
            }
            return games;
        } catch (Exception e) {
            throw new DataAccessException("Unable to list games", e, 500);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameData = ? WHERE gameID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, game.whiteUsername());
                stmt.setString(2, game.blackUsername());
                stmt.setString(3, game.gameName());
                stmt.setString(4, game.gameData());
                stmt.setInt(5, game.gameID());
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to update game", e, 500);
        }
    }

    @Override
    public void updateGamePlayer(int gameID, String color, String username) {

    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM games")) {
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to clear games table", e, 500);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, gameData) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, null);  // No white player yet
                stmt.setString(2, null);  // No black player yet
                stmt.setString(3, gameName);
                stmt.setString(4, null);  // No game data yet

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new DataAccessException("Failed to create game");
                }

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newGameID = keys.getInt(1);
                        return new GameData(newGameID, null, null, gameName, null);
                    } else {
                        throw new DataAccessException("Failed to get generated game ID");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error: ", e, 500);
        }
    }

}
