package tourapp.view.tour_controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
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
import org.testfx.matcher.base.NodeMatchers;
import tourapp.model.location.Location;
import tourapp.model.tour.Tour;
import tourapp.model.user.User;
import tourapp.model.user.UserTour;
import tourapp.model.user.UserType;
import tourapp.service.tour_service.TourService;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTourService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.*;
import tourapp.view.auth_controller.LoginController;
import tourapp.view.user_controller.UserCabinetController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
class BookedToursControllerTest {

    @InjectMocks private BookedToursController controller;

    @Mock private TourService tourService;
    @Mock private UserTourService userTourService;
    @Mock private UserService userService;
    @Mock private SessionManager sessionManager;
    @Mock private ControllerFactory controllerFactory;
    @Mock private SessionManager.UserSession userSession;
    @Mock private User mockUser;
    @Mock private UserType mockUserType;
    @Mock private NavigationController navigationController;
    @Mock private DashboardController dashboardController;
    @Mock private AdminPanelController adminPanelController;
    @Mock private UserCabinetController userCabinetController;
    @Mock private LoginController loginController;
    @Mock private Logger logger;

    private Stage stage;
    private Tour testTour1;
    private Tour testTour2;
    private UserTour testUserTour1;
    private UserTour testUserTour2;
    private User testCustomer;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupMocks();
        controller = spy(new BookedToursController(stage, sessionManager, tourService, userTourService, userService, controllerFactory));
        BaseController.logger = logger;
        controller.show();
    }

    @BeforeEach
    void setUp() {
        setupTestData();
        reset(tourService, userTourService, userService, sessionManager, controllerFactory, navigationController,
                dashboardController, adminPanelController, userCabinetController, loginController);
        setupMocks();
    }

    private void setupMocks() {
        try {
            when(sessionManager.getCurrentSession()).thenReturn(userSession);
            when(sessionManager.hasActiveSession()).thenReturn(true);
            when(userSession.user()).thenReturn(mockUser);
            when(userSession.isAdmin()).thenReturn(false);
            when(userSession.isManager()).thenReturn(false);
            when(userSession.isCustomer()).thenReturn(true);

            when(mockUser.getId()).thenReturn(1);
            when(mockUser.getName()).thenReturn("Test Customer");
            when(mockUser.getEmail()).thenReturn("customer@test.com");
            when(mockUser.getUserType()).thenReturn(mockUserType);
            when(mockUser.isAdmin()).thenReturn(false);

            setupTestData();

            when(userTourService.findById1(1)).thenReturn(Arrays.asList(testUserTour1, testUserTour2));
            when(userTourService.findById2(anyInt())).thenReturn(Arrays.asList(testUserTour1));
            when(userTourService.countUsersByTourId(anyInt())).thenReturn(2);
            when(userService.getById(1)).thenReturn(testCustomer);
            when(tourService.getAll()).thenReturn(Arrays.asList(testTour1, testTour2));
            when(tourService.getById(1)).thenReturn(testTour1);
            when(tourService.getById(2)).thenReturn(testTour2);

            when(controllerFactory.createNavigationController()).thenReturn(navigationController);
            when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
            when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
            when(controllerFactory.createAdminPanelController()).thenReturn(adminPanelController);
            when(controllerFactory.createUserCabinetController()).thenReturn(userCabinetController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);

        } catch (SQLException e) {
            System.out.println("Setup failed: " + e.getMessage());
        }
    }

    private void setupTestData() {
        testTour1 = new Tour();
        testTour1.setId(1);
        testTour1.setDescription("Тур до Карпат");
        testTour1.setPrice(2500.0);
        testTour1.setStartDate(LocalDate.now().plusDays(10));
        testTour1.setEndDate(LocalDate.now().plusDays(17));
        testTour1.setActive(true);

        testTour2 = new Tour();
        testTour2.setId(2);
        testTour2.setDescription("Морський круїз");
        testTour2.setPrice(4500.0);
        testTour2.setStartDate(LocalDate.now().plusDays(20));
        testTour2.setEndDate(LocalDate.now().plusDays(30));
        testTour2.setActive(false);

        testUserTour1 = new UserTour();
        testUserTour1.setUserId(1);
        testUserTour1.setTourId(1);
        testUserTour1.setTour(testTour1);

        testUserTour2 = new UserTour();
        testUserTour2.setUserId(1);
        testUserTour2.setTourId(2);
        testUserTour2.setTour(testTour2);

        testCustomer = new User();
        testCustomer.setId(1);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("customer@test.com");

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

    private void closeOpenDialogs(FxRobot robot) {
        try {
            robot.press(KeyCode.ESCAPE);
            Thread.sleep(100);
        } catch (Exception ignored) {
        }
    }

    @Test
    void testControllerInitializationForCustomer(FxRobot robot) {
        waitForFxEvents();

        assertNotNull(stage.getScene());
        assertTrue(stage.isShowing());
        assertEquals("TourApp - Заброньовані тури", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ScrollPane cardScrollPane = robot.lookup("#cardScrollPane").queryAs(ScrollPane.class);
                FlowPane bookedTourCardContainer = robot.lookup("#bookedTourCardContainer").queryAs(FlowPane.class);
                Label noBookingsLabel = robot.lookup("#noBookingsLabel").queryAs(Label.class);

                assertNotNull(cardScrollPane);
                assertNotNull(bookedTourCardContainer);
                assertNotNull(noBookingsLabel);
                assertTrue(cardScrollPane.isVisible());
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
    void testControllerInitializationForAdmin(FxRobot robot) {
        when(userSession.isCustomer()).thenReturn(false);
        when(userSession.isAdmin()).thenReturn(true);

        waitForFxEvents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupViewBasedOnRole();
                VBox adminView = robot.lookup("#adminView").queryAs(VBox.class);
                TableView bookingTable = robot.lookup("#bookingTable").queryAs(TableView.class);

                assertNotNull(adminView);
                assertNotNull(bookingTable);
                assertTrue(adminView.isVisible());
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
    void testNavigationBarInitialization() {
        HBox mockNavBar = new HBox();
        when(navigationController.createNavigationBar(NavigationController.PAGE_BOOKED_TOURS))
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
        verify(navigationController).createNavigationBar(NavigationController.PAGE_BOOKED_TOURS);
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

        ArgumentCaptor<Runnable> profileCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(navigationController).setOnProfile(profileCaptor.capture());
        profileCaptor.getValue().run();
        verify(controllerFactory).createUserCabinetController();
        verify(userCabinetController).show();
    }

    @Test
    void testAdminPanelAccessForAdmin() {
        when(userSession.isCustomer()).thenReturn(false);
        when(userSession.isAdmin()).thenReturn(true);

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

        ArgumentCaptor<Runnable> adminCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(navigationController).setOnAdminPanel(adminCaptor.capture());
        adminCaptor.getValue().run();
        verify(controllerFactory).createAdminPanelController();
        verify(adminPanelController).show();
    }

    @Test
    void testLoadCustomerBookedTours() throws SQLException {
        doNothing().when(controller).displayUserToursAsCards(any());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadCustomerBookedTours();
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
    void testLoadCustomerBookedToursEmpty() throws SQLException {
        when(userTourService.findById1(1)).thenReturn(Collections.emptyList());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadCustomerBookedTours();
                Label noBookingsLabel = controller.noBookingsLabel;
                assertTrue(noBookingsLabel.isVisible());
                assertEquals("У вас поки немає заброньованих турів", noBookingsLabel.getText());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userTourService).findById1(1);
    }

    @Test
    void testDisplayUserToursAsCards(FxRobot robot) {
        waitForFxEvents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.displayUserToursAsCards(Arrays.asList(testUserTour1, testUserTour2));
                FlowPane container = controller.bookedTourCardContainer;
                assertEquals(2, container.getChildren().size());
                assertFalse(controller.noBookingsLabel.isVisible());
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
    void testCreateTourCard() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                VBox card = controller.createTourCard(testTour1, testUserTour1);
                assertNotNull(card);
                assertEquals(280.0, card.getPrefWidth());
                assertTrue(card.getStyleClass().contains("user-dashboard-card"));
                assertEquals(6, card.getChildren().size());
            } catch (IOException e) {
                throw new RuntimeException(e);
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
    void testLoadBookingStatisticsForAdmin() throws SQLException {
        when(userSession.isCustomer()).thenReturn(false);
        when(userSession.isAdmin()).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupViewBasedOnRole();
                controller.loadBookingStatistics();
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(tourService).getAll();
        verify(userTourService, times(2)).countUsersByTourId(anyInt());
    }

    @Test
    void testBookingStatisticClass() {
        BookedToursController.BookingStatistic statistic =
                new BookedToursController.BookingStatistic(testTour1, 5);

        assertEquals(testTour1, statistic.getTour());
        assertEquals(5, statistic.getBookingCount());
        assertEquals(1, statistic.getTourId());
        assertEquals("Тур до Карпат", statistic.getTourDescription());
        assertEquals(2500.0, statistic.getTourPrice());
        assertEquals(testTour1.getStartDate(), statistic.getTourStartDate());
        assertEquals(testTour1.getEndDate(), statistic.getTourEndDate());
        assertTrue(statistic.getTourActive());
        assertEquals(5, statistic.bookingCount());
    }

    @Test
    void testShowTourDetails(FxRobot robot) throws SQLException {
        doNothing().when(controller).showError(anyString());

        Platform.runLater(() -> {
            controller.showTourDetails(testTour1);
        });
        waitForFxEvents();

        closeOpenDialogs(robot);
    }

    @Test
    void testShowTourBookingDetails() throws SQLException {
        BookedToursController.BookingStatistic statistic =
                new BookedToursController.BookingStatistic(testTour1, 2);

        Platform.runLater(() -> {
            controller.showTourBookingDetails(statistic);
        });
        waitForFxEvents();

        verify(userTourService).findById2(1);
        verify(userService).getById(1);
    }

    @Test
    void testCancelBooking(FxRobot robot) throws SQLException, InterruptedException {
        when(userTourService.deleteLink(1, 1)).thenReturn(true);
        doNothing().when(controller).showInfo(anyString());

        Platform.runLater(() -> {
            controller.cancelBooking(testUserTour1);
        });
        Thread.sleep(200);

        try {
            robot.clickOn("Yes");
        } catch (Exception ignored) {
        }
        Thread.sleep(200);

        verify(userTourService).deleteLink(1, 1);
    }

    @Test
    void testLoadBookedToursWithSQLException() throws SQLException {
        when(userTourService.findById1(1)).thenThrow(new SQLException("Database error"));
        doNothing().when(controller).showError(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadCustomerBookedTours();
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(controller).showError(contains("Помилка завантаження заброньованих турів"));
        verify(userTourService).findById1(1);
    }

    @Test
    void testLoadBookingStatisticsWithSQLException() throws SQLException {
        when(userSession.isCustomer()).thenReturn(false);
        when(userSession.isAdmin()).thenReturn(true);
        when(tourService.getAll()).thenThrow(new SQLException("Database error"));
        doNothing().when(controller).showError(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadBookingStatistics();
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(controller).showError(contains("Помилка завантаження статистики бронювань"));
        verify(tourService).getAll();
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
    void testSetupViewBasedOnRoleForCustomer(FxRobot robot) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupViewBasedOnRole();
                assertTrue(controller.cardScrollPane.isVisible());
                assertTrue(controller.cardScrollPane.isManaged());
                assertFalse(controller.adminView.isVisible());
                assertFalse(controller.adminView.isManaged());
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
    void testSetupViewBasedOnRoleForAdmin(FxRobot robot) {
        when(userSession.isCustomer()).thenReturn(false);
        when(userSession.isAdmin()).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupViewBasedOnRole();
                assertFalse(controller.cardScrollPane.isVisible());
                assertFalse(controller.cardScrollPane.isManaged());
                assertTrue(controller.adminView.isVisible());
                assertTrue(controller.adminView.isManaged());
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
}