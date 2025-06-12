package tourapp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tourapp.model.user.User;
import tourapp.util.SessionManager.UserSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @Mock private User mockUser;
    @Mock private User mockAdminUser;
    @Mock private User mockManagerUser;
    @Mock private User mockCustomerUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionManager = new SessionManager();

        when(mockAdminUser.isAdmin()).thenReturn(true);
        when(mockAdminUser.isManager()).thenReturn(false);
        when(mockAdminUser.isCustomer()).thenReturn(false);

        when(mockManagerUser.isAdmin()).thenReturn(false);
        when(mockManagerUser.isManager()).thenReturn(true);
        when(mockManagerUser.isCustomer()).thenReturn(false);

        when(mockCustomerUser.isAdmin()).thenReturn(false);
        when(mockCustomerUser.isManager()).thenReturn(false);
        when(mockCustomerUser.isCustomer()).thenReturn(true);
    }

    @Test
    void shouldStartSessionSuccessfully() {
        sessionManager.startSession(mockUser);

        assertTrue(sessionManager.hasActiveSession());
        assertNotNull(sessionManager.getCurrentSession());
        assertEquals(mockUser, sessionManager.getCurrentSession().user());
    }

    @Test
    void shouldNotHaveActiveSessionInitially() {
        assertFalse(sessionManager.hasActiveSession());
        assertNull(sessionManager.getCurrentSession());
    }

    @Test
    void shouldEndSessionSuccessfully() {
        sessionManager.startSession(mockUser);
        assertTrue(sessionManager.hasActiveSession());

        sessionManager.endSession();

        assertFalse(sessionManager.hasActiveSession());
        assertNull(sessionManager.getCurrentSession());
    }

    @Test
    void shouldHandleMultipleEndSessionCallsSafely() {
        sessionManager.startSession(mockUser);

        sessionManager.endSession();
        assertFalse(sessionManager.hasActiveSession());

        assertDoesNotThrow(() -> sessionManager.endSession());
        assertFalse(sessionManager.hasActiveSession());
    }

    @Test
    void shouldHandleEndSessionWhenNoActiveSession() {
        assertFalse(sessionManager.hasActiveSession());

        assertDoesNotThrow(() -> sessionManager.endSession());
        assertFalse(sessionManager.hasActiveSession());
    }

    @Test
    void shouldOverwriteExistingSession() {
        User firstUser = mock(User.class);
        User secondUser = mock(User.class);

        sessionManager.startSession(firstUser);
        UserSession firstSession = sessionManager.getCurrentSession();

        sessionManager.startSession(secondUser);
        UserSession secondSession = sessionManager.getCurrentSession();

        assertTrue(sessionManager.hasActiveSession());
        assertEquals(secondUser, sessionManager.getCurrentSession().user());
        assertNotSame(firstSession, secondSession);
    }

    @Test
    void shouldHandleMultipleSessionOperations() {
        sessionManager.startSession(mockUser);
        assertTrue(sessionManager.hasActiveSession());

        sessionManager.endSession();
        assertFalse(sessionManager.hasActiveSession());

        sessionManager.startSession(mockAdminUser);
        assertTrue(sessionManager.hasActiveSession());
        assertTrue(sessionManager.getCurrentSession().isAdmin());

        sessionManager.endSession();
        assertFalse(sessionManager.hasActiveSession());
    }

    @Test
    void shouldHandleNullUserInSession() {
        sessionManager.startSession(null);

        assertTrue(sessionManager.hasActiveSession());
        assertNotNull(sessionManager.getCurrentSession());
        assertNull(sessionManager.getCurrentSession().user());
    }

    @Test
    void shouldHandleUserSessionWithNullUserSafely() {
        sessionManager.startSession(null);
        UserSession session = sessionManager.getCurrentSession();

        assertThrows(NullPointerException.class, session::isAdmin);
        assertThrows(NullPointerException.class, session::isManager);
        assertThrows(NullPointerException.class, session::isCustomer);
    }

    @Test
    void shouldHandleGetCurrentSessionWhenNoActiveSession() {
        assertNull(sessionManager.getCurrentSession());

        sessionManager.endSession();
        assertNull(sessionManager.getCurrentSession());
    }

    @Test
    void userSessionShouldIdentifyAdminCorrectly() {
        sessionManager.startSession(mockAdminUser);
        UserSession session = sessionManager.getCurrentSession();

        assertTrue(session.isAdmin());
        assertFalse(session.isManager());
        assertFalse(session.isCustomer());
    }

    @Test
    void userSessionShouldIdentifyManagerCorrectly() {
        sessionManager.startSession(mockManagerUser);
        UserSession session = sessionManager.getCurrentSession();

        assertFalse(session.isAdmin());
        assertTrue(session.isManager());
        assertFalse(session.isCustomer());
    }

    @Test
    void userSessionShouldIdentifyCustomerCorrectly() {
        sessionManager.startSession(mockCustomerUser);
        UserSession session = sessionManager.getCurrentSession();

        assertFalse(session.isAdmin());
        assertFalse(session.isManager());
        assertTrue(session.isCustomer());
    }

    @Test
    void shouldHandleUserWithMultipleRoles() {
        User multiRoleUser = mock(User.class);
        when(multiRoleUser.isAdmin()).thenReturn(true);
        when(multiRoleUser.isManager()).thenReturn(true);
        when(multiRoleUser.isCustomer()).thenReturn(false);

        sessionManager.startSession(multiRoleUser);
        UserSession session = sessionManager.getCurrentSession();

        assertTrue(session.isAdmin());
        assertTrue(session.isManager());
        assertFalse(session.isCustomer());
    }

    @Test
    void shouldHandleUserWithNoRoles() {
        User noRoleUser = mock(User.class);
        when(noRoleUser.isAdmin()).thenReturn(false);
        when(noRoleUser.isManager()).thenReturn(false);
        when(noRoleUser.isCustomer()).thenReturn(false);

        sessionManager.startSession(noRoleUser);
        UserSession session = sessionManager.getCurrentSession();

        assertFalse(session.isAdmin());
        assertFalse(session.isManager());
        assertFalse(session.isCustomer());
    }

    @Test
    void userSessionRecordShouldWorkCorrectly() {
        UserSession session1 = new UserSession(mockUser);
        UserSession session2 = new UserSession(mockUser);
        UserSession session3 = new UserSession(mockAdminUser);

        assertEquals(session1, session2);
        assertNotEquals(session1, session3);
        assertEquals(mockUser, session1.user());
    }

    @Test
    void userSessionShouldHaveProperEqualsAndHashCode() {
        UserSession session1 = new UserSession(mockUser);
        UserSession session2 = new UserSession(mockUser);
        UserSession session3 = new UserSession(mockAdminUser);
        UserSession nullSession = new UserSession(null);

        assertEquals(session1, session2);
        assertEquals(session1, session1);
        assertEquals(session2, session1);
        assertNotEquals(session1, session3);
        assertNotEquals(session1, null);
        assertNotEquals(session1, "not a session");

        assertEquals(session1.hashCode(), session2.hashCode());

        assertEquals(nullSession, new UserSession(null));
    }

    @Test
    void userSessionToStringShouldWorkCorrectly() {
        UserSession session = new UserSession(mockUser);
        String toString = session.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("UserSession"));
        assertTrue(toString.contains("user"));
    }

    @Test
    void userSessionSetUserMethodShouldExistButNotChangeUser() {
        UserSession session = new UserSession(mockUser);
        User originalUser = session.user();

        assertDoesNotThrow(() -> session.setUser(mockAdminUser));

        assertSame(originalUser, session.user());
        assertEquals(mockUser, session.user());
    }

    @Test
    void shouldHandleRapidSessionSwitching() {
        for (int i = 0; i < 100; i++) {
            User tempUser = mock(User.class);
            sessionManager.startSession(tempUser);
            assertTrue(sessionManager.hasActiveSession());
            assertEquals(tempUser, sessionManager.getCurrentSession().user());
        }
    }

    @Test
    void shouldMaintainSessionConsistencyDuringConcurrentLikeOperations() {
        sessionManager.startSession(mockAdminUser);
        UserSession originalSession = sessionManager.getCurrentSession();

        assertTrue(sessionManager.hasActiveSession());
        assertSame(originalSession, sessionManager.getCurrentSession());
        assertTrue(sessionManager.getCurrentSession().isAdmin());

        sessionManager.endSession();
        assertFalse(sessionManager.hasActiveSession());
    }

    @Test
    void shouldHandleSessionStateChecksConsistently() {
        assertFalse(sessionManager.hasActiveSession());
        assertNull(sessionManager.getCurrentSession());

        sessionManager.startSession(mockManagerUser);
        assertTrue(sessionManager.hasActiveSession());
        assertNotNull(sessionManager.getCurrentSession());
        assertTrue(sessionManager.getCurrentSession().isManager());

        sessionManager.endSession();
        assertFalse(sessionManager.hasActiveSession());
        assertNull(sessionManager.getCurrentSession());
    }

    @Test
    void shouldHandleCompleteSessionWorkflow() {
        assertFalse(sessionManager.hasActiveSession());

        sessionManager.startSession(mockAdminUser);
        assertTrue(sessionManager.hasActiveSession());
        assertTrue(sessionManager.getCurrentSession().isAdmin());

        sessionManager.endSession();
        assertFalse(sessionManager.hasActiveSession());

        sessionManager.startSession(mockManagerUser);
        assertTrue(sessionManager.hasActiveSession());
        assertTrue(sessionManager.getCurrentSession().isManager());
        assertFalse(sessionManager.getCurrentSession().isAdmin());

        sessionManager.startSession(mockCustomerUser);
        assertTrue(sessionManager.hasActiveSession());
        assertTrue(sessionManager.getCurrentSession().isCustomer());
        assertFalse(sessionManager.getCurrentSession().isManager());

        sessionManager.endSession();
        assertFalse(sessionManager.hasActiveSession());
    }
}