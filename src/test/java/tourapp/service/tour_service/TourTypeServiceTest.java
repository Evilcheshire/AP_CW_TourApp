package tourapp.service.tour_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.tour_dao.TourTypeDao;
import tourapp.model.tour.TourType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourTypeServiceTest {

    @Mock
    private TourTypeDao tourTypeDao;

    @Mock
    private Function<TourType, String> mockNameExtractor;

    private TourTypeService tourTypeService;

    @BeforeEach
    void setUp() {
        tourTypeService = new TourTypeService(tourTypeDao);
        lenient().when(tourTypeDao.getNameExtractor()).thenReturn(mockNameExtractor);
    }

    @Test
    void getAll_ShouldReturnAllTourTypes() throws SQLException {
        // Given
        List<TourType> expectedTypes = Arrays.asList(new TourType(), new TourType());
        when(tourTypeDao.findAll()).thenReturn(expectedTypes);

        // When
        List<TourType> result = tourTypeService.getAll();

        // Then
        assertEquals(expectedTypes, result);
        verify(tourTypeDao).findAll();
    }

    @Test
    void create_ShouldCreateTourTypeSuccessfully() throws SQLException {
        // Given
        TourType tourType = new TourType();
        when(mockNameExtractor.apply(tourType)).thenReturn("Adventure");
        when(tourTypeDao.existsWithName("Adventure")).thenReturn(false);
        when(tourTypeDao.create(tourType)).thenReturn(true);

        // When
        boolean result = tourTypeService.create(tourType);

        // Then
        assertTrue(result);
        verify(tourTypeDao).create(tourType);
    }
}
