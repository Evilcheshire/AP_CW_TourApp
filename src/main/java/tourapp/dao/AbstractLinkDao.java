package tourapp.dao;

import tourapp.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLinkDao<T> {
    protected final ConnectionFactory connectionFactory;

    public AbstractLinkDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    protected abstract String getTableName();
    protected abstract String getId1Column();
    protected abstract String getId2Column();

    protected abstract T mapWithAdditionalData(int id1, int id2) throws SQLException;

    public boolean create(int id1, int id2) throws SQLException {
        String sql = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)", getTableName(), getId1Column(), getId2Column());
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id1);
            stmt.setInt(2, id2);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id1, int id2) throws SQLException {
        String sql = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", getTableName(), getId1Column(), getId2Column());
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id1);
            stmt.setInt(2, id2);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteAllById1(int id1) throws SQLException {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", getTableName(), getId1Column());
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id1);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteAllById2(int id2) throws SQLException {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", getTableName(), getId2Column());
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id2);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existsLink(int id1, int id2) throws SQLException {
        String sql = String.format("SELECT 1 FROM %s WHERE %s = ? AND %s = ?", getTableName(), getId1Column(), getId2Column());
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id1);
            stmt.setInt(2, id2);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<T> findAllLinks() throws SQLException {
        String sql = String.format("SELECT %s, %s FROM %s", getId1Column(), getId2Column(), getTableName());
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                int id1 = rs.getInt(getId1Column());
                int id2 = rs.getInt(getId2Column());
                result.add(mapWithAdditionalData(id1, id2));
            }
            return result;
        }
    }

    public List<T> findById1(int id1) throws SQLException {
        String sql = String.format("SELECT %s FROM %s WHERE %s = ?", getId2Column(), getTableName(), getId1Column());
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id1);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    int id2 = rs.getInt(getId2Column());
                    result.add(mapWithAdditionalData(id1, id2));
                }
                return result;
            }
        }
    }

    public List<T> findById2(int id2) throws SQLException {
        String sql = String.format("SELECT %s FROM %s WHERE %s = ?", getId1Column(), getTableName(), getId2Column());
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id2);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    int id1 = rs.getInt(getId1Column());
                    result.add(mapWithAdditionalData(id1, id2));
                }
                return result;
            }
        }
    }
}
