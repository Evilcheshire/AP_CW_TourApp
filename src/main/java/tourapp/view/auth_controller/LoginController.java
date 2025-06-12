package tourapp.view.auth_controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tourapp.model.user.User;
import tourapp.service.user_service.UserService;
import tourapp.util.ControllerFactory;
import tourapp.util.validation.FormValidator;
import tourapp.util.SessionManager;
import tourapp.view.BaseController;

public class LoginController extends BaseController {

    private final UserService userService;
    private final ControllerFactory controllerFactory;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    public LoginController(UserService userService,
                           SessionManager sessionManager,
                           Stage stage,
                           ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.userService = userService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/auth/login.fxml", "Вхід");
    }

    @FXML
    public void handleLogin() {
        if (!FormValidator.validateLoginForm(emailField, passwordField)) {
            FormValidator.showValidationErrors(stage);
            emailField.clear();
            passwordField.clear();
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            User user = userService.authenticate(email, password);

            if (user == null) {
                showError("Неправильна електронна пошта або пароль.");
                return;
            }

            sessionManager.startSession(user);
            showInfo("Вітаємо, " + user.getName() + "!");
            logger.info("Виконано вхід: {}.", user);

            controllerFactory.createDashboardController().show();
        } catch (Exception e) {
            showError("Сталася помилка при вході: " + e.getMessage());
        }
    }

    @FXML
    public void goToRegister() {
        controllerFactory.createRegisterController().show();
    }
}