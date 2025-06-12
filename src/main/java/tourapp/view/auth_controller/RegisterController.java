package tourapp.view.auth_controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.validation.FormValidator;
import tourapp.util.SessionManager;
import tourapp.view.BaseController;

import java.sql.SQLException;

public class RegisterController extends BaseController {

    private final UserService userService;
    private final UserTypeService userTypeService;
    private final ControllerFactory controllerFactory;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    public RegisterController(UserService userService,
                              UserTypeService userTypeService,
                              SessionManager sessionManager,
                              Stage stage,
                              ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.userService = userService;
        this.userTypeService = userTypeService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/auth/register.fxml", "Реєстрація");
    }

    @FXML
    void handleRegister() {
        if (!FormValidator.validateRegisterForm(usernameField, emailField, passwordField, confirmPasswordField)) {
            FormValidator.showValidationErrors(stage);
            usernameField.clear();
            emailField.clear();
            passwordField.clear();
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            User existingUser = userService.findByEmail(email);
            if (existingUser != null) {
                showError("Користувач з такою поштою вже існує.");
                return;
            }

            UserType role = userTypeService.findByExactName("CUSTOMER");
            if (role == null) {
                showError("Роль користувача не знайдена. Зверніться до адміністратора.");
                return;
            }

            User newUser = new User(username, email, password, role);

            boolean created = userService.create(newUser);
            if (created) {
                showInfo("Реєстрація успішна!");
                logger.info("Виконано реєстрацію: {}.", newUser);
                User authenticatedUser = userService.authenticate(email, password);
                if (authenticatedUser != null) {
                    sessionManager.startSession(authenticatedUser);
                    controllerFactory.createDashboardController().show();
                } else {
                    showError("Помилка автентифікації після реєстрації.");
                }
            } else {
                showError("Не вдалося створити користувача.");
            }
        } catch (SQLException e) {
            showError("Помилка при реєстрації: " + e.getMessage());
        } catch (Exception e) {
            showError("Невідома помилка при реєстрації: " + e.getMessage());
        }
    }

    @FXML
    void goToLogin() {
        controllerFactory.createLoginController().show();
    }
}