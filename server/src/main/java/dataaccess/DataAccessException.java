package dataaccess;

public class DataAccessException extends Exception {
    private final int statusCode;

    public DataAccessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
