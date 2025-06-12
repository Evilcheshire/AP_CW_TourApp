package tourapp.view.user_controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tourapp.model.user.User;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseController;
import tourapp.view.NavigationController;
import tourapp.view.auth_controller.LoginController;

import java.sql.SQLException;

public class UserCabinetController extends BaseController {

    @FXML BorderPane mainLayout;
    @FXML TextField nameField;
    @FXML TextField emailField;
    @FXML Label userTypeLabel;
    @FXML PasswordField currentPasswordField;
    @FXML PasswordField newPasswordField;
    @FXML PasswordField confirmPasswordField;
    @FXML private Button updateProfileButton;
    @FXML private Button changePasswordButton;

    private final ControllerFactory controllerFactory;
    private final UserService userService;
    private User currentUser;

    public UserCabinetController(Stage stage,
                                 SessionManager sessionManager,
                                 UserService userService,
                                 ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.controllerFactory = controllerFactory;
        this.userService = userService;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/user/userCabinet.fxml", "TourApp - Особистий кабінет");
    }

    @FXML
    public void initialize() {
        currentUser = sessionManager.getCurrentSession().user();
        initializeNavigationBar();
        initializeProfileTab();
        loadUserData();
    }

    void initializeNavigationBar() {
        try {
            NavigationController navigationController = controllerFactory.createNavigationController();

            navigationController.setOnLogout(() -> {
                sessionManager.endSession();
                LoginController loginController = controllerFactory.createLoginController();
                loginController.show();
            });

            navigationController.setOnExit(stage::close);

            navigationController.setOnSearch(() -> {
                controllerFactory.createDashboardController().show();
            });

            navigationController.setOnBooked(() -> {
                controllerFactory.createBookedToursController().show();
            });

            navigationController.setOnAdminPanel(() -> {
                if (isAdmin() || isManager()) {
                    controllerFactory.createAdminPanelController().show();
                } else {
                    showInfo("У вас немає прав для доступу до адміністративної панелі");
                }
            });

            navigationController.setOnProfile(() -> {
                controllerFactory.createUserCabinetController().show();
            });

            HBox navBar = navigationController.createNavigationBar(NavigationController.PAGE_PROFILE);
            mainLayout.setTop(navBar);
        } catch (Exception e) {
            showError("Помилка ініціалізації навігації: " + e.getMessage());
        }
    }

    private void initializeProfileTab() {
        updateProfileButton.setOnAction(e -> handleUpdateProfile());
        changePasswordButton.setOnAction(e -> handleChangePassword());
    }

    void loadUserData() {
        try {
            User freshUserData = userService.getById(currentUser.getId());
            if (freshUserData != null) {
                currentUser = freshUserData;
                sessionManager.getCurrentSession().setUser(currentUser);
            }

            nameField.setText(currentUser.getName() != null ? currentUser.getName() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            userTypeLabel.setText(currentUser.getUserType().getName());

        } catch (SQLException e) {
            showError("Помилка завантаження даних користувача: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateProfile() {

        TextField tempNameField = new TextField(nameField.getText());
        TextField tempEmailField = new TextField(emailField.getText());

        if (!FormValidator.validateRegisterForm(tempNameField, tempEmailField, null, null)) {
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        try {
            User existingUser = userService.findByEmail(email);
            if (existingUser != null && existingUser.getId() != currentUser.getId()) {
                showError("Користувач з такою email адресою вже існує");
                return;
            }

            currentUser.setName(name);
            currentUser.setEmail(email);

            boolean updated = userService.update(currentUser);
            if (updated) {
                sessionManager.getCurrentSession().setUser(currentUser);
                showInfo("Дані профілю успішно оновлено");
                logger.info("Оновлено дані користувача: {}", currentUser.toString());
            } else {
                showError("Не вдалося оновити дані профілю");
            }
        } catch (SQLException e) {
            showError("Помилка оновлення профілю: " + e.getMessage());
        }
    }

    @FXML
    public void handleChangePassword() {
        if (!FormValidator.validatePasswordChangeForm(currentPasswordField, newPasswordField, confirmPasswordField)) {
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();

        try {
            User authenticatedUser = userService.authenticate(currentUser.getEmail(), currentPassword);
            if (authenticatedUser == null) {
                FormValidator.addError(currentPasswordField, "Поточний пароль невірний");
                return;
            }

            boolean changed = userService.changePassword(currentUser.getId(), newPassword);
            if (changed) {
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                FormValidator.clearErrors();
                showInfo("Пароль успішно змінено");
                logger.info("Відредаговано пароль користувача: {}", currentUser.toString());
            } else {
                showError("Не вдалося змінити пароль");
            }
        } catch (SQLException e) {
            showError("Помилка зміни паролю: " + e.getMessage());
        }
    }
}