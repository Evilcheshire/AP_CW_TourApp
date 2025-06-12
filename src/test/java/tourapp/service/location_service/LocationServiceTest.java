package tourapp.service.location_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import tourapp.dao.location_dao.LocationDao;
import tourapp.model.location.Location;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock private LocationDao locationDao;

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(locationDao);
    }

    @Test
    void create_ShouldCallDaoInsert() throws SQLException {
        // Given
        Location location = new Location();

        // When
        locationService.create(location);

        // Then
        verify(locationDao).create(location);
    }

    @Test
    void update_ShouldCallDaoUpdate() throws SQLException {
        // Given
        Location location = new Location();

        // When
        locationService.update(location);

        // Then
        verify(locationDao).update(location);
    }

    @Test
    void create_ShouldThrowException_WhenDaoThrows() throws SQLException {
        // Given
        Location location = new Location();
        doThrow(new SQLException("Database error")).when(locationDao).create(location);

        // When & Then
        assertThrows(SQLException.class, () -> locationService.create(location));
        verify(locationDao).create(location);
    }
}