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
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Error: bad request", 400);
        }

        userDAO.insertUser(user);

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        authDAO.insertAuth(auth);

        return auth;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request", 409);
        }

        var user = userDAO.getUser(request.username());

        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, user.username());
        authDAO.insertAuth(authData);

        return new LoginResult(user.username(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }
        authDAO.deleteAuth(authToken);
    }
}
