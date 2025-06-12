package tourapp.service.location_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.location_dao.LocationTypeDao;
import tourapp.model.location.LocationType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationTypeServiceTest {

    @Mock
    private LocationTypeDao locationTypeDao;

    @Mock
    private Function<LocationType, String> mockNameExtractor;

    private LocationTypeService locationTypeService;

    @BeforeEach
    void setUp() {
        locationTypeService = new LocationTypeService(locationTypeDao);
        lenient().when(locationTypeDao.getNameExtractor()).thenReturn(mockNameExtractor);
    }

    @Test
    void getAll_ShouldReturnAllLocationTypes() throws SQLException {
        // Given
        List<LocationType> expectedTypes = Arrays.asList(new LocationType(), new LocationType());
        when(locationTypeDao.findAll()).thenReturn(expectedTypes);

        // When
        List<LocationType> result = locationTypeService.getAll();

        // Then
        assertEquals(expectedTypes, result);
        verify(locationTypeDao).findAll();
    }

    @Test
    void create_ShouldCreateLocationTypeSuccessfully() throws SQLException {
        // Given
        LocationType locationType = new LocationType();
        when(mockNameExtractor.apply(locationType)).thenReturn("Beach");
        when(locationTypeDao.existsWithName("Beach")).thenReturn(false);
        when(locationTypeDao.create(locationType)).thenReturn(true);

        // When
        boolean result = locationTypeService.create(locationType);

        // Then
        assertTrue(result);
        verify(locationTypeDao).create(locationType);
    }
}
