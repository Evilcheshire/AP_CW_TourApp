package tourapp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.util.DaoUtils.JoinInfo;
import tourapp.util.DaoUtils.ResultSetMapper;

import java.sql.*;
import java.util.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaoUtilsTest {

    @Mock private PreparedStatement preparedStatement;
    @Mock private Connection connection;
    @Mock private ResultSet resultSet;
    @Mock private ResultSetMapper<String> mapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSetParameter_WithNull() throws SQLException {
        DaoUtils.setParameter(preparedStatement, 1, null);
        verify(preparedStatement).setNull(1, Types.NULL);
    }

    @Test
    void testSetParameter_WithInteger() throws SQLException {
        DaoUtils.setParameter(preparedStatement, 1, 42);
        verify(preparedStatement).setInt(1, 42);
    }

    @Test
    void testSetParameter_WithLong() throws SQLException {
        DaoUtils.setParameter(preparedStatement, 1, 42L);
        verify(preparedStatement).setLong(1, 42L);
    }

    @Test
    void testSetParameter_WithDouble() throws SQLException {
        DaoUtils.setParameter(preparedStatement, 1, 42.5);
        verify(preparedStatement).setDouble(1, 42.5);
    }

    @Test
    void testSetParameter_WithFloat() throws SQLException {
        DaoUtils.setParameter(preparedStatement, 1, 42.5f);
        verify(preparedStatement).setFloat(1, 42.5f);
    }

    @Test
    void testSetParameter_WithBoolean() throws SQLException {
        DaoUtils.setParameter(preparedStatement, 1, true);
        verify(preparedStatement).setBoolean(1, true);
    }

    @Test
    void testSetParameter_WithString() throws SQLException {
        DaoUtils.setParameter(preparedStatement, 1, "test");
        verify(preparedStatement).setString(1, "test");
    }

    @Test
    void testSetParameter_WithTimestamp() throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        DaoUtils.setParameter(preparedStatement, 1, timestamp);
        verify(preparedStatement).setTimestamp(1, timestamp);
    }

    @Test
    void testSetParameter_WithSqlDate() throws SQLException {
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        DaoUtils.setParameter(preparedStatement, 1, sqlDate);
        verify(preparedStatement).setDate(1, sqlDate);
    }

    @Test
    void testSetParameter_WithUtilDate() throws SQLException {
        Date utilDate = new Date();
        DaoUtils.setParameter(preparedStatement, 1, utilDate);
        verify(preparedStatement).setDate(eq(1), any(java.sql.Date.class));
    }

    @Test
    void testSetParameter_WithOtherObject() throws SQLException {
        Object customObject = new Object();
        DaoUtils.setParameter(preparedStatement, 1, customObject);
        verify(preparedStatement).setObject(1, customObject);
    }

    @Test
    void testJoinInfo_ConstructorWithoutRequiredParams() {
        JoinInfo joinInfo = new JoinInfo("INNER JOIN", "users", "u", "u.id = t.user_id");

        assertEquals("INNER JOIN users u ON u.id = t.user_id", joinInfo.getJoinSql());
        assertEquals("u", joinInfo.getAlias());
        assertEquals("u.id = t.user_id", joinInfo.getCondition());
        assertEquals("users", joinInfo.getJoinTable());
    }

    @Test
    void testJoinInfo_ConstructorWithRequiredParams() {
        JoinInfo joinInfo = new JoinInfo("LEFT JOIN", "orders", "o", "o.user_id = u.id", "orderId");

        assertEquals("LEFT JOIN orders o ON o.user_id = u.id", joinInfo.getJoinSql());
        assertEquals("o", joinInfo.getAlias());
        assertEquals("o.user_id = u.id", joinInfo.getCondition());
        assertEquals("orders", joinInfo.getJoinTable());
    }

    @Test
    void testJoinInfo_IsRequired_EmptyRequiredParams() {
        JoinInfo joinInfo = new JoinInfo("INNER JOIN", "users", "u", "u.id = t.user_id");
        Set<String> activeParams = Set.of("name", "email");

        assertTrue(joinInfo.isRequired(activeParams));
    }

    @Test
    void testJoinInfo_IsRequired_WithMatchingParams() {
        JoinInfo joinInfo = new JoinInfo("LEFT JOIN", "orders", "o", "o.user_id = u.id", "orderId");
        Set<String> activeParams = Set.of("orderId", "name");

        assertTrue(joinInfo.isRequired(activeParams));
    }

    @Test
    void testJoinInfo_IsRequired_WithoutMatchingParams() {
        JoinInfo joinInfo = new JoinInfo("LEFT JOIN", "orders", "o", "o.user_id = u.id", "orderId");
        Set<String> activeParams = Set.of("name", "email");

        assertFalse(joinInfo.isRequired(activeParams));
    }

    @Test
    void testBuildWhereClause_SimpleSearch() {
        Map<String, Object> searchParams = Map.of("name", "John");
        Map<String, String> columnMappings = Map.of("name", "u.name");
        List<JoinInfo> joinInfos = List.of(
                new JoinInfo("INNER JOIN", "user_types", "ut", "u.user_type_id = ut.id")
        );

        StringBuilder result = DaoUtils.buildWhereClause("users", "u", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("SELECT DISTINCT u.*"));
        assertTrue(query.contains("FROM users u"));
        assertTrue(query.contains("INNER JOIN user_types ut ON u.user_type_id = ut.id"));
        assertTrue(query.contains("WHERE 1=1"));
        assertTrue(query.contains("AND u.name LIKE ?"));
    }

    @Test
    void testBuildWhereClause_WithKeywordSearch() {
        Map<String, Object> searchParams = Map.of("keyword", "search term");
        Map<String, String> columnMappings = new HashMap<>();
        List<JoinInfo> joinInfos = new ArrayList<>();

        StringBuilder result = DaoUtils.buildWhereClause("locations", "l", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("AND (l.name LIKE ? OR l.description LIKE ? OR l.country LIKE ?)"));
    }

    @Test
    void testBuildWhereClause_WithKeywordSearch_NonLocations() {
        Map<String, Object> searchParams = Map.of("keyword", "search term");
        Map<String, String> columnMappings = new HashMap<>();
        List<JoinInfo> joinInfos = new ArrayList<>();

        StringBuilder result = DaoUtils.buildWhereClause("transports", "t", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("AND (t.name LIKE ? OR tt.name LIKE ?)"));
    }

    @Test
    void testBuildWhereClause_WithLocationTypesJoin() {
        Map<String, Object> searchParams = Map.of("typeId", 1);
        Map<String, String> columnMappings = Map.of("typeId", "lt.id");
        List<JoinInfo> joinInfos = List.of(
                new JoinInfo("INNER JOIN", "location_types", "lt", "l.type_id = lt.id")
        );

        StringBuilder result = DaoUtils.buildWhereClause("locations", "l", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("lt.id AS location_type_id"));
        assertTrue(query.contains("lt.name AS location_type_name"));
    }

    @Test
    void testBuildWhereClause_WithTransportTypesJoin() {
        Map<String, Object> searchParams = Map.of("typeId", 1);
        Map<String, String> columnMappings = Map.of("typeId", "tt.id");
        List<JoinInfo> joinInfos = List.of(
                new JoinInfo("INNER JOIN", "transport_types", "tt", "t.type_id = tt.id")
        );

        StringBuilder result = DaoUtils.buildWhereClause("transports", "t", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("tt.id AS type_id"));
        assertTrue(query.contains("tt.name AS type_name"));
    }

    @Test
    void testBuildWhereClause_WithListParameter() {
        Map<String, Object> searchParams = Map.of("ids", Arrays.asList(1, 2, 3));
        Map<String, String> columnMappings = Map.of("ids", "u.id");
        List<JoinInfo> joinInfos = new ArrayList<>();

        StringBuilder result = DaoUtils.buildWhereClause("users", "u", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("AND u.id IN (?,?,?)"));
    }

    @Test
    void testBuildWhereClause_WithEmptyList() {
        Map<String, Object> searchParams = Map.of("ids", new ArrayList<>());
        Map<String, String> columnMappings = Map.of("ids", "u.id");
        List<JoinInfo> joinInfos = new ArrayList<>();

        StringBuilder result = DaoUtils.buildWhereClause("users", "u", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertFalse(query.contains("AND u.id IN"));
    }

    @Test
    void testBuildWhereClause_WithMinMaxParameters() {
        Map<String, Object> searchParams = Map.of(
                "minPrice", 100,
                "maxPrice", 500,
                "startDate", "2024-01-01",
                "endDate", "2024-12-31"
        );
        Map<String, String> columnMappings = Map.of(
                "price", "t.price",
                "date", "t.date"
        );
        List<JoinInfo> joinInfos = new ArrayList<>();

        StringBuilder result = DaoUtils.buildWhereClause("tours", "t", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("AND t.price >= ?"));
        assertTrue(query.contains("AND t.price <= ?"));
        assertTrue(query.contains("AND t.date >= ?"));
        assertTrue(query.contains("AND t.date <= ?"));
    }

    @Test
    void testSetWhereParameters_SimpleString() throws SQLException {
        Map<String, Object> searchParams = Map.of("name", "John");
        Map<String, String> columnMappings = Map.of("name", "u.name");

        DaoUtils.setWhereParameters(preparedStatement, searchParams, columnMappings);

        verify(preparedStatement).setString(1, "%John%");
    }

    @Test
    void testSetWhereParameters_WithKeywordForLocations() throws SQLException {
        Map<String, Object> searchParams = Map.of("keyword", "search");
        Map<String, String> columnMappings = new HashMap<>();

        DaoUtils.setWhereParameters(preparedStatement, searchParams, columnMappings);

        verify(preparedStatement, times(3)).setString(anyInt(), eq("%search%"));
    }

    @Test
    void testSetWhereParameters_WithList() throws SQLException {
        Map<String, Object> searchParams = Map.of("ids", Arrays.asList(1, 2, 3));
        Map<String, String> columnMappings = Map.of("ids", "u.id");

        DaoUtils.setWhereParameters(preparedStatement, searchParams, columnMappings);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setInt(3, 3);
    }

    @Test
    void testSetWhereParameters_WithIdParameter() throws SQLException {
        Map<String, Object> searchParams = Map.of("id", "123");
        Map<String, String> columnMappings = Map.of("id", "u.id");

        DaoUtils.setWhereParameters(preparedStatement, searchParams, columnMappings);

        verify(preparedStatement).setString(1, "123");
        verify(preparedStatement, never()).setString(1, "%123%");
    }

    @Test
    void testSetWhereParameters_WithNullColumn() throws SQLException {
        Map<String, Object> searchParams = Map.of("unknown", "value");
        Map<String, String> columnMappings = new HashMap<>();

        DaoUtils.setWhereParameters(preparedStatement, searchParams, columnMappings);

        verify(preparedStatement, never()).setString(anyInt(), anyString());
    }

    @Test
    void testExecuteSearchQuery_WithPagination() throws SQLException {
        Map<String, Object> searchParams = Map.of("name", "John");
        Map<String, String> columnMappings = Map.of("name", "u.name");
        List<JoinInfo> joinInfos = new ArrayList<>();

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(mapper.map(resultSet)).thenReturn("John Doe");

        List<String> result = DaoUtils.executeSearchQuery(
                connection, "users", "u", searchParams, columnMappings, joinInfos, mapper
        );

        assertEquals(1, result.size());
        assertEquals("John Doe", result.getFirst());
        verify(preparedStatement).setString(1, "%John%");
    }

    @Test
    void testExecuteSearchQuery_WithoutPagination() throws SQLException {
        Map<String, Object> searchParams = new HashMap<>();
        Map<String, String> columnMappings = new HashMap<>();
        List<JoinInfo> joinInfos = new ArrayList<>();

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<String> result = DaoUtils.executeSearchQuery(
                connection, "users", "u", searchParams, columnMappings, joinInfos, mapper
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testExecuteSearchQuery_WithSQLException() throws SQLException {
        Map<String, Object> searchParams = Map.of("name", "John");
        Map<String, String> columnMappings = Map.of("name", "u.name");
        List<JoinInfo> joinInfos = new ArrayList<>();

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Test exception"));

        assertThrows(SQLException.class, () -> DaoUtils.executeSearchQuery(
                connection, "users", "u", searchParams, columnMappings, joinInfos, mapper
        ));
    }

    @Test
    void testResolveColumn_DirectMapping() {
        Map<String, String> columnMappings = Map.of("name", "u.name");

        String result = DaoUtils.resolveColumn("name", columnMappings);

        assertEquals("u.name", result);
    }

    @Test
    void testResolveColumn_MinParameter() {
        Map<String, String> columnMappings = Map.of("price", "t.price");

        String result = DaoUtils.resolveColumn("minPrice", columnMappings);

        assertEquals("t.price", result);
    }

    @Test
    void testResolveColumn_MaxParameter() {
        Map<String, String> columnMappings = Map.of("price", "t.price");

        String result = DaoUtils.resolveColumn("maxPrice", columnMappings);

        assertEquals("t.price", result);
    }

    @Test
    void testResolveColumn_StartParameter() {
        Map<String, String> columnMappings = Map.of("date", "t.date");

        String result = DaoUtils.resolveColumn("startDate", columnMappings);

        assertEquals("t.date", result);
    }

    @Test
    void testResolveColumn_EndParameter() {
        Map<String, String> columnMappings = Map.of("date", "t.date");

        String result = DaoUtils.resolveColumn("endDate", columnMappings);

        assertEquals("t.date", result);
    }

    @Test
    void testResolveColumn_EmptyStrippedParameter() {
        Map<String, String> columnMappings = new HashMap<>();

        String result = DaoUtils.resolveColumn("min", columnMappings);

        assertNull(result);
    }

    @Test
    void testResolveColumn_NotFoundInMappings() {
        Map<String, String> columnMappings = new HashMap<>();

        String result = DaoUtils.resolveColumn("unknownColumn", columnMappings);

        assertNull(result);
    }

    @Test
    void testBuildWhereClause_ComplexJoinDependencies() {
        Map<String, Object> searchParams = Map.of("categoryName", "Electronics");
        Map<String, String> columnMappings = Map.of("categoryName", "c.name");
        List<JoinInfo> joinInfos = Arrays.asList(
                new JoinInfo("INNER JOIN", "product_categories", "pc", "p.id = pc.product_id"),
                new JoinInfo("INNER JOIN", "categories", "c", "pc.category_id = c.id")
        );

        StringBuilder result = DaoUtils.buildWhereClause("products", "p", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("INNER JOIN product_categories pc ON p.id = pc.product_id"));
        assertTrue(query.contains("INNER JOIN categories c ON pc.category_id = c.id"));
        assertTrue(query.contains("AND c.name LIKE ?"));
    }

    @Test
    void testBuildWhereClause_ConditionalJoins() {
        Map<String, Object> searchParams = Map.of("name", "Test Product");
        Map<String, String> columnMappings = Map.of("name", "p.name");
        List<JoinInfo> joinInfos = Arrays.asList(
                new JoinInfo("LEFT JOIN", "reviews", "r", "p.id = r.product_id", "rating"),
                new JoinInfo("INNER JOIN", "categories", "c", "p.category_id = c.id")
        );

        StringBuilder result = DaoUtils.buildWhereClause("products", "p", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("INNER JOIN categories c ON p.category_id = c.id"));
        assertFalse(query.contains("LEFT JOIN reviews r ON p.id = r.product_id"));
    }

    @Test
    void testBuildWhereClause_JoinCircularDependencyHandling() {
        Map<String, Object> searchParams = Map.of("userName", "John");
        Map<String, String> columnMappings = Map.of("userName", "u.name");
        List<JoinInfo> joinInfos = Arrays.asList(
                new JoinInfo("INNER JOIN", "user_profiles", "up", "u.id = up.user_id"),
                new JoinInfo("INNER JOIN", "users", "u", "o.user_id = u.id"),
                new JoinInfo("INNER JOIN", "orders", "o", "p.order_id = o.id")
        );

        StringBuilder result = DaoUtils.buildWhereClause("products", "p", searchParams, columnMappings, joinInfos);

        String query = result.toString();
        assertTrue(query.contains("FROM products p"));
        assertTrue(query.contains("AND u.name LIKE ?"));
    }

    @Test
    void testSetWhereParameters_ComplexScenario() throws SQLException {
        Map<String, Object> searchParams = new LinkedHashMap<>();
        searchParams.put("keyword", "search");
        searchParams.put("ids", Arrays.asList(1, 2, 3));
        searchParams.put("minPrice", 100);
        searchParams.put("name", "Product");
        searchParams.put("id", "123");

        Map<String, String> columnMappings = Map.of(
                "ids", "p.id",
                "price", "p.price",
                "name", "p.name",
                "id", "p.id"
        );

        DaoUtils.setWhereParameters(preparedStatement, searchParams, columnMappings);

        verify(preparedStatement, times(3)).setString(anyInt(), eq("%search%")); // keyword (3 times)
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).setInt(5, 2);
        verify(preparedStatement).setInt(6, 3);
        verify(preparedStatement).setInt(7, 100);
        verify(preparedStatement).setString(8, "%Product%");
        verify(preparedStatement).setString(9, "123");
    }

    @Test
    void testResultSetMapper_FunctionalInterface() throws SQLException {
        ResultSetMapper<String> testMapper = rs -> rs.getString("name");

        when(resultSet.getString("name")).thenReturn("Test Name");

        String result = testMapper.map(resultSet);

        assertEquals("Test Name", result);
        verify(resultSet).getString("name");
    }
}