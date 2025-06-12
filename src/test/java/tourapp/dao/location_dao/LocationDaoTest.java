package tourapp.dao.location_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.location.Location;
import tourapp.model.location.LocationType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocationDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private LocationDao dao;
    private Location testLocation;
    private LocationType testLocationType;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new LocationDao(connectionFactory);

        testLocationType = new LocationType(1, "City");
        testLocation = new Location();
        testLocation.setId(1);
        testLocation.setName("Test Location");
        testLocation.setCountry("Test Country");
        testLocation.setDescription("Test Description");
        testLocation.setLocationType(testLocationType);
    }

    @Test
    void testCreate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Location newLocation = new Location();
        newLocation.setName("New Location");
        newLocation.setCountry("New Country");
        newLocation.setDescription("New Description");
        newLocation.setLocationType(testLocationType);

        // When
        boolean result = dao.create(newLocation);

        // Then
        assertTrue(result);
        assertEquals(2, newLocation.getId());
        verify(preparedStatement).setString(1, "New Location");
        verify(preparedStatement).setString(2, "New Country");
        verify(preparedStatement).setString(3, "New Description");
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateWithoutLocationType() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Location newLocation = new Location();
        newLocation.setName("New Location");
        newLocation.setCountry("New Country");
        newLocation.setDescription("New Description");
        // No location type set

        // When
        boolean result = dao.create(newLocation);

        // Then
        assertTrue(result);
        verify(preparedStatement).setNull(4, Types.INTEGER);
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        Location newLocation = new Location();
        newLocation.setName("New Location");

        // When
        boolean result = dao.create(newLocation);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.update(testLocation);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Test Location");
        verify(preparedStatement).setString(2, "Test Country");
        verify(preparedStatement).setString(3, "Test Description");
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).setInt(5, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateWithoutLocationType() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        testLocation.setLocationType(null);

        // When
        boolean result = dao.update(testLocation);

        // Then
        assertTrue(result);
        verify(preparedStatement).setNull(4, Types.INTEGER);
    }

    @Test
    void testUpdateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.update(testLocation);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindById() throws Exception {
        // Given
        Location expectedLocation = new Location();
        expectedLocation.setId(1);
        expectedLocation.setName("Test Location");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedLocation));

            // When
            Location result = dao.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Test Location", result.getName());
        }
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        // Given
        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of());

            // When
            Location result = dao.findById(999);

            // Then
            assertNull(result);
        }
    }

    @Test
    void testFindAll() throws Exception {
        // Given
        Location location1 = new Location();
        location1.setId(1);
        location1.setName("Location 1");

        Location location2 = new Location();
        location2.setId(2);
        location2.setName("Location 2");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(location1, location2));

            // When
            List<Location> result = dao.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Location 1", result.get(0).getName());
            assertEquals("Location 2", result.get(1).getName());
        }
    }

    @Test
    void testSearch() throws Exception {
        // Given
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("name", "test");

        Location searchResult = new Location();
        searchResult.setId(1);
        searchResult.setName("Test Location");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(searchResult));

            // When
            List<Location> result = dao.search(searchParams);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Location", result.get(0).getName());
        }
    }

    @Test
    void testDelete() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.delete(1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDeleteFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.delete(999);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetBaseAlias() {
        // When
        String alias = dao.getBaseAlias();

        // Then
        assertEquals("l", alias);
    }

    @Test
    void testInitJoinInfos() {
        // When
        var joinInfos = dao.initJoinInfos();

        // Then
        assertNotNull(joinInfos);
        assertEquals(1, joinInfos.size());

        var joinInfo = joinInfos.getFirst();
        assertEquals("LEFT JOIN location_types lt ON l.location_type_id = lt.id", joinInfo.getJoinSql());
        assertEquals("lt", joinInfo.getAlias());
    }

    @Test
    void testInitColumnMappings() {
        // When
        Map<String, String> columnMappings = dao.initColumnMappings();

        // Then
        assertNotNull(columnMappings);
        assertEquals("l.id", columnMappings.get("id"));
        assertEquals("l.name", columnMappings.get("name"));
        assertEquals("l.country", columnMappings.get("country"));
        assertEquals("l.description", columnMappings.get("description"));
        assertEquals("l.location_type_id", columnMappings.get("locationTypeId"));
        assertEquals("lt.name", columnMappings.get("locationType"));
        assertEquals("l.name", columnMappings.get("keyword"));
    }

    @Test
    void testRowMapper() throws Exception {
        // Given
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Test Location");
        when(resultSet.getString("country")).thenReturn("Test Country");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getInt("location_type_id")).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getString("location_type_name")).thenReturn("City");

        // When
        Location result = dao.rowMapper.map(resultSet);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Location", result.getName());
        assertEquals("Test Country", result.getCountry());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getLocationType());
        assertEquals(1, result.getLocationType().getId());
        assertEquals("City", result.getLocationType().getName());
    }

    @Test
    void testRowMapperWithoutLocationType() throws Exception {
        // Given
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Test Location");
        when(resultSet.getString("country")).thenReturn("Test Country");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getInt("location_type_id")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);

        // When
        Location result = dao.rowMapper.map(resultSet);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Location", result.getName());
        assertEquals("Test Country", result.getCountry());
        assertEquals("Test Description", result.getDescription());
        assertNull(result.getLocationType());
    }
}