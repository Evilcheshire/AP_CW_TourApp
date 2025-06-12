package tourapp.dao.transport_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransportDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;

    private TransportDao dao;
    private Transport testTransport;
    private TransportType testTransportType;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new TransportDao(connectionFactory);

        testTransportType = new TransportType(1, "Bus");
        testTransport = new Transport(1, "City Bus", testTransportType, 25.50);
    }

    @Test
    void testInitJoinInfos() {
        var joinInfos = dao.initJoinInfos();

        assertNotNull(joinInfos);
        assertEquals(1, joinInfos.size());

        // Verify specific join
        assertTrue(joinInfos.stream().anyMatch(j ->
                j.getJoinTable().equals("transport_types") && j.getAlias().equals("tt")));
    }
    @Test
    void testInitColumnMappings() {
        // When
        Map<String, String> mappings = dao.initColumnMappings();

        // Then
        assertNotNull(mappings);
        assertEquals("t.id", mappings.get("id"));
        assertEquals("t.name", mappings.get("name"));
        assertEquals("t.type_id", mappings.get("typeId"));
        assertEquals("tt.name", mappings.get("typeName"));
        assertEquals("t.price_per_person", mappings.get("minPrice"));
        assertEquals("t.price_per_person", mappings.get("maxPrice"));
        assertEquals("t.price_per_person", mappings.get("pricePerPerson"));
        assertEquals("tt.name", mappings.get("transport_type"));
    }

    @Test
    void testGetBaseAlias() {
        // When
        String alias = dao.getBaseAlias();

        // Then
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

        Transport newTransport = new Transport(0, "Express Bus", testTransportType, 45.00);

        // When
        boolean result = dao.create(newTransport);

        // Then
        assertTrue(result);
        assertEquals(2, newTransport.getId());
        verify(preparedStatement).setString(1, "Express Bus");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setDouble(3, 45.00);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.create(testTransport);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.update(testTransport);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "City Bus");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setDouble(3, 25.50);
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.update(testTransport);

        // Then
        assertFalse(result);
    }

    @Test
    void testMapTransport() throws Exception {
        // Given
        mockResultSetForTransport();

        // When
        Transport result = TransportDao.mapTransport(resultSet);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("City Bus", result.getName());
        assertEquals(25.50, result.getPricePerPerson());
        assertNotNull(result.getType());
        assertEquals(1, result.getType().getId());
        assertEquals("Bus", result.getType().getName());
    }

    private void mockResultSetForTransport() throws SQLException {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("City Bus");
        when(resultSet.getDouble("price_per_person")).thenReturn(25.50);
        when(resultSet.getInt("type_id")).thenReturn(1);
        when(resultSet.getString("type_name")).thenReturn("Bus");
    }
}