package tourapp.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractLinkDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;

    private TestLinkDao dao;
        record TestLinkEntity(int id1, int id2, String additionalData) {
    }

    static class TestLinkDao extends AbstractLinkDao<TestLinkEntity> {
        public TestLinkDao(ConnectionFactory connectionFactory) {
            super(connectionFactory);
        }

        @Override
        protected String getTableName() { return "test_links"; }

        @Override
        protected String getId1Column() { return "entity1_id"; }

        @Override
        protected String getId2Column() { return "entity2_id"; }

        @Override
        protected TestLinkEntity mapWithAdditionalData(int id1, int id2) {
            return new TestLinkEntity(id1, id2, "test_data");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new TestLinkDao(connectionFactory);
    }

    @Test
    void testCreate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.create(1, 2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = dao.delete(1, 2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
    }

    @Test
    void testDeleteAllById1() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        // When
        boolean result = dao.deleteAllById1(1);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testDeleteAllById2() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        // When
        boolean result = dao.deleteAllById2(2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 2);
    }

    @Test
    void testExistsLink() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // When
        boolean result = dao.existsLink(1, 2);

        // Then
        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
    }

    @Test
    void testExistsLinkNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        boolean result = dao.existsLink(1, 2);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindAllLinks() throws Exception {
        // Given
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("entity1_id")).thenReturn(1, 2);
        when(resultSet.getInt("entity2_id")).thenReturn(3, 4);

        // When
        List<TestLinkEntity> result = dao.findAllLinks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.getFirst().id1());
        assertEquals(3, result.getFirst().id2());
    }

    @Test
    void testFindById1() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("entity2_id")).thenReturn(3);

        // When
        List<TestLinkEntity> result = dao.findById1(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().id1());
        assertEquals(3, result.getFirst().id2());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void testFindById2() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("entity1_id")).thenReturn(1);

        // When
        List<TestLinkEntity> result = dao.findById2(3);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().id1());
        assertEquals(3, result.getFirst().id2());
        verify(preparedStatement).setInt(1, 3);
    }
}
