package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAOInterface;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import model.UserData;

import java.util.UUID;

public class UserService {

    private final UserDAOInterface userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAOInterface userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws DataAccessException {
        // Basic input validation
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        // Check if username already exists
        if (userDAO.getUser(user.getUsername()) != null) {
            throw new DataAccessException("Error: username already taken", 403);
        }

        // Insert the new user
        userDAO.insertUser(user);

        // Generate auth token for new user
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.getUsername());
        authDAO.insertAuth(auth);

        return auth;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        // Basic input validation
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        // Get the user by username
        UserData user = userDAO.getUser(request.username());

        if (user == null || !user.getPassword().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        // Generate new auth token on successful login
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, user.getUsername());
        authDAO.insertAuth(authData);

        return new LoginResult(user.getUsername(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        authDAO.deleteAuth(authToken);
    }
}
