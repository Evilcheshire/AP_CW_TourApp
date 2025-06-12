package tourapp.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourapp.dao.location_dao.LocationDao;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractGenericDao<T> {
    protected Logger logger = LoggerFactory.getLogger(LocationDao.class);
    protected final ConnectionFactory connectionFactory;
    public final String tableName;
    public final DaoUtils.ResultSetMapper<T> rowMapper;

    protected Map<String, String> columnMappings;
    protected List<JoinInfo> joinInfos;

    public AbstractGenericDao(ConnectionFactory connectionFactory, String tableName, DaoUtils.ResultSetMapper<T> rowMapper) {
        this.connectionFactory = connectionFactory;
        this.tableName = tableName;
        this.rowMapper = rowMapper;
        this.columnMappings = initColumnMappings();
        this.joinInfos = initJoinInfos();
    }

    protected Map<String, String> initColumnMappings() {
        return new HashMap<>();
    }

    protected List<JoinInfo> initJoinInfos() {
        return new ArrayList<>();
    }

    public String getBaseAlias() {
        return tableName.substring(0, 1);
    }

    public List<T> findAll() throws SQLException {
        try (Connection conn = connectionFactory.getConnection()) {
            return DaoUtils.executeSearchQuery(
                    conn,
                    tableName,
                    getBaseAlias(),
                    Map.of(),
                    columnMappings,
                    joinInfos,
                    rowMapper
            );
        }
    }

    public T findById(int id) throws SQLException {
        try (Connection conn = connectionFactory.getConnection()) {
            List<T> results = DaoUtils.executeSearchQuery(
                    conn,
                    tableName,
                    getBaseAlias(),
                    Map.of("id", id),
                    columnMappings,
                    joinInfos,
                    rowMapper
            );
            return results.isEmpty() ? null : results.getFirst();
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<T> search(Map<String, Object> searchParams) throws SQLException {
        try (Connection conn = connectionFactory.getConnection()) {
            List<JoinInfo> effectiveJoins = new ArrayList<>(joinInfos);

            return DaoUtils.executeSearchQuery(
                    conn,
                    tableName,
                    getBaseAlias(),
                    searchParams,
                    columnMappings,
                    effectiveJoins,
                    rowMapper
            );
        }
    }

    public abstract boolean create(T entity) throws SQLException;

    public boolean update(T entity, Optional<Integer> id) throws SQLException { return false; };

    public boolean update(T entity, int id) throws SQLException {
        return update(entity, Optional.of(id));
    }

    public boolean update(T entity) throws SQLException {
        return update(entity, Optional.empty());
    }
}