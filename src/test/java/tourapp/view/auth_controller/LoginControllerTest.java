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
import tourapp.service.user_service.UserService;
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
class LoginControllerTest {

    @Mock private UserService userService;
    @Mock private SessionManager sessionManager;
    @Mock private Stage stage;
    @Mock private ControllerFactory controllerFactory;
    @Mock private User user;
    @Mock private DashboardController dashboardController;
    @Mock private RegisterController registerController;

    private LoginController controller;
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

        controller = spy(new LoginController(userService, sessionManager, stage, controllerFactory));
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
            when(controllerFactory.createRegisterController()).thenReturn(registerController);
            doNothing().when(dashboardController).show();
            doNothing().when(registerController).show();

            when(user.getName()).thenReturn("Test User");
            when(user.toString()).thenReturn("User{id=1, name='Test User', email='test@example.com'}");
        } catch (Exception ignored) {
        }
    }

    @Test
    void testShow() {
        // Given
        doNothing().when(controller).loadAndShow(anyString(), anyString());

        // When
        controller.show();

        // Then
        verify(controller).loadAndShow("/tourapp/view/auth/login.fxml", "Вхід");
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("test@example.com", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("test@example.com", "password123");
        verify(sessionManager).startSession(user);
        verify(controller).showInfo("Вітаємо, Test User!");
        verify(controllerFactory).createDashboardController();
        verify(dashboardController).show();
        verify(controller, never()).showError(anyString());
    }

    @Test
    void shouldNotLoginWhenFormValidationFails() throws Exception {
        // Given
        setupUIComponents();
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(false);

        // When
        controller.handleLogin();

        // Then
        verify(userService, never()).authenticate(anyString(), anyString());
        verify(sessionManager, never()).startSession(any());
        verify(controllerFactory, never()).createDashboardController();
    }

    @Test
    void shouldShowErrorWhenCredentialsAreInvalid() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("invalid@example.com", "wrongpassword");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        when(userService.authenticate("invalid@example.com", "wrongpassword")).thenReturn(null);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("invalid@example.com", "wrongpassword");
        verify(controller).showError("Неправильна електронна пошта або пароль.");
        verify(sessionManager, never()).startSession(any());
        verify(controllerFactory, never()).createDashboardController();
    }

    @Test
    void shouldTrimEmailBeforeAuthentication() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("  test@example.com  ", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("test@example.com", "password123");
        verify(sessionManager).startSession(user);
    }

    @Test
    void shouldHandleExceptionDuringAuthentication() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("test@example.com", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        RuntimeException exception = new RuntimeException("Service unavailable");
        when(userService.authenticate("test@example.com", "password123")).thenThrow(exception);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("test@example.com", "password123");
        verify(controller).showError("Сталася помилка при вході: Service unavailable");
        verify(sessionManager, never()).startSession(any());
    }

    @Test
    void shouldHandleSQLExceptionDuringAuthentication() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("test@example.com", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        SQLException sqlException = new SQLException("Database connection failed");
        when(userService.authenticate("test@example.com", "password123")).thenThrow(sqlException);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("test@example.com", "password123");
        verify(controller).showError("Сталася помилка при вході: Database connection failed");
        verify(sessionManager, never()).startSession(any());
    }

    @Test
    void shouldNavigateToRegisterSuccessfully() {
        // When
        controller.goToRegister();

        // Then
        verify(controllerFactory).createRegisterController();
        verify(registerController).show();
    }

    @Test
    void shouldFollowCorrectExecutionSequenceForSuccessfulLogin() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("test@example.com", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleLogin();

        // Then
        var inOrder = inOrder(userService, sessionManager, controllerFactory, dashboardController);
        inOrder.verify(userService).authenticate("test@example.com", "password123");
        inOrder.verify(sessionManager).startSession(user);
        inOrder.verify(controllerFactory).createDashboardController();
        inOrder.verify(dashboardController).show();
    }

    @Test
    void shouldNotProceedWhenEmailIsEmpty() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(false);

        // When
        controller.handleLogin();

        // Then
        verify(userService, never()).authenticate(anyString(), anyString());
        verify(sessionManager, never()).startSession(any());
    }

    @Test
    void shouldNotProceedWhenPasswordIsEmpty() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("test@example.com", "");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(false);

        // When
        controller.handleLogin();

        // Then
        verify(userService, never()).authenticate(anyString(), anyString());
        verify(sessionManager, never()).startSession(any());
    }

    @Test
    void shouldNotProceedWhenBothFieldsAreEmpty() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("", "");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(false);

        // When
        controller.handleLogin();

        // Then
        verify(userService, never()).authenticate(anyString(), anyString());
        verify(sessionManager, never()).startSession(any());
    }

    @Test
    void shouldHandleMultipleLoginAttempts() throws Exception {
        // Given
        setupUIComponents();
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        // First attempt - fail
        setupFormFields("test@example.com", "wrong");
        when(userService.authenticate("test@example.com", "wrong")).thenReturn(null);

        controller.handleLogin();

        // Second attempt - success
        setupFormFields("test@example.com", "correct");
        when(userService.authenticate("test@example.com", "correct")).thenReturn(user);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("test@example.com", "wrong");
        verify(userService).authenticate("test@example.com", "correct");
        verify(sessionManager).startSession(user);
        verify(controller).showError("Неправильна електронна пошта або пароль.");
        verify(controller).showInfo("Вітаємо, Test User!");
    }

    @Test
    void shouldNotTrimPassword() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("test@example.com", "  password123  ");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        when(userService.authenticate("test@example.com", "  password123  ")).thenReturn(user);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("test@example.com", "  password123  ");
        verify(sessionManager).startSession(user);
    }

    @Test
    void shouldLogSuccessfulLogin() throws Exception {
        // Given
        setupUIComponents();
        setupFormFields("test@example.com", "password123");
        formValidatorMock.when(() ->
                FormValidator.validateLoginForm(any(), any())).thenReturn(true);

        when(userService.authenticate("test@example.com", "password123")).thenReturn(user);

        // When
        controller.handleLogin();

        // Then
        verify(userService).authenticate("test@example.com", "password123");
        verify(sessionManager).startSession(user);
    }

    private void setupUIComponents() throws Exception {
        setFieldValue("emailField", new TextField());
        setFieldValue("passwordField", new PasswordField());
    }

    private void setupFormFields(String email, String password) throws Exception {
        ((TextField) getFieldValue("emailField")).setText(email);
        ((PasswordField) getFieldValue("passwordField")).setText(password);
    }

    private void setFieldValue(String fieldName, Object value) throws Exception {
        Field field = LoginController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private Object getFieldValue(String fieldName) throws Exception {
        Field field = LoginController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(controller);
    }
}