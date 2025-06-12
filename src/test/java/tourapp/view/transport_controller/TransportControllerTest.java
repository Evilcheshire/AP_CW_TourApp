package tourapp.view.transport_controller;

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
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.transport_service.TransportService;
import tourapp.service.transport_service.TransportTypeService;
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
class TransportControllerTest {

    @InjectMocks private TransportController controller;
    @Mock private TransportService transportService;
    @Mock private TransportTypeService transportTypeService;
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
    private Transport testTransport1;
    private Transport testTransport2;
    private TransportType testTransportType;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupMocks();
        controller = spy(new TransportController(stage, sessionManager, transportService, transportTypeService, controllerFactory));
        BaseController.logger = logger;
        controller.show();
    }

    @BeforeEach
    void setUp() {
        setupTestData();
        reset(transportService, transportTypeService, controllerFactory, navigationController,
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

            List<Transport> transports = Arrays.asList(testTransport1, testTransport2);
            List<TransportType> transportTypes = Arrays.asList(testTransportType);

            when(transportService.getAll()).thenReturn(transports);
            when(transportService.search(any(Map.class))).thenReturn(transports);
            when(transportService.getById(1)).thenReturn(testTransport1);
            when(transportService.getById(2)).thenReturn(testTransport2);
            when(transportTypeService.getAll()).thenReturn(transportTypes);

            when(controllerFactory.createNavigationController()).thenReturn(navigationController);
            when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
            when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
            when(controllerFactory.createBookedToursController()).thenReturn(bookedToursController);
            when(controllerFactory.createAdminPanelController()).thenReturn(adminPanelController);
            when(controllerFactory.createUserCabinetController()).thenReturn(userCabinetController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);

            TransportEditController mockTransportEditController = mock(TransportEditController.class);
            TransportTypeEditController mockTransportTypeEditController = mock(TransportTypeEditController.class);
            when(controllerFactory.createTransportEditController(any())).thenReturn(mockTransportEditController);
            when(controllerFactory.createTransportTypeEditController(any())).thenReturn(mockTransportTypeEditController);

        } catch (SQLException e) {
            System.out.println("Setup failed: " + e.getMessage());
        }
    }

    private void setupTestData() {
        testTransportType = new TransportType();
        testTransportType.setId(1);
        testTransportType.setName("Автобус");

        testTransport1 = new Transport();
        testTransport1.setId(1);
        testTransport1.setName("Mercedes-Benz Sprinter");
        testTransport1.setPricePerPerson(200.0);
        testTransport1.setType(testTransportType);

        testTransport2 = new Transport();
        testTransport2.setId(2);
        testTransport2.setName("Iveco Daily");
        testTransport2.setPricePerPerson(150.0);
        testTransport2.setType(testTransportType);
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
        assertEquals("TourApp - Управління транспортом", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView transportTable = robot.lookup("#transportTable").queryAs(TableView.class);
                assertNotNull(transportTable);

                ComboBox typeCombo = robot.lookup("#transportTypeFilterCombo").queryAs(ComboBox.class);
                assertNotNull(typeCombo);
                assertEquals("Всі", typeCombo.getValue());

                Spinner minPriceSpinner = robot.lookup("#minPriceSpinner").queryAs(Spinner.class);
                Spinner maxPriceSpinner = robot.lookup("#maxPriceSpinner").queryAs(Spinner.class);
                assertNotNull(minPriceSpinner);
                assertNotNull(maxPriceSpinner);
                assertEquals(0.0, (Double) minPriceSpinner.getValue());
                assertEquals(5000.0, (Double) maxPriceSpinner.getValue());
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
    void testAccessControlsForAdmin(FxRobot robot) {
        when(userSession.isAdmin()).thenReturn(true);
        when(userSession.isManager()).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();

                Button addTransportButton = robot.lookup("#addTransportButton").queryAs(Button.class);
                Button editTransportButton = robot.lookup("#editTransportButton").queryAs(Button.class);
                Button deleteTransportButton = robot.lookup("#deleteTransportButton").queryAs(Button.class);
                Button addTransportTypeButton = robot.lookup("#addTransportTypeButton").queryAs(Button.class);
                Button editTransportTypeButton = robot.lookup("#editTransportTypeButton").queryAs(Button.class);

                assertTrue(addTransportButton.isVisible());
                assertTrue(editTransportButton.isVisible());
                assertTrue(deleteTransportButton.isVisible());
                assertTrue(addTransportTypeButton.isVisible());
                assertTrue(editTransportTypeButton.isVisible());
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
    void testAccessControlsForManager(FxRobot robot) {
        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();

                Button addTransportButton = robot.lookup("#addTransportButton").queryAs(Button.class);
                Button editTransportButton = robot.lookup("#editTransportButton").queryAs(Button.class);
                Button deleteTransportButton = robot.lookup("#deleteTransportButton").queryAs(Button.class);
                Button addTransportTypeButton = robot.lookup("#addTransportTypeButton").queryAs(Button.class);
                Button editTransportTypeButton = robot.lookup("#editTransportTypeButton").queryAs(Button.class);

                assertTrue(addTransportButton.isVisible());
                assertTrue(editTransportButton.isVisible());
                assertTrue(deleteTransportButton.isVisible());
                assertFalse(addTransportTypeButton.isVisible());
                assertFalse(editTransportTypeButton.isVisible());
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
    void testResetFilters(FxRobot robot) throws InterruptedException {
        waitForFxEvents();

        robot.clickOn("#keywordField").write("Test");
        Platform.runLater(() -> {
            ComboBox<String> transportTypeCombo = robot.lookup("#transportTypeFilterCombo").queryAs(ComboBox.class);
            transportTypeCombo.setValue("Автобус");
            Spinner<Double> minPriceSpinner = robot.lookup("#minPriceSpinner").queryAs(Spinner.class);
            Spinner<Double> maxPriceSpinner = robot.lookup("#maxPriceSpinner").queryAs(Spinner.class);
            minPriceSpinner.getValueFactory().setValue(100.0);
            maxPriceSpinner.getValueFactory().setValue(300.0);
        });
        Thread.sleep(200);

        robot.clickOn("#resetFiltersButton");
        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TextField keywordField = robot.lookup("#keywordField").queryAs(TextField.class);
                ComboBox<String> transportTypeCombo = robot.lookup("#transportTypeFilterCombo").queryAs(ComboBox.class);
                Spinner<Double> minPriceSpinner = robot.lookup("#minPriceSpinner").queryAs(Spinner.class);
                Spinner<Double> maxPriceSpinner = robot.lookup("#maxPriceSpinner").queryAs(Spinner.class);

                assertTrue(keywordField.getText().isEmpty());
                assertEquals("Всі", transportTypeCombo.getValue());
                assertEquals(0.0, (Double) minPriceSpinner.getValue());
                assertEquals(5000.0, (Double) maxPriceSpinner.getValue());
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
    void testFilterWithSQLExceptions(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        when(transportService.search(any(Map.class)))
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
    void testAddNewTransport() {
        TransportEditController mockEditController = mock(TransportEditController.class);
        when(controllerFactory.createTransportEditController(null))
                .thenReturn(mockEditController);

        controller.addNewTransport();

        verify(controllerFactory).createTransportEditController(null);
        verify(mockEditController).setOnSaveCallback(any(Runnable.class));
        verify(mockEditController).show();
    }

    @Test
    void testAddNewTransportType() {
        TransportTypeEditController mockEditController = mock(TransportTypeEditController.class);
        when(controllerFactory.createTransportTypeEditController(null))
                .thenReturn(mockEditController);

        controller.addNewTransportType();

        verify(controllerFactory).createTransportTypeEditController(null);
        verify(mockEditController).setOnSaveCallback(any(Runnable.class));
        verify(mockEditController).show();
    }

    @Test
    void testEditSelectedTransport(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Thread.sleep(500);
        Platform.runLater(() -> {
            TableView<Transport> transportTable = robot.lookup("#transportTable").queryAs(TableView.class);
            transportTable.getItems().add(testTransport1);
            transportTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        robot.clickOn("#editTransportButton");
        verify(controllerFactory).createTransportEditController(any(Transport.class));

        reset(controllerFactory);
        Platform.runLater(() -> {
            TableView<Transport> transportTable = robot.lookup("#transportTable").queryAs(TableView.class);
            transportTable.getSelectionModel().clearSelection();
        });

        robot.clickOn("#editTransportButton");
        verify(controllerFactory, never()).createTransportEditController(any(Transport.class));
    }

    @Test
    void testEditSelectedTransportType() {
        doNothing().when(controller).showInfo(anyString());
        doNothing().when(controller).showTransportTypeSelectionDialog();

        controller.editSelectedTransportType();

        verify(controller).showInfo("Виберіть тип транспорту для редагування");
        verify(controller).showTransportTypeSelectionDialog();
    }

    @Test
    void testDeleteSelectedTransport(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Platform.runLater(() -> {
            TableView<Transport> transportTable = robot.lookup("#transportTable").queryAs(TableView.class);
            transportTable.getItems().add(testTransport1);
            transportTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        doNothing().when(controller).showInfo(anyString());

        robot.clickOn("#deleteTransportButton");
        Thread.sleep(200);

        HelperMethods.clickOnYes(robot);
        Thread.sleep(200);

        verify(transportService).delete(1);
        verify(controller).showInfo("Транспорт успішно видалено.");
        verify(logger).info(contains("Видалено транспорт"), eq(testTransport1.toString()));
    }

    @Test
    void testShowTransportDetails(FxRobot robot) throws SQLException {
        Transport detailedTransport = new Transport();
        detailedTransport.setId(1);
        detailedTransport.setName("Mercedes-Benz Sprinter");
        detailedTransport.setPricePerPerson(200.0);
        detailedTransport.setType(testTransportType);

        when(transportService.getById(1)).thenReturn(detailedTransport);

        Platform.runLater(() -> {
            controller.showTransportDetails(testTransport1);
        });
        waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        verify(transportService).getById(1);

        robot.clickOn("OK");
        waitForFxEvents();
    }

    @Test
    void testShowTransportDetails_notFound(FxRobot robot) throws SQLException {
        when(transportService.getById(1)).thenReturn(null);
        doNothing().when(controller).showError(anyString());

        controller.showTransportDetails(testTransport1);

        verify(controller).showError("Не вдалося знайти транспорт з ID: 1");
    }

    @Test
    void testShowTransportDetails_withSQLException(FxRobot robot) throws SQLException {
        when(transportService.getById(1)).thenThrow(new SQLException("Database error"));
        doNothing().when(controller).showError(anyString());

        controller.showTransportDetails(testTransport1);

        verify(controller).showError(contains("Помилка при отриманні деталей транспорту"));
    }

    @Test
    void testShowTransportTypeSelectionDialog(FxRobot robot) {
        Platform.runLater(() -> {
            controller.showTransportTypeSelectionDialog();
        });
        waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        verifyThat("OK", isVisible());
        verifyThat("Cancel", isVisible());

        robot.clickOn("Cancel");
        waitForFxEvents();

        verify(controllerFactory, never()).createTransportTypeEditController(any());
    }

    @Test
    void testTableRowDoubleClick(FxRobot robot) {
        Platform.runLater(() -> {
            controller.transportTable.setItems(FXCollections.observableArrayList(Arrays.asList(testTransport1, testTransport2)));
        });
        waitForFxEvents();

        TableRow<Transport> firstRow = robot.lookup(".table-row-cell").nth(0).query();
        robot.doubleClickOn(firstRow);
        waitForFxEvents();

        verifyThat(".dialog-pane", NodeMatchers.isVisible());
    }
}