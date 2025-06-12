package tourapp.view.user_controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.util.SessionManager;
import tourapp.view.HelperMethods;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class UserEditControllerTest {

    @Mock private UserService userService;
    @Mock private UserTypeService userTypeService;
    @Mock private SessionManager sessionManager;
    @Mock private SessionManager.UserSession userSession;
    @Mock private User mockUser;
    @Mock private UserType mockUserType;

    private UserEditController editController;
    private UserEditController createController;
    private Stage primaryStage;
    private UserType testUserType1;
    private UserType testUserType2;
    private UserType adminUserType;
    private User userToEdit;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;
        setupTestData();
        setupMocks();

        editController = spy(new UserEditController(primaryStage, sessionManager, userService, userTypeService, userToEdit));
        editController.setOnSave(() -> {});
        editController.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(userService, userTypeService, sessionManager, userSession, mockUser, mockUserType);
        setupTestData();
        setupMocks();

        if (editController != null) {
            editController.setOnSave(() -> {});
        }
    }

    private void setupTestData() {
        testUserType1 = new UserType();
        testUserType1.setId(1);
        testUserType1.setName("USER");

        testUserType2 = new UserType();
        testUserType2.setId(2);
        testUserType2.setName("MANAGER");

        adminUserType = new UserType();
        adminUserType.setId(3);
        adminUserType.setName("ADMIN");

        userToEdit = new User();
        userToEdit.setId(1);
        userToEdit.setName("Іван Петренко");
        userToEdit.setEmail("ivan@test.com");
        userToEdit.setUserType(testUserType1);
    }

    private void setupMocks() throws SQLException {
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

        when(userTypeService.getAll()).thenReturn(List.of(testUserType1, testUserType2, adminUserType));
        when(userService.getById(1)).thenReturn(userToEdit);
        when(userService.findByEmail("ivan@test.com")).thenReturn(userToEdit);
    }

    @Test
    void testEditModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Редагування користувача", editController.editStage.getTitle());
    }

    @Test
    void testEditModeFieldsPrePopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
        ComboBox<UserType> userTypeComboBox = robot.lookup("#userTypeComboBox").queryAs(ComboBox.class);
        CheckBox changePasswordCheckBox = robot.lookup("#changePasswordCheckBox").queryAs(CheckBox.class);

        verifyThat(nameField, hasText("Іван Петренко"));
        verifyThat(emailField, hasText("ivan@test.com"));
        assertEquals(testUserType1, userTypeComboBox.getValue());
        assertTrue(changePasswordCheckBox.isVisible());
        assertFalse(changePasswordCheckBox.isSelected());
    }

    @Test
    void testUserTypeComboBoxPopulatedInEditMode(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<UserType> userTypeComboBox = robot.lookup("#userTypeComboBox").queryAs(ComboBox.class);

        assertEquals(3, userTypeComboBox.getItems().size());
        assertEquals("USER", userTypeComboBox.getItems().get(0).getName());
        assertEquals("MANAGER", userTypeComboBox.getItems().get(1).getName());
        assertEquals("ADMIN", userTypeComboBox.getItems().get(2).getName());
        assertEquals(testUserType1, userTypeComboBox.getValue());
    }

    @Test
    void testPasswordFieldsHiddenByDefaultInEditMode(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        PasswordField passwordField = robot.lookup("#passwordField").queryAs(PasswordField.class);
        PasswordField confirmPasswordField = robot.lookup("#confirmPasswordField").queryAs(PasswordField.class);
        Label passwordLabel = robot.lookup("#passwordLabel").queryAs(Label.class);
        Label confirmPasswordLabel = robot.lookup("#confirmPasswordLabel").queryAs(Label.class);

        assertFalse(passwordField.isVisible());
        assertFalse(confirmPasswordField.isVisible());
        assertFalse(passwordLabel.isVisible());
        assertFalse(confirmPasswordLabel.isVisible());
    }

    @Test
    void testPasswordFieldsVisibleWhenChangePasswordChecked(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            CheckBox changePasswordCheckBox = robot.lookup("#changePasswordCheckBox").queryAs(CheckBox.class);
            changePasswordCheckBox.setSelected(true);
        });

        WaitForAsyncUtils.waitForFxEvents();

        PasswordField passwordField = robot.lookup("#passwordField").queryAs(PasswordField.class);
        PasswordField confirmPasswordField = robot.lookup("#confirmPasswordField").queryAs(PasswordField.class);
        Label passwordLabel = robot.lookup("#passwordLabel").queryAs(Label.class);
        Label confirmPasswordLabel = robot.lookup("#confirmPasswordLabel").queryAs(Label.class);

        assertTrue(passwordField.isVisible());
        assertTrue(confirmPasswordField.isVisible());
        assertTrue(passwordLabel.isVisible());
        assertTrue(confirmPasswordLabel.isVisible());
    }

    @Test
    void testSuccessfulUpdateExistingUser(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(userService.update(any(User.class))).thenReturn(true);
        when(userService.findByEmail("updated@test.com")).thenReturn(null);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        editController.setOnSave(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
            ComboBox<UserType> userTypeComboBox = robot.lookup("#userTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Оновлений Користувач");
            emailField.setText("updated@test.com");
            userTypeComboBox.setValue(testUserType2);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(userService, timeout(3000)).update(argThat(user ->
                user.getId() == 1 &&
                        user.getName().equals("Оновлений Користувач") &&
                        user.getEmail().equals("updated@test.com") &&
                        user.getUserType().equals(testUserType2)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyNameInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, never()).update(any(User.class));
    }

    @Test
    void testValidationErrorForEmptyEmailInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
            emailField.setText("");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, never()).update(any(User.class));
    }

    @Test
    void testValidationErrorForEmptyUserTypeInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ComboBox<UserType> userTypeComboBox = robot.lookup("#userTypeComboBox").queryAs(ComboBox.class);
            userTypeComboBox.setValue(null);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, never()).update(any(User.class));
    }

    @Test
    void testDuplicateEmailValidationExcludesCurrentUser(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(userService.update(any(User.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        editController.setOnSave(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(userService, timeout(3000)).update(any(User.class));
        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testDuplicateEmailValidationDetectsOtherDuplicates(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setEmail("other@test.com");

        when(userService.findByEmail("other@test.com")).thenReturn(anotherUser);

        Platform.runLater(() -> {
            TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
            emailField.setText("other@test.com");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, never()).update(any(User.class));
    }

    @Test
    void testCancelButtonInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Змінене ім'я");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, never()).update(any(User.class));
    }

    @Test
    void testCreateModeSetup(FxRobot robot) throws SQLException {
        Platform.runLater(() -> {
            if (editController.editStage.isShowing()) {
                editController.editStage.close();
            }

            createController = new UserEditController(primaryStage, sessionManager, userService, userTypeService, null);
            createController.setOnSave(() -> {});
            createController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Створення користувача", createController.editStage.getTitle());

        CheckBox changePasswordCheckBox = robot.lookup("#changePasswordCheckBox").queryAs(CheckBox.class);
        PasswordField passwordField = robot.lookup("#passwordField").queryAs(PasswordField.class);
        PasswordField confirmPasswordField = robot.lookup("#confirmPasswordField").queryAs(PasswordField.class);

        assertFalse(changePasswordCheckBox.isVisible());
        assertTrue(passwordField.isVisible());
        assertTrue(confirmPasswordField.isVisible());
    }

    @Test
    void testSuccessfulCreateNewUser(FxRobot robot) throws SQLException, InterruptedException {
        Platform.runLater(() -> {
            if (editController.editStage.isShowing()) {
                editController.editStage.close();
            }

            createController = new UserEditController(primaryStage, sessionManager, userService, userTypeService, null);
            createController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        when(userService.create(any(User.class))).thenReturn(true);
        when(userService.findByEmail("new@test.com")).thenReturn(null);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        createController.setOnSave(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
            ComboBox<UserType> userTypeComboBox = robot.lookup("#userTypeComboBox").queryAs(ComboBox.class);
            PasswordField passwordField = robot.lookup("#passwordField").queryAs(PasswordField.class);
            PasswordField confirmPasswordField = robot.lookup("#confirmPasswordField").queryAs(PasswordField.class);

            nameField.setText("Новий Користувач");
            emailField.setText("new@test.com");
            userTypeComboBox.setValue(testUserType1);
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(userService, timeout(3000)).create(argThat(user ->
                user.getName().equals("Новий Користувач") &&
                        user.getEmail().equals("new@test.com") &&
                        user.getUserType().equals(testUserType1)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testCreateModeRequiresPassword(FxRobot robot) throws SQLException {
        Platform.runLater(() -> {
            if (editController.editStage.isShowing()) {
                editController.editStage.close();
            }

            createController = new UserEditController(primaryStage, sessionManager, userService, userTypeService, null);
            createController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);
            ComboBox<UserType> userTypeComboBox = robot.lookup("#userTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Новий Користувач");
            emailField.setText("new@test.com");
            userTypeComboBox.setValue(testUserType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, never()).create(any(User.class));
    }

    @Test
    void testNonAdminCannotSeeAdminUserType(FxRobot robot) {
        Platform.runLater(() -> {
            if (editController.editStage.isShowing()) {
                editController.editStage.close();
            }
        });

        WaitForAsyncUtils.waitForFxEvents();

        when(userSession.isAdmin()).thenReturn(false);
        when(userSession.isManager()).thenReturn(true);
        when(userSession.isCustomer()).thenReturn(false);

        Platform.runLater(() -> {
            UserEditController nonAdminController = new UserEditController(
                    primaryStage, sessionManager, userService, userTypeService, userToEdit);
            nonAdminController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<UserType> userTypeComboBox = robot.lookup("#userTypeComboBox").queryAs(ComboBox.class);

        assertEquals(2, userTypeComboBox.getItems().size());
        assertTrue(userTypeComboBox.getItems().stream().noneMatch(type -> type.getName().equals("ADMIN")));
    }

    @Test
    void testTrimWhitespaceOnSave(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(userService.update(any(User.class))).thenReturn(true);
        when(userService.findByEmail("trimmed@test.com")).thenReturn(null);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        editController.setOnSave(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField emailField = robot.lookup("#emailField").queryAs(TextField.class);

            nameField.setText("  Ім'я з пробілами  ");
            emailField.setText("  trimmed@test.com  ");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(userService, timeout(3000)).update(argThat(user ->
                user.getName().equals("Ім'я з пробілами") &&
                        user.getEmail().equals("trimmed@test.com")));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testSQLExceptionHandlingOnUpdate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database error"))
                .when(userService).update(any(User.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Оновлене ім'я");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, timeout(3000)).update(any(User.class));
    }

    @Test
    void testUserIdPreservedOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(userService.update(any(User.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        editController.setOnSave(callbackLatch::countDown);

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Нове ім'я");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(userService, timeout(3000)).update(argThat(user ->
                user.getId() == 1));
    }

    @Test
    void testLoadUserDataWithNullUser(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(userService.getById(1)).thenReturn(null);

        Platform.runLater(() -> {
            editController.editStage.close();
            UserEditController newController = new UserEditController(
                    primaryStage, sessionManager, userService, userTypeService, userToEdit);
            newController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        verify(userService, timeout(3000)).getById(1);
    }

    @Test
    void testSessionAccessibilityChecks() {
        assertTrue(sessionManager.hasActiveSession());

        SessionManager.UserSession session = sessionManager.getCurrentSession();
        assertNotNull(session);
        assertTrue(session.isAdmin());
        assertFalse(session.isManager());
        assertFalse(session.isCustomer());

        User sessionUser = session.user();
        assertNotNull(sessionUser);
        assertTrue(sessionUser.isAdmin());

        verify(sessionManager, atLeastOnce()).getCurrentSession();
    }

    @Test
    void testCallbackExecutionDiagnosticForUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(userService.update(any(User.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        AtomicBoolean callbackSet = new AtomicBoolean(false);

        editController.setOnSave(() -> {
            System.out.println("=== USER UPDATE CALLBACK EXECUTED ===");
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });
        callbackSet.set(true);

        System.out.println("User update callback set: " + callbackSet.get());

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Тест callback оновлення");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "User update callback was not executed within timeout");

        verify(userService, timeout(3000)).update(any(User.class));
        System.out.println("User update callback executed: " + callbackExecuted.get());
        System.out.println("User update stage showing: " + editController.editStage.isShowing());

        assertTrue(callbackExecuted.get(), "User update callback was not executed even with diagnostic");
    }
}