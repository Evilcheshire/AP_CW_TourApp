package tourapp.dao.meal_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MealDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private MealMealTypeDao mealTypeLinkDao;

    private MealDao dao;
    private Meal testMeal;
    private MealType testMealType;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);

        dao = spy(new MealDao(connectionFactory));

        // Use reflection to inject mocked MealMealTypeDao
        java.lang.reflect.Field field = MealDao.class.getDeclaredField("mealTypeLinkDao");
        field.setAccessible(true);
        field.set(dao, mealTypeLinkDao);

        testMealType = new MealType(1, "Breakfast");
        testMeal = new Meal(1, "Standard Breakfast", 1, 25.0);
        List<MealType> mealTypes = new ArrayList<>();
        mealTypes.add(testMealType);
        testMeal.setMealTypes(mealTypes);
    }

    @Test
    void testConstructor() {
        // Create a new instance to test the actual constructor
        MealDao actualDao = new MealDao(connectionFactory);

        assertNotNull(actualDao);
        assertEquals("meals", actualDao.tableName);
        assertNotNull(actualDao.rowMapper);
    }

    @Test
    void testRowMapper() throws Exception {
        // Given
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Standard Breakfast");
        when(resultSet.getInt("meals_per_day")).thenReturn(1);
        when(resultSet.getDouble("cost_per_day")).thenReturn(25.0);

        // When
        Meal result = dao.rowMapper.map(resultSet);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Standard Breakfast", result.getName());
        assertEquals(1, result.getMealsPerDay());
        assertEquals(25.0, result.getCostPerDay());
    }

    @Test
    void testFindById() throws Exception {
        // Given
        Meal baseMeal = new Meal(1, "Standard Breakfast", 1, 25.0);
        List<MealType> mealTypes = List.of(testMealType);

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(baseMeal));

            when(mealTypeLinkDao.findById1(1)).thenReturn(mealTypes);

            // When
            Meal result = dao.findById(1);

            // Then
            assertNotNull(result);
            assertEquals("Standard Breakfast", result.getName());
            assertEquals(1, result.getMealsPerDay());
            assertEquals(25.0, result.getCostPerDay());
            assertNotNull(result.getMealTypes());
            assertEquals(1, result.getMealTypes().size());
            assertEquals("Breakfast", result.getMealTypes().getFirst().getName());
            verify(mealTypeLinkDao).findById1(1);
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
            Meal result = dao.findById(999);

            // Then
            assertNull(result);
            verify(mealTypeLinkDao, never()).findById1(anyInt());
        }
    }

    @Test
    void testFindAll() throws Exception {
        // Given
        Meal meal1 = new Meal(1, "Standard Breakfast", 1, 25.0);
        Meal meal2 = new Meal(2, "Deluxe Dinner", 1, 45.0);
        List<Meal> baseMeals = List.of(meal1, meal2);
        List<MealType> mealTypes1 = List.of(testMealType);
        List<MealType> mealTypes2 = List.of(new MealType(2, "Dinner"));

        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(baseMeals);

            when(mealTypeLinkDao.findById1(1)).thenReturn(mealTypes1);
            when(mealTypeLinkDao.findById1(2)).thenReturn(mealTypes2);

            // When
            List<Meal> result = dao.findAll();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            Meal resultMeal1 = result.getFirst();
            assertEquals("Standard Breakfast", resultMeal1.getName());
            assertNotNull(resultMeal1.getMealTypes());
            assertEquals(1, resultMeal1.getMealTypes().size());

            Meal resultMeal2 = result.get(1);
            assertEquals("Deluxe Dinner", resultMeal2.getName());
            assertNotNull(resultMeal2.getMealTypes());
            assertEquals(1, resultMeal2.getMealTypes().size());

            verify(mealTypeLinkDao).findById1(1);
            verify(mealTypeLinkDao).findById1(2);
        }
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
        when(mealTypeLinkDao.create(anyInt(), anyInt())).thenReturn(true);

        Meal newMeal = new Meal();
        newMeal.setName("New Meal");
        newMeal.setMealsPerDay(2);
        newMeal.setCostPerDay(30.0);
        List<MealType> mealTypes = new ArrayList<>();
        mealTypes.add(testMealType);
        newMeal.setMealTypes(mealTypes);

        // When
        boolean result = dao.create(newMeal);

        // Then
        assertTrue(result);
        assertEquals(2, newMeal.getId());
        verify(preparedStatement).setString(1, "New Meal");
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setDouble(3, 30.0);
        verify(preparedStatement).executeUpdate();
        verify(mealTypeLinkDao).create(2, 1);
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        Meal newMeal = new Meal();
        newMeal.setName("New Meal");

        // When
        boolean result = dao.create(newMeal);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(mealTypeLinkDao.deleteAllById1(anyInt())).thenReturn(true);
        when(mealTypeLinkDao.create(anyInt(), anyInt())).thenReturn(true);

        // When
        boolean result = dao.update(testMeal);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Standard Breakfast");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setDouble(3, 25.0);
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).executeUpdate();
        verify(mealTypeLinkDao).deleteAllById1(1);
        verify(mealTypeLinkDao).create(1, 1);
    }

    @Test
    void testUpdateWithMultipleMealTypes() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(mealTypeLinkDao.deleteAllById1(anyInt())).thenReturn(true);
        when(mealTypeLinkDao.create(anyInt(), anyInt())).thenReturn(true);

        List<MealType> mealTypes = new ArrayList<>();
        mealTypes.add(new MealType(1, "Breakfast"));
        mealTypes.add(new MealType(2, "Lunch"));
        testMeal.setMealTypes(mealTypes);

        // When
        boolean result = dao.update(testMeal);

        // Then
        assertTrue(result);
        verify(mealTypeLinkDao).deleteAllById1(1);
        verify(mealTypeLinkDao).create(1, 1);
        verify(mealTypeLinkDao).create(1, 2);
    }

    @Test
    void testUpdateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        when(mealTypeLinkDao.deleteAllById1(anyInt())).thenReturn(true);
        when(mealTypeLinkDao.create(anyInt(), anyInt())).thenReturn(true);

        // When
        boolean result = dao.update(testMeal);

        // Then
        assertFalse(result);
    }

    @Test
    void testDelete() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(mealTypeLinkDao.deleteAllById1(anyInt())).thenReturn(true);

        // When
        boolean result = dao.delete(1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
        verify(mealTypeLinkDao).deleteAllById1(1);
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
        assertEquals("m", alias);
    }

    @Test
    void testInitJoinInfos() {
        var joinInfos = dao.initJoinInfos();

        assertNotNull(joinInfos);
        assertEquals(2, joinInfos.size());

        // Test first join
        var firstJoin = joinInfos.getFirst();
        assertEquals("LEFT JOIN meal_meal_types mmt ON m.id = mmt.meal_id", firstJoin.getJoinSql());
        assertEquals("mmt", firstJoin.getAlias());

        // Test second join
        var secondJoin = joinInfos.get(1);
        assertEquals("LEFT JOIN meal_types mt ON mmt.meal_type_id = mt.id", secondJoin.getJoinSql());
        assertEquals("mt", secondJoin.getAlias());
    }

    @Test
    void testInitColumnMappings() {
        // When
        Map<String, String> columnMappings = dao.initColumnMappings();

        // Then
        assertNotNull(columnMappings);
        assertEquals("m.id", columnMappings.get("id"));
        assertEquals("m.name", columnMappings.get("name"));
        assertEquals("m.meals_per_day", columnMappings.get("mealsPerDay"));
        assertEquals("m.cost_per_day", columnMappings.get("minCostPerDay"));
        assertEquals("m.cost_per_day", columnMappings.get("maxCostPerDay"));
        assertEquals("mt.name", columnMappings.get("mealType"));
    }
}
