package tourapp.dao.user_dao;

import tourapp.dao.AbstractLinkDao;
import tourapp.dao.tour_dao.TourDao;
import tourapp.model.tour.Tour;
import tourapp.model.user.UserTour;
import tourapp.util.ConnectionFactory;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class UserTourDao extends AbstractLinkDao<UserTour> {
    private final TourDao tourDao;

    public UserTourDao(ConnectionFactory connectionFactory, TourDao tourDao) {
        super(connectionFactory);
        this.tourDao = tourDao;
    }

    @Override
    protected String getTableName() {
        return "user_tours";
    }

    @Override
    protected String getId1Column() {
        return "user_id";
    }

    @Override
    protected String getId2Column() {
        return "tour_id";
    }

    @Override
    protected UserTour mapWithAdditionalData(int userId, int tourId) throws SQLException {
        Tour tour = tourDao.findById(tourId);
        return (tour != null) ? new UserTour(userId, tour) : new UserTour(userId, tourId);
    }

    public List<UserTour> search(Integer userId, Integer tourId, Integer locationId, Integer tourTypeId,
                                 Date startDate, Date endDate, Double minPrice, Double maxPrice) throws SQLException {

        StringBuilder query = new StringBuilder(
                "SELECT DISTINCT ut.user_id, ut.tour_id FROM user_tours ut " +
                        "JOIN tours t ON ut.tour_id = t.id " +
                        "LEFT JOIN tour_locations tl ON t.id = tl.tour_id WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();
        if (userId != null) {
            query.append(" AND ut.user_id = ?");
            params.add(userId);
        }
        if (tourId != null) {
            query.append(" AND ut.tour_id = ?");
            params.add(tourId);
        }
        if (locationId != null) {
            query.append(" AND tl.location_id = ?");
            params.add(locationId);
        }
        if (tourTypeId != null) {
            query.append(" AND t.type_id = ?");
            params.add(tourTypeId);
        }
        if (startDate != null) {
            query.append(" AND t.start_date >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            query.append(" AND t.end_date <= ?");
            params.add(endDate);
        }
        if (minPrice != null) {
            query.append(" AND t.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            query.append(" AND t.price <= ?");
            params.add(maxPrice);
        }

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<UserTour> result = new ArrayList<>();
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    int tid = rs.getInt("tour_id");
                    result.add(mapWithAdditionalData(uid, tid));
                }
                return result;
            }
        }
    }

    public int countUsersByTourId(int tourId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT user_id) AS user_count FROM user_tours WHERE tour_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tourId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("user_count") : 0;
            }
        }
    }
}
