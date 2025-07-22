package dataaccess.dao;

import dataaccess.memory.InMemoryUserDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.sql.MySQLGameDAO;
import dataaccess.sql.SQLUserDAO;

public class DAOFactory {
    private static final boolean useMySQL = true;

    public static UserDAOInterface getUserDAO() {
        if (useMySQL) {
            return new SQLUserDAO(); // ✅ Returns SQL-backed User DAO
        } else {
            return new InMemoryUserDAO();
        }
    }

    public static GameDAOInterface getGameDAO() {
        if (useMySQL) {
            return new MySQLGameDAO(); // ✅ Returns SQL-backed Game DAO
        } else {
            return new MemoryGameDAO();
        }
    }
}
