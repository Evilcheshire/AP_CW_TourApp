package tourapp.dao.meal_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.meal.MealType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MealTypeDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private MealTypeDao dao;
    private MealType testMealType;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new MealTypeDao(connectionFactory);
        testMealType = new MealType(1, "Breakfast");
    }

    @Test
    void testConstructor() {
        assertNotNull(dao);
        assertEquals("meal_types", dao.tableName);
    }

    @Test
    void testRowMapper() throws Exception {
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Breakfast");

        MealType result = dao.rowMapper.map(resultSet);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Breakfast", result.getName());
    }

    @Test
    void testNameGetter() {
        String name = dao.getNameExtractor().apply(testMealType);

        assertEquals("Breakfast", name);
    }

    @Test
    void testIdGetter() {
        var idOptional = dao.getIdExtractor().apply(testMealType);

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

        MealType newMealType = new MealType(0, "Lunch");

        // When
        boolean result = dao.create(newMealType);

        // Then
        assertTrue(result);
        assertEquals(2, newMealType.getId());
        verify(preparedStatement).setString(1, "Lunch");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        MealType newMealType = new MealType(0, "Lunch");

        // When
        boolean result = dao.create(newMealType);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.update(testMealType);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Breakfast");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateWithExplicitId() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        MealType mealTypeWithoutId = new MealType(0, "Updated Breakfast");

        // When
        boolean result = dao.update(mealTypeWithoutId, 1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Updated Breakfast");
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    void testUpdateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.update(testMealType);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindById() throws Exception {
        // Given
        MealType expectedType = new MealType(1, "Breakfast");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            MealType result = dao.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Breakfast", result.getName());
        }
    }

    @Test
    void testFindAll() throws Exception {
        // Given
        MealType type1 = new MealType(1, "Breakfast");
        MealType type2 = new MealType(2, "Lunch");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(type1, type2));

            // When
            List<MealType> result = dao.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Breakfast", result.get(0).getName());
            assertEquals("Lunch", result.get(1).getName());
        }
    }

    @Test
    void testFindByName() throws Exception {
        // Given
        MealType expectedType = new MealType(1, "Breakfast");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            List<MealType> result = dao.findByName("Breakfast");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Breakfast", result.get(0).getName());
        }
    }

    @Test
    void testFindByExactName() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Breakfast");

        // When
        MealType result = dao.findByExactName("Breakfast");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Breakfast", result.getName());
        verify(preparedStatement).setString(1, "Breakfast");
    }

    @Test
    void testFindByExactNameNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        MealType result = dao.findByExactName("NonExistent");

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
        boolean result = dao.existsWithName("Breakfast");

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Breakfast");
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
        MealType expectedType = new MealType(1, "Breakfast");
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("name", "Breakfast");

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(expectedType));

            // When
            List<MealType> result = dao.search(searchParams);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Breakfast", result.getFirst().getName());
        }
    }

    @Test
    void testGetBaseAlias() {
        // When
        String alias = dao.getBaseAlias();

        // Then
        assertEquals("m", alias);
    }
}