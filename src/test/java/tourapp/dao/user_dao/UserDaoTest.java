package tourapp.dao.user_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSet generatedKeys;

    private UserDao userDao;
    private User testUser;
    private UserType testUserType;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDao(connectionFactory);
        testUserType = new UserType(1, "Admin");
        testUser = new User(1, "John Doe", "john@example.com", "hashedPassword", testUserType);

        lenient().when(connectionFactory.getConnection()).thenReturn(connection);
    }

    @Test
    void testInitJoinInfos() {
        List<JoinInfo> joinInfos = userDao.initJoinInfos();

        assertEquals(1, joinInfos.size());
        JoinInfo joinInfo = joinInfos.getFirst();
        assertEquals("INNER JOIN user_types ut ON u.user_type_id = ut.id", joinInfo.getJoinSql());
    }

    @Test
    void testInitColumnMappings() {
        Map<String, String> mappings = userDao.initColumnMappings();

        assertEquals(5, mappings.size());
        assertEquals("u.id", mappings.get("id"));
        assertEquals("u.name", mappings.get("name"));
        assertEquals("u.email", mappings.get("email"));
        assertEquals("u.user_type_id", mappings.get("userTypeId"));
        assertEquals("ut.name", mappings.get("typeName"));
    }

    @Test
    void testFindById_UserExists() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        setupResultSetForUser();

        User result = userDao.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testFindById_UserNotExists() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        User result = userDao.findById(1);

        assertNull(result);
    }

    @Test
    void testFindAll() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        setupResultSetForUser();

        List<User> result = userDao.findAll();

        assertEquals(2, result.size());
        verify(statement).executeQuery(anyString());
    }

    @Test
    void testFindByEmail_UserExists() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        setupResultSetForUser();

        User result = userDao.findByEmail("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(preparedStatement).setString(1, "john@example.com");
    }

    @Test
    void testFindByEmail_UserNotExists() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        User result = userDao.findByEmail("nonexistent@example.com");

        assertNull(result);
    }

    @Test
    void testSearchByTerm() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        setupResultSetForUser();

        List<User> result = userDao.searchByTerm("john");

        assertEquals(1, result.size());
        verify(preparedStatement).setString(1, "%john%");
        verify(preparedStatement).setString(2, "%john%");
    }

    @Test
    void testCreate_Success() throws SQLException {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(5);

        mock(User.class);

        boolean result = userDao.create(testUser);

        assertTrue(result);
        assertEquals(5, testUser.getId());
        verify(preparedStatement).setString(1, "John Doe");
        verify(preparedStatement).setString(2, "john@example.com");
        verify(preparedStatement).setString(3, testUser.getPasswordHash());
        verify(preparedStatement).setInt(4, 1);
    }

    @Test
    void testCreate_NoAffectedRows() throws SQLException {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        mock(User.class);
        when(User.hashPassword("plainPassword")).thenReturn("hashedPassword");

        boolean result = userDao.create(testUser);

        assertFalse(result);
    }

    @Test
    void testCreate_NoGeneratedKeys() throws SQLException {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(false);

        mock(User.class);
        when(User.hashPassword("plainPassword")).thenReturn("hashedPassword");

        boolean result = userDao.create(testUser);

        assertTrue(result);
        assertEquals(1, testUser.getId());
    }

    @Test
    void testUpdate_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDao.update(testUser);

        assertTrue(result);
        verify(preparedStatement).setString(1, "John Doe");
        verify(preparedStatement).setString(2, "john@example.com");
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setInt(4, 1);
    }

    @Test
    void testUpdate_NoRowsAffected() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = userDao.update(testUser);

        assertFalse(result);
    }

    @Test
    void testChangePassword_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        mockStatic(User.class);
        when(User.hashPassword("newPassword")).thenReturn("newHashedPassword");

        boolean result = userDao.changePassword(1, "newPassword");

        assertTrue(result);
        verify(preparedStatement).setString(1, "newHashedPassword");
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    void testChangePassword_NoRowsAffected() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        mock(User.class);
        when(User.hashPassword("newPassword")).thenReturn("newHashedPassword");

        boolean result = userDao.changePassword(1, "newPassword");

        assertFalse(result);
    }

    @Test
    void testAuthenticate_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("password")).thenReturn("hashedPassword");

        mock(User.class);
        when(User.verifyPassword("plainPassword", "hashedPassword")).thenReturn(true);

        boolean result = userDao.authenticate("john@example.com", "plainPassword");

        assertTrue(result);
        verify(preparedStatement).setString(1, "john@example.com");
    }

    @Test
    void testAuthenticate_WrongPassword() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("password")).thenReturn("hashedPassword");

        mock(User.class);
        when(User.verifyPassword("wrongPassword", "hashedPassword")).thenReturn(false);

        boolean result = userDao.authenticate("john@example.com", "wrongPassword");

        assertFalse(result);
    }

    @Test
    void testAuthenticate_UserNotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        boolean result = userDao.authenticate("nonexistent@example.com", "password");

        assertFalse(result);
    }

    private void setupResultSetForUser() throws SQLException {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("John Doe");
        when(resultSet.getString("email")).thenReturn("john@example.com");
        when(resultSet.getString("password")).thenReturn("hashedPassword");
        when(resultSet.getInt("type_id")).thenReturn(1);
        when(resultSet.getString("type_name")).thenReturn("Admin");
    }
}