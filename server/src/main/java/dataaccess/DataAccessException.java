package dataaccess;

public class DataAccessException extends Exception {
    private final int statusCode;

    // Constructor storing the status code
    public DataAccessException(String message, Exception statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    // Optional constructor for cause chaining
    public DataAccessException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    // Constructor with just message defaults to 400
    public DataAccessException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public int statusCode() {
        return statusCode;
    }
}
