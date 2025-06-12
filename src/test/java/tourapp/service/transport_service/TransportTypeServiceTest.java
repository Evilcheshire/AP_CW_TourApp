package tourapp.service.transport_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.transport_dao.TransportTypeDao;
import tourapp.model.transport.TransportType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportTypeServiceTest {

    @Mock
    private TransportTypeDao transportTypeDao;

    @Mock
    private Function<TransportType, String> mockNameExtractor;

    private TransportTypeService transportTypeService;

    @BeforeEach
    void setUp() {
        transportTypeService = new TransportTypeService(transportTypeDao);
        lenient().when(transportTypeDao.getNameExtractor()).thenReturn(mockNameExtractor);
    }

    @Test
    void getAll_ShouldReturnAllTransportTypes() throws SQLException {
        // Given
        List<TransportType> expectedTypes = Arrays.asList(new TransportType(), new TransportType());
        when(transportTypeDao.findAll()).thenReturn(expectedTypes);

        // When
        List<TransportType> result = transportTypeService.getAll();

        // Then
        assertEquals(expectedTypes, result);
        verify(transportTypeDao).findAll();
    }

    @Test
    void create_ShouldCreateTransportTypeSuccessfully() throws SQLException {
        // Given
        TransportType transportType = new TransportType();
        when(mockNameExtractor.apply(transportType)).thenReturn("Bus");
        when(transportTypeDao.existsWithName("Bus")).thenReturn(false);
        when(transportTypeDao.create(transportType)).thenReturn(true);

        // When
        boolean result = transportTypeService.create(transportType);

        // Then
        assertTrue(result);
        verify(transportTypeDao).create(transportType);
    }
}
