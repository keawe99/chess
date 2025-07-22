package dataaccess;

public class DataAccessException extends Exception {
    public DataAccessException(String message, int i) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String failedToGetGameId) {
    }

    public int statusCode() {
        return 400;
    }
}
