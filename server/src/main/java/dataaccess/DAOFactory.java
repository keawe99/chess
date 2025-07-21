package dataaccess;

public class DAOFactory {
    private static final boolean useMySQL = true; // Set to false for in-memory

    public static UserDAOInterface getUserDAO() {
        if (useMySQL) {
            return new MySQLUserDAO();
        } else {
            return new InMemoryUserDAO();
        }
    }
}
