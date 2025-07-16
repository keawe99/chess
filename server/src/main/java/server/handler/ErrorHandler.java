package server.handler;

import com.google.gson.Gson;
import model.ErrorResponse;
import spark.Response;
import dataaccess.DataAccessException;

public class ErrorHandler {
    private static final Gson gson = new Gson();

    public static String handleDataAccessException(DataAccessException e, Response res) {
        if (e.getMessage().toLowerCase().contains("unauthorized")) {
            res.status(401);
        } else {
            res.status(500);
        }
        return gson.toJson(new ErrorResponse(e.getMessage()));
    }
}
