package tourapp.dao.meal_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.meal.MealType;
import tourapp.util.ConnectionFactory;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MealMealTypeDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;

    private MealMealTypeDao dao;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new MealMealTypeDao(connectionFactory);
    }

    @Test
    void testGetTableName() {
        assertEquals("meal_meal_types", dao.getTableName());
    }

    @Test
    void testGetId1Column() {
        assertEquals("meal_id", dao.getId1Column());
    }

    @Test
    void testGetId2Column() {
        assertEquals("meal_type_id", dao.getId2Column());
    }

    @Test
    void testMapWithAdditionalData() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenReturn("Breakfast");

        // When
        MealType result = dao.mapWithAdditionalData(1, 2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("Breakfast", result.getName());
        verify(preparedStatement).setInt(1, 2);
    }

    @Test
    void testMapWithAdditionalDataNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        MealType result = dao.mapWithAdditionalData(1, 999);

        // Then
        assertNotNull(result);
        assertEquals(999, result.getId());
        assertNull(result.getName());
    }

    @Test
    void testCreate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.create(1, 2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.create(1, 2);

        // Then
        assertFalse(result);
    }

    @Test
    void testDelete() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.delete(1, 2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDeleteFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.delete(1, 999);

        // Then
        assertFalse(result);
    }

    @Test
    void testDeleteAllById1() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        // When
        boolean result = dao.deleteAllById1(1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDeleteAllById1NoRecords() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.deleteAllById1(999);

        // Then
        assertFalse(result);
    }

    @Test
    void testDeleteAllById2() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.deleteAllById2(2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 2);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testExistsLink() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // When
        boolean result = dao.existsLink(1, 2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
    }

    @Test
    void testExistsLinkNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        boolean result = dao.existsLink(1, 999);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindAllLinks() throws Exception {
        // Given
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("meal_id")).thenReturn(1, 2);
        when(resultSet.getInt("meal_type_id")).thenReturn(1, 2);

        // Mock the nested connection for mapWithAdditionalData
        Connection nestedConnection = mock(Connection.class);
        PreparedStatement nestedStmt = mock(PreparedStatement.class);
        ResultSet nestedRs = mock(ResultSet.class);

        when(connectionFactory.getConnection()).thenReturn(connection, nestedConnection, nestedConnection);
        when(nestedConnection.prepareStatement(anyString())).thenReturn(nestedStmt);
        when(nestedStmt.executeQuery()).thenReturn(nestedRs);
        when(nestedRs.next()).thenReturn(true, true);
        when(nestedRs.getString("name")).thenReturn("Breakfast", "Lunch");

        // When
        List<MealType> result = dao.findAllLinks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Breakfast", result.get(0).getName());
        assertEquals("Lunch", result.get(1).getName());
    }

    @Test
    void testFindById1() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("meal_type_id")).thenReturn(1, 2);

        // Mock the nested connection for mapWithAdditionalData
        Connection nestedConnection1 = mock(Connection.class);
        Connection nestedConnection2 = mock(Connection.class);
        PreparedStatement nestedStmt1 = mock(PreparedStatement.class);
        PreparedStatement nestedStmt2 = mock(PreparedStatement.class);
        ResultSet nestedRs1 = mock(ResultSet.class);
        ResultSet nestedRs2 = mock(ResultSet.class);

        when(connectionFactory.getConnection()).thenReturn(connection, nestedConnection1, nestedConnection2);
        when(nestedConnection1.prepareStatement(anyString())).thenReturn(nestedStmt1);
        when(nestedConnection2.prepareStatement(anyString())).thenReturn(nestedStmt2);
        when(nestedStmt1.executeQuery()).thenReturn(nestedRs1);
        when(nestedStmt2.executeQuery()).thenReturn(nestedRs2);
        when(nestedRs1.next()).thenReturn(true);
        when(nestedRs2.next()).thenReturn(true);
        when(nestedRs1.getString("name")).thenReturn("Breakfast");
        when(nestedRs2.getString("name")).thenReturn("Lunch");

        // When
        List<MealType> result = dao.findById1(1);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Breakfast", result.get(0).getName());
        assertEquals(2, result.get(1).getId());
        assertEquals("Lunch", result.get(1).getName());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testFindById1NoResults() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        List<MealType> result = dao.findById1(999);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById2() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("meal_id")).thenReturn(1);

        // Mock the nested connection for mapWithAdditionalData
        Connection nestedConnection = mock(Connection.class);
        PreparedStatement nestedStmt = mock(PreparedStatement.class);
        ResultSet nestedRs = mock(ResultSet.class);

        when(connectionFactory.getConnection()).thenReturn(connection, nestedConnection);
        when(nestedConnection.prepareStatement(anyString())).thenReturn(nestedStmt);
        when(nestedStmt.executeQuery()).thenReturn(nestedRs);
        when(nestedRs.next()).thenReturn(true);
        when(nestedRs.getString("name")).thenReturn("Breakfast");

        // When
        List<MealType> result = dao.findById2(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Breakfast", result.get(0).getName());
        verify(preparedStatement).setInt(1, 1);
    }
}