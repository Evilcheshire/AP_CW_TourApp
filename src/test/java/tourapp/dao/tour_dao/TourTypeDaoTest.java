package tourapp.dao.tour_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.tour.TourType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TourTypeDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private TourTypeDao dao;
    private TourType testTourType;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new TourTypeDao(connectionFactory);
        testTourType = new TourType(1, "Cruise");
    }

    @Test
    void testConstructor() {
        assertNotNull(dao);
        assertEquals("tour_types", dao.tableName);
    }

    @Test
    void testRowMapper() throws Exception {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Cruise");

        TourType result = dao.rowMapper.map(resultSet);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Cruise", result.getName());
    }

    @Test
    void testNameGetter() {
        String name = dao.getNameExtractor().apply(testTourType);

        assertEquals("Cruise", name);
    }

    @Test
    void testIdGetter() {
        var idOptional = dao.getIdExtractor().apply(testTourType);

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

        TourType newTourType = new TourType(0, "Adventure");

        // When
        boolean result = dao.create(newTourType);

        // Then
        assertTrue(result);
        assertEquals(2, newTourType.getId());
        verify(preparedStatement).setString(1, "Adventure");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        TourType newTourType = new TourType(0, "Adventure");

        // When
        boolean result = dao.create(newTourType);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.update(testTourType);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Cruise");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateWithExplicitId() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        TourType tourTypeWithoutId = new TourType(0, "Updated Cruise");

        // When
        boolean result = dao.update(tourTypeWithoutId, 1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Updated Cruise");
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    void testUpdateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.update(testTourType);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindById() throws Exception {
        // Given
        TourType expectedType = new TourType(1, "Cruise");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            TourType result = dao.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Cruise", result.getName());
        }
    }

    @Test
    void testFindAll() throws Exception {
        // Given
        TourType type1 = new TourType(1, "Cruise");
        TourType type2 = new TourType(2, "Adventure");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(type1, type2));

            // When
            List<TourType> result = dao.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Cruise", result.get(0).getName());
            assertEquals("Adventure", result.get(1).getName());
        }
    }

    @Test
    void testFindByName() throws Exception {
        // Given
        TourType expectedType = new TourType(1, "Cruise");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            List<TourType> result = dao.findByName("Cruise");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Cruise", result.get(0).getName());
        }
    }

    @Test
    void testFindByExactName() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Cruise");

        // When
        TourType result = dao.findByExactName("Cruise");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Cruise", result.getName());
        verify(preparedStatement).setString(1, "Cruise");
    }

    @Test
    void testFindByExactNameNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        TourType result = dao.findByExactName("NonExistent");

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
        boolean result = dao.existsWithName("Cruise");

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Cruise");
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
        TourType expectedType = new TourType(1, "Cruise");
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("name", "Cruise");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            List<TourType> result = dao.search(searchParams);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Cruise", result.getFirst().getName());
        }
    }

    @Test
    void testGetBaseAlias() {
        // When
        String alias = dao.getBaseAlias();

        // Then
        assertEquals("t", alias);
    }
}