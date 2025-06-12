package tourapp.dao.user_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.dao.tour_dao.TourDao;
import tourapp.model.tour.Tour;
import tourapp.model.user.UserTour;
import tourapp.util.ConnectionFactory;

import java.sql.*;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserTourDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private TourDao tourDao;
    @Mock private Tour tour;

    private UserTourDao dao;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new UserTourDao(connectionFactory, tourDao);
    }

    @Test
    void testGetTableName() {
        assertEquals("user_tours", dao.getTableName());
    }

    @Test
    void testGetId1Column() {
        assertEquals("user_id", dao.getId1Column());
    }

    @Test
    void testGetId2Column() {
        assertEquals("tour_id", dao.getId2Column());
    }

    @Test
    void testMapWithAdditionalDataWithTour() throws SQLException {
        // Given
        when(tourDao.findById(1)).thenReturn(tour);

        // When
        UserTour result = dao.mapWithAdditionalData(1, 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(tour, result.getTour());
        verify(tourDao).findById(1);
    }

    @Test
    void testMapWithAdditionalDataWithoutTour() throws SQLException {
        // Given
        when(tourDao.findById(1)).thenReturn(null);

        // When
        UserTour result = dao.mapWithAdditionalData(1, 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getTourId());
        verify(tourDao).findById(1);
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
    }

    @Test
    void testDeleteAllById2() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        // When
        boolean result = dao.deleteAllById2(2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 2);
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
        boolean result = dao.existsLink(1, 2);

        // Then
        assertFalse(result);
    }

    @Test
    void testSearchWithAllParameters() throws Exception {
        // Given
        Date startDate = Date.valueOf("2024-01-01");
        Date endDate = Date.valueOf("2024-12-31");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getInt("tour_id")).thenReturn(2);
        when(tourDao.findById(2)).thenReturn(tour);

        // When
        List<UserTour> result = dao.search(1, 2, 3, 4, startDate, endDate, 100.0, 500.0);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());

        // Verify all parameters are set
        verify(preparedStatement).setObject(1, 1);      // userId
        verify(preparedStatement).setObject(2, 2);      // tourId
        verify(preparedStatement).setObject(3, 3);      // locationId
        verify(preparedStatement).setObject(4, 4);      // tourTypeId
        verify(preparedStatement).setObject(5, startDate); // startDate
        verify(preparedStatement).setObject(6, endDate);   // endDate
        verify(preparedStatement).setObject(7, 100.0);     // minPrice
        verify(preparedStatement).setObject(8, 500.0);     // maxPrice
    }

    @Test
    void testSearchWithUserIdOnly() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getInt("tour_id")).thenReturn(2);
        when(tourDao.findById(2)).thenReturn(tour);

        // When
        List<UserTour> result = dao.search(1, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preparedStatement).setObject(1, 1); // Only userId should be set
    }

    @Test
    void testSearchWithNoParameters() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getInt("tour_id")).thenReturn(2);
        when(tourDao.findById(2)).thenReturn(tour);

        // When
        List<UserTour> result = dao.search(null, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        // No parameters should be set except for the base query
    }

    @Test
    void testSearchEmptyResult() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        List<UserTour> result = dao.search(1, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCountUsersByTourId() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("user_count")).thenReturn(5);

        // When
        int result = dao.countUsersByTourId(1);

        // Then
        assertEquals(5, result);
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testCountUsersByTourIdNoResults() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        int result = dao.countUsersByTourId(1);

        // Then
        assertEquals(0, result);
    }

    @Test
    void testFindAllLinks() throws Exception {
        // Given
        when(connection.createStatement()).thenReturn(mock(Statement.class));
        when(connection.createStatement().executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("user_id")).thenReturn(1, 2);
        when(resultSet.getInt("tour_id")).thenReturn(3, 4);
        when(tourDao.findById(3)).thenReturn(tour);
        when(tourDao.findById(4)).thenReturn(tour);

        // When
        List<UserTour> result = dao.findAllLinks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getUserId());
        assertEquals(2, result.get(1).getUserId());
    }

    @Test
    void testFindById1() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("tour_id")).thenReturn(3);
        when(tourDao.findById(3)).thenReturn(tour);

        // When
        List<UserTour> result = dao.findById1(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testFindById2() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(tourDao.findById(3)).thenReturn(tour);

        // When
        List<UserTour> result = dao.findById2(3);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
        verify(preparedStatement).setInt(1, 3);
    }
}