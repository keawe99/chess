package dataaccess;

public class DAOFactory {
    private static final boolean useMySQL = true;

    public static UserDAOInterface getUserDAO() {
        if (useMySQL) {
            return new MySQLUserDAO();
        } else {
            return new InMemoryUserDAO();
        }
    }

    public static GameDAOInterface getGameDAO() {
        if (useMySQL) {
            return new MySQLGameDAO();
        } else {
            return new MemoryGameDAO();
        }
    }
}
