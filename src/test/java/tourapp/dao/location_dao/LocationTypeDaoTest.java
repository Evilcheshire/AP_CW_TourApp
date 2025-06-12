package tourapp.dao.location_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.location.LocationType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocationTypeDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private LocationTypeDao dao;
    private LocationType testLocationType;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new LocationTypeDao(connectionFactory);
        testLocationType = new LocationType(1, "Museum");
    }

    @Test
    void testConstructor() {
        assertNotNull(dao);
        assertEquals("location_types", dao.tableName);
    }

    @Test
    void testRowMapper() throws Exception {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Museum");

        LocationType result = dao.rowMapper.map(resultSet);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Museum", result.getName());
    }

    @Test
    void testNameGetter() {
        String name = dao.getNameExtractor().apply(testLocationType);

        assertEquals("Museum", name);
    }

    @Test
    void testIdGetter() {
        var idOptional = dao.getIdExtractor().apply(testLocationType);

        assertTrue(idOptional.isPresent());
        assertEquals(1, idOptional.get());
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

        LocationType newLocationType = new LocationType(0, "Park");

        // When
        boolean result = dao.create(newLocationType);

        // Then
        assertTrue(result);
        assertEquals(2, newLocationType.getId());
        verify(preparedStatement).setString(1, "Park");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        LocationType newLocationType = new LocationType(0, "Park");

        // When
        boolean result = dao.create(newLocationType);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.update(testLocationType);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Museum");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateWithExplicitId() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        LocationType locationTypeWithoutId = new LocationType(0, "Updated Museum");

        // When
        boolean result = dao.update(locationTypeWithoutId, 1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Updated Museum");
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    void testUpdateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.update(testLocationType);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindById() throws Exception {
        // Given
        LocationType expectedType = new LocationType(1, "Museum");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            LocationType result = dao.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Museum", result.getName());
        }
    }

    @Test
    void testFindAll() throws Exception {
        // Given
        LocationType type1 = new LocationType(1, "Museum");
        LocationType type2 = new LocationType(2, "Park");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(type1, type2));

            // When
            List<LocationType> result = dao.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Museum", result.get(0).getName());
            assertEquals("Park", result.get(1).getName());
        }
    }

    @Test
    void testFindByName() throws Exception {
        // Given
        LocationType expectedType = new LocationType(1, "Museum");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            List<LocationType> result = dao.findByName("Museum");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Museum", result.get(0).getName());
        }
    }

    @Test
    void testFindByExactName() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Museum");

        // When
        LocationType result = dao.findByExactName("Museum");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Museum", result.getName());
        verify(preparedStatement).setString(1, "Museum");
    }

    @Test
    void testFindByExactNameNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        LocationType result = dao.findByExactName("NonExistent");

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
        boolean result = dao.existsWithName("Museum");

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Museum");
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
    void testSearch() throws Exception {
        // Given
        LocationType expectedType = new LocationType(1, "Museum");
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("name", "Museum");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            List<LocationType> result = dao.search(searchParams);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Museum", result.getFirst().getName());
        }
    }

    @Test
    void testGetBaseAlias() {
        // When
        String alias = dao.getBaseAlias();

        // Then
        assertEquals("l", alias);
    }
}