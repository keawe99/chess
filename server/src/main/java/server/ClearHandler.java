package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;

public class ClearHandler implements Route {

    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response) {
        ClearService service = new ClearService(new MemoryDataAccess());

        try {
            service.clear();
            response.status(200);
            return gson.toJson(new HashMap<>()); // Return empty JSON object
        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(
                    new ErrorMessage("Error: " + e.getMessage())
            );
        }
    }

    private record ErrorMessage(String message) {}
}
