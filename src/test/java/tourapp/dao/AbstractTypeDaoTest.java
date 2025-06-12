package tourapp.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AbstractTypeDaoTest {

    @Mock private ConnectionFactory connectionFactory;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private TestTypeEntity testEntity;
    private TestTypeDao dao;

    static class TestTypeEntity {
        private int id;
        private String name;

        public TestTypeEntity() {}
        public TestTypeEntity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class TestTypeDao extends AbstractTypeDao<TestTypeEntity> {
        public TestTypeDao(ConnectionFactory connectionFactory) {
            super(
                    connectionFactory,
                    "test_types",
                    rs -> new TestTypeEntity(rs.getInt("id"), rs.getString("name")),
                    TestTypeEntity::getName,
                    testTypeEntity -> Optional.of(testTypeEntity.getId()),
                    TestTypeEntity::setId
            );
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionFactory.getConnection()).thenReturn(connection);
        dao = new TestTypeDao(connectionFactory);
        testEntity = new TestTypeEntity(1, "Test Type");
    }

    @Test
    void testCreate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        TestTypeEntity entity = new TestTypeEntity(0, "New Type");

        // When
        boolean result = dao.create(entity);

        // Then
        assertTrue(result);
        assertEquals(1, entity.getId());
        verify(preparedStatement).setString(1, "New Type");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdate() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        dao.update(testEntity, 1);

        // Then
        verify(preparedStatement).setString(1, "Test Type");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testFindByName() throws Exception {
        try (var mockedDaoUtils = mockStatic(DaoUtils.class)) {
            mockedDaoUtils.when(() -> DaoUtils.executeSearchQuery(
                    any(), anyString(), anyString(), any(), any(), any(), any()
            )).thenReturn(List.of(testEntity));

            // When
            List<TestTypeEntity> result = dao.findByName("Test Type");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Type", result.getFirst().getName());
        }
    }

    @Test
    void testFindByExactName() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Test Type");

        // When
        TestTypeEntity result = dao.findByExactName("Test Type");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Type", result.getName());
        verify(preparedStatement).setString(1, "Test Type");
    }

    @Test
    void testFindByExactNameNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        TestTypeEntity result = dao.findByExactName("NonExistent");

        // Then
        assertNull(result);
    }

    @Test
    void testExistsWithName() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        // When
        boolean result = dao.existsWithName("Test Type");

        // Then
        assertTrue(result);
        verify(preparedStatement).setString(1, "Test Type");
    }

    @Test
    void testExistsWithNameNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        // When
        boolean result = dao.existsWithName("NonExistent");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetNameExtractor() {
        // When
        Function<TestTypeEntity, String> extractor = dao.getNameExtractor();

        // Then
        assertNotNull(extractor);
        assertEquals("Test Type", extractor.apply(testEntity));
    }
}
