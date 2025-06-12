package tourapp.service.type_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.AbstractTypeDao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractTypeServiceTest {

    @Mock
    private AbstractTypeDao<TestType> mockDao;

    @Mock
    private Function<TestType, String> mockNameExtractor;

    private AbstractTypeService<TestType> service;

    static class TestType {
        private final String name;
        private final int id;

        public TestType(String name) {
            this(name, 0);
        }

        public TestType(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestType testType = (TestType) obj;
            return id == testType.id &&
                    ((name == null && testType.name == null) ||
                            (name != null && name.equals(testType.name)));
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    @BeforeEach
    void setUp() {
        service = new AbstractTypeService<TestType>(mockDao) {};
        lenient().when(mockDao.getNameExtractor()).thenReturn(mockNameExtractor);
    }

    @Test
    void getAll_ShouldReturnAllTypes() throws SQLException {
        // Given
        List<TestType> expectedTypes = Arrays.asList(
                new TestType("Type1", 1),
                new TestType("Type2", 2)
        );
        when(mockDao.findAll()).thenReturn(expectedTypes);

        // When
        List<TestType> result = service.getAll();

        // Then
        assertEquals(expectedTypes, result);
        verify(mockDao).findAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoTypes() throws SQLException {
        // Given
        when(mockDao.findAll()).thenReturn(Collections.emptyList());

        // When
        List<TestType> result = service.getAll();

        // Then
        assertTrue(result.isEmpty());
        verify(mockDao).findAll();
    }

    @Test
    void getAll_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.findAll()).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.getAll());
        verify(mockDao).findAll();
    }

    @Test
    void getById_ShouldReturnType_WhenExists() throws SQLException {
        // Given
        TestType expectedType = new TestType("Test Type", 1);
        when(mockDao.findById(1)).thenReturn(expectedType);

        // When
        TestType result = service.getById(1);

        // Then
        assertEquals(expectedType, result);
        verify(mockDao).findById(1);
    }

    @Test
    void getById_ShouldReturnNull_WhenNotExists() throws SQLException {
        // Given
        when(mockDao.findById(999)).thenReturn(null);

        // When
        TestType result = service.getById(999);

        // Then
        assertNull(result);
        verify(mockDao).findById(999);
    }

    @Test
    void getById_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.findById(1)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.getById(1));
        verify(mockDao).findById(1);
    }

    // ========== create() Tests ==========
    @Test
    void create_ShouldCreateSuccessfully_WhenValidName() throws SQLException {
        // Given
        TestType type = new TestType("Test Type");
        when(mockNameExtractor.apply(type)).thenReturn("Test Type");
        when(mockDao.existsWithName("Test Type")).thenReturn(false);
        when(mockDao.create(type)).thenReturn(true);

        // When
        boolean result = service.create(type);

        // Then
        assertTrue(result);
        verify(mockDao).create(type);
        verify(mockDao).existsWithName("Test Type");
    }

    @Test
    void create_ShouldReturnFalse_WhenDaoReturnsFalse() throws SQLException {
        // Given
        TestType type = new TestType("Test Type");
        when(mockNameExtractor.apply(type)).thenReturn("Test Type");
        when(mockDao.existsWithName("Test Type")).thenReturn(false);
        when(mockDao.create(type)).thenReturn(false);

        // When
        boolean result = service.create(type);

        // Then
        assertFalse(result);
        verify(mockDao).create(type);
        verify(mockDao).existsWithName("Test Type");
    }

    @Test
    void create_ShouldThrowException_WhenNameIsEmpty() throws SQLException {
        // Given
        TestType type = new TestType("");
        when(mockNameExtractor.apply(type)).thenReturn("");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(type)
        );
        assertEquals("Назва не може бути порожньою.", exception.getMessage());
        verify(mockDao, never()).create(any());
        verify(mockDao, never()).existsWithName(anyString());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsBlank() throws SQLException {
        // Given
        TestType type = new TestType("   ");
        when(mockNameExtractor.apply(type)).thenReturn("   ");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(type)
        );
        assertEquals("Назва не може бути порожньою.", exception.getMessage());
        verify(mockDao, never()).create(any());
        verify(mockDao, never()).existsWithName(anyString());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsNull() throws SQLException {
        // Given
        TestType type = new TestType(null);
        when(mockNameExtractor.apply(type)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(type)
        );
        assertEquals("Назва не може бути порожньою.", exception.getMessage());
        verify(mockDao, never()).create(any());
        verify(mockDao, never()).existsWithName(anyString());
    }

    @Test
    void create_ShouldThrowException_WhenNameAlreadyExists() throws SQLException {
        // Given
        TestType type = new TestType("Existing Type");
        when(mockNameExtractor.apply(type)).thenReturn("Existing Type");
        when(mockDao.existsWithName("Existing Type")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(type)
        );
        assertEquals("Об'єкт з такою назвою вже існує.", exception.getMessage());
        verify(mockDao, never()).create(any());
        verify(mockDao).existsWithName("Existing Type");
    }

    @Test
    void create_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        TestType type = new TestType("Test Type");
        when(mockNameExtractor.apply(type)).thenReturn("Test Type");
        when(mockDao.existsWithName("Test Type")).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.create(type));
        verify(mockDao).existsWithName("Test Type");
        verify(mockDao, never()).create(any());
    }

    @Test
    void update_ShouldUpdateSuccessfully_WhenValidName() throws SQLException {
        // Given
        TestType type = new TestType("Updated Type");
        when(mockNameExtractor.apply(type)).thenReturn("Updated Type");

        // When
        service.update(1, type);

        // Then
        verify(mockDao).update(type, 1);
    }

    @Test
    void update_ShouldThrowException_WhenNameIsEmpty() throws SQLException {
        // Given
        TestType type = new TestType("");
        when(mockNameExtractor.apply(type)).thenReturn("");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1, type)
        );
        assertEquals("Назва не може бути порожньою.", exception.getMessage());
        verify(mockDao, never()).update(any(), anyInt());
    }

    @Test
    void update_ShouldThrowException_WhenNameIsBlank() throws SQLException {
        // Given
        TestType type = new TestType("   ");
        when(mockNameExtractor.apply(type)).thenReturn("   ");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1, type)
        );
        assertEquals("Назва не може бути порожньою.", exception.getMessage());
        verify(mockDao, never()).update(any(), anyInt());
    }

    @Test
    void update_ShouldThrowException_WhenNameIsNull() throws SQLException {
        // Given
        TestType type = new TestType(null);
        when(mockNameExtractor.apply(type)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1, type)
        );
        assertEquals("Назва не може бути порожньою.", exception.getMessage());
        verify(mockDao, never()).update(any(), anyInt());
    }

    @Test
    void update_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        TestType type = new TestType("Updated Type");
        when(mockNameExtractor.apply(type)).thenReturn("Updated Type");
        doThrow(new SQLException("Database error")).when(mockDao).update(type, 1);

        // When & Then
        assertThrows(SQLException.class, () -> service.update(1, type));
        verify(mockDao).update(type, 1);
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
    void delete_ShouldReturnFalse_WhenNotDeleted() throws SQLException {
        // Given
        when(mockDao.delete(999)).thenReturn(false);

        // When
        boolean result = service.delete(999);

        // Then
        assertFalse(result);
        verify(mockDao).delete(999);
    }

    @Test
    void delete_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.delete(1)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.delete(1));
        verify(mockDao).delete(1);
    }

    @Test
    void searchByName_ShouldReturnResults() throws SQLException {
        // Given
        List<TestType> expectedResults = Arrays.asList(
                new TestType("Test1", 1),
                new TestType("Test2", 2)
        );
        when(mockDao.findByName("Test")).thenReturn(expectedResults);

        // When
        List<TestType> result = service.searchByName("Test");

        // Then
        assertEquals(expectedResults, result);
        verify(mockDao).findByName("Test");
    }

    @Test
    void searchByName_ShouldReturnEmptyList_WhenNoMatches() throws SQLException {
        // Given
        when(mockDao.findByName("NonExistent")).thenReturn(Collections.emptyList());

        // When
        List<TestType> result = service.searchByName("NonExistent");

        // Then
        assertTrue(result.isEmpty());
        verify(mockDao).findByName("NonExistent");
    }

    @Test
    void searchByName_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.findByName("Test")).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.searchByName("Test"));
        verify(mockDao).findByName("Test");
    }

    @Test
    void findByExactName_ShouldReturnType_WhenExists() throws SQLException {
        // Given
        TestType expectedType = new TestType("Exact Name", 1);
        when(mockDao.findByExactName("Exact Name")).thenReturn(expectedType);

        // When
        TestType result = service.findByExactName("Exact Name");

        // Then
        assertEquals(expectedType, result);
        verify(mockDao).findByExactName("Exact Name");
    }

    @Test
    void findByExactName_ShouldReturnNull_WhenNotExists() throws SQLException {
        // Given
        when(mockDao.findByExactName("NonExistent")).thenReturn(null);

        // When
        TestType result = service.findByExactName("NonExistent");

        // Then
        assertNull(result);
        verify(mockDao).findByExactName("NonExistent");
    }

    @Test
    void findByExactName_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.findByExactName("Test")).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.findByExactName("Test"));
        verify(mockDao).findByExactName("Test");
    }
}