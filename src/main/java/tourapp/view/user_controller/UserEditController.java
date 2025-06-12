package tourapp.view.user_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserEditController extends BaseController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<UserType> userTypeComboBox;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox changePasswordCheckBox;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label titleLabel;

    private final UserService userService;
    private final UserTypeService userTypeService;
    private User editingUser;
    private boolean isEditMode = false;
    private Runnable onSaveCallback;
    private ObservableList<UserType> userTypes;

    final Stage editStage;

    public UserEditController(Stage stage,
                              SessionManager sessionManager,
                              UserService userService,
                              UserTypeService userTypeService,
                              User editingUser) {
        super(stage, sessionManager);
        this.userService = userService;
        this.userTypeService = userTypeService;
        this.editingUser = editingUser;
        this.isEditMode = editingUser != null;
        this.userTypes = FXCollections.observableArrayList();

        this.editStage = new Stage();
        this.editStage.initModality(Modality.WINDOW_MODAL);
        this.editStage.initOwner(stage);
    }

    @Override
    public void show() {
        String title = isEditMode ? "Редагування користувача" : "Створення користувача";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tourapp/view/user/userEdit.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            editStage.setScene(scene);
            editStage.setTitle(title);
            editStage.setResizable(true);
            editStage.show();
        } catch (IOException e) {
            showError("Не вдалося завантажити інтерфейс: " + e.getMessage());
        }
    }

    public void setOnSave(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void initialize() {
        setupControls();
        loadUserTypes();

        if (isEditMode) {
            setupEditMode();
        } else {
            setupCreateMode();
        }
    }

    private void setupControls() {
        userTypeComboBox.setItems(userTypes);

        userTypeComboBox.setCellFactory(listView -> new ListCell<UserType>() {
            @Override
            protected void updateItem(UserType userType, boolean empty) {
                super.updateItem(userType, empty);
                if (empty || userType == null) {
                    setText(null);
                } else {
                    setText(userType.getName());
                }
            }
        });

        userTypeComboBox.setButtonCell(new ListCell<UserType>() {
            @Override
            protected void updateItem(UserType userType, boolean empty) {
                super.updateItem(userType, empty);
                if (empty || userType == null) {
                    setText("Оберіть тип користувача");
                } else {
                    setText(userType.getName());
                }
            }
        });

        changePasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setPasswordFieldsVisible(shouldValidatePassword());
        });
    }

    private void setupEditMode() {
        titleLabel.setText("Редагування користувача");

        loadUserData();

        changePasswordCheckBox.setVisible(true);
        changePasswordCheckBox.setManaged(true);
        changePasswordCheckBox.setSelected(false);

        setPasswordFieldsVisible(shouldValidatePassword());
    }

    private void loadUserData() {
        try {
            User fullUser = userService.getById(editingUser.getId());
            if (fullUser != null) {
                nameField.setText(fullUser.getName());

                emailField.setText(fullUser.getEmail());

                UserType userType = fullUser.getUserType();
                if (userType != null) {
                    UserType matchingType = userTypes.stream()
                            .filter(type -> type.getId() == userType.getId())
                            .findFirst()
                            .orElse(null);
                    userTypeComboBox.setValue(matchingType);
                }
            }
        } catch (SQLException e) {
            showError("Помилка завантаження даних користувача: " + e.getMessage());
        }
    }

    private void setupCreateMode() {
        titleLabel.setText("Створення користувача");

        changePasswordCheckBox.setVisible(false);
        changePasswordCheckBox.setManaged(false);

        setPasswordFieldsVisible(true);
    }

    private void setPasswordFieldsVisible(boolean visible) {
        passwordField.setVisible(visible);
        passwordField.setManaged(visible);
        if (!visible) {
            passwordField.clear();
        }

        confirmPasswordField.setVisible(visible);
        confirmPasswordField.setManaged(visible);
        if (!visible) {
            confirmPasswordField.clear();
        }

        passwordLabel.setVisible(visible);
        passwordLabel.setManaged(visible);

        confirmPasswordLabel.setVisible(visible);
        confirmPasswordLabel.setManaged(visible);
    }

    private void loadUserTypes() {
        try {
            List<UserType> typeList = userTypeService.getAll();
            userTypes.setAll(typeList);
            if (!isAdmin()) {
                userTypes.removeIf(userType -> userType.getName().equalsIgnoreCase("ADMIN"));
            }
        } catch (SQLException e) {
            showError("Помилка завантаження типів користувачів: " + e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        FormValidator.clearErrors();

        boolean isPasswordRequired = shouldValidatePassword();

        if (!FormValidator.validateUserEditForm(
                nameField,
                emailField,
                userTypeComboBox,
                passwordField,
                confirmPasswordField,
                isPasswordRequired)) {
            FormValidator.showValidationErrors(editStage);

            return;
        }

        try {
            String email = emailField.getText().trim();
            User existingUser = userService.findByEmail(email);
            if (existingUser != null && (!isEditMode || existingUser.getId() != editingUser.getId())) {
                FormValidator.addError(emailField, "Користувач з такою email адресою вже існує");
                return;
            }
        } catch (SQLException e) {
            showError("Помилка при перевірці унікальності email: " + e.getMessage());
            return;
        }

        try {
            if (isEditMode) {
                updateExistingUser();
            } else {
                createNewUser();
            }

            onSaveCallback.run();
            editStage.close();
        } catch (SQLException e) {
            showError("Помилка збереження користувача: " + e.getMessage());
        }
    }

    private boolean shouldValidatePassword() {
        return !isEditMode || (changePasswordCheckBox != null && changePasswordCheckBox.isSelected());
    }

    private void createNewUser() throws SQLException {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        UserType userType = userTypeComboBox.getValue();

        User newUser = new User(name, email, password, userType);

        boolean created = userService.create(newUser);
        if (created) {
            FormValidator.clearErrors();
            showInfo("Користувача успішно створено");
            logger.info("Створено користувача: {}", newUser);
        } else {
            showError("Не вдалося створити користувача");
        }
    }

    private void updateExistingUser() throws SQLException {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        UserType userType = userTypeComboBox.getValue();

        editingUser.setName(name);
        editingUser.setEmail(email);
        editingUser.setUserType(userType);

        boolean updated = userService.update(editingUser);
        if (!updated) {
            showError("Не вдалося оновити дані користувача");
            return;
        }

        if (changePasswordCheckBox != null && changePasswordCheckBox.isSelected()) {
            String newPassword = passwordField.getText();
            boolean passwordUpdated = userService.changePassword(editingUser.getId(), newPassword);
            if (!passwordUpdated) {
                showError("Дані користувача оновлено, але не вдалося змінити пароль");
                return;
            }
        }

        FormValidator.clearErrors();
        showInfo("Дані користувача успішно оновлено");
        logger.info("Відредаговано користувача: {}", editingUser.toString());
    }

    @FXML
    public void handleCancel() {
        FormValidator.clearErrors();
        editStage.close();
    }

    @Override
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(editStage);
        alert.showAndWait();
        logger.error(message);
    }

    @Override
    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Інформація");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(editStage);
        alert.showAndWait();
    }
}