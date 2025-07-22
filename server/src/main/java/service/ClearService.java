package service;

import dataaccess.*;

public class ClearService {
    private final UserDAOInterface userDAO;
    private final MemoryGameDAO memoryGameDAO;
    private final AuthDAO authDAO;

    public ClearService(UserDAOInterface userDAO, MemoryGameDAO memoryGameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.memoryGameDAO = memoryGameDAO;
        this.authDAO = authDAO;
    }

    public void clear() throws DataAccessException {
        userDAO.clear();
        memoryGameDAO.clear();
        authDAO.clear();
    }
}

