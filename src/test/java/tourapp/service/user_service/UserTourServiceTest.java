package tourapp.service.user_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.user_dao.UserTourDao;
import tourapp.model.user.UserTour;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTourServiceTest {

    @Mock
    private UserTourDao userTourDao;

    private UserTourService userTourService;

    @BeforeEach
    void setUp() {
        userTourService = new UserTourService(userTourDao);
    }

    @Test
    void search_ShouldReturnSearchResults() throws SQLException {
        // Given
        List<UserTour> expectedResults = Arrays.asList(new UserTour(), new UserTour());
        Date startDate = Date.valueOf("2023-01-01");
        Date endDate = Date.valueOf("2023-12-31");

        when(userTourDao.search(1, 2, 3, 4, startDate, endDate, 100.0, 500.0))
                .thenReturn(expectedResults);

        // When
        List<UserTour> result = userTourService.search(1, 2, 3, 4, startDate, endDate, 100.0, 500.0);

        // Then
        assertEquals(expectedResults, result);
        verify(userTourDao).search(1, 2, 3, 4, startDate, endDate, 100.0, 500.0);
    }

    @Test
    void countUsersByTourId_ShouldReturnCount() throws SQLException {
        // Given
        when(userTourDao.countUsersByTourId(1)).thenReturn(5);

        // When
        int result = userTourService.countUsersByTourId(1);

        // Then
        assertEquals(5, result);
        verify(userTourDao).countUsersByTourId(1);
    }

    @Test
    void search_WithNullParameters_ShouldWork() throws SQLException {
        // Given
        List<UserTour> expectedResults = List.of(new UserTour());
        when(userTourDao.search(null, null, null, null, null, null, null, null))
                .thenReturn(expectedResults);

        // When
        List<UserTour> result = userTourService.search(null, null, null, null, null, null, null, null);

        // Then
        assertEquals(expectedResults, result);
        verify(userTourDao).search(null, null, null, null, null, null, null, null);
    }
}
