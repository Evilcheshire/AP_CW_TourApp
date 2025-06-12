package tourapp.dao;

import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractTypeDao<T> extends AbstractGenericDao<T> {

    private final Function<T, String> nameGetter;
    private final Function<T, Optional<Integer>> idGetter;
    private final BiConsumer<T, Integer> idSetter;

    public AbstractTypeDao(
            ConnectionFactory connectionFactory,
            String tableName,
            DaoUtils.ResultSetMapper<T> rowMapper,
            Function<T, String> nameGetter,
            Function<T, Optional<Integer>> idGetter,
            BiConsumer<T, Integer> idSetter
    ) {
        super(connectionFactory, tableName, rowMapper);
        this.nameGetter = nameGetter;
        this.idGetter = idGetter;
        this.idSetter = idSetter;
        this.columnMappings.put("name", getBaseAlias() + ".name");
    }

    public Function<T, String> getNameExtractor() {
        return nameGetter;
    }

    public Function<T, Optional<Integer>> getIdExtractor() {
        return idGetter;
    }

    @Override
    public boolean create(T entity) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (name) VALUES (?)";

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nameGetter.apply(entity));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        idSetter.accept(entity, keys.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean update(T entity, Optional<Integer> idOptional) throws SQLException {
        Integer entityId = idOptional.orElse(null);

        if (entityId == null) {
            Optional<Integer> entityIdOpt = idGetter.apply(entity);
            if (entityIdOpt.isEmpty()) {
                throw new IllegalArgumentException("ID не знайдено ні в параметрах, ні в entity");
            }
            entityId = entityIdOpt.get();
        }

        String sql = "UPDATE " + tableName + " SET name = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nameGetter.apply(entity));
            stmt.setInt(2, entityId);

            return stmt.executeUpdate() > 0;
        }
    }

    public List<T> findByName(String name) throws SQLException {
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("name", name);
        return search(searchParams);
    }

    public T findByExactName(String name) throws SQLException {
        String query = "SELECT * FROM " + tableName + " WHERE name = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.map(rs);
                }
            }
        }
        return null;
    }

    public boolean existsWithName(String name) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE name = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}