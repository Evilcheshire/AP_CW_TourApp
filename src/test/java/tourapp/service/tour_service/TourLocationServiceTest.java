package tourapp.service.tour_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.tour_dao.TourLocationDao;
import tourapp.model.tour.TourLocation;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourLocationServiceTest {

    @Mock private TourLocationDao tourLocationDao;

    private TourLocationService tourLocationService;

    @BeforeEach
    void setUp() {
        tourLocationService = new TourLocationService(tourLocationDao);
    }

    @Test
    void createLink_ShouldCreateLinkSuccessfully() throws SQLException {
        // Given
        when(tourLocationDao.existsLink(1, 2)).thenReturn(false);
        when(tourLocationDao.create(1, 2)).thenReturn(true);

        // When
        boolean result = tourLocationService.createLink(1, 2);

        // Then
        assertTrue(result);
        verify(tourLocationDao).existsLink(1, 2);
        verify(tourLocationDao).create(1, 2);
    }

    @Test
    void findById1_ShouldReturnTourLocations() throws SQLException {
        // Given
        List<TourLocation> expectedTourLocations = Arrays.asList(new TourLocation(), new TourLocation());
        when(tourLocationDao.findById1(1)).thenReturn(expectedTourLocations);

        // When
        List<TourLocation> result = tourLocationService.findById1(1);

        // Then
        assertEquals(expectedTourLocations, result);
        verify(tourLocationDao).findById1(1);
    }

    @Test
    void deleteAllById1_ShouldDeleteAllLinksForTour() throws SQLException {
        // Given
        when(tourLocationDao.deleteAllById1(1)).thenReturn(true);

        // When
        boolean result = tourLocationService.deleteAllById1(1);

        // Then
        assertTrue(result);
        verify(tourLocationDao).deleteAllById1(1);
    }
}
