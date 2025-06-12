package tourapp.dao.user_dao;

import tourapp.dao.AbstractGenericDao;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.*;
import java.util.*;

public class UserDao extends AbstractGenericDao<User> {

    public UserDao(ConnectionFactory connectionFactory) {
        super(connectionFactory, "users", UserDao::mapUser);
    }

    @Override
    public List<JoinInfo> initJoinInfos() {
        List<JoinInfo> joins = new ArrayList<>();
        joins.add(new JoinInfo(
                "INNER JOIN", "user_types", "ut",
                "u.user_type_id = ut.id"
        ));
        return joins;
    }

    @Override
    public Map<String, String> initColumnMappings() {
        Map<String, String> columnMappings = new HashMap<>();
        columnMappings.put("id", "u.id");
        columnMappings.put("name", "u.name");
        columnMappings.put("email", "u.email");
        columnMappings.put("userTypeId", "u.user_type_id");
        columnMappings.put("typeName", "ut.name");
        return columnMappings;
    }

    @Override
    public User findById(int id) throws SQLException {
        String query = """
            SELECT u.id, u.name, u.email, u.password,
                   ut.id AS type_id, ut.name AS type_name
            FROM users u
            INNER JOIN user_types ut ON u.user_type_id = ut.id
            WHERE u.id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String query = """
            SELECT u.id, u.name, u.email, u.password,
                   ut.id AS type_id, ut.name AS type_name
            FROM users u
            INNER JOIN user_types ut ON u.user_type_id = ut.id
            ORDER BY u.name
        """;

        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
            return users;
        }
    }

    public User findByEmail(String email) throws SQLException {
        String query = """
            SELECT u.id, u.name, u.email, u.password,
                   ut.id AS type_id, ut.name AS type_name
            FROM users u
            INNER JOIN user_types ut ON u.user_type_id = ut.id
            WHERE u.email = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public List<User> searchByTerm(String term) throws SQLException {
        String query = """
            SELECT u.id, u.name, u.email, u.password,
                   ut.id AS type_id, ut.name AS type_name
            FROM users u
            INNER JOIN user_types ut ON u.user_type_id = ut.id
            WHERE u.name LIKE ? OR u.email LIKE ?
            ORDER BY u.name
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + term + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
                return users;
            }
        }
    }

    public boolean create(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password, user_type_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, user.getUserType().getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }

            return true;
        }
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, user_type_id = ? WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getUserType().getId());
            stmt.setInt(4, user.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean changePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = User.hashPassword(newPassword);
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean authenticate(String email, String password) throws SQLException {
        String sql = "SELECT password FROM users WHERE email = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return User.verifyPassword(password, rs.getString("password"));
                }
            }
        }
        return false;
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        UserType userType = new UserType(
                rs.getInt("type_id"),
                rs.getString("type_name")
        );

        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                userType
        );
    }
}