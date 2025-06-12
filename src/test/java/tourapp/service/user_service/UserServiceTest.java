package tourapp.service.user_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.user_dao.UserDao;
import tourapp.model.user.User;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
    }

    @Test
    void findByEmail_ShouldReturnUser() throws SQLException {
        // Given
        User expectedUser = new User();
        when(userDao.findByEmail("test@example.com")).thenReturn(expectedUser);

        // When
        User result = userService.findByEmail("test@example.com");

        // Then
        assertEquals(expectedUser, result);
        verify(userDao).findByEmail("test@example.com");
    }

    @Test
    void create_ShouldReturnTrue_WhenUserCreated() throws SQLException {
        // Given
        User user = new User();
        when(userDao.create(user)).thenReturn(true);

        // When
        boolean result = userService.create(user);

        // Then
        assertTrue(result);
        verify(userDao).create(user);
    }

    @Test
    void update_ShouldReturnTrue_WhenUserUpdated() throws SQLException {
        // Given
        User user = new User();
        when(userDao.update(user)).thenReturn(true);

        // When
        boolean result = userService.update(user);

        // Then
        assertTrue(result);
        verify(userDao).update(user);
    }

    @Test
    void changePassword_ShouldReturnTrue_WhenPasswordChanged() throws SQLException {
        // Given
        when(userDao.changePassword(1, "newPassword")).thenReturn(true);

        // When
        boolean result = userService.changePassword(1, "newPassword");

        // Then
        assertTrue(result);
        verify(userDao).changePassword(1, "newPassword");
    }

    @Test
    void authenticate_ShouldReturnUser_WhenCredentialsValid() throws SQLException {
        // Given
        User expectedUser = new User();
        when(userDao.authenticate("test@example.com", "password")).thenReturn(true);
        when(userDao.findByEmail("test@example.com")).thenReturn(expectedUser);

        // When
        User result = userService.authenticate("test@example.com", "password");

        // Then
        assertEquals(expectedUser, result);
        verify(userDao).authenticate("test@example.com", "password");
        verify(userDao).findByEmail("test@example.com");
    }

    @Test
    void authenticate_ShouldReturnNull_WhenCredentialsInvalid() throws SQLException {
        // Given
        when(userDao.authenticate("test@example.com", "wrongPassword")).thenReturn(false);

        // When
        User result = userService.authenticate("test@example.com", "wrongPassword");

        // Then
        assertNull(result);
        verify(userDao).authenticate("test@example.com", "wrongPassword");
        verify(userDao, never()).findByEmail(any());
    }

    @Test
    void searchByTerm_ShouldReturnListOfUsers() throws SQLException {
        // Given
        String searchTerm = "john";
        User user1 = new User();
        User user2 = new User();
        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(userDao.searchByTerm(searchTerm)).thenReturn(expectedUsers);

        // When
        List<User> result = userService.searchByTerm(searchTerm);

        // Then
        assertEquals(expectedUsers, result);
        assertEquals(2, result.size());
        verify(userDao).searchByTerm(searchTerm);
    }
}
