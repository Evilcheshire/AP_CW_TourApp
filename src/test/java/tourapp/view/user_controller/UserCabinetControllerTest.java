package tourapp.view.user_controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseController;
import tourapp.view.NavigationController;
import tourapp.view.AdminPanelController;
import tourapp.view.auth_controller.LoginController;
import tourapp.view.tour_controller.BookedToursController;
import tourapp.view.tour_controller.DashboardController;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class UserCabinetControllerTest {

    private UserCabinetController controller;

    @Mock private UserService userService;
    @Mock private SessionManager sessionManager;
    @Mock private ControllerFactory controllerFactory;
    @Mock private NavigationController navigationController;
    @Mock private DashboardController dashboardController;
    @Mock private BookedToursController bookedToursController;
    @Mock private AdminPanelController adminPanelController;
    @Mock private LoginController loginController;
    @Mock private Logger logger;

    private Stage stage;
    private User testUser;
    private UserType adminUserType;
    private UserType customerUserType;
    private SessionManager.UserSession userSession;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);
        setupTestData();
        setupMocks();

        controller = spy(new UserCabinetController(stage, sessionManager, userService, controllerFactory));
        BaseController.logger = logger;

        controller.show();
    }

    @BeforeEach
    void setUp() {
        if (controller != null) {
            reset(userService, controllerFactory, navigationController,
                    dashboardController, bookedToursController, adminPanelController,
                    loginController);
            setupMocks();
        }
    }

    private void setupTestData() {
        adminUserType = new UserType();
        adminUserType.setId(1);
        adminUserType.setName("ADMIN");

        customerUserType = new UserType();
        customerUserType.setId(2);
        customerUserType.setName("CUSTOMER");

        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setUserType(adminUserType);

        userSession = new SessionManager.UserSession(testUser);
    }

    private void setupMocks() {
        try {
            when(sessionManager.getCurrentSession()).thenReturn(userSession);
            when(sessionManager.hasActiveSession()).thenReturn(true);

            when(controllerFactory.createNavigationController()).thenReturn(navigationController);
            when(navigationController.createNavigationBar(anyString())).thenReturn(new HBox());
            when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
            when(controllerFactory.createBookedToursController()).thenReturn(bookedToursController);
            when(controllerFactory.createAdminPanelController()).thenReturn(adminPanelController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);

            when(userService.getById(1)).thenReturn(testUser);

        } catch (Exception e) {
            System.out.println("Setup failed: " + e.getMessage());
            e.printStackTrace();
        }
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
    void testShowMethod() {
        UserCabinetController testController = spy(new UserCabinetController(stage, sessionManager, userService, controllerFactory));

        doNothing().when(testController).loadAndShow(anyString(), anyString());

        testController.show();

        verify(testController).loadAndShow("/tourapp/view/user/userCabinet.fxml", "TourApp - Особистий кабінет");
    }

    @Test
    void testControllerInitialization(FxRobot robot) {
        waitForFxEvents();

        assertNotNull(stage.getScene());
        assertTrue(stage.isShowing());
        assertEquals("TourApp - Особистий кабінет", stage.getTitle());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                BorderPane mainLayout = robot.lookup("#mainLayout").queryAs(BorderPane.class);
                TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
                TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
                Label userTypeLabel = robot.lookup("#userTypeLabel").queryAs(Label.class);
                PasswordField currentPasswordField = robot.lookup("#currentPasswordField").queryAs(PasswordField.class);
                PasswordField newPasswordField = robot.lookup("#newPasswordField").queryAs(PasswordField.class);
                PasswordField confirmPasswordField = robot.lookup("#confirmPasswordField").queryAs(PasswordField.class);
                Button updateProfileButton = robot.lookup("#updateProfileButton").queryAs(Button.class);
                Button changePasswordButton = robot.lookup("#changePasswordButton").queryAs(Button.class);

                assertNotNull(mainLayout, "Main layout should not be null");
                assertNotNull(nameField, "Name field should not be null");
                assertNotNull(emailField, "Email field should not be null");
                assertNotNull(userTypeLabel, "User type label should not be null");
                assertNotNull(currentPasswordField, "Current password field should not be null");
                assertNotNull(newPasswordField, "New password field should not be null");
                assertNotNull(confirmPasswordField, "Confirm password field should not be null");
                assertNotNull(updateProfileButton, "Update profile button should not be null");
                assertNotNull(changePasswordButton, "Change password button should not be null");

                assertEquals("Test User", nameField.getText());
                assertEquals("test@example.com", emailField.getText());
                assertEquals("ADMIN", userTypeLabel.getText());
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
        when(navigationController.createNavigationBar(NavigationController.PAGE_PROFILE))
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
        verify(navigationController).createNavigationBar(NavigationController.PAGE_PROFILE);
        verify(navigationController).setOnLogout(any(Runnable.class));
        verify(navigationController).setOnExit(any(Runnable.class));
        verify(navigationController).setOnSearch(any(Runnable.class));
        verify(navigationController).setOnBooked(any(Runnable.class));
        verify(navigationController).setOnAdminPanel(any(Runnable.class));
    }

    @Test
    void testNavigationBarInitialization() {
        HBox mockNavBar = new HBox();
        when(navigationController.createNavigationBar(NavigationController.PAGE_PROFILE))
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
        verify(navigationController).createNavigationBar(NavigationController.PAGE_PROFILE);
        verify(navigationController).setOnLogout(any(Runnable.class));
        verify(navigationController).setOnExit(any(Runnable.class));
        verify(navigationController).setOnSearch(any(Runnable.class));
        verify(navigationController).setOnBooked(any(Runnable.class));
        verify(navigationController).setOnAdminPanel(any(Runnable.class));
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
    }

    @Test
    void testLoadUserDataSuccess(FxRobot robot) throws SQLException {

        when(userService.getById(1)).thenReturn(testUser);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadUserData();

                TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
                TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
                Label userTypeLabel = robot.lookup("#userTypeLabel").queryAs(Label.class);

                assertEquals("Test User", nameField.getText());
                assertEquals("test@example.com", emailField.getText());
                assertEquals("ADMIN", userTypeLabel.getText());
            } finally {
                latch.countDown();
            }
        });

        assertEquals(testUser, userSession.user());
    }

    @Test
    void testLoadUserDataWithSQLException() throws SQLException {
        when(userService.getById(1)).thenThrow(new SQLException("Database error"));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadUserData();
            } finally {
                latch.countDown();
            }
        });
    }

    @Test
    void testLoadUserDataWithNullUserFromService(FxRobot robot) throws SQLException {
        when(userService.getById(1)).thenReturn(null);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadUserData();

                TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
                TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);

                assertEquals("Test User", nameField.getText());
                assertEquals("test@example.com", emailField.getText());
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(userService).getById(1);
    }

    @Test
    void testHandleUpdateProfileSuccess(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validateRegisterForm(any(), any(), any(), any()))
                    .thenReturn(true);

            when(userService.findByEmail("newemail@example.com")).thenReturn(null);
            when(userService.update(any(User.class))).thenReturn(true);

            robot.clickOn("#nameField").eraseText(20).write("New Name");
            robot.clickOn("#emailField").eraseText(30).write("newemail@example.com");

            Thread.sleep(200);

            robot.clickOn("#updateProfileButton");
            Thread.sleep(500);

            verify(userService).findByEmail("newemail@example.com");
            verify(userService).update(any(User.class));
        }
    }

    @Test
    void testHandleUpdateProfileValidationFailed(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validateRegisterForm(any(), any(), any(), any()))
                    .thenReturn(false);

            robot.clickOn("#nameField").eraseText(20).write("");
            robot.clickOn("#emailField").eraseText(30).write("invalid-email");

            Thread.sleep(200);

            robot.clickOn("#updateProfileButton");
            Thread.sleep(200);

            verify(userService, never()).findByEmail(anyString());
            verify(userService, never()).update(any(User.class));
        }
    }

    @Test
    void testHandleUpdateProfileEmailAlreadyExists(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validateRegisterForm(any(), any(), any(), any()))
                    .thenReturn(true);

            User existingUser = new User();
            existingUser.setId(2);
            existingUser.setEmail("existing@example.com");

            when(userService.findByEmail("existing@example.com")).thenReturn(existingUser);

            robot.clickOn("#nameField").eraseText(20).write("New Name");
            robot.clickOn("#emailField").eraseText(30).write("existing@example.com");

            Thread.sleep(200);

            robot.clickOn("#updateProfileButton");
            Thread.sleep(200);

            verify(userService).findByEmail("existing@example.com");
            verify(userService, never()).update(any(User.class));
        }
    }

    @Test
    void testHandleChangePasswordSuccess(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validatePasswordChangeForm(any(), any(), any()))
                    .thenReturn(true);

            when(userService.authenticate("test@example.com", "currentpass")).thenReturn(testUser);
            when(userService.changePassword(1, "newpass")).thenReturn(true);

            robot.clickOn("#currentPasswordField").write("currentpass");
            robot.clickOn("#newPasswordField").write("newpass");
            robot.clickOn("#confirmPasswordField").write("newpass");

            Thread.sleep(200);

            robot.clickOn("#changePasswordButton");
            Thread.sleep(500);
        }
    }

    @Test
    void testNavigationLogoutCallback() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initializeNavigationBar();

                ArgumentCaptor<Runnable> logoutCaptor = ArgumentCaptor.forClass(Runnable.class);
                verify(navigationController).setOnLogout(logoutCaptor.capture());

                Runnable logoutCallback = logoutCaptor.getValue();
                logoutCallback.run();

                verify(sessionManager).endSession();
                verify(controllerFactory).createLoginController();
                verify(loginController).show();
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
    void testNavigationAdminPanelCallbackWithAdminAccess() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                doReturn(true).when(controller).isAdmin();
                doReturn(false).when(controller).isManager();

                controller.initializeNavigationBar();

                ArgumentCaptor<Runnable> adminPanelCaptor = ArgumentCaptor.forClass(Runnable.class);
                verify(navigationController).setOnAdminPanel(adminPanelCaptor.capture());

                Runnable adminPanelCallback = adminPanelCaptor.getValue();
                adminPanelCallback.run();

                verify(controllerFactory).createAdminPanelController();
                verify(adminPanelController).show();
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
    void testNavigationAdminPanelCallbackWithoutAccess(FxRobot robot) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                doReturn(false).when(controller).isAdmin();
                doReturn(false).when(controller).isManager();

                controller.initializeNavigationBar();

                ArgumentCaptor<Runnable> adminPanelCaptor = ArgumentCaptor.forClass(Runnable.class);
                verify(navigationController).setOnAdminPanel(adminPanelCaptor.capture());

                Runnable adminPanelCallback = adminPanelCaptor.getValue();
                adminPanelCallback.run();

                verify(controllerFactory, never()).createAdminPanelController();
                verify(adminPanelController, never()).show();
            } finally {
                latch.countDown();
            }
        });
    }

    @Test
    void testHandleChangePasswordValidationFailed(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validatePasswordChangeForm(any(), any(), any()))
                    .thenReturn(false);

            robot.clickOn("#currentPasswordField").write("weak");
            robot.clickOn("#newPasswordField").write("123");
            robot.clickOn("#confirmPasswordField").write("456");

            Thread.sleep(200);

            robot.clickOn("#changePasswordButton");
            Thread.sleep(200);

            verify(userService, never()).authenticate(anyString(), anyString());
            verify(userService, never()).changePassword(anyInt(), anyString());
        }
    }

    @Test
    void testHandleChangePasswordIncorrectCurrentPassword(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validatePasswordChangeForm(any(), any(), any()))
                    .thenReturn(true);

            when(userService.authenticate("test@example.com", "wrongpass")).thenReturn(null);

            robot.clickOn("#currentPasswordField").write("wrongpass");
            robot.clickOn("#newPasswordField").write("newpass1");
            robot.clickOn("#confirmPasswordField").write("newpass1");

            Thread.sleep(200);

            robot.clickOn("#changePasswordButton");
            Thread.sleep(500);

            verify(userService).authenticate("test@example.com", "wrongpass");
            verify(userService, never()).changePassword(anyInt(), anyString());

            mockedValidator.verify(() -> FormValidator.addError(any(PasswordField.class), eq("Поточний пароль невірний")));
        }
    }

    @Test
    void testHandleChangePasswordServiceFailure(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validatePasswordChangeForm(any(), any(), any()))
                    .thenReturn(true);

            when(userService.authenticate("test@example.com", "currentpass1")).thenReturn(testUser);
            when(userService.changePassword(1, "newpass1")).thenReturn(false);

            robot.clickOn("#currentPasswordField").write("currentpass1");
            robot.clickOn("#newPasswordField").write("newpass1");
            robot.clickOn("#confirmPasswordField").write("newpass1");

            Thread.sleep(200);

            robot.clickOn("#changePasswordButton");
            Thread.sleep(500);

            verify(userService).authenticate("test@example.com", "currentpass1");
            verify(userService).changePassword(1, "newpass1");

            PasswordField currentPasswordField = robot.lookup("#currentPasswordField").queryAs(PasswordField.class);
            PasswordField newPasswordField = robot.lookup("#newPasswordField").queryAs(PasswordField.class);
            PasswordField confirmPasswordField = robot.lookup("#confirmPasswordField").queryAs(PasswordField.class);

            verify(controller).showError("Не вдалося змінити пароль");
            assertEquals("currentpass1", currentPasswordField.getText());
            assertEquals("newpass1", newPasswordField.getText());
            assertEquals("newpass1", confirmPasswordField.getText());
        }
    }

    @Test
    void testHandleChangePasswordSQLException(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validatePasswordChangeForm(any(), any(), any()))
                    .thenReturn(true);

            when(userService.authenticate("test@example.com", "currentpass1"))
                    .thenThrow(new SQLException("Database connection error"));

            robot.clickOn("#currentPasswordField").write("currentpass1");
            robot.clickOn("#newPasswordField").write("newpass1");
            robot.clickOn("#confirmPasswordField").write("newpass1");

            Thread.sleep(200);

            robot.clickOn("#changePasswordButton");
            Thread.sleep(500);

            verify(userService).authenticate("test@example.com", "currentpass1");
            verify(userService, never()).changePassword(anyInt(), anyString());
        }
    }

    @Test
    void testHandleChangePasswordChangePasswordSQLException(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validatePasswordChangeForm(any(), any(), any()))
                    .thenReturn(true);

            when(userService.authenticate("test@example.com", "currentpass1")).thenReturn(testUser);
            when(userService.changePassword(1, "newpass1"))
                    .thenThrow(new SQLException("Database update error"));

            robot.clickOn("#currentPasswordField").write("currentpass1");
            robot.clickOn("#newPasswordField").write("newpass1");
            robot.clickOn("#confirmPasswordField").write("newpass1");

            Thread.sleep(200);

            robot.clickOn("#changePasswordButton");
            Thread.sleep(500);

            verify(controller).showError("Помилка зміни паролю: Database update error");
            verify(userService).changePassword(1, "newpass1");
        }
    }

    @Test
    void testHandleChangePasswordEmptyFields(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validatePasswordChangeForm(any(), any(), any()))
                    .thenReturn(false);

            robot.clickOn("#changePasswordButton");
            Thread.sleep(200);

            verify(userService, never()).changePassword(anyInt(), anyString());
        }
    }

    @Test
    void testHandleUpdateProfileWithSameEmail(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validateRegisterForm(any(), any(), any(), any()))
                    .thenReturn(true);

            when(userService.findByEmail("test@example.com")).thenReturn(testUser);
            when(userService.update(any(User.class))).thenReturn(true);

            robot.clickOn("#nameField").eraseText(20).write("Updated Name");
            robot.clickOn("#emailField").eraseText(30).write("test@example.com");

            Thread.sleep(200);

            robot.clickOn("#updateProfileButton");
            Thread.sleep(500);

            verify(userService).findByEmail("test@example.com");
            verify(userService).update(any(User.class));
        }
    }

    @Test
    void testHandleUpdateProfileServiceUpdateFailed(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validateRegisterForm(any(), any(), any(), any()))
                    .thenReturn(true);

            when(userService.findByEmail("newemail@example.com")).thenReturn(null);
            when(userService.update(any(User.class))).thenReturn(false);

            robot.clickOn("#nameField").eraseText(20).write("New Name");
            robot.clickOn("#emailField").eraseText(30).write("newemail@example.com");

            Thread.sleep(200);

            robot.clickOn("#updateProfileButton");
            Thread.sleep(500);

            verify(userService).findByEmail("newemail@example.com");
            verify(userService).update(any(User.class));
            verify(controller).showError("Не вдалося оновити дані профілю");
        }
    }

    @Test
    void testHandleUpdateProfileSQLExceptionOnFindByEmail(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validateRegisterForm(any(), any(), any(), any()))
                    .thenReturn(true);

            when(userService.findByEmail("newemail@example.com"))
                    .thenThrow(new SQLException("Database connection error"));

            robot.clickOn("#nameField").eraseText(20).write("New Name");
            robot.clickOn("#emailField").eraseText(30).write("newemail@example.com");

            Thread.sleep(200);

            robot.clickOn("#updateProfileButton");
            Thread.sleep(500);

            verify(userService).findByEmail("newemail@example.com");
            verify(userService, never()).update(any(User.class));
            verify(controller).showError("Помилка оновлення профілю: Database connection error");
        }
    }

    @Test
    void testHandleUpdateProfileSQLExceptionOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        waitForFxEvents();

        try (MockedStatic<FormValidator> mockedValidator = mockStatic(FormValidator.class)) {
            mockedValidator.when(() -> FormValidator.validateRegisterForm(any(), any(), any(), any()))
                    .thenReturn(true);

            when(userService.findByEmail("newemail@example.com")).thenReturn(null);
            when(userService.update(any(User.class)))
                    .thenThrow(new SQLException("Database update error"));

            robot.clickOn("#nameField").eraseText(20).write("New Name");
            robot.clickOn("#emailField").eraseText(30).write("newemail@example.com");

            Thread.sleep(200);

            robot.clickOn("#updateProfileButton");
            Thread.sleep(500);

            verify(userService).findByEmail("newemail@example.com");
            verify(userService).update(any(User.class));
            verify(controller).showError("Помилка оновлення профілю: Database update error");
        }
    }

    @Test
    void testInitializeProfileTab(FxRobot robot) {
        waitForFxEvents();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Button updateProfileButton = robot.lookup("#updateProfileButton").queryAs(Button.class);
                Button changePasswordButton = robot.lookup("#changePasswordButton").queryAs(Button.class);

                assertNotNull(updateProfileButton.getOnAction());
                assertNotNull(changePasswordButton.getOnAction());
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