package tourapp.view.location_controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
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
import tourapp.model.location.LocationType;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.location_service.LocationService;
import tourapp.service.location_service.LocationTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.*;
import tourapp.view.auth_controller.LoginController;
import tourapp.view.tour_controller.BookedToursController;
import tourapp.view.tour_controller.DashboardController;
import tourapp.view.user_controller.UserCabinetController;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
class LocationControllerTest {

    @InjectMocks private LocationController controller;
    @Mock private LocationService locationService;
    @Mock private LocationTypeService locationTypeService;
    @Mock private SessionManager sessionManager;
    @Mock private ControllerFactory controllerFactory;
    @Mock private SessionManager.UserSession userSession;
    @Mock private User mockUser;
    @Mock private UserType mockUserType;
    @Mock private NavigationController navigationController;
    @Mock private DashboardController dashboardController;
    @Mock private BookedToursController bookedToursController;
    @Mock private AdminPanelController adminPanelController;
    @Mock private UserCabinetController userCabinetController;
    @Mock private LoginController loginController;
    @Mock private Logger logger;

    private Stage stage;
    private Location testLocation1;
    private Location testLocation2;
    private LocationType testLocationType;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupMocks();
        controller = spy(new LocationController(stage, sessionManager, locationService, locationTypeService, controllerFactory));
        BaseController.logger = logger;
        controller.show();
    }

    @BeforeEach
    void setUp() {
        setupTestData();
        reset(locationService, locationTypeService, controllerFactory, navigationController,
                dashboardController, bookedToursController, adminPanelController,
                userCabinetController, loginController);
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

            setupTestData();

            List<Location> locations = Arrays.asList(testLocation1, testLocation2);
            List<LocationType> locationTypes = Arrays.asList(testLocationType);

            when(locationService.getAll()).thenReturn(locations);
            when(locationService.search(any(Map.class))).thenReturn(locations);
            when(locationService.getById(1)).thenReturn(testLocation1);
            when(locationService.getById(2)).thenReturn(testLocation2);
            when(locationTypeService.getAll()).thenReturn(locationTypes);

            when(controllerFactory.createNavigationController()).thenReturn(navigationController);
            when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
            when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
            when(controllerFactory.createBookedToursController()).thenReturn(bookedToursController);
            when(controllerFactory.createAdminPanelController()).thenReturn(adminPanelController);
            when(controllerFactory.createUserCabinetController()).thenReturn(userCabinetController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);

            LocationEditController mockLocationEditController = mock(LocationEditController.class);
            LocationTypeEditController mockLocationTypeEditController = mock(LocationTypeEditController.class);
            when(controllerFactory.createLocationEditController(any())).thenReturn(mockLocationEditController);
            when(controllerFactory.createLocationTypeEditController(any())).thenReturn(mockLocationTypeEditController);

        } catch (SQLException e) {
            System.out.println("Setup failed: " + e.getMessage());
        }
    }

    private void setupTestData() {
        testLocationType = new LocationType();
        testLocationType.setId(1);
        testLocationType.setName("Місто");

        testLocation1 = new Location();
        testLocation1.setId(1);
        testLocation1.setName("Київ");
        testLocation1.setCountry("Україна");
        testLocation1.setDescription("Столиця України");
        testLocation1.setLocationType(testLocationType);

        testLocation2 = new Location();
        testLocation2.setId(2);
        testLocation2.setName("Львів");
        testLocation2.setCountry("Україна");
        testLocation2.setDescription("Культурна столиця");
        testLocation2.setLocationType(testLocationType);
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
    void testControllerInitialization(FxRobot robot) {
        waitForFxEvents();

        assertNotNull(stage.getScene());
        assertTrue(stage.isShowing());
        assertEquals("TourApp - Керування локаціями", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView locationTable = robot.lookup("#locationTable").queryAs(TableView.class);
                assertNotNull(locationTable);

                ComboBox countryCombo = robot.lookup("#countryFilterCombo").queryAs(ComboBox.class);
                ComboBox typeCombo = robot.lookup("#countryTypeFilterCombo").queryAs(ComboBox.class);
                assertNotNull(countryCombo);
                assertNotNull(typeCombo);
                assertEquals("Всі", countryCombo.getValue());
                assertEquals("Всі", typeCombo.getValue());
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
        when(navigationController.createNavigationBar(NavigationController.PAGE_DASHBOARD))
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
        verify(navigationController).createNavigationBar(NavigationController.PAGE_DASHBOARD);
        verify(navigationController).setOnLogout(any(Runnable.class));
        verify(navigationController).setOnExit(any(Runnable.class));
        verify(navigationController).setOnSearch(any(Runnable.class));
        verify(navigationController).setOnBooked(any(Runnable.class));
        verify(navigationController).setOnAdminPanel(any(Runnable.class));
        verify(navigationController).setOnProfile(any(Runnable.class));
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
    }

    @Test
    void testAdminPanelAccess() {
        when(userSession.isAdmin()).thenReturn(true);
        when(userSession.isManager()).thenReturn(false);

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

        reset(controllerFactory, adminPanelController);
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(false);
        when(userSession.isCustomer()).thenReturn(true);

        CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                assertFalse(userSession.isAdmin());
                assertFalse(userSession.isManager());
            } finally {
                latch2.countDown();
            }
        });

        try {
            assertTrue(latch2.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testFilterOperations(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(locationService, times(1)).search(any(Map.class));

        reset(locationService);
        when(locationService.search(any(Map.class))).thenReturn(Arrays.asList(testLocation1));

        robot.clickOn("#keywordField").eraseText(10).write("Київ");
        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(locationService, times(1)).search(argThat(filters ->
                filters.containsKey("keyword") && "Київ".equals(filters.get("keyword"))));

        robot.clickOn("#resetFiltersButton");
        Thread.sleep(200);
        verify(locationService, atLeastOnce()).search(any(Map.class));
    }

    @Test
    void testFilterWithSQLExceptions(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        when(locationService.search(any(Map.class)))
                .thenThrow(new SQLException("Database connection error"));

        robot.clickOn("#filterButton");
        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Button filterButton = robot.lookup("#filterButton").queryAs(Button.class);
                assertNotNull(filterButton);
                assertFalse(filterButton.isDisabled());
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
    void testLocationEditOperations(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Thread.sleep(500);
        Platform.runLater(() -> {
            TableView<Location> locationTable = robot.lookup("#locationTable").queryAs(TableView.class);
            locationTable.getItems().add(testLocation1);
            locationTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        robot.clickOn("#editLocationButton");
        verify(controllerFactory).createLocationEditController(any(Location.class));

        reset(controllerFactory);
        Platform.runLater(() -> {
            TableView<Location> locationTable = robot.lookup("#locationTable").queryAs(TableView.class);
            locationTable.getSelectionModel().clearSelection();
        });

        robot.clickOn("#editLocationButton");
        verify(controllerFactory, never()).createLocationEditController(any(Location.class));
    }

    @Test
    void testDeleteSelectedLocation(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Platform.runLater(() -> {
            TableView<Location> locationTable = robot.lookup("#locationTable").queryAs(TableView.class);
            locationTable.getItems().add(testLocation1);
            locationTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        doNothing().when(controller).showInfo(anyString());

        robot.clickOn("#deleteLocationButton");
        Thread.sleep(200);

        HelperMethods.clickOnYes(robot);
        Thread.sleep(200);

        verify(locationService).delete(1);
        verify(controller).showInfo("Локацію успішно видалено.");
        verify(logger).info(contains("Видалено локацію"), eq(testLocation1.toString()));
    }

    @Test
    void testDeleteSelectedLocation_withoutSelection(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        doNothing().when(controller).showInfo(anyString());

        Platform.runLater(() -> {
            TableView<Location> locationTable = robot.lookup("#locationTable").queryAs(TableView.class);
            locationTable.getSelectionModel().clearSelection();
        });

        Thread.sleep(200);
        robot.clickOn("#deleteLocationButton");
        Thread.sleep(200);

        verify(controller).showInfo("Виберіть локацію для видалення");
        verify(locationService, never()).delete(anyInt());
    }

    @Test
    void testInitializationWithExceptions() throws SQLException {
        when(locationService.getAll()).thenThrow(new SQLException("Database error"));
        when(locationTypeService.getAll()).thenThrow(new SQLException("Database error"));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                LocationController errorController = new LocationController(
                        stage, sessionManager, locationService, locationTypeService, controllerFactory);
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
    void testShowLocationDetails(FxRobot robot) throws SQLException {
        Location detailedLocation = new Location();
        detailedLocation.setId(1);
        detailedLocation.setName("Київ");
        detailedLocation.setCountry("Україна");
        detailedLocation.setDescription("Столиця України");
        detailedLocation.setLocationType(testLocationType);

        when(locationService.getById(1)).thenReturn(detailedLocation);

        Platform.runLater(() -> {
            controller.showLocationDetails(testLocation1);
        });
        waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        verify(locationService).getById(1);

        robot.clickOn("OK");
        waitForFxEvents();
    }

    @Test
    void testShowLocationTypeSelectionDialog(FxRobot robot) {
        Platform.runLater(() -> {
            controller.showLocationTypeSelectionDialog();
        });
        waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        verifyThat("OK", isVisible());
        verifyThat("Cancel", isVisible());

        robot.clickOn("Cancel");
        waitForFxEvents();

        verify(controllerFactory, never()).createLocationTypeEditController(any());
    }

    @Test
    void testShowLocationTypeSelectionDialog_withEmptyList() throws Exception {
        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());
        doNothing().when(controller).showError(anyString());

        controller.showLocationTypeSelectionDialog();

        verify(controller).showError("Не знайдено жодного типу локації");
    }

    @Test
    void testTableInteractions(FxRobot robot) throws SQLException {
        waitForFxEvents();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView<Location> locationTable = robot.lookup("#locationTable").queryAs(TableView.class);
                locationTable.getItems().clear();
                locationTable.getItems().addAll(testLocation1, testLocation2);
                locationTable.refresh();
            } finally {
                setupLatch.countDown();
            }
        });

        try {
            robot.clickOn(".table-row-cell");
            robot.doubleClickOn(".table-row-cell");
            Thread.sleep(200);
            closeOpenDialogs(robot);

            verify(locationService, atLeastOnce()).getById(anyInt());
        } catch (Exception e) {
            closeOpenDialogs(robot);
        }
    }

    @Test
    void testEditSelectedLocationType_shouldShowInfoAndOpenDialog() {
        doNothing().when(controller).showInfo(anyString());
        doNothing().when(controller).showLocationTypeSelectionDialog();

        controller.editSelectedLocationType();

        verify(controller).showInfo("Виберіть тип локації для редагування");
        verify(controller).showLocationTypeSelectionDialog();
    }

    @Test
    public void testShowLocationTypeEditDialog_Success() {
        LocationType testLocationType = new LocationType();
        testLocationType.setId(1);
        testLocationType.setName("Test Location Type");

        LocationTypeEditController mockEditController = mock(LocationTypeEditController.class);

        when(controllerFactory.createLocationTypeEditController(testLocationType))
                .thenReturn(mockEditController);

        controller.showLocationTypeEditDialog(testLocationType);

        verify(controllerFactory).createLocationTypeEditController(testLocationType);
        verify(mockEditController).setOnSaveCallback(any(Runnable.class));
        verify(mockEditController).show();
    }

    @Test
    public void testAddNewLocation() {
        LocationEditController mockEditController = mock(LocationEditController.class);
        when(controllerFactory.createLocationEditController(null))
                .thenReturn(mockEditController);

        controller.addNewLocation();

        verify(controllerFactory).createLocationEditController(null);
        verify(mockEditController).setOnSaveCallback(any(Runnable.class));
        verify(mockEditController).show();
    }

    @Test
    public void testAddNewLocationType() {
        LocationTypeEditController mockEditController = mock(LocationTypeEditController.class);

        when(controllerFactory.createLocationTypeEditController(null))
                .thenReturn(mockEditController);

        controller.addNewLocationType();

        verify(controllerFactory).createLocationTypeEditController(null);
        verify(mockEditController).setOnSaveCallback(any(Runnable.class));
        verify(mockEditController).show();
    }

    @Test
    public void testAddNewLocation_CallbackFunctionality() {
        LocationEditController mockEditController = mock(LocationEditController.class);
        when(controllerFactory.createLocationEditController(null))
                .thenReturn(mockEditController);

        ArgumentCaptor<Runnable> callbackCaptor = ArgumentCaptor.forClass(Runnable.class);

        controller.addNewLocation();

        verify(mockEditController).setOnSaveCallback(callbackCaptor.capture());
        Runnable capturedCallback = callbackCaptor.getValue();

        assertNotNull(capturedCallback);
        assertDoesNotThrow(() -> capturedCallback.run());
    }

    @Test
    void testTableRowDoubleClick(FxRobot robot) {
        Platform.runLater(() -> {
            controller.locationTable.setItems(FXCollections.observableArrayList(Arrays.asList(testLocation1, testLocation2)));
        });
        waitForFxEvents();

        TableRow<Location> firstRow = robot.lookup(".table-row-cell").nth(0).query();
        robot.doubleClickOn(firstRow);
        waitForFxEvents();

        verifyThat(".dialog-pane", NodeMatchers.isVisible());
    }
}