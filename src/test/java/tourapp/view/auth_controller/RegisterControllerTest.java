package tourapp.view.auth_controller;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.tour_controller.DashboardController;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class RegisterControllerTest {

    @Mock private UserService userService;
    @Mock private UserTypeService userTypeService;
    @Mock private SessionManager sessionManager;
    @Mock private Stage stage;
    @Mock private ControllerFactory controllerFactory;
    @Mock private User user;
    @Mock private User existingUser;
    @Mock private UserType userType;
    @Mock private DashboardController dashboardController;
    @Mock private LoginController loginController;

    private RegisterController controller;
    private MockedStatic<FormValidator> formValidatorMock;

    @Start
    void start(Stage stage) {
        this.stage = stage;
    }

    @BeforeEach
    void setUp() {
        System.setProperty("testfx.headless", "true");
        MockitoAnnotations.openMocks(this);

        formValidatorMock = mockStatic(FormValidator.class);

        controller = spy(new RegisterController(userService, userTypeService, sessionManager, stage, controllerFactory));
        setupBasicMocks();
    }

    @AfterEach
    void tearDown() {
        if (formValidatorMock != null) {
            formValidatorMock.close();
        }
    }

    private void setupBasicMocks() {
        try {
            doNothing().when(controller).showError(anyString());
            doNothing().when(controller).showInfo(anyString());
            doNothing().when(controller).loadAndShow(anyString(), anyString());

            when(controllerFactory.createDashboardController()).thenReturn(dashboardController);
            when(controllerFactory.createLoginController()).thenReturn(loginController);
            doNothing().when(dashboardController).show();
            doNothing().when(loginController).show();

            when(user.toString()).thenReturn("User{id=1, name='TestUser', email='test@example.com'}");
            when(userType.getName()).thenReturn("CUSTOMER");
        } catch (Exception e) {
        }
    }

    @Test
    void testShow() {
        doNothing().when(controller).loadAndShow(anyString(), anyString());

        controller.show();

        verify(controller).loadAndShow("/tourapp/view/auth/register.fxml", "Реєстрація");
    }

    @Test
    void shouldRegisterSuccessfullyWithValidData() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("test@example.com")).thenReturn(null);
        when(userTypeService.findByExactName("CUSTOMER")).thenReturn(userType);
        when(userService.create(any(User.class))).thenReturn(true);
        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleRegister();

        // Then
        verify(userService).findByEmail("test@example.com");
        verify(userTypeService).findByExactName("CUSTOMER");
        verify(userService).create(any(User.class));
        verify(userService).authenticate("test@example.com", "password123");
        verify(sessionManager).startSession(user);
        verify(controller).showInfo("Реєстрація успішна!");
        verify(controllerFactory).createDashboardController();
        verify(dashboardController).show();
        verify(controller, never()).showError(anyString());
    }

    @Test
    void shouldShowErrorWhenUserAlreadyExists() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "existing@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("existing@example.com")).thenReturn(existingUser);

        // When
        controller.handleRegister();

        // Then
        verify(userService).findByEmail("existing@example.com");
        verify(controller).showError("Користувач з такою поштою вже існує.");
        verify(userTypeService, never()).findByExactName(anyString());
        verify(userService, never()).create(any(User.class));
    }

    @Test
    void shouldShowErrorWhenUserTypeNotFound() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("test@example.com")).thenReturn(null);
        when(userTypeService.findByExactName("CUSTOMER")).thenReturn(null);

        // When
        controller.handleRegister();

        // Then
        verify(userService).findByEmail("test@example.com");
        verify(userTypeService).findByExactName("CUSTOMER");
        verify(controller).showError("Роль користувача не знайдена. Зверніться до адміністратора.");
        verify(userService, never()).create(any(User.class));
    }

    @Test
    void shouldShowErrorWhenUserCreationFails() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("test@example.com")).thenReturn(null);
        when(userTypeService.findByExactName("CUSTOMER")).thenReturn(userType);
        when(userService.create(any(User.class))).thenReturn(false);

        // When
        controller.handleRegister();

        // Then
        verify(userService).create(any(User.class));
        verify(controller).showError("Не вдалося створити користувача.");
        verify(userService, never()).authenticate(anyString(), anyString());
        verify(sessionManager, never()).startSession(any());
    }

    @Test
    void shouldShowErrorWhenAuthenticationAfterRegistrationFails() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("test@example.com")).thenReturn(null);
        when(userTypeService.findByExactName("CUSTOMER")).thenReturn(userType);
        when(userService.create(any(User.class))).thenReturn(true);
        when(userService.authenticate("test@example.com", "password123")).thenReturn(null);

        // When
        controller.handleRegister();

        // Then
        verify(controller).showInfo("Реєстрація успішна!");
        verify(userService).authenticate("test@example.com", "password123");
        verify(controller).showError("Помилка автентифікації після реєстрації.");
        verify(sessionManager, never()).startSession(any());
    }

    @Test
    void shouldHandleSQLExceptionDuringRegistration() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        SQLException sqlException = new SQLException("Database error");
        when(userService.findByEmail("test@example.com")).thenThrow(sqlException);

        // When
        controller.handleRegister();

        // Then
        verify(controller).showError("Помилка при реєстрації: Database error");
    }

    @Test
    void shouldHandleGeneralExceptionDuringRegistration() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        RuntimeException exception = new RuntimeException("General error");
        when(userService.findByEmail("test@example.com")).thenThrow(exception);

        // When
        controller.handleRegister();

        // Then
        verify(controller).showError("Невідома помилка при реєстрації: General error");
    }

    @Test
    void shouldTrimFieldsBeforeProcessing() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("  testuser  ", "  test@example.com  ", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("test@example.com")).thenReturn(null);
        when(userTypeService.findByExactName("CUSTOMER")).thenReturn(userType);
        when(userService.create(any(User.class))).thenReturn(true);
        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleRegister();

        // Then
        verify(userService).findByEmail("test@example.com");
        verify(userService).authenticate("test@example.com", "password123");
    }

    @Test
    void shouldNavigateToLoginSuccessfully() {
        // When
        controller.goToLogin();

        // Then
        verify(controllerFactory).createLoginController();
        verify(loginController).show();
    }

    @Test
    void shouldSetupUserObjectCorrectly() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("test@example.com")).thenReturn(null);
        when(userTypeService.findByExactName("CUSTOMER")).thenReturn(userType);
        when(userService.create(any(User.class))).thenReturn(true);
        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleRegister();

        // Then
        verify(userService).create(argThat(newUser ->
                "testuser".equals(newUser.getName()) &&
                        "test@example.com".equals(newUser.getEmail()) &&
                        userType.equals(newUser.getUserType())
        ));
    }

    @Test
    void shouldFollowCorrectExecutionSequence() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("testuser", "test@example.com", "password123", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateRegisterForm(any(), any(), any(), any())).thenReturn(true);

        when(userService.findByEmail("test@example.com")).thenReturn(null);
        when(userTypeService.findByExactName("CUSTOMER")).thenReturn(userType);
        when(userService.create(any(User.class))).thenReturn(true);
        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleRegister();

        // Then
        var inOrder = inOrder(userService, userTypeService, sessionManager, controllerFactory, dashboardController);
        inOrder.verify(userService).findByEmail("test@example.com");
        inOrder.verify(userTypeService).findByExactName("CUSTOMER");
        inOrder.verify(userService).create(any(User.class));
        inOrder.verify(userService).authenticate("test@example.com", "password123");
        inOrder.verify(sessionManager).startSession(user);
        inOrder.verify(controllerFactory).createDashboardController();
        inOrder.verify(dashboardController).show();
    }

    private void setupUIComponents() throws Exception {
        setFieldValue("usernameField", new TextField());
        setFieldValue("emailField", new TextField());
        setFieldValue("passwordField", new PasswordField());
        setFieldValue("confirmPasswordField", new PasswordField());
    }

    private void setupFormFields(String username, String email, String password, String confirmPassword) throws Exception {
        ((TextField) getFieldValue("usernameField")).setText(username);
        ((TextField) getFieldValue("emailField")).setText(email);
        ((PasswordField) getFieldValue("passwordField")).setText(password);
        ((PasswordField) getFieldValue("confirmPasswordField")).setText(confirmPassword);
    }

    private void setFieldValue(String fieldName, Object value) throws Exception {
        Field field = RegisterController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private Object getFieldValue(String fieldName) throws Exception {
        Field field = RegisterController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(controller);
    }
}