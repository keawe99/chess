package dataaccess.sql;

import dataaccess.dao.AuthDAOInterface;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySQLAuthDAO implements AuthDAOInterface {

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO auth (token, username) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, auth.authToken());
                stmt.setString(2, auth.username());
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to insert auth token");
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT token, username FROM auth WHERE token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, token);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("token"), rs.getString("username"));
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to retrieve auth token");
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM auth WHERE token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, token);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to delete auth token");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auth")) {
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to clear auth table");
        }
    }
}
