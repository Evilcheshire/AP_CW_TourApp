package tourapp.dao.tour_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.meal_dao.MealDao;
import tourapp.dao.transport_dao.TransportDao;
import tourapp.model.location.Location;
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.model.tour.Tour;
import tourapp.model.tour.TourLocation;
import tourapp.model.tour.TourType;
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private MealDao mealDao;
    @Mock private TransportDao transportDao;
    @Mock private TourTypeDao tourTypeDao;
    @Mock private TourLocationDao tourLocationDao;

    private TourDao tourDao;
    private Tour testTour;
    private TourType testTourType;
    private Transport testTransport;
    private Meal testMeal;
    private Location testLocation;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(connectionFactory.getConnection()).thenReturn(connection);

        tourDao = new TourDao(connectionFactory, mealDao, transportDao, tourTypeDao, tourLocationDao);

        // Setup test data
        setupTestData();
    }

    private void setupTestData() {
        // Create test tour type
        testTourType = new TourType();
        testTourType.setId(1);
        testTourType.setName("Cultural");

        // Create test transport type
        TransportType transportType = new TransportType();
        transportType.setId(1);
        transportType.setName("Bus");

        // Create test transport
        testTransport = new Transport();
        testTransport.setId(1);
        testTransport.setName("Express Bus");
        testTransport.setType(transportType);
        testTransport.setPricePerPerson(50.0);

        // Create test meal types
        MealType breakfastType = new MealType();
        breakfastType.setId(1);
        breakfastType.setName("Breakfast");

        MealType lunchType = new MealType();
        lunchType.setId(2);
        lunchType.setName("Lunch");

        // Create test meal
        testMeal = new Meal();
        testMeal.setId(1);
        testMeal.setName("Full Board");
        testMeal.setMealsPerDay(3);
        testMeal.setCostPerDay(45.0);
        testMeal.setMealTypes(List.of(breakfastType, lunchType));

        // Create test location
        testLocation = new Location();
        testLocation.setId(1);
        testLocation.setName("Paris");
        testLocation.setCountry("France");
        testLocation.setDescription("City of Light");

        // Create test tour
        testTour = new Tour();
        testTour.setId(1);
        testTour.setDescription("Paris Cultural Tour");
        testTour.setType(testTourType);
        testTour.setTransport(testTransport);
        testTour.setMeal(testMeal);
        testTour.setStartDate(LocalDate.of(2025, 6, 15));
        testTour.setEndDate(LocalDate.of(2025, 7, 15));
        testTour.setPrice(1200.0);
        testTour.setActive(true);
        testTour.setLocations(List.of(testLocation));
    }

    @Test
    void testInitJoinInfos() {
        var joinInfos = tourDao.initJoinInfos();

        assertNotNull(joinInfos);
        assertEquals(7, joinInfos.size());

        // Verify specific joins
        assertTrue(joinInfos.stream().anyMatch(j ->
                j.getJoinTable().equals("tour_types") && j.getAlias().equals("tt")));
        assertTrue(joinInfos.stream().anyMatch(j ->
                j.getJoinTable().equals("transports") && j.getAlias().equals("tr")));
        assertTrue(joinInfos.stream().anyMatch(j ->
                j.getJoinTable().equals("meals") && j.getAlias().equals("m")));
        assertTrue(joinInfos.stream().anyMatch(j ->
                j.getJoinTable().equals("locations") && j.getAlias().equals("l")));
    }

    @Test
    void testInitColumnMappings() {
        Map<String, String> columnMappings = tourDao.initColumnMappings();

        assertNotNull(columnMappings);
        assertFalse(columnMappings.isEmpty());

        // Verify key mappings
        assertEquals("t.id", columnMappings.get("id"));
        assertEquals("t.description", columnMappings.get("description"));
        assertEquals("t.type_id", columnMappings.get("type_id"));
        assertEquals("t.transport_id", columnMappings.get("transport_id"));
        assertEquals("t.meal_id", columnMappings.get("meal_id"));
        assertEquals("t.start_date", columnMappings.get("startDate"));
        assertEquals("t.end_date", columnMappings.get("endDate"));
        assertEquals("t.price", columnMappings.get("minPrice"));
        assertEquals("t.price", columnMappings.get("maxPrice"));
        assertEquals("t.is_active", columnMappings.get("is_active"));
        assertEquals("l.country", columnMappings.get("country"));
        assertEquals("tt.name", columnMappings.get("tour_type"));
        assertEquals("mt.name", columnMappings.get("meal_types"));
        assertEquals("tr.name", columnMappings.get("transport_type"));
    }

    @Test
    void testGetBaseAlias() {
        // Test the base alias generation from table name
        assertEquals("t", tourDao.getBaseAlias());
    }

    @Test
    void testCreate_Success() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        Tour newTour = new Tour();
        newTour.setDescription("New Tour");
        newTour.setType(testTourType);
        newTour.setTransport(testTransport);
        newTour.setMeal(testMeal);
        newTour.setStartDate(LocalDate.of(2025, 6, 15));
        newTour.setEndDate(LocalDate.of(2025, 7, 15));
        newTour.setPrice(1500.0);
        newTour.setActive(true);
        newTour.setLocations(List.of(testLocation));

        // When
        boolean result = tourDao.create(newTour);

        // Then
        assertTrue(result);
        assertEquals(1, newTour.getId());

        verify(preparedStatement).setString(1, "New Tour");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).setDate(5, Date.valueOf("2025-06-15"));
        verify(preparedStatement).setDate(6, Date.valueOf("2025-07-15"));
        verify(preparedStatement).setDouble(7, 1500.0);
        verify(preparedStatement).setBoolean(8, true);
        verify(preparedStatement).executeUpdate();

        verify(tourLocationDao).deleteAllById1(1);
        verify(tourLocationDao).create(1, 1);
    }

    @Test
    void testCreate_WithoutLocations() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);

        Tour newTour = new Tour();
        newTour.setDescription("Tour without locations");
        newTour.setType(testTourType);
        newTour.setTransport(testTransport);
        newTour.setMeal(testMeal);
        newTour.setStartDate(LocalDate.of(2025, 6, 15));
        newTour.setEndDate(LocalDate.of(2025, 7, 15));
        newTour.setPrice(1500.0);
        newTour.setActive(true);
        newTour.setLocations(null);

        // When
        boolean result = tourDao.create(newTour);

        // Then
        assertTrue(result);
        assertEquals(2, newTour.getId());
        verify(tourLocationDao).deleteAllById1(2);
        verify(tourLocationDao, never()).create(anyInt(), anyInt());
    }

    @Test
    void testUpdate_Success() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        testTour.setDescription("Updated Tour Description");
        testTour.setPrice(1300.0);

        // When
        boolean result = tourDao.update(testTour);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Updated Tour Description");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).setDate(5, Date.valueOf("2025-06-15"));
        verify(preparedStatement).setDate(6, Date.valueOf("2025-07-15"));
        verify(preparedStatement).setDouble(7, 1300.0);
        verify(preparedStatement).setBoolean(8, true);
        verify(preparedStatement).setInt(9, 1);
        verify(preparedStatement).executeUpdate();

        verify(tourLocationDao).deleteAllById1(1);
        verify(tourLocationDao).create(1, 1);
    }

    @Test
    void testUpdate_WithNullReferences() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Tour tourWithNulls = new Tour();
        tourWithNulls.setId(1);
        tourWithNulls.setDescription("Tour with null references");
        tourWithNulls.setType(null);
        tourWithNulls.setTransport(null);
        tourWithNulls.setMeal(null);
        tourWithNulls.setStartDate(LocalDate.of(2025, 6, 15));
        tourWithNulls.setEndDate(LocalDate.of(2025, 7, 15));
        tourWithNulls.setPrice(1200.0);
        tourWithNulls.setActive(true);
        tourWithNulls.setLocations(Collections.emptyList());

        // When
        boolean result = tourDao.update(tourWithNulls);

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Tour with null references");
        verify(preparedStatement).setNull(2, Types.INTEGER);
        verify(preparedStatement).setNull(3, Types.INTEGER);
        verify(preparedStatement).setNull(4, Types.INTEGER);
        verify(preparedStatement).setDate(5, Date.valueOf("2025-06-15"));
        verify(preparedStatement).setDate(6, Date.valueOf("2025-07-15"));
        verify(preparedStatement).setDouble(7, 1200.0);
        verify(preparedStatement).setBoolean(8, true);
        verify(preparedStatement).setInt(9, 1);

        verify(tourLocationDao).deleteAllById1(1);
        verify(tourLocationDao, never()).create(anyInt(), anyInt());
    }

    @Test
    void testUpdate_NoRowsAffected() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = tourDao.update(testTour);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Tour result = tourDao.findById(999);

        // Then
        assertNull(result);
    }

    @Test
    void testFindById_Success() throws SQLException {
        // Given
        setupFindByIdMocks();

        // When
        Tour result = tourDao.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Paris Cultural Tour", result.getDescription());
        assertEquals(LocalDate.of(2025, 6, 15), result.getStartDate());
        assertEquals(LocalDate.of(2025, 7, 15), result.getEndDate());
        assertEquals(1200.0, result.getPrice());
        assertTrue(result.isActive());
        assertNotNull(result.getType());
        assertEquals(1, result.getType().getId());

        // Перевіряємо, що connection був викликаний
        verify(connectionFactory).getConnection();
    }

    @Test
    void testFindByIdWithDependencies_Success() throws SQLException {
        // Given
        setupFindByIdMocks();

        // Мокуємо залежні DAO
        when(tourTypeDao.findById(1)).thenReturn(testTourType);
        when(transportDao.findById(1)).thenReturn(testTransport);
        when(mealDao.findById(1)).thenReturn(testMeal);

        TourLocation tourLocation = new TourLocation();
        tourLocation.setTourId(1);
        tourLocation.setLocationId(1);
        tourLocation.setLocation(testLocation);
        when(tourLocationDao.findById1(1)).thenReturn(List.of(tourLocation));

        // When
        Tour result = tourDao.findByIdWithDependencies(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Paris Cultural Tour", result.getDescription());

        // Перевіряємо заповнені залежності
        assertNotNull(result.getType());
        assertEquals("Cultural", result.getType().getName());
        assertNotNull(result.getTransport());
        assertEquals("Express Bus", result.getTransport().getName());
        assertNotNull(result.getMeal());
        assertEquals("Full Board", result.getMeal().getName());
        assertNotNull(result.getLocations());
        assertEquals(1, result.getLocations().size());
        assertEquals("Paris", result.getLocations().getFirst().getName());

        // Перевіряємо виклики DAO
        verify(tourTypeDao).findById(1);
        verify(transportDao).findById(1);
        verify(mealDao).findById(1);
        verify(tourLocationDao).findById1(1);
    }

    @Test
    void testFindByIdWithDependencies_NotFound() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Tour result = tourDao.findByIdWithDependencies(999);

        // Then
        assertNull(result);
    }

    @Test
    void testDelete_Success() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = tourDao.delete(1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete_NotFound() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = tourDao.delete(999);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindAll() throws SQLException {
        // Given
        try (MockedStatic<DaoUtils> mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(testTour));

            // When
            List<Tour> results = tourDao.findAll();

            // Then
            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(testTour, results.get(0));
        }
    }

    @Test
    void testSearch() throws SQLException {
        // Given
        Map<String, Object> searchParams = Map.of("description", "Paris");

        try (MockedStatic<DaoUtils> mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(testTour));

            // When
            List<Tour> results = tourDao.search(searchParams);

            // Then
            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(testTour, results.get(0));
        }
    }

    @Test
    void testSaveTourLocations_WithLocations() throws SQLException {
        // Given
        Location location2 = new Location();
        location2.setId(2);
        location2.setName("Nice");

        testTour.setLocations(List.of(testLocation, location2));

        // When - call private method through update
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        tourDao.update(testTour);

        // Then
        verify(tourLocationDao).deleteAllById1(1);
        verify(tourLocationDao).create(1, 1);
        verify(tourLocationDao).create(1, 2);
    }

    @Test
    void testSaveTourLocations_WithNullLocations() throws SQLException {
        // Given
        testTour.setLocations(null);

        // When - call private method through update
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        tourDao.update(testTour);

        // Then
        verify(tourLocationDao).deleteAllById1(1);
        verify(tourLocationDao, never()).create(anyInt(), anyInt());
    }

    @Test
    void testRowMapper_CompleteObject() throws SQLException {
        // Given
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("description")).thenReturn("Test Tour");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2025-06-15"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2025-07-15"));
        when(resultSet.getDouble("price")).thenReturn(1200.0);
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getInt("type_id")).thenReturn(1);
        when(resultSet.getInt("transport_id")).thenReturn(1);
        when(resultSet.getInt("meal_id")).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false, false, false);

        // When - access the row mapper through a search operation
        Map<String, Object> searchParams = new HashMap<>();

        try (MockedStatic<DaoUtils> mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenAnswer(invocation -> {
                DaoUtils.ResultSetMapper<Tour> mapper = invocation.getArgument(6);
                return List.of(mapper.map(resultSet));
            });

            List<Tour> results = tourDao.search(searchParams);

            // Then
            assertNotNull(results);
            assertEquals(1, results.size());
            Tour result = results.getFirst();
            assertEquals(1, result.getId());
            assertEquals("Test Tour", result.getDescription());
            assertEquals(LocalDate.of(2025, 6, 15), result.getStartDate());
            assertEquals(LocalDate.of(2025, 7, 15), result.getEndDate());
            assertEquals(1200.0, result.getPrice());
            assertTrue(result.isActive());
            assertNotNull(result.getType());
            assertEquals(1, result.getType().getId());
            assertNotNull(result.getTransport());
            assertEquals(1, result.getTransport().getId());
            assertNotNull(result.getMeal());
            assertEquals(1, result.getMeal().getId());
        }
    }

    @Test
    void testRowMapper_WithNullDates() throws SQLException {
        // Given
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("description")).thenReturn("Test Tour");
        when(resultSet.getDate("start_date")).thenReturn(null);
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getDouble("price")).thenReturn(1200.0);
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.wasNull()).thenReturn(true, true, true);

        // When - access the row mapper through a search operation
        Map<String, Object> searchParams = new HashMap<>();

        try (MockedStatic<DaoUtils> mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenAnswer(invocation -> {
                DaoUtils.ResultSetMapper<Tour> mapper = invocation.getArgument(6);
                return List.of(mapper.map(resultSet));
            });

            List<Tour> results = tourDao.search(searchParams);

            // Then
            assertNotNull(results);
            assertEquals(1, results.size());
            Tour result = results.get(0);
            assertNull(result.getStartDate());
            assertNull(result.getEndDate());
        }
    }

    @Test
    void testSQLException_InFindById() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> tourDao.findById(1));
    }

    @Test
    void testSQLException_InCreate() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> tourDao.create(testTour));
    }

    @Test
    void testSQLException_InUpdate() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> tourDao.update(testTour));
    }

    @Test
    void testSQLException_InSaveTourLocations() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        lenient().when(preparedStatement.executeUpdate()).thenReturn(1);
        doThrow(new SQLException("Location save error")).when(tourLocationDao).deleteAllById1(anyInt());

        // When & Then
        assertThrows(SQLException.class, () -> tourDao.update(testTour));
    }

    private void setupFindByIdMocks() throws SQLException {
        // Мокуємо Connection
        Connection mockConnection = mock(Connection.class);
        when(connectionFactory.getConnection()).thenReturn(mockConnection);

        // Мокуємо PreparedStatement
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        // Мокуємо ResultSet з даними туру
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        // Налаштовуємо послідовність викликів ResultSet.next()
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);

        // Налаштовуємо дані для туру
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("description")).thenReturn("Paris Cultural Tour");
        when(mockResultSet.getDate("start_date")).thenReturn(Date.valueOf("2025-06-15"));
        when(mockResultSet.getDate("end_date")).thenReturn(Date.valueOf("2025-07-15"));
        when(mockResultSet.getDouble("price")).thenReturn(1200.0);
        when(mockResultSet.getBoolean("is_active")).thenReturn(true);

        // Налаштовуємо ID зв'язаних сутностей
        when(mockResultSet.getInt("type_id")).thenReturn(1);
        when(mockResultSet.wasNull()).thenReturn(false); // для type_id

        when(mockResultSet.getInt("transport_id")).thenReturn(1);
        when(mockResultSet.getInt("meal_id")).thenReturn(1);
    }
}