package tourapp.view.meal_controller;

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
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.meal_service.MealMealTypeService;
import tourapp.service.meal_service.MealService;
import tourapp.service.meal_service.MealTypeService;
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
class MealControllerTest {

    @InjectMocks private MealController controller;

    @Mock private MealService mealService;
    @Mock private MealTypeService mealTypeService;
    @Mock private MealMealTypeService mealMealTypeService;
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
    private Meal testMeal1;
    private Meal testMeal2;
    private MealType testMealType1;
    private MealType testMealType2;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupMocks();
        controller = spy(new MealController(stage, sessionManager, mealService, mealTypeService, mealMealTypeService, controllerFactory));
        BaseController.logger = logger;
        controller.show();
    }

    @BeforeEach
    void setUp() {
        setupTestData();
        reset(mealService, mealTypeService, mealMealTypeService, controllerFactory, navigationController,
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

            List<Meal> meals = Arrays.asList(testMeal1, testMeal2);
            List<MealType> mealTypes = Arrays.asList(testMealType1, testMealType2);

            when(mealService.getAll()).thenReturn(meals);
            when(mealService.search(any(Map.class))).thenReturn(meals);
            when(mealService.getById(1)).thenReturn(testMeal1);
            when(mealService.getById(2)).thenReturn(testMeal2);
            when(mealTypeService.getAll()).thenReturn(mealTypes);
            when(mealMealTypeService.findById1(anyInt())).thenReturn(mealTypes);

            when(controllerFactory.createNavigationController()).thenReturn(navigationController);
            when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
            when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
            when(controllerFactory.createBookedToursController()).thenReturn(bookedToursController);
            when(controllerFactory.createAdminPanelController()).thenReturn(adminPanelController);
            when(controllerFactory.createUserCabinetController()).thenReturn(userCabinetController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);

            MealEditController mockMealEditController = mock(MealEditController.class);
            MealTypeEditController mockMealTypeEditController = mock(MealTypeEditController.class);
            when(controllerFactory.createMealEditController(any())).thenReturn(mockMealEditController);
            when(controllerFactory.createMealTypeEditController(any())).thenReturn(mockMealTypeEditController);

        } catch (SQLException e) {
            System.out.println("Setup failed: " + e.getMessage());
        }
    }

    private void setupTestData() {
        testMealType1 = new MealType();
        testMealType1.setId(1);
        testMealType1.setName("Сніданок");

        testMealType2 = new MealType();
        testMealType2.setId(2);
        testMealType2.setName("Обід");

        testMeal1 = new Meal();
        testMeal1.setId(1);
        testMeal1.setName("Континентальний сніданок");
        testMeal1.setMealsPerDay(1);
        testMeal1.setCostPerDay(250.0);
        testMeal1.setMealTypes(Arrays.asList(testMealType1));

        testMeal2 = new Meal();
        testMeal2.setId(2);
        testMeal2.setName("Повний пансіон");
        testMeal2.setMealsPerDay(3);
        testMeal2.setCostPerDay(850.0);
        testMeal2.setMealTypes(Arrays.asList(testMealType1, testMealType2));
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
        assertEquals("TourApp - Управління харчуванням", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView mealTable = robot.lookup("#mealTable").queryAs(TableView.class);
                assertNotNull(mealTable);

                TextField keywordField = robot.lookup("#keywordField").queryAs(TextField.class);
                ComboBox mealTypeCombo = robot.lookup("#mealTypeFilterCombo").queryAs(ComboBox.class);
                Spinner minPriceSpinner = robot.lookup("#minPriceSpinner").queryAs(Spinner.class);
                Spinner maxPriceSpinner = robot.lookup("#maxPriceSpinner").queryAs(Spinner.class);

                assertNotNull(keywordField);
                assertNotNull(mealTypeCombo);
                assertNotNull(minPriceSpinner);
                assertNotNull(maxPriceSpinner);
                assertEquals("Всі", mealTypeCombo.getValue());
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
    void testAccessControlsForAdmin(FxRobot robot) {
        when(userSession.isAdmin()).thenReturn(true);
        when(userSession.isManager()).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setupAccessControls();

                Button addMealButton = robot.lookup("#addMealButton").queryAs(Button.class);
                Button editMealButton = robot.lookup("#editMealButton").queryAs(Button.class);
                Button deleteMealButton = robot.lookup("#deleteMealButton").queryAs(Button.class);
                Button addMealTypeButton = robot.lookup("#addMealTypeButton").queryAs(Button.class);
                Button editMealTypeButton = robot.lookup("#editMealTypeButton").queryAs(Button.class);

                assertTrue(addMealButton.isVisible());
                assertTrue(editMealButton.isVisible());
                assertTrue(deleteMealButton.isVisible());
                assertTrue(addMealTypeButton.isVisible());
                assertTrue(editMealTypeButton.isVisible());
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

                Button addMealButton = robot.lookup("#addMealButton").queryAs(Button.class);
                Button editMealButton = robot.lookup("#editMealButton").queryAs(Button.class);
                Button deleteMealButton = robot.lookup("#deleteMealButton").queryAs(Button.class);
                Button addMealTypeButton = robot.lookup("#addMealTypeButton").queryAs(Button.class);
                Button editMealTypeButton = robot.lookup("#editMealTypeButton").queryAs(Button.class);

                assertTrue(addMealButton.isVisible());
                assertTrue(editMealButton.isVisible());
                assertTrue(deleteMealButton.isVisible());
                assertFalse(addMealTypeButton.isVisible());
                assertFalse(editMealTypeButton.isVisible());
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
    void testFilterOperations(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(mealService, times(1)).search(any(Map.class));

        reset(mealService);
        when(mealService.search(any(Map.class))).thenReturn(Arrays.asList(testMeal1));

        robot.clickOn("#keywordField").eraseText(20).write("Континентальний");
        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(mealService, times(1)).search(argThat(filters ->
                filters.containsKey("name") && "Континентальний".equals(filters.get("name"))
        ));

        reset(mealService);
        Platform.runLater(() -> {
            ComboBox<String> mealTypeCombo = robot.lookup("#mealTypeFilterCombo").queryAs(ComboBox.class);
            mealTypeCombo.setValue("Сніданок");
        });
        Thread.sleep(200);
        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(mealService, times(1)).search(argThat(filters ->
                filters.containsKey("meal_type") && "Сніданок".equals(filters.get("meal_type"))
        ));
    }

    @Test
    void testPriceFilters(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        Platform.runLater(() -> {
            Spinner<Double> minPriceSpinner = robot.lookup("#minPriceSpinner").queryAs(Spinner.class);
            Spinner<Double> maxPriceSpinner = robot.lookup("#maxPriceSpinner").queryAs(Spinner.class);
            minPriceSpinner.getValueFactory().setValue(200.0);
            maxPriceSpinner.getValueFactory().setValue(500.0);
        });
        Thread.sleep(200);

        robot.clickOn("#filterButton");
        Thread.sleep(200);

        verify(mealService, times(1)).search(argThat(filters ->
                filters.containsKey("minPrice") &&
                        filters.containsKey("maxPrice") &&
                        Double.valueOf(200.0).equals(filters.get("minPrice")) &&
                        Double.valueOf(500.0).equals(filters.get("maxPrice"))
        ));
    }

    @Test
    void testFilterWithSQLExceptions(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        when(mealService.search(any(Map.class)))
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

        verify(controller).showError("Помилка завантаження даних харчування: Database connection error");

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testResetFilters(FxRobot robot) throws InterruptedException {
        waitForFxEvents();

        robot.clickOn("#keywordField").write("Test");
        Platform.runLater(() -> {
            ComboBox<String> mealTypeCombo = robot.lookup("#mealTypeFilterCombo").queryAs(ComboBox.class);
            mealTypeCombo.setValue("Сніданок");
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
                ComboBox<String> mealTypeCombo = robot.lookup("#mealTypeFilterCombo").queryAs(ComboBox.class);
                Spinner<Double> minPriceSpinner = robot.lookup("#minPriceSpinner").queryAs(Spinner.class);
                Spinner<Double> maxPriceSpinner = robot.lookup("#maxPriceSpinner").queryAs(Spinner.class);

                assertTrue(keywordField.getText().isEmpty());
                assertEquals("Всі", mealTypeCombo.getValue());
                assertEquals(0.0, (Double) minPriceSpinner.getValue());
                assertEquals(1000.0, (Double) maxPriceSpinner.getValue());
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
    void testAddNewMeal(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        waitForFxEvents();

        robot.clickOn("#addMealButton");
        Thread.sleep(200);

        verify(controllerFactory).createMealEditController(null);
    }

    @Test
    void testEditSelectedMeal(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Thread.sleep(500);
        Platform.runLater(() -> {
            TableView<Meal> mealTable = robot.lookup("#mealTable").queryAs(TableView.class);
            mealTable.getItems().add(testMeal1);
            mealTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        robot.clickOn("#editMealButton");
        verify(controllerFactory).createMealEditController(any(Meal.class));

        reset(controllerFactory);
        Platform.runLater(() -> {
            TableView<Meal> mealTable = robot.lookup("#mealTable").queryAs(TableView.class);
            mealTable.getSelectionModel().clearSelection();
        });

        robot.clickOn("#editMealButton");
        verify(controllerFactory, never()).createMealEditController(any(Meal.class));
    }

    @Test
    void testDeleteSelectedMeal(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);

        Platform.runLater(() -> {
            TableView<Meal> mealTable = robot.lookup("#mealTable").queryAs(TableView.class);
            mealTable.getItems().add(testMeal1);
            mealTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);
        doNothing().when(controller).showInfo(anyString());

        robot.clickOn("#deleteMealButton");
        Thread.sleep(200);

        HelperMethods.clickOnYes(robot);
        Thread.sleep(200);

        verify(mealService).delete(1);
        verify(controller).showInfo(contains("успішно видалено"));
        verify(logger).info(contains("Видалено харчування"), eq(testMeal1.toString()));
    }

    @Test
    void testDeleteSelectedMeal_withoutSelection(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        doNothing().when(controller).showError(anyString());

        Platform.runLater(() -> {
            TableView<Meal> mealTable = robot.lookup("#mealTable").queryAs(TableView.class);
            mealTable.getSelectionModel().clearSelection();
        });

        Thread.sleep(200);
        robot.clickOn("#deleteMealButton");
        Thread.sleep(200);

        verify(controller).showError("Виберіть харчування для видалення");
        verify(mealService, never()).delete(anyInt());
    }

    @Test
    void testAddNewMealType(FxRobot robot) throws InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        waitForFxEvents();

        robot.clickOn("#addMealTypeButton");
        Thread.sleep(200);

        verify(controllerFactory).createMealTypeEditController(null);
    }

    @Test
    void testEditSelectedMealType(FxRobot robot) throws SQLException, InterruptedException {
        when(userSession.isAdmin()).thenReturn(true);
        waitForFxEvents();

        robot.clickOn("#editMealTypeButton");
        Thread.sleep(500);

        verifyThat(".dialog-pane", isVisible());
        robot.clickOn("OK");
        Thread.sleep(200);

        verify(mealTypeService).getAll();
        verify(controllerFactory).createMealTypeEditController(any(MealType.class));
    }

    @Test
    void testEditSelectedMealType_withEmptyList() throws SQLException {
        when(userSession.isAdmin()).thenReturn(true);
        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());
        doNothing().when(controller).showInfo(anyString());

        controller.editSelectedMealType();

        verify(controller).showInfo("Типи харчування відсутні");
        verify(controllerFactory, never()).createMealTypeEditController(any());
    }

    @Test
    void testShowMealDetails(FxRobot robot) throws SQLException {
        Meal detailedMeal = new Meal();
        detailedMeal.setId(1);
        detailedMeal.setName("Континентальний сніданок");
        detailedMeal.setMealsPerDay(1);
        detailedMeal.setCostPerDay(250.0);
        detailedMeal.setMealTypes(Arrays.asList(testMealType1));

        when(mealService.getById(1)).thenReturn(detailedMeal);

        Platform.runLater(() -> {
            controller.showMealDetails(testMeal1);
        });
        waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        verify(mealService).getById(1);

        robot.clickOn("OK");
        waitForFxEvents();
    }

    @Test
    void testTableInteractions(FxRobot robot) {
        waitForFxEvents();

        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView<Meal> mealTable = robot.lookup("#mealTable").queryAs(TableView.class);
                mealTable.getItems().clear();
                mealTable.getItems().addAll(testMeal1, testMeal2);
                mealTable.refresh();
            } finally {
                setupLatch.countDown();
            }
        });

        try {
            robot.clickOn(".table-row-cell");
            robot.doubleClickOn(".table-row-cell");
            Thread.sleep(200);
            closeOpenDialogs(robot);

            verify(mealService, atLeastOnce()).getById(anyInt());
        } catch (Exception e) {
            closeOpenDialogs(robot);
        }
    }

    @Test
    void testInitializationWithExceptions() throws SQLException {
        when(mealService.search(any(Map.class))).thenThrow(new SQLException("Database error"));
        when(mealTypeService.getAll()).thenThrow(new SQLException("Database error"));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                MealController errorController = new MealController(
                        stage, sessionManager, mealService, mealTypeService, mealMealTypeService, controllerFactory);
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
    void testLoadMealsWithEmptyResult(FxRobot robot) throws SQLException, InterruptedException {
        when(mealService.search(any(Map.class))).thenReturn(Collections.emptyList());
        doNothing().when(controller).showInfo(anyString());

        waitForFxEvents();
        robot.clickOn("#filterButton");
        Thread.sleep(200);

        verify(controller).showInfo("За вказаними критеріями харчування не знайдено");
    }

    @Test
    void testTableRowDoubleClick(FxRobot robot) {
        Platform.runLater(() -> {
            controller.mealTable.setItems(FXCollections.observableArrayList(Arrays.asList(testMeal1, testMeal2)));
        });
        waitForFxEvents();

        TableRow<Meal> firstRow = robot.lookup(".table-row-cell").nth(0).query();
        robot.doubleClickOn(firstRow);
        waitForFxEvents();

        verifyThat(".dialog-pane", NodeMatchers.isVisible());
    }
}