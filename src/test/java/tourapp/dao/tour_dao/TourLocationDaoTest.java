package tourapp.dao.tour_dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.dao.location_dao.LocationDao;
import tourapp.model.location.Location;
import tourapp.model.location.LocationType;
import tourapp.model.tour.TourLocation;
import tourapp.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TourLocationDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;
    @Mock private LocationDao locationDao;

    private TourLocationDao dao;
    private Location testLocation;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new TourLocationDao(connectionFactory, locationDao);

        // Setup test location
        LocationType locationType = new LocationType(1, "City");
        testLocation = new Location();
        testLocation.setId(1);
        testLocation.setName("Paris");
        testLocation.setCountry("France");
        testLocation.setDescription("City of Light");
        testLocation.setLocationType(locationType);
    }

    @Test
    void testGetTableName() {
        // When
        String tableName = dao.getTableName();

        // Then
        assertEquals("tour_locations", tableName);
    }

    @Test
    void testGetId1Column() {
        // When
        String id1Column = dao.getId1Column();

        // Then
        assertEquals("tour_id", id1Column);
    }

    @Test
    void testGetId2Column() {
        // When
        String id2Column = dao.getId2Column();

        // Then
        assertEquals("location_id", id2Column);
    }

    @Test
    void testMapWithAdditionalData() throws Exception {
        // Given
        when(locationDao.findById(1)).thenReturn(testLocation);

        // When
        TourLocation result = dao.mapWithAdditionalData(1, 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTourId());
        assertNotNull(result.getLocation());
        assertEquals("Paris", result.getLocation().getName());
        assertEquals("France", result.getLocation().getCountry());
        verify(locationDao).findById(1);
    }

    @Test
    void testCreate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.create(1, 1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testCreateFailed() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = dao.create(1, 1);

        // Then
        assertFalse(result);
    }

    @Test
    void testDelete() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.delete(1, 1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 1);
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
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.deleteAllById2(1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testExistsLink() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // When
        boolean result = dao.existsLink(1, 1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    void testExistsLinkNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        boolean result = dao.existsLink(1, 1);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindAllLinks() throws Exception {
        // Given
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("tour_id")).thenReturn(1, 2);
        when(resultSet.getInt("location_id")).thenReturn(1, 2);
        when(locationDao.findById(1)).thenReturn(testLocation);
        when(locationDao.findById(2)).thenReturn(testLocation);

        // When
        List<TourLocation> result = dao.findAllLinks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getTourId());
        assertEquals(2, result.get(1).getTourId());
    }

    @Test
    void testFindById1() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("location_id")).thenReturn(1, 2);
        when(locationDao.findById(1)).thenReturn(testLocation);
        when(locationDao.findById(2)).thenReturn(testLocation);

        // When
        List<TourLocation> result = dao.findById1(1);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getTourId());
        assertEquals(1, result.get(1).getTourId());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testFindById2() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("tour_id")).thenReturn(1);
        when(locationDao.findById(1)).thenReturn(testLocation);

        // When
        List<TourLocation> result = dao.findById2(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTourId());
        assertEquals("Paris", result.get(0).getLocation().getName());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testFindById1Empty() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        List<TourLocation> result = dao.findById1(999);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById2Empty() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        List<TourLocation> result = dao.findById2(999);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}