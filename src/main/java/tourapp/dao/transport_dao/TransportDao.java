package tourapp.dao.transport_dao;

import tourapp.dao.AbstractGenericDao;
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportDao extends AbstractGenericDao<Transport> {

    public TransportDao(ConnectionFactory connectionFactory) {
        super(connectionFactory, "transports", TransportDao::mapTransport);
    }

    public List<JoinInfo> initJoinInfos() {
        List<JoinInfo> joins = new ArrayList<>();

        joins.add(new JoinInfo(
                "INNER JOIN", "transport_types", "tt",
                "t.type_id = tt.id"));

        return joins;
    }

    public Map<String, String> initColumnMappings() {
        Map<String, String> columnMappings = new HashMap<>();
        columnMappings.put("id", "t.id");
        columnMappings.put("name", "t.name");
        columnMappings.put("typeId", "t.type_id");
        columnMappings.put("typeName", "tt.name");
        columnMappings.put("minPrice", "t.price_per_person");
        columnMappings.put("maxPrice", "t.price_per_person");
        columnMappings.put("pricePerPerson", "t.price_per_person");
        columnMappings.put("transport_type", "tt.name");
        return columnMappings;
    }

    public boolean create(Transport transport) throws SQLException {
        String query = """
            INSERT INTO transports (name, type_id, price_per_person)
            VALUES (?, ?, ?)
        """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, transport.getName());
            stmt.setInt(2, transport.getType().getId());
            stmt.setDouble(3, transport.getPricePerPerson());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transport.setId(generatedKeys.getInt(1));
                }
            }

            return true;
        }
    }

    public boolean update(Transport transport) throws SQLException {
        String query = """
            UPDATE transports
            SET name = ?, type_id = ?, price_per_person = ?
            WHERE id = ?
        """;

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, transport.getName());
            stmt.setInt(2, transport.getType().getId());
            stmt.setDouble(3, transport.getPricePerPerson());
            stmt.setInt(4, transport.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    static Transport mapTransport(ResultSet rs) throws SQLException {
          TransportType type = new TransportType(
                rs.getInt("type_id"),
                rs.getString("type_name")
        );

        return new Transport(
                rs.getInt("id"),
                rs.getString("name"),
                type,
                rs.getDouble("price_per_person")
        );
    }
}