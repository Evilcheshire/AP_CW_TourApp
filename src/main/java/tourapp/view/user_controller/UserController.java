package tourapp.view.user_controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tourapp.model.user.User;
import tourapp.model.user.UserType;
import tourapp.service.user_service.UserService;
import tourapp.service.user_service.UserTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.view.BaseController;
import tourapp.view.NavigationController;

import java.util.List;

public class UserController extends BaseController {

    @FXML
    BorderPane mainLayout;

    @FXML private TextField keywordField;
    @FXML private ComboBox<UserType> userTypeFilterCombo;
    @FXML private Button filterButton;

    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Long> idCol;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> typeCol;

    private final ControllerFactory controllerFactory;
    private final UserService userService;
    private final UserTypeService userTypeService;
    private ObservableList<User> users;
    private ObservableList<UserType> userTypes;

    public UserController(Stage stage,
                          SessionManager sessionManager,
                          UserService userService,
                          UserTypeService userTypeService,
                          ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.controllerFactory = controllerFactory;
        this.userService = userService;
        this.userTypeService = userTypeService;
        this.users = FXCollections.observableArrayList();
        this.userTypes = FXCollections.observableArrayList();
    }

    @Override
    public void show() {
         loadAndShow("/tourapp/view/user/userDashboard.fxml", "TourApp - Управління користувачами");
    }

    @FXML
    public void initialize() {
        initializeNavigationBar();
        initializeTable();
        initializeControls();
        loadUserTypes();
        loadUsers();
    }

    void initializeNavigationBar() {
        try {
            NavigationController navigationController = controllerFactory.createNavigationController();

            navigationController.setOnLogout(() -> {
                sessionManager.endSession();
                controllerFactory.createLoginController().show();
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

            HBox navBar = navigationController.createNavigationBar(NavigationController.PAGE_DASHBOARD);
            mainLayout.setTop(navBar);
        } catch (Exception e) {
            showError("Помилка ініціалізації навігації: " + e.getMessage());
        }
    }

    private void initializeTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        emailCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEmail()));
        typeCol.setCellValueFactory(cellData -> {
            UserType userType = cellData.getValue().getUserType();
            String roleName = userType != null ? userType.getName() : "Не визначено";
            return new ReadOnlyObjectWrapper<>(roleName);
        });

        userTable.setItems(users);
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateButtonStates()
        );
    }

    private void initializeControls() {
        userTypeFilterCombo.setItems(userTypes);
        userTypeFilterCombo.setCellFactory(listView -> new ListCell<UserType>() {
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

        userTypeFilterCombo.setButtonCell(new ListCell<UserType>() {
            @Override
            protected void updateItem(UserType userType, boolean empty) {
                super.updateItem(userType, empty);
                if (empty || userType == null) {
                    setText("Всі ролі");
                } else {
                    setText(userType.getName());
                }
            }
        });

        updateButtonStates();
    }

    private void loadUserTypes() {
        try {
            List<UserType> typeList = userTypeService.getAll();
            userTypes.setAll(typeList);
        } catch (Exception e) {
            showError("Помилка завантаження типів користувачів: " + e.getMessage());
        }
    }

    void loadUsers() {
        try {
            List<User> userList = userService.getAll();
            users.setAll(userList);
        } catch (Exception e) {
            showError("Помилка завантаження користувачів: " + e.getMessage());
        }
    }

    private void updateButtonStates() {
        User selectedUser = userTable != null ? userTable.getSelectionModel().getSelectedItem() : null;
        boolean hasSelection = selectedUser != null;

        editUserButton.setDisable(!hasSelection);

        boolean canDelete = hasSelection &&
                !(selectedUser.getId() == (sessionManager.getCurrentSession().user().getId())) &&
                (!selectedUser.isAdmin() || (sessionManager.getCurrentSession().user().getId() == 1));
        deleteUserButton.setDisable(!canDelete);
    }

    @FXML
    public void onFilterButtonClicked() {
        handleSearch();
    }

    private void handleSearch() {
        try {
            String searchTerm = keywordField != null ? keywordField.getText().trim() : "";
            UserType selectedRole = userTypeFilterCombo != null ? userTypeFilterCombo.getValue() : null;

            List<User> filteredUsers;

            if (searchTerm.isEmpty() && selectedRole == null) {
                filteredUsers = userService.getAll();
            } else if (!searchTerm.isEmpty() && selectedRole == null) {
                filteredUsers = userService.searchByTerm(searchTerm);
            } else if (searchTerm.isEmpty()) {
                filteredUsers = userService.getAll().stream()
                        .filter(user -> user.getUserType() != null &&
                                user.getUserType().getId() == (selectedRole.getId()))
                        .toList();
            } else {
                filteredUsers = userService.searchByTerm(searchTerm).stream()
                        .filter(user -> user.getUserType() != null &&
                                user.getUserType().getId() == (selectedRole.getId()))
                        .toList();
            }

            users.setAll(filteredUsers);
            if (filteredUsers.isEmpty()) {
                showInfo("За вказаними критеріями користувачів не знайдено");
            }
        } catch (Exception e) {
            showError("Помилка пошуку користувачів: " + e.getMessage());
        }
    }

    @FXML
    public void addNewUser() {
        UserEditController editController = controllerFactory.createUserEditController(null);
        editController.setOnSave(this::loadUsers);
        editController.show();
    }

    @FXML
    public void editSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Оберіть користувача для редагування");
            return;
        }

        UserEditController editController = controllerFactory.createUserEditController(selectedUser);
        editController.setOnSave(this::loadUsers);
        editController.show();
    }

    @FXML
    public void deleteSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Оберіть користувача для видалення");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Підтвердження видалення");
        confirmation.setHeaderText("Видалення користувача");
        confirmation.setContentText(String.format(
                "Ви впевнені, що хочете видалити користувача '%s' (%s)?",
                selectedUser.getName(), selectedUser.getEmail()
        ));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(selectedUser.getId());
                    loadUsers();
                    showInfo("Користувача успішно видалено");
                    logger.info("Видалено користувача: {}", selectedUser.toString());
                } catch (Exception e) {
                    showError("Помилка видалення користувача: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onResetFiltersClicked() {
        resetAllFilters();
        showInfo("Фільтри скинуто до початкових значень");
        loadUsers();
    }

    private void resetAllFilters() {
        keywordField.clear();
        userTypeFilterCombo.setValue(null);
    }
}