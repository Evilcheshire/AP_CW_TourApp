package tourapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.AbstractGenericDao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractGenericServiceTest {

    @Mock
    private AbstractGenericDao<String> mockDao;

    private AbstractGenericService<String> service;

    @BeforeEach
    void setUp() {
        service = new AbstractGenericService<String>(mockDao) {
        };
    }

    @Test
    void getAll_ShouldReturnAllItems() throws SQLException {
        // Given
        List<String> expectedItems = Arrays.asList("item1", "item2", "item3");
        when(mockDao.findAll()).thenReturn(expectedItems);

        // When
        List<String> result = service.getAll();

        // Then
        assertEquals(expectedItems, result);
        verify(mockDao).findAll();
    }

    @Test
    void getById_ShouldReturnItem() throws SQLException {
        // Given
        String expectedItem = "item1";
        when(mockDao.findById(1)).thenReturn(expectedItem);

        // When
        String result = service.getById(1);

        // Then
        assertEquals(expectedItem, result);
        verify(mockDao).findById(1);
    }

    @Test
    void delete_ShouldReturnTrue_WhenDeleted() throws SQLException {
        // Given
        when(mockDao.delete(1)).thenReturn(true);

        // When
        boolean result = service.delete(1);

        // Then
        assertTrue(result);
        verify(mockDao).delete(1);
    }

    @Test
    void search_ShouldReturnSearchResults() throws SQLException {
        // Given
        Map<String, Object> searchParams = Map.of("name", "test");
        List<String> expectedResults = Arrays.asList("result1", "result2");
        when(mockDao.search(searchParams)).thenReturn(expectedResults);

        // When
        List<String> result = service.search(searchParams);

        // Then
        assertEquals(expectedResults, result);
        verify(mockDao).search(searchParams);
    }
}
