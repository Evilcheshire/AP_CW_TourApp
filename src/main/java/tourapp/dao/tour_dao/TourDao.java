package tourapp.dao.tour_dao;

import tourapp.dao.AbstractGenericDao;
import tourapp.dao.meal_dao.MealDao;
import tourapp.dao.transport_dao.TransportDao;
import tourapp.model.location.Location;
import tourapp.model.meal.Meal;
import tourapp.model.tour.Tour;
import tourapp.model.tour.TourLocation;
import tourapp.model.tour.TourType;
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourDao extends AbstractGenericDao<Tour> {

    private final MealDao mealDao;
    private final TransportDao transportDao;
    private final TourTypeDao tourTypeDao;
    private final TourLocationDao tourLocationDao;

    public TourDao(ConnectionFactory connectionFactory,
                   MealDao mealDao,
                   TransportDao transportDao,
                   TourTypeDao tourTypeDao,
                   TourLocationDao tourLocationDao) {
        super(connectionFactory, "tours", TourDao::mapTour);
        this.mealDao = mealDao;
        this.transportDao = transportDao;
        this.tourTypeDao = tourTypeDao;
        this.tourLocationDao = tourLocationDao;
    }

    @Override
    public List<JoinInfo> initJoinInfos() {
        List<JoinInfo> joins = new ArrayList<>();

        joins.add(new JoinInfo(
                "LEFT JOIN", "tour_types", "tt",
                "t.type_id = tt.id",
                "type_id", "tour_type"));

        joins.add(new JoinInfo(
                "LEFT JOIN", "transports", "tr",
                "t.transport_id = tr.id",
                "transport_id", "transport_type"));

        joins.add(new JoinInfo(
                "LEFT JOIN", "meals", "m",
                "t.meal_id = m.id",
                "meal_id", "meal_types"));

        joins.add(new JoinInfo(
                "LEFT JOIN", "meal_meal_types", "mmt",
                "m.id = mmt.meal_id",
                "meal_types"));

        joins.add(new JoinInfo(
                "LEFT JOIN", "meal_types", "mt",
                "mmt.meal_type_id = mt.id",
                "meal_types"));

        joins.add(new JoinInfo(
                "LEFT JOIN", "tour_locations", "tl",
                "t.id = tl.tour_id",
                "country"));

        joins.add(new JoinInfo(
                "LEFT JOIN", "locations", "l",
                "tl.location_id = l.id",
                "country"));

        return joins;
    }

    public Tour findByIdWithDependencies(int id) throws SQLException {
        Tour tour = findById(id);
        if (tour == null) return null;

        if (tour.getType() != null) {
            tour.setType(tourTypeDao.findById(tour.getType().getId()));
        }

        if (tour.getTransport() != null) {
            tour.setTransport(transportDao.findById(tour.getTransport().getId()));
        }

        if (tour.getMeal() != null) {
            tour.setMeal(mealDao.findById(tour.getMeal().getId()));
        }

        List<Location> locations = tourLocationDao.findById1(tour.getId()).stream()
                .map(TourLocation::getLocation)
                .toList();
        tour.setLocations(locations);

        return tour;
    }

    public boolean create(Tour tour) throws SQLException {
        String sql = "INSERT INTO tours (description, type_id, transport_id, meal_id, start_date, end_date, price, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tour.getDescription());
            stmt.setInt(2, tour.getType().getId());
            stmt.setInt(3, tour.getTransport().getId());
            stmt.setInt(4, tour.getMeal().getId());
            stmt.setDate(5, Date.valueOf(tour.getStartDate()));
            stmt.setDate(6, Date.valueOf(tour.getEndDate()));
            stmt.setDouble(7, tour.getPrice());
            stmt.setBoolean(8, tour.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tour.setId(keys.getInt(1));
                }
            }

            saveTourLocations(tour);

            return true;
        }
    }

    public boolean update(Tour tour) throws SQLException {
        String sql = "UPDATE tours SET description = ?, type_id = ?, transport_id = ?, meal_id = ?, " +
                "start_date = ?, end_date = ?, price = ?, is_active = ? WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tour.getDescription());
            if (tour.getType() != null) {
                stmt.setInt(2, tour.getType().getId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            if (tour.getTransport() != null) {
                stmt.setInt(3, tour.getTransport().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            if (tour.getMeal() != null) {
                stmt.setInt(4, tour.getMeal().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setDate(5, Date.valueOf(tour.getStartDate()));
            stmt.setDate(6, Date.valueOf(tour.getEndDate()));
            stmt.setDouble(7, tour.getPrice());
            stmt.setBoolean(8, tour.isActive());
            stmt.setInt(9, tour.getId());

            saveTourLocations(tour);
            return stmt.executeUpdate() > 0;
        }
    }

    private void saveTourLocations(Tour tour) throws SQLException {
        tourLocationDao.deleteAllById1(tour.getId());

        if (tour.getLocations() != null) {
            for (Location location : tour.getLocations()) {
                tourLocationDao.create(tour.getId(), location.getId());
            }
        }
    }

    @Override
    public Map<String, String> initColumnMappings() {
        Map<String, String> columnMappings = new HashMap<>();
        columnMappings.put("id", "t.id");
        columnMappings.put("description", "t.description");
        columnMappings.put("type_id", "t.type_id");
        columnMappings.put("transport_id", "t.transport_id");
        columnMappings.put("meal_id", "t.meal_id");
        columnMappings.put("startDate", "t.start_date");
        columnMappings.put("endDate", "t.end_date");
        columnMappings.put("minPrice", "t.price");
        columnMappings.put("maxPrice", "t.price");
        columnMappings.put("is_active", "t.is_active");
        columnMappings.put("country", "l.country");
        columnMappings.put("tour_type", "tt.name");
        columnMappings.put("meal_types", "mt.name");
        columnMappings.put("transport_type", "tr.name");
        return columnMappings;
    }

    private static LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    private static Tour mapTour(ResultSet rs) throws SQLException {
        Tour tour = new Tour();
        tour.setId(rs.getInt("id"));
        tour.setDescription(rs.getString("description"));
        tour.setStartDate(toLocalDate(rs.getDate("start_date")));
        tour.setEndDate(toLocalDate(rs.getDate("end_date")));
        tour.setPrice(rs.getDouble("price"));
        tour.setActive(rs.getBoolean("is_active"));

        int typeId = rs.getInt("type_id");
        if (!rs.wasNull()) {
            TourType tourType = new TourType();
            tourType.setId(typeId);
            tour.setType(tourType);
        }

        int transportId = rs.getInt("transport_id");
        if (!rs.wasNull()) {
            Transport transport = new Transport();
            transport.setId(transportId);
            tour.setTransport(transport);
        }

        int mealId = rs.getInt("meal_id");
        if (!rs.wasNull()) {
            Meal meal = new Meal();
            meal.setId(mealId);
            tour.setMeal(meal);
        }

        return tour;
    }

}