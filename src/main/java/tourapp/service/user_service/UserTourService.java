package tourapp.service.user_service;

import tourapp.dao.user_dao.UserTourDao;
import tourapp.model.user.UserTour;
import tourapp.service.link_service.AbstractLinkService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class UserTourService extends AbstractLinkService<UserTour> {

    private final UserTourDao userTourDao;

    public UserTourService(UserTourDao userTourDao) {
        super(userTourDao);
        this.userTourDao = userTourDao;
    }

    public List<UserTour> search(Integer userId, Integer tourId, Integer locationId, Integer tourTypeId,
                                 Date startDate, Date endDate, Double minPrice, Double maxPrice) throws SQLException {
        return userTourDao.search(userId, tourId, locationId, tourTypeId, startDate, endDate, minPrice, maxPrice);
    }

    public int countUsersByTourId(int tourId) throws SQLException {
        return userTourDao.countUsersByTourId(tourId);
    }
}
