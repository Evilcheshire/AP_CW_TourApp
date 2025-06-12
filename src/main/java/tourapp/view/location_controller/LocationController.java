package tourapp.view.location_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tourapp.model.location.Location;
import tourapp.model.location.LocationType;
import tourapp.service.location_service.LocationService;
import tourapp.service.location_service.LocationTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.util.validation.TransportValidator;
import tourapp.util.validation.ValidationResult;
import tourapp.view.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationController extends BaseController {

    @FXML BorderPane mainLayout;
    @FXML TableView<Location> locationTable;
    @FXML private TableColumn<Location, Integer> idCol;
    @FXML private TableColumn<Location, String> nameCol;
    @FXML private TableColumn<Location, String> countryCol;
    @FXML private TableColumn<Location, String> descCol;
    @FXML private TableColumn<Location, String> locTypeCol;
    @FXML private TextField keywordField;
    @FXML private ComboBox<String> countryFilterCombo;
    @FXML private ComboBox<String> countryTypeFilterCombo;
    @FXML private Button filterButton;
    @FXML private Button addLocationButton;
    @FXML private Button addLocationTypeButton;
    @FXML private Button editLocationButton;
    @FXML private Button editLocationTypeButton;
    @FXML private Button deleteLocationButton;
    @FXML private StackPane viewContainer;
    @FXML private Button resetFiltersButton;

    private final LocationService locationService;
    LocationTypeService locationTypeService;
    private final ControllerFactory controllerFactory;

    public LocationController(Stage stage,
                              SessionManager sessionManager,
                              LocationService locationService,
                              LocationTypeService locationTypeService,
                              ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.locationService = locationService;
        this.locationTypeService = locationTypeService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/location/locationDashboard.fxml", "TourApp - Керування локаціями");
    }

    @FXML
    public void initialize() {
        initializeTableColumns();
        initializeFilters();
        initializeNavigationBar();
        setupAccessControls();
        loadLocations();
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
                if (!isCustomer()) {
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

    private void setupAccessControls() {
        addLocationButton.setVisible(isAdmin() || isManager());
        addLocationButton.setManaged(isAdmin() || isManager());
        editLocationButton.setVisible(isAdmin() || isManager());
        editLocationButton.setManaged(isAdmin() || isManager());
        deleteLocationButton.setVisible(isAdmin() || isManager());
        deleteLocationButton.setManaged(isAdmin() || isManager());
        addLocationTypeButton.setVisible(isAdmin());
        addLocationTypeButton.setManaged(isAdmin());
        editLocationTypeButton.setVisible(isAdmin());
        editLocationTypeButton.setManaged(isAdmin());
    }

    private void initializeTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        locTypeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Location location = getTableView().getItems().get(getIndex());
                    setText(location.getLocationType().getName());
                }
            }
        });

        locationTable.setRowFactory(tv -> {
            TableRow<Location> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Location location = row.getItem();
                    showLocationDetails(location);
                }
            });
            return row;
        });
    }

    void showLocationDetails(Location location) {
        try {
            Location locationWithDetails = locationService.getById(location.getId());

            if (locationWithDetails != null) {
                StringBuilder details = new StringBuilder();
                details.append("Назва: ").append(locationWithDetails.getName()).append("\n");
                details.append("Країна: ").append(locationWithDetails.getCountry()).append("\n");
                details.append("Опис: ").append(locationWithDetails.getDescription()).append("\n");

                if (locationWithDetails.getLocationType() != null) {
                    details.append("Тип локації: ").append(locationWithDetails.getLocationType().getName()).append("\n");
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
                alert.setTitle("Деталі локації");
                alert.setHeaderText("Локація #" + locationWithDetails.getId());
                alert.setContentText(details.toString());
                alert.showAndWait();
            } else {
                showError("Не вдалося знайти локацію з ID: " + location.getId());
            }
        } catch (SQLException e) {
            showError("Помилка при отриманні деталей локації: " + e.getMessage());
            
        }
    }

    private void initializeFilters() {
        try {
            loadFilterData();
        } catch (Exception e) {
            showError("Помилка ініціалізації фільтрів: " + e.getMessage());
        }
    }

    void loadFilterData() {
        try {
            ObservableList<String> countries = FXCollections.observableArrayList("Всі");
            List<Location> locations = locationService.getAll();
            locations.stream()
                    .map(Location::getCountry)
                    .distinct()
                    .sorted()
                    .forEach(countries::add);
            countryFilterCombo.setItems(countries);
            countryFilterCombo.setValue("Всі");

            ObservableList<String> locationTypes = FXCollections.observableArrayList("Всі");
            List<LocationType> types = locationTypeService.getAll();
            types.stream()
                    .map(LocationType::getName)
                    .sorted()
                    .forEach(locationTypes::add);
            countryTypeFilterCombo.setItems(locationTypes);
            countryTypeFilterCombo.setValue("Всі");
        } catch (SQLException e) {
            showError("Помилка завантаження даних для фільтрів: " + e.getMessage());
        }
    }

    @FXML
    void onFilterButtonClicked() {
        if (!validateFilters()) {
            FormValidator.showValidationErrors(stage);
            resetAllFilters();
            return;
        }
        loadLocations();
    }

    private boolean validateFilters() {
        FormValidator.clearErrors();

        if (keywordField != null && keywordField.getText() != null && !keywordField.getText().trim().isEmpty()) {
            ValidationResult keywordResult = TransportValidator.validateSearchKeyword(keywordField.getText());
            if (!keywordResult.valid()) {
                FormValidator.addError(keywordField, keywordResult.errorMessage());
                showInfo(keywordResult.errorMessage());
                return false;
            }
        }

        return true;
    }

    void loadLocations() {
        Map<String, Object> filters = new HashMap<>();

        String keyword = keywordField.getText();
        if (keyword != null && !keyword.isBlank()) {
            filters.put("keyword", keyword);
        }

        String country = countryFilterCombo.getValue();
        if (country != null && !country.equals("Всі")) {
            filters.put("country", country);
        }

        String locationType = countryTypeFilterCombo.getValue();
        if (locationType != null && !locationType.equals("Всі")) {
            filters.put("locationType", locationType);
        }

        try {
            List<Location> locations = locationService.search(filters);
            locationTable.setItems(FXCollections.observableArrayList(locations));
            if (locations.isEmpty()) {
                showInfo("За вказаними критеріями локацій не знайдено");
            }
        } catch (SQLException e) {
            showError("Помилка завантаження локацій: " + e.getMessage());
            
        }
    }

    @FXML
    public void addNewLocation() {
        showLocationEditDialog(null);
    }

    @FXML
    public void addNewLocationType() {
        showLocationTypeEditDialog(null);
    }

    @FXML
    public void editSelectedLocation() {
        Location selectedLocation = locationTable.getSelectionModel().getSelectedItem();
        if (selectedLocation != null) {
            showLocationEditDialog(selectedLocation);
        } else {
            showInfo("Виберіть локацію для редагування");
        }
    }

    @FXML
    public void editSelectedLocationType() {
        showInfo("Виберіть тип локації для редагування");
        showLocationTypeSelectionDialog();
    }

    void showLocationTypeSelectionDialog() {
        try {
            List<LocationType> types = locationTypeService.getAll();
            if (types.isEmpty()) {
                showError("Не знайдено жодного типу локації");
                return;
            }

            ChoiceDialog<LocationType> dialog = new ChoiceDialog<>(null, types);
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            dialog.setTitle("Вибір типу локації");
            dialog.setHeaderText("Виберіть тип локації для редагування");
            dialog.setContentText("Тип локації:");

            dialog.getItems().setAll(types);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return dialog.getSelectedItem();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(this::showLocationTypeEditDialog);
        } catch (SQLException e) {
            showError("Помилка завантаження типів локації: " + e.getMessage());
        }
    }

    private void showLocationEditDialog(Location location) {
        try {
            LocationEditController locationEditController = controllerFactory.createLocationEditController(location);
            locationEditController.setOnSaveCallback(this::loadLocations);
            locationEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування локації: " + e.getMessage());
            
        }
    }

    void showLocationTypeEditDialog(LocationType locationType) {
        try {
            LocationTypeEditController locationTypeEditController = controllerFactory.createLocationTypeEditController(locationType);
            locationTypeEditController.setOnSaveCallback(() -> {
                loadFilterData();
                loadLocations();
            });
            locationTypeEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування типу локації: " + e.getMessage());
        }
    }

    @FXML
    public void deleteSelectedLocation() {
        Location selectedLocation = locationTable.getSelectionModel().getSelectedItem();
        if (selectedLocation != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ви впевнені, що хочете видалити локацію '" + selectedLocation.getName() + "'?",
                    ButtonType.YES, ButtonType.NO);
            Stage alertStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            confirmAlert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/checkbox.png").toExternalForm()));
            confirmAlert.showAndWait();

            if (confirmAlert.getResult() == ButtonType.YES) {
                try {
                    locationService.delete(selectedLocation.getId());
                    loadLocations();
                    showInfo("Локацію успішно видалено.");
                    logger.info("Видалено локацію: {}", selectedLocation.toString());
                } catch (SQLException e) {
                    showError("Помилка видалення локації: " + e.getMessage());
                }
            }
        } else {
            showInfo("Виберіть локацію для видалення");
        }
    }

    @FXML
    private void onResetFiltersClicked() {
        resetAllFilters();
        showInfo("Фільтри скинуто до початкових значень");
        loadLocations();
    }

    private void resetAllFilters() {
        FormValidator.clearErrors();

        keywordField.clear();
        countryFilterCombo.setValue("Всі");
        countryTypeFilterCombo.setValue("Всі");
    }
}