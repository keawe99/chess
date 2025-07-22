package dataaccess;

public class DAOFactory {
    private static final boolean useMySQL = true;

    public static UserDAOInterface getUserDAO() {
        if (useMySQL) {
            return new SQLUserDAO(); // ✅ Updated
        } else {
            return new InMemoryUserDAO();
        }
    }

    public static GameDAOInterface getGameDAO() {
        if (useMySQL) {
            return new MySQLGameDAO(); // ✅ You'll build this soon
        } else {
            return new MemoryGameDAO();
        }
    }
}
