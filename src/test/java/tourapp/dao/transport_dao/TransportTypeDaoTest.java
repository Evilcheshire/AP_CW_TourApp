package tourapp.dao.transport_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.transport.TransportType;
import tourapp.util.ConnectionFactory;

import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransportTypeDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private TransportTypeDao dao;
    private TransportType testTransportType;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new TransportTypeDao(connectionFactory);
        testTransportType = new TransportType(1, "Train");
    }

    @Test
    void testConstructor() {
        assertNotNull(dao);
        assertEquals("transport_types", dao.tableName);
    }

    @Test
    void testRowMapper() throws Exception {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Train");

        TransportType result = dao.rowMapper.map(resultSet);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Train", result.getName());
    }

    @Test
    void testNameExtractor() {
        String name = dao.getNameExtractor().apply(testTransportType);
        assertEquals("Train", name);
    }

    @Test
    void testIdExtractor() {
        Optional<Integer> id = dao.getIdExtractor().apply(testTransportType);
        assertTrue(id.isPresent());
        assertEquals(1, id.get());
    }

    @Test
    void testGetBaseAlias() {
        String alias = dao.getBaseAlias();
        assertEquals("t", alias);
    }

    @Test
    void testCreate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);

        TransportType newType = new TransportType(0, "Plane");

        // When
        boolean result = dao.create(newType);

        // Then
        assertTrue(result);
        assertEquals(2, newType.getId());
        verify(preparedStatement).setString(1, "Plane");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.create(testTransportType);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.update(testTransportType);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Train");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateWithProvidedId() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.update(testTransportType, Optional.of(5));

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Train");
        verify(preparedStatement).setInt(2, 5);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.update(testTransportType);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindByExactName() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Train");

        // When
        TransportType result = dao.findByExactName("Train");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Train", result.getName());
        verify(preparedStatement).setString(1, "Train");
    }

    @Test
    void testFindByExactNameNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        TransportType result = dao.findByExactName("NonExistent");

        // Then
        assertNull(result);
    }

    @Test
    void testExistsWithName() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        // When
        boolean result = dao.existsWithName("Train");

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Train");
    }

    @Test
    void testExistsWithNameNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        // When
        boolean result = dao.existsWithName("NonExistent");

        // Then
        assertFalse(result);
    }
}