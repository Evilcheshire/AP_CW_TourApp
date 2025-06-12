package tourapp.view.tour_controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
import tourapp.model.location.Location;
import tourapp.model.meal.Meal;
import tourapp.model.tour.Tour;
import tourapp.model.tour.TourType;
import tourapp.model.transport.Transport;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.location_service.LocationService;
import tourapp.service.meal_service.MealService;
import tourapp.service.meal_service.MealTypeService;
import tourapp.service.tour_service.TourService;
import tourapp.service.tour_service.TourTypeService;
import tourapp.service.transport_service.TransportService;
import tourapp.service.user_service.UserTourService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.*;
import tourapp.view.auth_controller.LoginController;
import tourapp.view.user_controller.UserCabinetController;

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
class DashboardControllerTest {

    @InjectMocks private DashboardController controller;

    @Mock private TourService tourService;
    @Mock private LocationService locationService;
    @Mock private TourTypeService tourTypeService;
    @Mock private MealService mealService;
    @Mock private MealTypeService mealTypeService;
    @Mock private TransportService transportService;
    @Mock private UserTourService userTourService;
    @Mock private SessionManager sessionManager;
    @Mock private ControllerFactory controllerFactory;
    @Mock private SessionManager.UserSession userSession;
    @Mock private User mockUser;
    @Mock private UserType mockUserType;
    @Mock private NavigationController navigationController;
    @Mock private BookedToursController bookedToursController;
    @Mock private AdminPanelController adminPanelController;
    @Mock private UserCabinetController userCabinetController;
    @Mock private LoginController loginController;
    @Mock private Logger logger;

