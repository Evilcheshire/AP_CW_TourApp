package tourapp.view.user_controller;

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
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.*;
import tourapp.view.auth_controller.LoginController;
import tourapp.view.tour_controller.BookedToursController;
import tourapp.view.tour_controller.DashboardController;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class UserControllerTest {

    @InjectMocks private UserController controller;

    @Mock private UserService userService;
    @Mock private UserTypeService userTypeService;
    @Mock private SessionManager sessionManager;
    @Mock private ControllerFactory controllerFactory;
    @Mock private SessionManager.UserSession userSession;
    @Mock private User mockCurrentUser;
    @Mock private NavigationController navigationController;
    @Mock private DashboardController dashboardController;
    @Mock private BookedToursController bookedToursController;
    @Mock private AdminPanelController adminPanelController;
    @Mock private UserCabinetController userCabinetController;
    @Mock private LoginController loginController;
    @Mock private Logger logger;

    private Stage stage;
    private User testUser1;
    private User testUser2;
    private UserType adminUserType;
    private UserType customerUserType;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupMocks();
        controller = spy(new UserController(stage, sessionManager, userService, userTypeService, controllerFactory));
        BaseController.logger = logger;
        controller.show();
    }

    @BeforeEach
    void setUp() {
        setupTestData();
        reset(userService, userTypeService, controllerFactory, navigationController,
                dashboardController, bookedToursController, adminPanelController,
                userCabinetController, loginController);
        setupMocks();
    }

    private void setupMocks() {
        try {
            when(sessionManager.getCurrentSession()).thenReturn(userSession);
            when(sessionManager.hasActiveSession()).thenReturn(true);
            when(userSession.user()).thenReturn(mockCurrentUser);
            when(userSession.isAdmin()).thenReturn(true);
            when(userSession.isManager()).thenReturn(false);
            when(userSession.isCustomer()).thenReturn(false);

            when(mockCurrentUser.getId()).thenReturn(1);
            when(mockCurrentUser.getName()).thenReturn("Admin User");
            when(mockCurrentUser.getEmail()).thenReturn("admin@test.com");
            when(mockCurrentUser.getUserType()).thenReturn(adminUserType);
            when(mockCurrentUser.isAdmin()).thenReturn(true);

            setupTestData();

            List<User> users = Arrays.asList(testUser1, testUser2);
            List<UserType> userTypes = Arrays.asList(adminUserType, customerUserType);

            when(userService.getAll()).thenReturn(users);
            when(userService.searchByTerm(anyString())).thenReturn(users);
            when(userService.getById(anyInt())).thenReturn(testUser1);
            when(userTypeService.getAll()).thenReturn(userTypes);

            when(controllerFactory.createNavigationController()).thenReturn(navigationController);
            when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
            when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
            when(controllerFactory.createBookedToursController()).thenReturn(bookedToursController);
            when(controllerFactory.createAdminPanelController()).thenReturn(adminPanelController);
            when(controllerFactory.createUserCabinetController()).thenReturn(userCabinetController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);

            UserEditController mockUserEditController = mock(UserEditController.class);
            when(controllerFactory.createUserEditController(any())).thenReturn(mockUserEditController);

        } catch (Exception e) {
            System.out.println("Setup failed: " + e.getMessage());
        }
    }

    private void setupTestData() {
        adminUserType = new UserType();
        adminUserType.setId(1);
        adminUserType.setName("ADMIN");

        customerUserType = new UserType();
        customerUserType.setId(2);
        customerUserType.setName("CUSTOMER");

        testUser1 = new User();
        testUser1.setId(2);
        testUser1.setName("Test User 1");
        testUser1.setEmail("user1@test.com");
        testUser1.setUserType(customerUserType);

        testUser2 = new User();
        testUser2.setId(3);
        testUser2.setName("Test User 2");
        testUser2.setEmail("user2@test.com");
        testUser2.setUserType(customerUserType);
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
        assertEquals("TourApp - Управління користувачами", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView userTable = robot.lookup("#userTable").queryAs(TableView.class);
                assertNotNull(userTable);

                TextField keywordField = robot.lookup("#keywordField").queryAs(TextField.class);
                ComboBox userTypeCombo = robot.lookup("#userTypeFilterCombo").queryAs(ComboBox.class);
                assertNotNull(keywordField);
                assertNotNull(userTypeCombo);
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
    void testFilterOperations(FxRobot robot) throws Exception {
        waitForFxEvents();

        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(userService, atLeastOnce()).getAll();

        reset(userService);
        when(userService.searchByTerm(anyString())).thenReturn(Arrays.asList(testUser1));

        robot.clickOn("#keywordField").eraseText(10).write("Test User");
        robot.clickOn("#filterButton");
        Thread.sleep(200);
        verify(userService).searchByTerm("Test User");
    }

    @Test
    void testResetFilters(FxRobot robot) throws InterruptedException {
        waitForFxEvents();

        robot.clickOn("#keywordField").write("test");
        Platform.runLater(() -> {
            ComboBox<UserType> userTypeCombo = robot.lookup("#userTypeFilterCombo").queryAs(ComboBox.class);
            userTypeCombo.setValue(customerUserType);
        });
        Thread.sleep(200);

        robot.clickOn("#resetFiltersButton");
        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TextField keywordField = robot.lookup("#keywordField").queryAs(TextField.class);
                ComboBox<UserType> userTypeCombo = robot.lookup("#userTypeFilterCombo").queryAs(ComboBox.class);

                assertTrue(keywordField.getText().isEmpty());
                assertNull(userTypeCombo.getValue());
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
    void testSearchWithEmptyResults(FxRobot robot) throws Exception {
        waitForFxEvents();

        when(userService.searchByTerm(anyString())).thenReturn(Collections.emptyList());

        robot.clickOn("#keywordField").write("nonexistent");
        robot.clickOn("#filterButton");
        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
                assertTrue(userTable.getItems().isEmpty());
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
    void testAddNewUser(FxRobot robot) throws Exception {
        waitForFxEvents();

        UserEditController mockEditController = mock(UserEditController.class);
        when(controllerFactory.createUserEditController(null)).thenReturn(mockEditController);

        robot.clickOn("#addUserButton");
        Thread.sleep(200);

        verify(controllerFactory).createUserEditController(null);
        verify(mockEditController).setOnSave(any(Runnable.class));
        verify(mockEditController).show();
    }

    @Test
    void testEditSelectedUser(FxRobot robot) throws Exception {
        waitForFxEvents();

        Platform.runLater(() -> {
            TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
            userTable.getItems().add(testUser1);
            userTable.getSelectionModel().select(0);
        });
        Thread.sleep(200);

        UserEditController mockEditController = mock(UserEditController.class);
        when(controllerFactory.createUserEditController(any(User.class))).thenReturn(mockEditController);

        robot.clickOn("#editUserButton");
        Thread.sleep(200);

        verify(controllerFactory).createUserEditController(testUser1);
        verify(mockEditController).setOnSave(any(Runnable.class));
        verify(mockEditController).show();
    }

    @Test
    void testEditSelectedUserWithoutSelection(FxRobot robot) throws Exception {
        waitForFxEvents();

        Platform.runLater(() -> {
            TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
            userTable.getSelectionModel().clearSelection();
        });
        Thread.sleep(200);

        robot.clickOn("#editUserButton");
        Thread.sleep(200);

        verify(controllerFactory, never()).createUserEditController(any(User.class));
    }

    @Test
    void testDeleteSelectedUser(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        User testUser = new User();
        testUser.setId(5);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setUserType(customerUserType);

        Platform.runLater(() -> {
            TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
            userTable.getItems().clear();
            userTable.getItems().add(testUser);
            userTable.getSelectionModel().select(0);
        });

        Thread.sleep(200);

        reset(userService);
        when(userService.delete(5)).thenReturn(true);
        when(userService.getAll()).thenReturn(Arrays.asList(testUser1, testUser2));

        robot.clickOn("#deleteUserButton");
        Thread.sleep(200);

        HelperMethods.clickOnOK(robot);
        Thread.sleep(500);

        HelperMethods.clickOnOK(robot);
        Thread.sleep(500);

        verify(userService, timeout(2000)).delete(5);
        verify(logger, timeout(2000)).info(contains("Видалено користувача"), eq(testUser.toString()));
    }

    @Test
    void testDeleteSelectedUserWithoutSelection(FxRobot robot) throws Exception {
        waitForFxEvents();

        Platform.runLater(() -> {
            TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
            userTable.getSelectionModel().clearSelection();
        });
        Thread.sleep(200);

        robot.clickOn("#deleteUserButton");
        Thread.sleep(200);

        verify(userService, never()).delete(anyInt());
    }

    @Test
    void testDeleteCurrentUser_ShouldBeDisabled(FxRobot robot) throws Exception {
        waitForFxEvents();

        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setName("Current User");
        currentUser.setEmail("current@test.com");
        currentUser.setUserType(adminUserType);

        Platform.runLater(() -> {
            TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
            userTable.getItems().clear();
            userTable.getItems().add(currentUser);
            userTable.getSelectionModel().select(0);
        });
        Thread.sleep(300);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Button deleteButton = robot.lookup("#deleteUserButton").queryAs(Button.class);
                assertTrue(deleteButton.isDisabled(), "Delete button should be disabled for current user");
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
    void testDeleteAdminUser_OnlyAllowedByMainAdmin(FxRobot robot) throws Exception {
        waitForFxEvents();

        User adminUser = new User();
        adminUser.setId(5);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setUserType(adminUserType);

        when(mockCurrentUser.getId()).thenReturn(2);

        Platform.runLater(() -> {
            TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
            userTable.getItems().clear();
            userTable.getItems().add(adminUser);
            userTable.getSelectionModel().select(0);
        });
        Thread.sleep(300);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Button deleteButton = robot.lookup("#deleteUserButton").queryAs(Button.class);
                assertTrue(deleteButton.isDisabled(), "Delete button should be disabled for admin users (except main admin)");
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
    void testTableInitialization(FxRobot robot) {
        waitForFxEvents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
                assertEquals(4, userTable.getColumns().size());

                TableColumn<User, Long> idCol = (TableColumn<User, Long>) userTable.getColumns().get(0);
                TableColumn<User, String> nameCol = (TableColumn<User, String>) userTable.getColumns().get(1);
                TableColumn<User, String> emailCol = (TableColumn<User, String>) userTable.getColumns().get(2);
                TableColumn<User, String> typeCol = (TableColumn<User, String>) userTable.getColumns().get(3);

                assertNotNull(idCol.getCellValueFactory());
                assertNotNull(nameCol.getCellValueFactory());
                assertNotNull(emailCol.getCellValueFactory());
                assertNotNull(typeCol.getCellValueFactory());
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
    void testTableSelectionUpdatesButtonStates(FxRobot robot) throws Exception {
        waitForFxEvents();

        Platform.runLater(() -> {
            TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
            userTable.getItems().clear();
            userTable.getItems().add(testUser1);
            userTable.getSelectionModel().select(0);
        });
        Thread.sleep(200);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Button editButton = robot.lookup("#editUserButton").queryAs(Button.class);
                Button deleteButton = robot.lookup("#deleteUserButton").queryAs(Button.class);
                assertFalse(editButton.isDisabled());
                assertFalse(deleteButton.isDisabled());
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
    void testSearchByKeywordAndUserType(FxRobot robot) throws Exception {
        waitForFxEvents();

        User adminUser = new User();
        adminUser.setId(4);
        adminUser.setName("Test Admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setUserType(adminUserType);

        User customerUser = new User();
        customerUser.setId(5);
        customerUser.setName("Test Customer");
        customerUser.setEmail("customer@test.com");
        customerUser.setUserType(customerUserType);

        List<User> searchResults = Arrays.asList(adminUser, customerUser);
        when(userService.searchByTerm("Test")).thenReturn(searchResults);

        robot.clickOn("#keywordField").eraseText(20).write("Test");

        Platform.runLater(() -> {
            ComboBox<UserType> userTypeCombo = robot.lookup("#userTypeFilterCombo").queryAs(ComboBox.class);
            userTypeCombo.setValue(customerUserType);
        });
        Thread.sleep(100);

        robot.clickOn("#filterButton");
        Thread.sleep(200);

        verify(userService).searchByTerm("Test");
        verify(userService, never()).getAll();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
                assertEquals(1, userTable.getItems().size());
                assertEquals(customerUser, userTable.getItems().getFirst());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testSearchWithEmptyKeyword_TreatedAsNoKeyword(FxRobot robot) throws Exception {
        waitForFxEvents();

        User adminUser = new User();
        adminUser.setId(4);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setUserType(adminUserType);

        List<User> allUsers = Arrays.asList(testUser1, testUser2, adminUser);
        when(userService.getAll()).thenReturn(allUsers);

        robot.clickOn("#keywordField").eraseText(20).write("   ");

        Platform.runLater(() -> {
            ComboBox<UserType> userTypeCombo = robot.lookup("#userTypeFilterCombo").queryAs(ComboBox.class);
            userTypeCombo.setValue(adminUserType);
        });
        Thread.sleep(100);

        robot.clickOn("#filterButton");
        Thread.sleep(200);

        verify(userService).getAll();
        verify(userService, never()).searchByTerm(anyString());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TableView<User> userTable = robot.lookup("#userTable").queryAs(TableView.class);
                assertEquals(1, userTable.getItems().size());
                assertEquals(adminUser, userTable.getItems().get(0));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}