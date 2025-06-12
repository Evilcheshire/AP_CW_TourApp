package tourapp.service.user_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.user_dao.UserTypeDao;
import tourapp.model.user.UserType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTypeServiceTest {

    @Mock
    private UserTypeDao userTypeDao;

    @Mock
    private Function<UserType, String> mockNameExtractor;

    private UserTypeService userTypeService;

    @BeforeEach
    void setUp() {
        userTypeService = new UserTypeService(userTypeDao);
        lenient().when(userTypeDao.getNameExtractor()).thenReturn(mockNameExtractor);
    }

    @Test
    void getAll_ShouldReturnAllUserTypes() throws SQLException {
        // Given
        List<UserType> expectedTypes = Arrays.asList(new UserType(), new UserType());
        when(userTypeDao.findAll()).thenReturn(expectedTypes);

        // When
        List<UserType> result = userTypeService.getAll();

        // Then
        assertEquals(expectedTypes, result);
        verify(userTypeDao).findAll();
    }

    @Test
    void create_ShouldCreateUserTypeSuccessfully() throws SQLException {
        // Given
        UserType userType = new UserType();
        when(mockNameExtractor.apply(userType)).thenReturn("Admin");
        when(userTypeDao.existsWithName("Admin")).thenReturn(false);
        when(userTypeDao.create(userType)).thenReturn(true);

        // When
        boolean result = userTypeService.create(userType);

        // Then
        assertTrue(result);
        verify(userTypeDao).create(userType);
    }
}