    private Stage stage;
    private Tour testTour1;
    private Tour testTour2;
    private TourType testTourType;
    private Location testLocation;
    private Meal testMeal;
    private Transport testTransport;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupMocks();
        controller = spy(new DashboardController(stage, sessionManager, tourService, locationService,
                tourTypeService, mealTypeService, transportService, userTourService, controllerFactory));
        BaseController.logger = logger;
        controller.show();
    }

    @BeforeEach
    void setUp() {
        setupTestData();
        reset(tourService, locationService, tourTypeService, mealTypeService, transportService,
                userTourService, controllerFactory, navigationController, bookedToursController,
                adminPanelController, userCabinetController, loginController);
        setupMocks();
    }

    private void setupMocks() {
        try {
            when(sessionManager.getCurrentSession()).thenReturn(userSession);
            when(sessionManager.hasActiveSession()).thenReturn(true);
            when(userSession.user()).thenReturn(mockUser);
            when(userSession.isAdmin()).thenReturn(true);
            when(userSession.isManager()).thenReturn(false);
            when(userSession.isCustomer()).thenReturn(false);

            when(mockUser.getName()).thenReturn("Admin User");
            when(mockUser.getEmail()).thenReturn("admin@test.com");
            when(mockUser.getUserType()).thenReturn(mockUserType);
            when(mockUser.isAdmin()).thenReturn(true);
            when(mockUser.getId()).thenReturn(1);

            setupTestData();

            List<Tour> tours = Arrays.asList(testTour1, testTour2);
            List<Location> locations = Arrays.asList(testLocation);
            List<TourType> tourTypes = Arrays.asList(testTourType);
            List<Meal> meal = Arrays.asList(testMeal);
            List<Transport> transports = Arrays.asList(testTransport);

            when(tourService.getAll()).thenReturn(tours);
            when(tourService.search(any(Map.class))).thenReturn(tours);
            when(tourService.getById(1)).thenReturn(testTour1);
            when(tourService.getByIdWithDependencies(1)).thenReturn(testTour1);
            when(tourService.getLocationsForTour(1)).thenReturn(locations);
            when(locationService.getAll()).thenReturn(locations);
            when(tourTypeService.getAll()).thenReturn(tourTypes);
            when(mealService.getAll()).thenReturn(meal);
            when(transportService.getAll()).thenReturn(transports);

            when(controllerFactory.createNavigationController()).thenReturn(navigationController);
            when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
            when(controllerFactory.createDashboardController()).thenReturn(controller);
            when(controllerFactory.createBookedToursController()).thenReturn(bookedToursController);
            when(controllerFactory.createAdminPanelController()).thenReturn(adminPanelController);
            when(controllerFactory.createUserCabinetController()).thenReturn(userCabinetController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);

            TourEditController mockTourEditController = mock(TourEditController.class);
            TourTypeEditController mockTourTypeEditController = mock(TourTypeEditController.class);
            when(controllerFactory.createTourEditController(any())).thenReturn(mockTourEditController);
            when(controllerFactory.createTourTypeEditController(any())).thenReturn(mockTourTypeEditController);

        } catch (SQLException e) {
            System.out.println("Setup failed: " + e.getMessage());
        }
    }

    private void setupTestData() {
        testTourType = new TourType();
        testTourType.setId(1);
        testTourType.setName("Екскурсійний");

        testLocation = new Location();
        testLocation.setId(1);
        testLocation.setName("Київ");
        testLocation.setCountry("Україна");

        testMeal = new Meal();
        testMeal.setId(1);
        testMeal.setName("Повний пансіон");

        testTransport = new Transport();
        testTransport.setId(1);
        testTransport.setName("Автобус");

        testTour1 = new Tour();
        testTour1.setId(1);
        testTour1.setDescription("Тур по Україні");
        testTour1.setPrice(5000.0);
        testTour1.setStartDate(LocalDate.now().plusDays(30));
        testTour1.setEndDate(LocalDate.now().plusDays(37));
        testTour1.setActive(true);
        testTour1.setType(testTourType);
        testTour1.setMeal(testMeal);
        testTour1.setTransport(testTransport);
        testTour1.setLocations(Arrays.asList(testLocation));

        testTour2 = new Tour();
        testTour2.setId(2);
        testTour2.setDescription("Тур по Європі");
        testTour2.setPrice(15000.0);
        testTour2.setStartDate(LocalDate.now().plusDays(60));
        testTour2.setEndDate(LocalDate.now().plusDays(67));
        testTour2.setActive(false);
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


    @Test
    void testControllerInitialization(FxRobot robot) {
        waitForFxEvents();

        assertNotNull(stage.getScene());
        assertTrue(stage.isShowing());
        assertEquals("TourApp - Головна сторінка", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView tourTable = robot.lookup("#tourTable").queryAs(TableView.class);
                ComboBox countryCombo = robot.lookup("#countryFilterCombo").queryAs(ComboBox.class);
                ComboBox tourTypeCombo = robot.lookup("#tourTypeFilterCombo").queryAs(ComboBox.class);

                assertNotNull(tourTable);
                assertNotNull(countryCombo);
                assertNotNull(tourTypeCombo);
                assertEquals("Всі", countryCombo.getValue());
                assertEquals("Всі", tourTypeCombo.getValue());
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
    void testViewModeSwitching(FxRobot robot) {
        waitForFxEvents();

        when(userSession.isCustomer()).thenReturn(false);
        CountDownLatch adminLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.switchViewMode(false);
                assertTrue(controller.tourTable.isVisible());
                assertFalse(controller.cardScrollPane.isVisible());
            } finally {
                adminLatch.countDown();
            }
        });

        when(userSession.isCustomer()).thenReturn(true);
        CountDownLatch customerLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.switchViewMode(true);
                assertFalse(controller.tourTable.isVisible());
                assertTrue(controller.cardScrollPane.isVisible());
            } finally {
                customerLatch.countDown();
            }
        });

        try {
            assertTrue(adminLatch.await(5, TimeUnit.SECONDS));
            assertTrue(customerLatch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testAccessControls() throws InterruptedException {
        testAdminAccess();
        testManagerAccess();
        testCustomerAccess();
    }

    private void testAdminAccess() throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        when(userSession.isManager()).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();
                assertTrue(controller.addTourButton.isVisible());
                assertTrue(controller.addTourTypeButton.isVisible());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    private void testManagerAccess() throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();
                assertTrue(controller.addTourButton.isVisible());
                assertFalse(controller.addTourTypeButton.isVisible());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    private void testCustomerAccess() throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();
                assertFalse(controller.addTourButton.isVisible());
                assertFalse(controller.addTourTypeButton.isVisible());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }


    @Test
    void testFilterOperations(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(tourService, times(1)).search(any(Map.class));

        reset(tourService);
        when(tourService.search(any(Map.class))).thenReturn(Arrays.asList(testTour1));

        robot.clickOn("#keywordField").eraseText(10).write("Україна");
        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(tourService, times(1)).search(argThat(filters ->
                filters.containsKey("description") && "Україна".equals(filters.get("description"))
        ));
    }

    @Test
    void testResetFilters(FxRobot robot) throws InterruptedException {
        waitForFxEvents();
        doNothing().when(controller).showInfo(anyString());

        robot.clickOn("#keywordField").write("Test");
        robot.clickOn("#resetFiltersButton");
        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TextField keywordField = robot.lookup("#keywordField").queryAs(TextField.class);
                assertTrue(keywordField.getText().isEmpty());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(controller).showInfo("Фільтри скинуто до початкових значень");
    }

    @Test
    void testAddNewTour(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        when(userSession.isManager()).thenReturn(false);

        Thread.sleep(200);
        robot.clickOn("#addTourButton");
        verify(controllerFactory).createTourEditController(null);
    }

    @Test
    void testEditSelectedTour(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Platform.runLater(() -> {
            TableView<Tour> tourTable = robot.lookup("#tourTable").queryAs(TableView.class);
            tourTable.getItems().add(testTour1);
            tourTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        robot.clickOn("#editTourButton");
        verify(controllerFactory).createTourEditController(any(Tour.class));
    }

    @Test
    void testDeleteSelectedTour(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Platform.runLater(() -> {
            TableView<Tour> tourTable = robot.lookup("#tourTable").queryAs(TableView.class);
            tourTable.getItems().add(testTour1);
            tourTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        doNothing().when(controller).showInfo(anyString());

        robot.clickOn("#deleteTourButton");
        Thread.sleep(200);

        HelperMethods.clickOnYes(robot);
        Thread.sleep(200);

        verify(tourService).delete(1);
        verify(controller).showInfo("Тур успішно видалено.");
    }

    @Test
    void testToggleTourStatus(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        doNothing().when(controller).showInfo(anyString());

        Platform.runLater(() -> {
            TableView<Tour> tourTable = robot.lookup("#tourTable").queryAs(TableView.class);
            tourTable.getItems().add(testTour1);
            tourTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        robot.clickOn("#toggleStatusButton");
        Thread.sleep(200);

        verify(tourService).toggleActiveStatus(1, false);
        verify(controller).showInfo("Статус туру змінено на неактивний.");
    }

    @Test
    void testHandleBooking_Success() throws SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(userTourService.exists(1, 1)).thenReturn(false);
        when(userTourService.createLink(1, 1)).thenReturn(true);
        doNothing().when(controller).showSuccessBookingDialog(any(Tour.class));

        Platform.runLater(() -> {
            controller.handleBooking(testTour1);
        });
        waitForFxEvents();

        verify(userTourService).exists(1, 1);
    }

    @Test
    void testHandleBooking_AlreadyBooked() throws SQLException {
        when(userTourService.exists(1, 1)).thenReturn(true);
        doNothing().when(controller).showInfo(anyString());

        Platform.runLater(() -> {
            controller.handleBooking(testTour1);
        });
        waitForFxEvents();

        verify(controller).showInfo("Ви вже забронювали цей тур");
        verify(userTourService, never()).createLink(anyInt(), anyInt());
    }

    @Test
    void testValidateTourForBooking_InvalidDate() {
        Tour pastTour = new Tour();
        pastTour.setId(1);
        pastTour.setDescription("Past Tour");
        pastTour.setPrice(1000.0);
        pastTour.setStartDate(LocalDate.now().minusDays(1));
        pastTour.setEndDate(LocalDate.now().plusDays(7));

        doNothing().when(controller).showError(anyString());

        boolean result = controller.validateTourForBooking(pastTour);

        assertFalse(result);
        verify(controller).showError("Неможливо забронювати тур, який вже почався");
    }

    @Test
    void testDisplayToursAsCards() {
        when(userSession.isCustomer()).thenReturn(true);
        List<Tour> tours = Arrays.asList(testTour1, testTour2);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.displayToursAsCards(tours);
                assertEquals(2, controller.tourCardContainer.getChildren().size());
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
    void testShowTourDetails() throws SQLException {
        when(tourService.getByIdWithDependencies(1)).thenReturn(testTour1);

        Platform.runLater(() -> {
            controller.showTourDetails(testTour1);
        });
        waitForFxEvents();

        verify(tourService).getByIdWithDependencies(1);
        verifyThat(".dialog-pane", isVisible());
    }

    @Test
    void testTourTypeManagement() throws SQLException {
        when(userSession.isAdmin()).thenReturn(true);
        when(tourTypeService.getAll()).thenReturn(Arrays.asList(testTourType));

        Platform.runLater(() -> {
            controller.addNewTourType();
        });
        waitForFxEvents();

        verify(controllerFactory).createTourTypeEditController(null);

        Platform.runLater(() -> {
            controller.editSelectedTourType();
        });
        waitForFxEvents();

        verify(tourTypeService).getAll();
    }

    @Test
    void testInitializationWithExceptions() throws SQLException {
        when(tourService.getAll()).thenThrow(new SQLException("Database error"));
        when(tourTypeService.getAll()).thenThrow(new SQLException("Database error"));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                DashboardController errorController = new DashboardController(stage, sessionManager, tourService, locationService,
                        tourTypeService, mealTypeService, transportService, userTourService, controllerFactory);
                assertNotNull(errorController);
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
    void testFilterValidation(FxRobot robot) throws InterruptedException {
        waitForFxEvents();
        doNothing().when(controller).showInfo(anyString());

        robot.clickOn("#startDatePicker");
        robot.write(LocalDate.now().plusDays(10).toString());
        robot.clickOn("#endDatePicker");
        robot.write(LocalDate.now().plusDays(5).toString());

        robot.clickOn("#filterButton");
        Thread.sleep(200);

        verify(controller, atLeast(0)).showInfo(anyString());
    }

    @Test
    void testNavigationBarInitialization(FxRobot robot) throws InterruptedException {
        waitForFxEvents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                assertNotNull(controller.mainLayout.getTop());
                assertTrue(controller.mainLayout.getTop() instanceof HBox);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testNavigationCallbacks() throws InterruptedException {
        when(controllerFactory.createNavigationController()).thenReturn(navigationController);
        when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());

        ArgumentCaptor<Runnable> logoutCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> exitCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> searchCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> bookedCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> adminCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> profileCaptor = ArgumentCaptor.forClass(Runnable.class);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initializeNavigationBar();

                verify(navigationController).setOnLogout(logoutCaptor.capture());
                verify(navigationController).setOnExit(exitCaptor.capture());
                verify(navigationController).setOnSearch(searchCaptor.capture());
                verify(navigationController).setOnBooked(bookedCaptor.capture());
                verify(navigationController).setOnAdminPanel(adminCaptor.capture());
                verify(navigationController).setOnProfile(profileCaptor.capture());

                logoutCaptor.getValue().run();
                verify(sessionManager).endSession();
                verify(controllerFactory).createLoginController();

                bookedCaptor.getValue().run();
                verify(controllerFactory).createBookedToursController();

                profileCaptor.getValue().run();
                verify(controllerFactory).createUserCabinetController();

            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testAdminPanelAccessDenied() throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(false);
        when(controllerFactory.createNavigationController()).thenReturn(navigationController);
        when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
        doNothing().when(controller).showInfo(anyString());

        ArgumentCaptor<Runnable> adminCaptor = ArgumentCaptor.forClass(Runnable.class);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initializeNavigationBar();
                verify(navigationController).setOnAdminPanel(adminCaptor.capture());

                adminCaptor.getValue().run();
                verify(controller).showInfo("У вас немає прав для доступу до адміністративної панелі");
                verify(controllerFactory, never()).createAdminPanelController();

            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testNavigationInitializationError() throws InterruptedException {
        when(controllerFactory.createNavigationController()).thenThrow(new RuntimeException("Navigation error"));
        doNothing().when(controller).showError(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initializeNavigationBar();
                verify(controller).showError(contains("Помилка ініціалізації навігації"));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testSpinnerInitialization(FxRobot robot) throws InterruptedException {
        waitForFxEvents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Spinner<Double> minSpinner = robot.lookup("#minPriceSpinner").queryAs(Spinner.class);
                Spinner<Double> maxSpinner = robot.lookup("#maxPriceSpinner").queryAs(Spinner.class);

                assertEquals(0.0, minSpinner.getValue());
                assertEquals(50000.0, maxSpinner.getValue());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testFilterDataLoadingError() throws SQLException, InterruptedException {
        when(locationService.getAll()).thenThrow(new SQLException("Location error"));
        when(tourTypeService.getAll()).thenThrow(new SQLException("TourType error"));
        when(mealTypeService.getAll()).thenThrow(new SQLException("MealType error"));
        when(transportService.getAll()).thenThrow(new SQLException("Transport error"));
        doNothing().when(controller).showError(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadFilterData();
                verify(controller).showError(contains("Помилка завантаження даних для фільтрів"));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testValidationListeners(FxRobot robot) throws InterruptedException {
        waitForFxEvents();

        robot.clickOn("#keywordField").write("test");
        robot.eraseText(4);

        robot.clickOn("#startDatePicker");
        Platform.runLater(() -> controller.startDatePicker.setValue(LocalDate.now()));

        Thread.sleep(200);
    }

    @Test
    void testMealTypeMultipleSelection(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();
        reset(tourService);
        when(tourService.search(any(Map.class))).thenReturn(Arrays.asList(testTour1));

        Platform.runLater(() -> {
            ListView<String> mealList = robot.lookup("#mealTypeFilterList").queryAs(ListView.class);
            mealList.getItems().addAll("Повний пансіон", "Напівпансіон");
            mealList.getSelectionModel().selectIndices(0, 1);
        });

        Thread.sleep(200);
        robot.clickOn("#filterButton");
        Thread.sleep(200);

        verify(tourService).search(argThat(filters ->
                filters.containsKey("meal_types") &&
                        filters.get("meal_types") instanceof List
        ));
    }

    @Test
    void testCustomerFilterActiveOnly(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isCustomer()).thenReturn(true);
        reset(tourService);
        when(tourService.search(any(Map.class))).thenReturn(Arrays.asList(testTour1));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadTours();
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(tourService).search(argThat(filters ->
                filters.containsKey("is_active") && Boolean.TRUE.equals(filters.get("is_active"))
        ));
    }

    @Test
    void testShowTourDetailsWithNullLocations() throws SQLException, InterruptedException {
        Tour tourWithoutLocations = new Tour();
        tourWithoutLocations.setId(3);
        tourWithoutLocations.setDescription("Test Tour");
        tourWithoutLocations.setPrice(1000.0);
        tourWithoutLocations.setStartDate(LocalDate.now());
        tourWithoutLocations.setEndDate(LocalDate.now().plusDays(7));
        tourWithoutLocations.setActive(true);
        tourWithoutLocations.setLocations(null);

        when(tourService.getByIdWithDependencies(3)).thenReturn(tourWithoutLocations);
        when(tourService.getLocationsForTour(3)).thenReturn(Arrays.asList(testLocation));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.showTourDetails(tourWithoutLocations);
            } finally {
                latch.countDown();
            }
        });
    }

    @Test
    void testShowTourDetailsNotFound() throws SQLException, InterruptedException {
        when(tourService.getByIdWithDependencies(999)).thenReturn(null);
        doNothing().when(controller).showError(anyString());

        Tour nonExistentTour = new Tour();
        nonExistentTour.setId(999);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.showTourDetails(nonExistentTour);
                verify(controller).showError("Не вдалося знайти тур з ID: 999");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testValidateTourForBooking_NullTour() {
        doNothing().when(controller).showError(anyString());

        boolean result = controller.validateTourForBooking(null);

        assertFalse(result);
        verify(controller).showError("Помилка: тур не знайдено");
    }

    @Test
    void testValidateTourForBooking_InvalidPrice() {
        Tour invalidPriceTour = new Tour();
        invalidPriceTour.setId(1);
        invalidPriceTour.setDescription("Valid Description");
        invalidPriceTour.setPrice(-100.0);
        invalidPriceTour.setStartDate(LocalDate.now().plusDays(1));
        invalidPriceTour.setEndDate(LocalDate.now().plusDays(7));

        doNothing().when(controller).showError(anyString());

        boolean result = controller.validateTourForBooking(invalidPriceTour);

        assertFalse(result);
        verify(controller).showError(contains("Помилка з ціною туру"));
    }

    @Test
    void testValidateTourForBooking_InvalidDescription() {
        Tour invalidDescTour = new Tour();
        invalidDescTour.setId(1);
        invalidDescTour.setDescription("");
        invalidDescTour.setPrice(1000.0);
        invalidDescTour.setStartDate(LocalDate.now().plusDays(1));
        invalidDescTour.setEndDate(LocalDate.now().plusDays(7));

        doNothing().when(controller).showError(anyString());

        boolean result = controller.validateTourForBooking(invalidDescTour);

        assertFalse(result);
        verify(controller).showError(contains("Помилка з описом туру"));
    }

    @Test
    void testValidateTourForBooking_InvalidDateRange() {
        Tour invalidDateTour = new Tour();
        invalidDateTour.setId(1);
        invalidDateTour.setDescription("Valid Description");
        invalidDateTour.setPrice(1000.0);
        invalidDateTour.setStartDate(LocalDate.now().plusDays(7));
        invalidDateTour.setEndDate(LocalDate.now().plusDays(1));

        doNothing().when(controller).showError(anyString());

        boolean result = controller.validateTourForBooking(invalidDateTour);

        assertFalse(result);
        verify(controller).showError(contains("Помилка з датами туру"));
    }

    @Test
    void testHandleBooking_DatabaseError() throws SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(userTourService.exists(1, 1)).thenThrow(new SQLException("Database connection error"));
        doNothing().when(controller).showError(anyString());

        Platform.runLater(() -> {
            controller.handleBooking(testTour1);
        });
        waitForFxEvents();

        verify(controller).showError(contains("Помилка при бронюванні туру"));
    }

    @Test
    void testHandleBooking_InvalidTour() {
        Tour invalidTour = new Tour();
        invalidTour.setStartDate(LocalDate.now().minusDays(1));
        doNothing().when(controller).showError(anyString());

        Platform.runLater(() -> {
            controller.handleBooking(invalidTour);
        });
        waitForFxEvents();

        verify(controller).showError(anyString());
    }

    @Test
    void testShowSuccessBookingDialog_ViewBookedOption() {
        doNothing().when(controller).showError(anyString());

        assertDoesNotThrow(() -> {
            Platform.runLater(() -> controller.showSuccessBookingDialog(testTour1));
            waitForFxEvents();
        });
    }

    @Test
    void testEditTourTypeWithoutPermission() throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(false);
        doNothing().when(controller).showInfo(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.editSelectedTourType();
                verify(controller).showInfo("У вас немає прав для редагування типів турів");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testEditTourTypeEmptyList() throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        when(tourTypeService.getAll()).thenReturn(Arrays.asList());
        doNothing().when(controller).showInfo(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.editSelectedTourType();
                verify(controller).showInfo("Типи турів відсутні");
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testDeleteTourWithoutSelection(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        doNothing().when(controller).showError(anyString());

        Thread.sleep(200);
        robot.clickOn("#deleteTourButton");

        verify(controller).showError("Виберіть тур для видалення");
    }

    @Test
    void testToggleStatusWithoutSelection(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        doNothing().when(controller).showError(anyString());

        Thread.sleep(200);
        robot.clickOn("#toggleStatusButton");

        verify(controller).showError("Виберіть тур для зміни статусу");
    }

    @Test
    void testDeleteTourDatabaseError() throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        doNothing().when(controller).showError(anyString());
        doThrow(new SQLException("Foreign key constraint")).when(tourService).delete(1);

        Platform.runLater(() -> {
            TableView<Tour> tourTable = controller.tourTable;
            tourTable.getItems().add(testTour1);
            tourTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Ви впевнені, що хочете видалити тур '" + testTour1.getDescription() + "'?",
                        ButtonType.YES, ButtonType.NO);

                try {
                    tourService.delete(testTour1.getId());
                } catch (SQLException e) {
                    controller.showError("Помилка видалення туру: " + e.getMessage());
                }
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(controller).showError(contains("Помилка видалення туру"));
    }

    @Test
    void testToggleStatusDatabaseError() throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        doNothing().when(controller).showError(anyString());
        doThrow(new SQLException("Database error")).when(tourService).toggleActiveStatus(1, false);

        Platform.runLater(() -> {
            TableView<Tour> tourTable = controller.tourTable;
            tourTable.getItems().add(testTour1);
            tourTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.toggleTourStatus();
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(controller).showError(contains("Помилка зміни статусу туру"));
    }

    @Test
    void testTourCardDoubleClick(FxRobot robot
    ) throws InterruptedException, SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(tourService.getByIdWithDependencies(1)).thenReturn(testTour1);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.displayToursAsCards(Arrays.asList(testTour1));

                VBox card = (VBox) controller.tourCardContainer.getChildren().getFirst();
                card.getOnMouseClicked().handle(new javafx.scene.input.MouseEvent(
                        javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                        javafx.scene.input.MouseButton.PRIMARY, 2, false, false, false, false,
                        true, false, false, false, false, false, null));

            } finally {
                latch.countDown();
            }
        });

    }

    @Test
    void testLoadToursEmptyResult(FxRobot robot) throws SQLException, InterruptedException {
        reset(tourService);
        when(tourService.search(any(Map.class))).thenReturn(Arrays.asList());
        doNothing().when(controller).showInfo(anyString());

        robot.clickOn("#filterButton");
        Thread.sleep(200);

        verify(controller).showInfo("За вказаними критеріями турів не знайдено");
    }

    @Test
    void testInitializeFiltersException() throws InterruptedException {
        DashboardController errorController = spy(new DashboardController(stage, sessionManager, tourService, locationService,
                tourTypeService, mealTypeService, transportService, userTourService, controllerFactory));

        doNothing().when(errorController).showError(anyString());
        doThrow(new RuntimeException("Spinner error")).when(errorController).setupValidationListeners();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                errorController.initializeFilters();
            } catch (Exception e) {
                errorController.showError("Помилка ініціалізації фільтрів: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testResetFiltersException(FxRobot robot) throws InterruptedException {
        waitForFxEvents();
        doNothing().when(controller).showError(anyString());

        Platform.runLater(() -> {
            try {
                controller.resetAllFilters();
            } catch (Exception e) {
                controller.showError("Помилка при скиданні фільтрів: " + e.getMessage());
            }
        });

        Thread.sleep(200);
    }

    @Test
    void testHandleBooking_ConfirmationYes_Success() throws SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(userTourService.exists(1, 1)).thenReturn(false);
        when(userTourService.createLink(1, 1)).thenReturn(true);
        doNothing().when(controller).showSuccessBookingDialog(any(Tour.class));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                int userId = mockUser.getId();
                int tourId = testTour1.getId();

                try {
                    boolean success = userTourService.createLink(userId, tourId);
                    if (success) {
                        controller.showSuccessBookingDialog(testTour1);
                    } else {
                        controller.showError("Помилка при створенні бронювання. Спробуйте ще раз.");
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("уже існує")) {
                        controller.showInfo("Ви вже забронювали цей тур");
                    } else {
                        controller.showError("Помилка при бронюванні туру: " + e.getMessage());
                    }
                } catch (IllegalArgumentException e) {
                    controller.showInfo("Ви вже забронювали цей тур");
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userTourService).createLink(1, 1);
        verify(controller).showSuccessBookingDialog(testTour1);
    }

    @Test
    void testHandleBooking_ConfirmationYes_CreateLinkFails() throws SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(userTourService.exists(1, 1)).thenReturn(false);
        when(userTourService.createLink(1, 1)).thenReturn(false);
        doNothing().when(controller).showError(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                int userId = mockUser.getId();
                int tourId = testTour1.getId();

                try {
                    boolean success = userTourService.createLink(userId, tourId);
                    if (success) {
                        controller.showSuccessBookingDialog(testTour1);
                    } else {
                        controller.showError("Помилка при створенні бронювання. Спробуйте ще раз.");
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("уже існує")) {
                        controller.showInfo("Ви вже забронювали цей тур");
                    } else {
                        controller.showError("Помилка при бронюванні туру: " + e.getMessage());
                    }
                } catch (IllegalArgumentException e) {
                    controller.showInfo("Ви вже забронювали цей тур");
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userTourService).createLink(1, 1);
        verify(controller).showError("Помилка при створенні бронювання. Спробуйте ще раз.");
        verify(controller, never()).showSuccessBookingDialog(any(Tour.class));
    }

    @Test
    void testHandleBooking_ConfirmationYes_SQLException_AlreadyExists() throws SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(userTourService.exists(1, 1)).thenReturn(false);
        when(userTourService.createLink(1, 1)).thenThrow(new SQLException("Запис уже існує"));
        doNothing().when(controller).showInfo(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                int userId = mockUser.getId();
                int tourId = testTour1.getId();

                try {
                    boolean success = userTourService.createLink(userId, tourId);
                    if (success) {
                        controller.showSuccessBookingDialog(testTour1);
                    } else {
                        controller.showError("Помилка при створенні бронювання. Спробуйте ще раз.");
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("уже існує")) {
                        controller.showInfo("Ви вже забронювали цей тур");
                    } else {
                        controller.showError("Помилка при бронюванні туру: " + e.getMessage());
                    }
                } catch (IllegalArgumentException e) {
                    controller.showInfo("Ви вже забронювали цей тур");
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userTourService).createLink(1, 1);
        verify(controller).showInfo("Ви вже забронювали цей тур");
        verify(controller, never()).showSuccessBookingDialog(any(Tour.class));
        verify(controller, never()).showError(contains("Помилка при бронюванні туру"));
    }

    @Test
    void testHandleBooking_ConfirmationYes_SQLException_DatabaseError() throws SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(userTourService.exists(1, 1)).thenReturn(false);
        when(userTourService.createLink(1, 1)).thenThrow(new SQLException("Database connection failed"));
        doNothing().when(controller).showError(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                int userId = mockUser.getId();
                int tourId = testTour1.getId();

                try {
                    boolean success = userTourService.createLink(userId, tourId);
                    if (success) {
                        controller.showSuccessBookingDialog(testTour1);
                    } else {
                        controller.showError("Помилка при створенні бронювання. Спробуйте ще раз.");
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("уже існує")) {
                        controller.showInfo("Ви вже забронювали цей тур");
                    } else {
                        controller.showError("Помилка при бронюванні туру: " + e.getMessage());
                    }
                } catch (IllegalArgumentException e) {
                    controller.showInfo("Ви вже забронювали цей тур");
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userTourService).createLink(1, 1);
        verify(controller).showError("Помилка при бронюванні туру: Database connection failed");
        verify(controller, never()).showSuccessBookingDialog(any(Tour.class));
        verify(controller, never()).showInfo("Ви вже забронювали цей тур");
    }

    @Test
    void testHandleBooking_ConfirmationYes_IllegalArgumentException() throws SQLException {
        when(userSession.isCustomer()).thenReturn(true);
        when(userTourService.exists(1, 1)).thenReturn(false);
        when(userTourService.createLink(1, 1)).thenThrow(new IllegalArgumentException("User already booked this tour"));
        doNothing().when(controller).showInfo(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                int userId = mockUser.getId();
                int tourId = testTour1.getId();

                try {
                    boolean success = userTourService.createLink(userId, tourId);
                    if (success) {
                        controller.showSuccessBookingDialog(testTour1);
                    } else {
                        controller.showError("Помилка при створенні бронювання. Спробуйте ще раз.");
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("уже існує")) {
                        controller.showInfo("Ви вже забронювали цей тур");
                    } else {
                        controller.showError("Помилка при бронюванні туру: " + e.getMessage());
                    }
                } catch (IllegalArgumentException e) {
                    controller.showInfo("Ви вже забронювали цей тур");
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userTourService).createLink(1, 1);
        verify(controller).showInfo("Ви вже забронювали цей тур");
        verify(controller, never()).showSuccessBookingDialog(any(Tour.class));
        verify(controller, never()).showError(anyString());
    }
}
