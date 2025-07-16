package server;

import com.google.gson.Gson;
import dataaccess.*;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;

public class ClearHandler implements Route {

    private final Gson gson = new Gson();
    private final MemoryDataAccess authDAO = MemoryDataAccess.getInstance(); // or use actual AuthDAO if preferred

    public ClearHandler(ClearService clearService) {
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            // Check Authorization
            String authToken = request.headers("Authorization");
            if (authToken == null || authToken.isBlank() || authDAO.read(authToken) == null) {
                response.status(401);
                return gson.toJson(new ErrorMessage("Error: unauthorized"));
            }

            ClearService service = new ClearService(MemoryDataAccess.getInstance());
            service.clear();

            response.status(200);
            return gson.toJson(new HashMap<>()); // empty JSON on success

        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}
