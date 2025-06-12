package tourapp.service.tour_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.tour_dao.TourDao;
import tourapp.model.location.Location;
import tourapp.model.tour.Tour;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock private TourDao tourDao;

    private TourService tourService;

    @BeforeEach
    void setUp() {
        tourService = new TourService(tourDao);
    }

    @Test
    void getByIdWithDependencies_ShouldReturnTourWithDependencies() throws SQLException {
        // Given
        Tour expectedTour = new Tour();
        when(tourDao.findByIdWithDependencies(1)).thenReturn(expectedTour);

        // When
        Tour result = tourService.getByIdWithDependencies(1);

        // Then
        assertEquals(expectedTour, result);
        verify(tourDao).findByIdWithDependencies(1);
    }

    @Test
    void toggleActiveStatus_ShouldReturnTrue_WhenTourExists() throws SQLException {
        // Given
        Tour tour = new Tour();
        tour.setActive(false);
        when(tourDao.findByIdWithDependencies(1)).thenReturn(tour);

        // When
        boolean result = tourService.toggleActiveStatus(1, true);

        // Then
        assertTrue(result);
        assertTrue(tour.isActive());
        verify(tourDao).findByIdWithDependencies(1);
        verify(tourDao).update(tour);
    }

    @Test
    void toggleActiveStatus_ShouldReturnFalse_WhenTourNotExists() throws SQLException {
        // Given
        when(tourDao.findByIdWithDependencies(1)).thenReturn(null);

        // When
        boolean result = tourService.toggleActiveStatus(1, true);

        // Then
        assertFalse(result);
        verify(tourDao).findByIdWithDependencies(1);
        verify(tourDao, never()).update(any());
    }

    @Test
    void findActiveTours_ShouldReturnActiveTours() throws SQLException {
        // Given
        List<Tour> expectedTours = Arrays.asList(new Tour(), new Tour());
        when(tourDao.search(Map.of("isActive", true))).thenReturn(expectedTours);

        // When
        List<Tour> result = tourService.findActiveTours();

        // Then
        assertEquals(expectedTours, result);
        verify(tourDao).search(Map.of("isActive", true));
    }

    @Test
    void findInactiveTours_ShouldReturnInactiveTours() throws SQLException {
        // Given
        List<Tour> expectedTours = Arrays.asList(new Tour(), new Tour());
        when(tourDao.search(Map.of("isActive", false))).thenReturn(expectedTours);

        // When
        List<Tour> result = tourService.findInactiveTours();

        // Then
        assertEquals(expectedTours, result);
        verify(tourDao).search(Map.of("isActive", false));
    }

    @Test
    void getLocationsForTour_ShouldReturnLocations_WhenTourExists() throws SQLException {
        // Given
        Tour tour = new Tour();
        List<Location> expectedLocations = Arrays.asList(new Location(), new Location());
        tour.setLocations(expectedLocations);
        when(tourDao.findByIdWithDependencies(1)).thenReturn(tour);

        // When
        List<Location> result = tourService.getLocationsForTour(1);

        // Then
        assertEquals(expectedLocations, result);
        verify(tourDao).findByIdWithDependencies(1);
    }

    @Test
    void getLocationsForTour_ShouldReturnEmptyList_WhenTourNotExists() throws SQLException {
        // Given
        when(tourDao.findByIdWithDependencies(1)).thenReturn(null);

        // When
        List<Location> result = tourService.getLocationsForTour(1);

        // Then
        assertTrue(result.isEmpty());
        verify(tourDao).findByIdWithDependencies(1);
    }

    @Test
    void getLocationsForTour_ShouldReturnEmptyList_WhenLocationsNull() throws SQLException {
        // Given
        Tour tour = new Tour();
        tour.setLocations(null);
        when(tourDao.findByIdWithDependencies(1)).thenReturn(tour);

        // When
        List<Location> result = tourService.getLocationsForTour(1);

        // Then
        assertTrue(result.isEmpty());
        verify(tourDao).findByIdWithDependencies(1);
    }
}
