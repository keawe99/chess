package service;

import dataaccess.DataAccessException;
import dataaccess.dao.AuthDAO;
import dataaccess.dao.UserDAOInterface;
import dataaccess.dao.GameDAOInterface;

public class ClearService {
    private final UserDAOInterface userDAO;
    private final GameDAOInterface gameDAO;
    private final AuthDAO authDAO;

    public ClearService(UserDAOInterface userDAO, GameDAOInterface gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clear() throws DataAccessException {
        userDAO.clear();
        gameDAO.clear();   // now uses the interface!
        authDAO.clear();
    }
}


