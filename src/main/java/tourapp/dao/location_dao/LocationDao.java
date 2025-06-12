package tourapp.dao.location_dao;

import tourapp.dao.AbstractGenericDao;
import tourapp.model.location.Location;
import tourapp.model.location.LocationType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationDao extends AbstractGenericDao<Location> {
    public LocationDao(ConnectionFactory connectionFactory) {
        super(connectionFactory, "locations", rs -> {
            Location location = new Location();
            location.setId(rs.getInt("id"));
            location.setName(rs.getString("name"));
            location.setCountry(rs.getString("country"));
            location.setDescription(rs.getString("description"));
            int typeId = rs.getInt("location_type_id");
            if (!rs.wasNull() && typeId > 0) {
                LocationType type = new LocationType();
                type.setId(typeId);
                String typeName = rs.getString("location_type_name");
                if (typeName != null) {
                    type.setName(typeName);
                    location.setLocationType(type);
                }
            }

            return location;
        });
    }

    @Override
    public List<JoinInfo> initJoinInfos() {
        List<JoinInfo> joins = new ArrayList<>();

        joins.add(new JoinInfo(
                "LEFT JOIN", "location_types", "lt",
                "l.location_type_id = lt.id"));

        return joins;
    }

    @Override
    public Map<String, String> initColumnMappings() {
        Map<String, String> columnMappings = new HashMap<>();
        columnMappings.put("id", "l.id");
        columnMappings.put("name", "l.name");
        columnMappings.put("country", "l.country");
        columnMappings.put("description", "l.description");
        columnMappings.put("locationTypeId", "l.location_type_id");
        columnMappings.put("locationType", "lt.name");
        columnMappings.put("keyword", "l.name");
        return columnMappings;
    }

    public boolean create(Location location) throws SQLException {
        String query = """
            INSERT INTO locations (name, country, description, location_type_id)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, location.getName());
            stmt.setString(2, location.getCountry());
            stmt.setString(3, location.getDescription());

            if (location.getLocationType() != null) {
                stmt.setInt(4, location.getLocationType().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    location.setId(keys.getInt(1));
                }
            }

            return true;
        }
    }

    public boolean update(Location location) throws SQLException {
        String query = """
            UPDATE locations
            SET name = ?, country = ?, description = ?, location_type_id = ?
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, location.getName());
            stmt.setString(2, location.getCountry());
            stmt.setString(3, location.getDescription());

            if (location.getLocationType() != null) {
                stmt.setInt(4, location.getLocationType().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(5, location.getId());
            return stmt.executeUpdate() > 0;
        }
    }
}