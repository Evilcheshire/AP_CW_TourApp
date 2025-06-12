package tourapp.service.transport_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.transport_dao.TransportDao;
import tourapp.model.transport.Transport;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock
    private TransportDao transportDao;

    private TransportService transportService;

    @BeforeEach
    void setUp() {
        transportService = new TransportService(transportDao);
    }

    @Test
    void create_ShouldReturnTrue_WhenTransportCreated() throws SQLException {
        // Given
        Transport transport = new Transport();
        when(transportDao.create(transport)).thenReturn(true);

        // When
        boolean result = transportService.create(transport);

        // Then
        assertTrue(result);
        verify(transportDao).create(transport);
    }

    @Test
    void create_ShouldReturnFalse_WhenTransportNotCreated() throws SQLException {
        // Given
        Transport transport = new Transport();
        when(transportDao.create(transport)).thenReturn(false);

        // When
        boolean result = transportService.create(transport);

        // Then
        assertFalse(result);
        verify(transportDao).create(transport);
    }

    @Test
    void update_ShouldReturnTrue_WhenTransportUpdated() throws SQLException {
        // Given
        Transport transport = new Transport();
        when(transportDao.update(transport)).thenReturn(true);

        // When
        boolean result = transportService.update(transport);

        // Then
        assertTrue(result);
        verify(transportDao).update(transport);
    }

    @Test
    void create_ShouldThrowException_WhenDaoThrows() throws SQLException {
        // Given
        Transport transport = new Transport();
        doThrow(new SQLException("Database error")).when(transportDao).create(transport);

        // When & Then
        assertThrows(SQLException.class, () -> transportService.create(transport));
        verify(transportDao).create(transport);
    }
}
