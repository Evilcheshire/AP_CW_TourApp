package tourapp.service.link_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.AbstractLinkDao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractLinkServiceTest {

    @Mock private AbstractLinkDao<TestLink> mockDao;

    private AbstractLinkService<TestLink> service;

    static class TestLink {
        private final int id1;
        private final int id2;
        private final String description;

        public TestLink(int id1, int id2, String description) {
            this.id1 = id1;
            this.id2 = id2;
            this.description = description;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestLink testLink = (TestLink) obj;
            return id1 == testLink.id1 && id2 == testLink.id2 &&
                    ((description == null && testLink.description == null) ||
                            (description != null && description.equals(testLink.description)));
        }

        @Override
        public int hashCode() {
            return id1 * 31 + id2;
        }

        @Override
        public String toString() {
            return "TestLink{id1=" + id1 + ", id2=" + id2 + ", description='" + description + "'}";
        }
    }

    @BeforeEach
    void setUp() {
        service = new AbstractLinkService<>(mockDao) {
        };
    }

    @Test
    void createLink_ShouldCreateSuccessfully_WhenLinkDoesNotExist() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenReturn(false);
        when(mockDao.create(1, 2)).thenReturn(true);

        // When
        boolean result = service.createLink(1, 2);

        // Then
        assertTrue(result);
        verify(mockDao).existsLink(1, 2);
        verify(mockDao).create(1, 2);
    }

    @Test
    void createLink_ShouldReturnFalse_WhenDaoReturnsFalse() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenReturn(false);
        when(mockDao.create(1, 2)).thenReturn(false);

        // When
        boolean result = service.createLink(1, 2);

        // Then
        assertFalse(result);
        verify(mockDao).existsLink(1, 2);
        verify(mockDao).create(1, 2);
    }

    @Test
    void createLink_ShouldThrowException_WhenLinkAlreadyExists() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createLink(1, 2)
        );
        assertEquals("Звʼязок уже існує.", exception.getMessage());
        verify(mockDao).existsLink(1, 2);
        verify(mockDao, never()).create(anyInt(), anyInt());
    }

    @Test
    void createLink_ShouldThrowSQLException_WhenExistsCheckFails() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.createLink(1, 2));
        verify(mockDao).existsLink(1, 2);
        verify(mockDao, never()).create(anyInt(), anyInt());
    }

    @Test
    void createLink_ShouldThrowSQLException_WhenCreateFails() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenReturn(false);
        when(mockDao.create(1, 2)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.createLink(1, 2));
        verify(mockDao).existsLink(1, 2);
        verify(mockDao).create(1, 2);
    }

    @Test
    void createLink_ShouldWorkForDifferentIds() throws SQLException {
        // Given
        when(mockDao.existsLink(10, 20)).thenReturn(false);
        when(mockDao.create(10, 20)).thenReturn(true);

        // When
        boolean result = service.createLink(10, 20);

        // Then
        assertTrue(result);
        verify(mockDao).existsLink(10, 20);
        verify(mockDao).create(10, 20);
    }

    @Test
    void deleteLink_ShouldReturnTrue_WhenDeleted() throws SQLException {
        // Given
        when(mockDao.delete(1, 2)).thenReturn(true);

        // When
        boolean result = service.deleteLink(1, 2);

        // Then
        assertTrue(result);
        verify(mockDao).delete(1, 2);
    }

    @Test
    void deleteLink_ShouldReturnFalse_WhenNotDeleted() throws SQLException {
        // Given
        when(mockDao.delete(1, 2)).thenReturn(false);

        // When
        boolean result = service.deleteLink(1, 2);

        // Then
        assertFalse(result);
        verify(mockDao).delete(1, 2);
    }

    @Test
    void deleteLink_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.delete(1, 2)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.deleteLink(1, 2));
        verify(mockDao).delete(1, 2);
    }

    @Test
    void deleteLink_ShouldWorkForDifferentIds() throws SQLException {
        // Given
        when(mockDao.delete(100, 200)).thenReturn(true);

        // When
        boolean result = service.deleteLink(100, 200);

        // Then
        assertTrue(result);
        verify(mockDao).delete(100, 200);
    }

    @Test
    void deleteAllById1_ShouldReturnTrue_WhenDeleted() throws SQLException {
        // Given
        when(mockDao.deleteAllById1(1)).thenReturn(true);

        // When
        boolean result = service.deleteAllById1(1);

        // Then
        assertTrue(result);
        verify(mockDao).deleteAllById1(1);
    }

    @Test
    void deleteAllById1_ShouldReturnFalse_WhenNoLinksDeleted() throws SQLException {
        // Given
        when(mockDao.deleteAllById1(999)).thenReturn(false);

        // When
        boolean result = service.deleteAllById1(999);

        // Then
        assertFalse(result);
        verify(mockDao).deleteAllById1(999);
    }

    @Test
    void deleteAllById1_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.deleteAllById1(1)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.deleteAllById1(1));
        verify(mockDao).deleteAllById1(1);
    }

    @Test
    void deleteAllById1_ShouldWorkForDifferentIds() throws SQLException {
        // Given
        when(mockDao.deleteAllById1(50)).thenReturn(true);

        // When
        boolean result = service.deleteAllById1(50);

        // Then
        assertTrue(result);
        verify(mockDao).deleteAllById1(50);
    }

    @Test
    void deleteAllById2_ShouldReturnTrue_WhenDeleted() throws SQLException {
        // Given
        when(mockDao.deleteAllById2(2)).thenReturn(true);

        // When
        boolean result = service.deleteAllById2(2);

        // Then
        assertTrue(result);
        verify(mockDao).deleteAllById2(2);
    }

    @Test
    void deleteAllById2_ShouldReturnFalse_WhenNoLinksDeleted() throws SQLException {
        // Given
        when(mockDao.deleteAllById2(999)).thenReturn(false);

        // When
        boolean result = service.deleteAllById2(999);

        // Then
        assertFalse(result);
        verify(mockDao).deleteAllById2(999);
    }

    @Test
    void deleteAllById2_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.deleteAllById2(2)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.deleteAllById2(2));
        verify(mockDao).deleteAllById2(2);
    }

    @Test
    void deleteAllById2_ShouldWorkForDifferentIds() throws SQLException {
        // Given
        when(mockDao.deleteAllById2(75)).thenReturn(true);

        // When
        boolean result = service.deleteAllById2(75);

        // Then
        assertTrue(result);
        verify(mockDao).deleteAllById2(75);
    }

    // ========== exists() Tests ==========
    @Test
    void exists_ShouldReturnTrue_WhenLinkExists() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenReturn(true);

        // When
        boolean result = service.exists(1, 2);

        // Then
        assertTrue(result);
        verify(mockDao).existsLink(1, 2);
    }

    @Test
    void exists_ShouldReturnFalse_WhenLinkDoesNotExist() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenReturn(false);

        // When
        boolean result = service.exists(1, 2);

        // Then
        assertFalse(result);
        verify(mockDao).existsLink(1, 2);
    }

    @Test
    void exists_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.existsLink(1, 2)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.exists(1, 2));
        verify(mockDao).existsLink(1, 2);
    }

    @Test
    void exists_ShouldWorkForDifferentIds() throws SQLException {
        // Given
        when(mockDao.existsLink(300, 400)).thenReturn(true);

        // When
        boolean result = service.exists(300, 400);

        // Then
        assertTrue(result);
        verify(mockDao).existsLink(300, 400);
    }

    @Test
    void findAll_ShouldReturnAllLinks() throws SQLException {
        // Given
        List<TestLink> expectedLinks = Arrays.asList(
                new TestLink(1, 2, "link1"),
                new TestLink(3, 4, "link2")
        );
        when(mockDao.findAllLinks()).thenReturn(expectedLinks);

        // When
        List<TestLink> result = service.findAll();

        // Then
        assertEquals(expectedLinks, result);
        verify(mockDao).findAllLinks();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoLinks() throws SQLException {
        // Given
        when(mockDao.findAllLinks()).thenReturn(Collections.emptyList());

        // When
        List<TestLink> result = service.findAll();

        // Then
        assertTrue(result.isEmpty());
        verify(mockDao).findAllLinks();
    }

    @Test
    void findById2_ShouldReturnLinks_WhenLinksExist() throws SQLException {
        // Given
        List<TestLink> expectedLinks = Arrays.asList(
                new TestLink(1, 5, "link1"),
                new TestLink(3, 5, "link2")
        );
        when(mockDao.findById2(5)).thenReturn(expectedLinks);

        // When
        List<TestLink> result = service.findById2(5);

        // Then
        assertEquals(expectedLinks, result);
        verify(mockDao).findById2(5);
    }

    @Test
    void findById2_ShouldReturnEmptyList_WhenNoLinksExist() throws SQLException {
        // Given
        when(mockDao.findById2(999)).thenReturn(Collections.emptyList());

        // When
        List<TestLink> result = service.findById2(999);

        // Then
        assertTrue(result.isEmpty());
        verify(mockDao).findById2(999);
    }

    @Test
    void findById2_ShouldThrowSQLException_WhenDaoThrows() throws SQLException {
        // Given
        when(mockDao.findById2(5)).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(SQLException.class, () -> service.findById2(5));
        verify(mockDao).findById2(5);
    }

    @Test
    void findById2_ShouldWorkForDifferentIds() throws SQLException {
        // Given
        List<TestLink> expectedLinks = List.of(
                new TestLink(10, 100, "special link")
        );
        when(mockDao.findById2(100)).thenReturn(expectedLinks);

        // When
        List<TestLink> result = service.findById2(100);

        // Then
        assertEquals(expectedLinks, result);
        verify(mockDao).findById2(100);
    }

    @Test
    void findById2_ShouldReturnNullValues_WhenDaoReturnsNull() throws SQLException {
        // Given
        when(mockDao.findById2(1)).thenReturn(null);

        // When
        List<TestLink> result = service.findById2(1);

        // Then
        assertNull(result);
        verify(mockDao).findById2(1);
    }
}