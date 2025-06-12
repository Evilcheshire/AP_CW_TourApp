package tourapp.view;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import tourapp.model.user.User;
import tourapp.util.SessionManager;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
class NavigationControllerTest {

    @Mock private SessionManager mockSessionManager;
    @Mock private SessionManager.UserSession mockUserSession;
    @Mock private User mockUser;

    private NavigationController navigationController;
    private Stage testStage;

    @Start
    public void start(Stage stage) {
        this.testStage = stage;
    }

    @BeforeEach
    void setUp() throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.headless", "true");

        navigationController = new NavigationController(testStage, mockSessionManager);
        initializeRealComponents();
    }

    private void initializeRealComponents() throws Exception {
        Platform.runLater(() -> {
            try {
                Label userInfoLabel = new Label();
                Button btnProfile = new Button("Profile");
                Button btnSearch = new Button("Search");
                Button btnBooked = new Button("Booked");
                Button btnAdminPanel = new Button("Admin Panel");
                Button btnLogout = new Button("Logout");
                Button btnExitApp = new Button("Exit");
                HBox navigationBar = new HBox();

                injectField("userInfoLabel", userInfoLabel);
                injectField("btnProfile", btnProfile);
                injectField("btnSearch", btnSearch);
                injectField("btnBooked", btnBooked);
                injectField("btnAdminPanel", btnAdminPanel);
                injectField("btnLogout", btnLogout);
                injectField("btnExitApp", btnExitApp);
                injectField("navigationBar", navigationBar);
            } catch (Exception ignored) {
            }
        });

        waitForFxEvents();
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = NavigationController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(navigationController, value);
    }

    private void waitForFxEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetupNavigationWithAllCurrentPages() throws Exception {
        when(mockSessionManager.hasActiveSession()).thenReturn(true);
        when(mockSessionManager.getCurrentSession()).thenReturn(mockUserSession);
        when(mockUserSession.user()).thenReturn(mockUser);
        when(mockUserSession.isAdmin()).thenReturn(true);
        when(mockUserSession.isManager()).thenReturn(false);

        String[] pages = {
                NavigationController.PAGE_DASHBOARD,
                NavigationController.PAGE_PROFILE,
                NavigationController.PAGE_BOOKED_TOURS,
                NavigationController.PAGE_ADMIN_PANEL
        };

        for (String page : pages) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean testPassed = new AtomicBoolean(false);

            Platform.runLater(() -> {
                try {
                    navigationController.setCurrentPage(page);
                    navigationController.setupNavigation();

                    Button btnSearch = (Button) HelperMethods.getFieldValue(navigationController,"btnSearch");
                    Button btnProfile = (Button) HelperMethods.getFieldValue(navigationController,"btnProfile");
                    Button btnBooked = (Button) HelperMethods.getFieldValue(navigationController,"btnBooked");
                    Button btnAdminPanel = (Button) HelperMethods.getFieldValue(navigationController,"btnAdminPanel");

                    switch (page) {
                        case NavigationController.PAGE_DASHBOARD:
                            assertFalse(btnSearch.isVisible());
                            break;
                        case NavigationController.PAGE_PROFILE:
                            assertFalse(btnProfile.isVisible());
                            break;
                        case NavigationController.PAGE_BOOKED_TOURS:
                            assertFalse(btnBooked.isVisible());
                            break;
                        case NavigationController.PAGE_ADMIN_PANEL:
                            assertFalse(btnAdminPanel.isVisible());
                            break;
                    }

                    testPassed.set(true);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            assertTrue(testPassed.get(), "Test failed for page: " + page);
        }
    }

    @Test
    void testButtonTextForManagerInBookedButton() throws Exception {
        when(mockSessionManager.hasActiveSession()).thenReturn(true);
        when(mockSessionManager.getCurrentSession()).thenReturn(mockUserSession);
        when(mockUserSession.user()).thenReturn(mockUser);
        when(mockUserSession.isAdmin()).thenReturn(false);
        when(mockUserSession.isManager()).thenReturn(true);
        when(mockUser.getName()).thenReturn("Manager User");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean testPassed = new AtomicBoolean(false);

        Platform.runLater(() -> {
            try {
                navigationController.setupNavigation();

                Button btnBooked = (Button) HelperMethods.getFieldValue(navigationController,"btnBooked");
                assertEquals("Заброньовані тури", btnBooked.getText());

                testPassed.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        assertTrue(testPassed.get());
    }

    @Test
    void testButtonTextForRegularUserInBookedButton() throws Exception {
        when(mockSessionManager.hasActiveSession()).thenReturn(true);
        when(mockSessionManager.getCurrentSession()).thenReturn(mockUserSession);
        when(mockUserSession.user()).thenReturn(mockUser);
        when(mockUserSession.isAdmin()).thenReturn(false);
        when(mockUserSession.isManager()).thenReturn(false);
        when(mockUser.getName()).thenReturn("Regular User");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean testPassed = new AtomicBoolean(false);

        Platform.runLater(() -> {
            try {
                navigationController.setupNavigation();

                Button btnBooked = (Button) HelperMethods.getFieldValue(navigationController,"btnBooked");
                assertEquals("Booked", btnBooked.getText());

                testPassed.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        assertTrue(testPassed.get());
    }

    @Test
    void testHandleAdminPanelWithAdminUser() {
        when(mockSessionManager.getCurrentSession()).thenReturn(mockUserSession);
        when(mockUserSession.isAdmin()).thenReturn(true);
        lenient().when(mockUserSession.isManager()).thenReturn(false);

        Runnable mockAdminCallback = mock(Runnable.class);
        navigationController.setOnAdminPanel(mockAdminCallback);

        navigationController.handleAdminPanel();

        verify(mockAdminCallback).run();
    }

    @Test
    void testSetCurrentPageUpdatesNavigation() throws Exception {
        when(mockSessionManager.hasActiveSession()).thenReturn(true);
        lenient().when(mockSessionManager.getCurrentSession()).thenReturn(mockUserSession);
        when(mockUserSession.user()).thenReturn(mockUser);
        when(mockUserSession.isAdmin()).thenReturn(false);
        when(mockUserSession.isManager()).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean testPassed = new AtomicBoolean(false);

        Platform.runLater(() -> {
            try {
                navigationController.setupNavigation();
                Button btnProfile = (Button) HelperMethods.getFieldValue(navigationController,"btnProfile");
                assertTrue(btnProfile.isVisible());

                navigationController.setCurrentPage(NavigationController.PAGE_PROFILE);

                assertFalse(btnProfile.isVisible());
                assertFalse(btnProfile.isManaged());

                testPassed.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        assertTrue(testPassed.get());
    }

    @Test
    void testCreateNavigationBarHandlesIOException() {
        NavigationController controller = new NavigationController(testStage, mockSessionManager);

        HBox result = controller.createNavigationBar("INVALID_PAGE");

        assertNotNull(result);
    }

    @Test
    void testCallbackSettersAndGetters() {
        Runnable profileCallback = mock(Runnable.class);
        Runnable searchCallback = mock(Runnable.class);
        Runnable bookedCallback = mock(Runnable.class);
        Runnable adminCallback = mock(Runnable.class);
        Runnable logoutCallback = mock(Runnable.class);
        Runnable exitCallback = mock(Runnable.class);

        navigationController.setOnProfile(profileCallback);
        navigationController.setOnSearch(searchCallback);
        navigationController.setOnBooked(bookedCallback);
        navigationController.setOnAdminPanel(adminCallback);
        navigationController.setOnLogout(logoutCallback);
        navigationController.setOnExit(exitCallback);

        navigationController.handleProfile();
        navigationController.handleSearch();
        navigationController.handleBooked();

        when(mockSessionManager.getCurrentSession()).thenReturn(mockUserSession);
        when(mockUserSession.isAdmin()).thenReturn(true);
        lenient().when(mockUserSession.isManager()).thenReturn(false);

        navigationController.handleAdminPanel();
        navigationController.handleLogout();
        navigationController.handleExitApp();

        verify(profileCallback).run();
        verify(searchCallback).run();
        verify(bookedCallback).run();
        verify(adminCallback).run();
        verify(logoutCallback).run();
        verify(exitCallback).run();
    }

    @Test
    void testMultipleSetCurrentPageCalls() throws Exception {
        when(mockSessionManager.hasActiveSession()).thenReturn(true);
        when(mockSessionManager.getCurrentSession()).thenReturn(mockUserSession);
        when(mockUserSession.user()).thenReturn(mockUser);
        when(mockUserSession.isAdmin()).thenReturn(true);
        when(mockUserSession.isManager()).thenReturn(false);

        assertDoesNotThrow(() -> {
            navigationController.setCurrentPage(NavigationController.PAGE_DASHBOARD);
            navigationController.setCurrentPage(NavigationController.PAGE_PROFILE);
            navigationController.setCurrentPage(NavigationController.PAGE_ADMIN_PANEL);
            navigationController.setCurrentPage(NavigationController.PAGE_BOOKED_TOURS);
        });
    }
}