package tourapp.view;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.slf4j.Logger;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.auth_controller.LoginController;
import tourapp.view.location_controller.LocationController;
import tourapp.view.meal_controller.MealController;
import tourapp.view.tour_controller.BookedToursController;
import tourapp.view.tour_controller.DashboardController;
import tourapp.view.transport_controller.TransportController;
import tourapp.view.user_controller.UserCabinetController;
import tourapp.view.user_controller.UserController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class AdminPanelControllerTest {

    @InjectMocks private AdminPanelController controller;
    @Mock private SessionManager sessionManager;
    @Mock private ControllerFactory controllerFactory;
    @Mock private SessionManager.UserSession userSession;
    @Mock private User mockUser;
    @Mock private UserType mockUserType;
    @Mock private NavigationController navigationController;
    @Mock private LoginController loginController;
    @Mock private DashboardController dashboardController;
    @Mock private BookedToursController bookedToursController;
    @Mock private UserCabinetController userCabinetController;
    @Mock private LocationController locationController;
    @Mock private MealController mealController;
    @Mock private TransportController transportController;
    @Mock private UserController userController;
    @Mock private Logger logger;

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupMocks();
        controller = spy(new AdminPanelController(stage, sessionManager, controllerFactory));
        BaseController.logger = logger;
        controller.show();
    }

    @BeforeEach
    void setUp() {
        reset(sessionManager, controllerFactory, navigationController, loginController,
                dashboardController, bookedToursController, userCabinetController,
                locationController, mealController, transportController, userController);
        setupMocks();
    }

    private void setupMocks() {
        when(sessionManager.getCurrentSession()).thenReturn(userSession);
        when(sessionManager.hasActiveSession()).thenReturn(true);
        when(userSession.user()).thenReturn(mockUser);
        when(userSession.isAdmin()).thenReturn(true);
        when(userSession.isManager()).thenReturn(false);
        when(userSession.isCustomer()).thenReturn(false);

        when(mockUser.getName()).thenReturn("Test Admin");
        when(mockUser.getEmail()).thenReturn("admin@test.com");
        when(mockUser.getUserType()).thenReturn(mockUserType);
        when(mockUser.isAdmin()).thenReturn(true);

        when(controllerFactory.createNavigationController()).thenReturn(navigationController);
        when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
        when(controllerFactory.createLoginController()).thenReturn(loginController);
        when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
        when(controllerFactory.createBookedToursController()).thenReturn(bookedToursController);
        when(controllerFactory.createUserCabinetController()).thenReturn(userCabinetController);
        when(controllerFactory.createLocationController()).thenReturn(locationController);
        when(controllerFactory.createMealController()).thenReturn(mealController);
        when(controllerFactory.createTransportController()).thenReturn(transportController);
        when(controllerFactory.createUserController()).thenReturn(userController);
    }

    private void waitForFxEvents() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void setupUIComponents() throws Exception {
        HelperMethods.setFieldValue(controller, "mainLayout", new BorderPane());
        HelperMethods.setFieldValue(controller, "welcomeLabel", new Label());
        HelperMethods.setFieldValue(controller, "btnUsers", new Button());
        HelperMethods.setFieldValue(controller, "usersCard", new VBox());
        HelperMethods.setFieldValue(controller, "btnTours", new Button());
        HelperMethods.setFieldValue(controller, "btnLocations", new Button());
        HelperMethods.setFieldValue(controller, "btnMeals", new Button());
        HelperMethods.setFieldValue(controller, "btnTransport", new Button());
        HelperMethods.setFieldValue(controller, "adminNavigation", new HBox());
        HelperMethods.setFieldValue(controller, "contentArea", new VBox());
    }

    @Test
    void testControllerInitialization(FxRobot robot) {
        waitForFxEvents();

        assertNotNull(stage.getScene());
        assertTrue(stage.isShowing());
        assertEquals("TourApp - Адміністративна панель", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Button btnTours = robot.lookup("#btnTours").queryAs(Button.class);
                Button btnLocations = robot.lookup("#btnLocations").queryAs(Button.class);
                Button btnMeals = robot.lookup("#btnMeals").queryAs(Button.class);
                Button btnTransport = robot.lookup("#btnTransport").queryAs(Button.class);
                Label welcomeLabel = robot.lookup("#welcomeLabel").queryAs(Label.class);

                assertNotNull(btnTours);
                assertNotNull(btnLocations);
                assertNotNull(btnMeals);
                assertNotNull(btnTransport);
                assertNotNull(welcomeLabel);
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testNavigationBarInitialization(FxRobot robot) {
        HBox mockNavBar = new HBox();
        when(navigationController.createNavigationBar(NavigationController.PAGE_ADMIN_PANEL))
                .thenReturn(mockNavBar);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initializeNavigationBar();
                assertEquals(mockNavBar, controller.mainLayout.getTop());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(controllerFactory).createNavigationController();
        verify(navigationController).createNavigationBar(NavigationController.PAGE_ADMIN_PANEL);
        verify(navigationController).setOnLogout(any(Runnable.class));
        verify(navigationController).setOnExit(any(Runnable.class));
        verify(navigationController).setOnSearch(any(Runnable.class));
        verify(navigationController).setOnBooked(any(Runnable.class));
        verify(navigationController).setOnAdminPanel(any(Runnable.class));
        verify(navigationController).setOnProfile(any(Runnable.class));
    }

    @Test
    void testNavigationBarInitializationException() {
        when(controllerFactory.createNavigationController())
                .thenThrow(new RuntimeException("Failed to create navigation controller"));
        doNothing().when(controller).showError(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initializeNavigationBar();
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(controllerFactory).createNavigationController();
        verify(controller).showError(contains("Помилка ініціалізації навігації"));
    }

    @Test
    void testNavigationCallbacks() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initializeNavigationBar();
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ArgumentCaptor<Runnable> logoutCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(navigationController).setOnLogout(logoutCaptor.capture());
        logoutCaptor.getValue().run();
        verify(sessionManager).endSession();
        verify(controllerFactory).createLoginController();
        verify(loginController).show();

        ArgumentCaptor<Runnable> searchCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(navigationController).setOnSearch(searchCaptor.capture());
        searchCaptor.getValue().run();
        verify(controllerFactory).createDashboardController();
        verify(dashboardController).show();

        ArgumentCaptor<Runnable> bookedCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(navigationController).setOnBooked(bookedCaptor.capture());
        bookedCaptor.getValue().run();
        verify(controllerFactory).createBookedToursController();
        verify(bookedToursController).show();

        ArgumentCaptor<Runnable> profileCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(navigationController).setOnProfile(profileCaptor.capture());
        profileCaptor.getValue().run();
        verify(controllerFactory).createUserCabinetController();
        verify(userCabinetController).show();

        ArgumentCaptor<Runnable> adminCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(navigationController).setOnAdminPanel(adminCaptor.capture());
        adminCaptor.getValue().run();
    }

    @Test
    void testAccessControlsForAdmin() throws Exception {
        when(userSession.isAdmin()).thenReturn(true);
        setupUIComponents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();

                Button btnUsers = (Button) HelperMethods.getFieldValue(controller, "btnUsers");
                VBox usersCard = (VBox) HelperMethods.getFieldValue(controller, "usersCard");

                assertTrue(btnUsers.isVisible());
                assertTrue(btnUsers.isManaged());
                assertTrue(usersCard.isVisible());
                assertTrue(usersCard.isManaged());
            } catch (Exception e) {
                fail("Exception during access control setup: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testAccessControlsForManager() throws Exception {
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(true);
        setupUIComponents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();

                Button btnUsers = (Button) HelperMethods.getFieldValue(controller, "btnUsers");
                VBox usersCard = (VBox) HelperMethods.getFieldValue(controller, "usersCard");

                assertFalse(btnUsers.isVisible());
                assertFalse(btnUsers.isManaged());
                assertFalse(usersCard.isVisible());
                assertFalse(usersCard.isManaged());
            } catch (Exception e) {
                fail("Exception during access control setup: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testWelcomeMessageForAdmin() throws Exception {
        when(userSession.isAdmin()).thenReturn(true);
        setupUIComponents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupWelcomeMessage();

                Label welcomeLabel = (Label) HelperMethods.getFieldValue(controller, "welcomeLabel");
                String expectedMessage = "Вітаємо в адміністративній панелі, Test Admin (Адміністратор)!";
                assertEquals(expectedMessage, welcomeLabel.getText());
            } catch (Exception e) {
                fail("Exception during welcome message setup: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testWelcomeMessageForManager() throws Exception {
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(true);
        setupUIComponents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupWelcomeMessage();

                Label welcomeLabel = (Label) HelperMethods.getFieldValue(controller, "welcomeLabel");
                String expectedMessage = "Вітаємо в адміністративній панелі, Test Admin (Менеджер)!";
                assertEquals(expectedMessage, welcomeLabel.getText());
            } catch (Exception e) {
                fail("Exception during welcome message setup: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testHandleToursManagement(FxRobot robot) throws InterruptedException {
        robot.clickOn("#btnTours");
        Thread.sleep(200);

        verify(controllerFactory).createDashboardController();
        verify(dashboardController).show();
    }

    @Test
    void testHandleLocationsManagement(FxRobot robot) throws InterruptedException {
        robot.clickOn("#btnLocations");
        Thread.sleep(200);

        verify(controllerFactory).createLocationController();
        verify(locationController).show();
    }

    @Test
    void testHandleMealsManagement(FxRobot robot) throws InterruptedException {
        robot.clickOn("#btnMeals");
        Thread.sleep(200);

        verify(controllerFactory).createMealController();
        verify(mealController).show();
    }

    @Test
    void testHandleTransportManagement(FxRobot robot) throws InterruptedException {
        robot.clickOn("#btnTransport");
        Thread.sleep(200);

        verify(controllerFactory).createTransportController();
        verify(transportController).show();
    }

    @Test
    void testHandleUsersManagementAsAdmin(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        robot.clickOn("#btnUsers");
        Thread.sleep(200);

        verify(controllerFactory).createUserController();
        verify(userController).show();
        verify(controller, never()).showInfo(anyString());
    }

    @Test
    void testHandleUsersManagementAsNonAdmin() {
        when(userSession.isAdmin()).thenReturn(false);
        doNothing().when(controller).showInfo(anyString());

        controller.handleUsersManagement();

        verify(controller).showInfo("Тільки адміністратори можуть управляти користувачами");
        verify(controllerFactory, never()).createUserController();
        verify(userController, never()).show();
    }

    @Test
    void testFullInitializationFlow() throws Exception {
        when(userSession.isAdmin()).thenReturn(true);
        setupUIComponents();
        doNothing().when(controller).showError(anyString());
        doNothing().when(controller).showInfo(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initialize();

                Button btnUsers = (Button) HelperMethods.getFieldValue(controller, "btnUsers");
                Label welcomeLabel = (Label) HelperMethods.getFieldValue(controller, "welcomeLabel");

                assertTrue(btnUsers.isVisible());
                assertTrue(welcomeLabel.getText().contains("Test Admin"));
                assertTrue(welcomeLabel.getText().contains("Адміністратор"));
            } catch (Exception e) {
                fail("Exception during initialization: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(controllerFactory).createNavigationController();
        verify(navigationController).createNavigationBar(NavigationController.PAGE_ADMIN_PANEL);
    }
}