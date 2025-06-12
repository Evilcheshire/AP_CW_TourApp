package tourapp.service.user_service;

import tourapp.dao.user_dao.UserDao;
import tourapp.model.user.User;
import tourapp.service.AbstractGenericService;

import java.sql.SQLException;
import java.util.List;

public class UserService extends AbstractGenericService<User> {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        super(userDao);
        this.userDao = userDao;
    }

    public User findByEmail(String email) throws SQLException {
        return userDao.findByEmail(email);
    }

    public List<User> searchByTerm(String term) throws SQLException {
        return userDao.searchByTerm(term);
    }

    public boolean changePassword(int userId, String newPassword) throws SQLException {
        return userDao.changePassword(userId, newPassword);
    }

    public User authenticate(String email, String password) throws SQLException {
        if (userDao.authenticate(email, password)) {
            return userDao.findByEmail(email);
        }
        return null;
    }
}