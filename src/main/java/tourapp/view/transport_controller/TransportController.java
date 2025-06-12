package tourapp.view.transport_controller;

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
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.service.transport_service.TransportService;
import tourapp.service.transport_service.TransportTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseController;
import tourapp.view.NavigationController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportController extends BaseController {

    @FXML BorderPane mainLayout;
    @FXML TableView<Transport> transportTable;
    @FXML private TableColumn<Transport, Integer> idCol;
    @FXML private TableColumn<Transport, String> nameCol;
    @FXML private TableColumn<Transport, String> typeCol;
    @FXML private TableColumn<Transport, Double> priceCol;
    @FXML private TextField keywordField;
    @FXML private ComboBox<String> transportTypeFilterCombo;
    @FXML private Spinner<Double> minPriceSpinner;
    @FXML private Spinner<Double> maxPriceSpinner;
    @FXML private Button filterButton;
    @FXML private Button addTransportButton;
    @FXML private Button addTransportTypeButton;
    @FXML private Button editTransportButton;
    @FXML private Button editTransportTypeButton;
    @FXML private Button deleteTransportButton;
    @FXML private StackPane viewContainer;

    private final TransportService transportService;
    private final TransportTypeService transportTypeService;
    private final ControllerFactory controllerFactory;

    public TransportController(Stage stage,
                               SessionManager sessionManager,
                               TransportService transportService,
                               TransportTypeService transportTypeService,
                               ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.transportService = transportService;
        this.transportTypeService = transportTypeService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/transport/transportDashboard.fxml", "TourApp - Управління транспортом");
    }

    @FXML
    public void initialize() {
        initializeTableColumns();
        initializeFilters();
        setupAccessControls();
        setupValidationListeners();
        initializeNavigationBar();
        loadTransports();
    }

    private void setupValidationListeners() {
        keywordField.textProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(keywordField);
        });

        transportTypeFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(transportTypeFilterCombo);
        });

        minPriceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(minPriceSpinner.getEditor());
        });

        maxPriceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(maxPriceSpinner.getEditor());
        });
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

    void setupAccessControls() {
        addTransportButton.setVisible(isAdmin() || isManager());
        addTransportButton.setManaged(isAdmin() || isManager());
        deleteTransportButton.setVisible(isAdmin() || isManager());
        deleteTransportButton.setManaged(isAdmin() || isManager());
        editTransportButton.setVisible(isAdmin() || isManager());
        editTransportButton.setManaged(isAdmin() || isManager());
        addTransportTypeButton.setVisible(isAdmin());
        addTransportTypeButton.setManaged(isAdmin());
        editTransportTypeButton.setVisible(isAdmin());
        editTransportTypeButton.setManaged(isAdmin());
    }

    private void initializeTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerPerson"));

        typeCol.setCellValueFactory(cellData -> {
            Transport transport = cellData.getValue();
            if (transport != null && transport.getType() != null) {
                return new javafx.beans.property.SimpleStringProperty(transport.getType().getName());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f грн", item));
                }
            }
        });

        transportTable.setRowFactory(tv -> {
            TableRow<Transport> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Transport transport = row.getItem();
                    showTransportDetails(transport);
                }
            });
            return row;
        });
    }

    void showTransportDetails(Transport transport) {
        try {
            Transport transportWithDetails = transportService.getById(transport.getId());
            if (transportWithDetails != null) {
                Alert alert = getAlert(transportWithDetails);
                alert.showAndWait();
            } else {
                showError("Не вдалося знайти транспорт з ID: " + transport.getId());
            }
        } catch (SQLException e) {
            showError("Помилка при отриманні деталей транспорту: " + e.getMessage());
        }
    }

    private Alert getAlert(Transport transportWithDetails) {
        String details = "Назва: " + transportWithDetails.getName() + "\n" +
                "Тип: " + transportWithDetails.getType().getName() + "\n" +
                "Ціна за людину: " + String.format("%.2f грн", transportWithDetails.getPricePerPerson()) + "\n";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
        alert.setTitle("Деталі транспорту");
        alert.setHeaderText("Транспорт #" + transportWithDetails.getId());
        alert.setContentText(details);
        return alert;
    }

    private void initializeFilters() {
        try {
            SpinnerValueFactory<Double> minFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 0, 100);
            SpinnerValueFactory<Double> maxFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 5000, 100);
            minPriceSpinner.setValueFactory(minFactory);
            maxPriceSpinner.setValueFactory(maxFactory);

            loadFilterData();
        } catch (Exception e) {
            showError("Помилка ініціалізації фільтрів: " + e.getMessage());
        }
    }

    private void loadFilterData() {
        try {
            ObservableList<String> transportTypes = FXCollections.observableArrayList("Всі");
            List<TransportType> types = transportTypeService.getAll();
            types.stream()
                    .map(TransportType::getName)
                    .sorted()
                    .forEach(transportTypes::add);
            transportTypeFilterCombo.setItems(transportTypes);
            transportTypeFilterCombo.setValue("Всі");
        } catch (SQLException e) {
            showError("Помилка завантаження даних для фільтрів: " + e.getMessage());
        }
    }

    @FXML
    private void onFilterButtonClicked() {
        if (validateFilters()) {
            loadTransports();
        }
    }

    private boolean validateFilters() {
        try {
            boolean isValid = FormValidator.validateTransportFilterForm(
                    keywordField,
                    transportTypeFilterCombo,
                    minPriceSpinner,
                    maxPriceSpinner
            );

            if (!isValid) {
                FormValidator.showValidationErrors(stage);
                resetAllFilters();
            }

            return isValid;
        } catch (Exception e) {
            showError("Помилка валідації фільтрів: " + e.getMessage());
            return false;
        }
    }

    private void loadTransports() {
        Map<String, Object> filters = new HashMap<>();

        String keyword = keywordField.getText();
        if (keyword != null && !keyword.isBlank()) {
            filters.put("keyword", keyword);
        }

        double minPrice = minPriceSpinner.getValue();
        double maxPrice = maxPriceSpinner.getValue();
        filters.put("minPrice", minPrice);
        filters.put("maxPrice", maxPrice);

        String transportType = transportTypeFilterCombo.getValue();
        if (transportType != null && !transportType.equals("Всі")) {
            filters.put("transport_type", transportType);
        }

        try {
            List<Transport> transports = transportService.search(filters);
            transportTable.setItems(FXCollections.observableArrayList(transports));
            if (transports.isEmpty()) {
                showInfo("За вказаними критеріями транспорт не знайдено");
            }
            logger.info("Завантажено {} записів транспорту за фільтрами", transports.size());
        } catch (SQLException e) {
            showError("Помилка завантаження транспорту: " + e.getMessage());
        }
    }

    @FXML
    public void addNewTransport() {
        showTransportEditDialog(null);
    }

    @FXML
    public void addNewTransportType() {
        showTransportTypeEditDialog(null);
    }

    @FXML
    public void editSelectedTransport() {
        Transport selectedTransport = transportTable.getSelectionModel().getSelectedItem();
        if (selectedTransport != null) {
            showTransportEditDialog(selectedTransport);
        } else {
            showError("Виберіть транспорт для редагування");
        }
    }

    @FXML
    public void editSelectedTransportType() {
        showInfo("Виберіть тип транспорту для редагування");
        showTransportTypeSelectionDialog();
    }

    void showTransportTypeSelectionDialog() {
        try {
            List<TransportType> types = transportTypeService.getAll();
            if (types.isEmpty()) {
                showError("Не знайдено жодного типу транспорту");
                return;
            }

            ChoiceDialog<TransportType> dialog = new ChoiceDialog<>(types.getFirst(), types);
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            dialog.setTitle("Вибір типу транспорту");
            dialog.setHeaderText("Виберіть тип транспорту для редагування");
            dialog.setContentText("Тип транспорту:");

            dialog.getItems().setAll(types);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return dialog.getSelectedItem();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(this::showTransportTypeEditDialog);
        } catch (SQLException e) {
            showError("Помилка завантаження типів транспорту: " + e.getMessage());
        }
    }

    @FXML
    public void deleteSelectedTransport() {
        Transport selectedTransport = transportTable.getSelectionModel().getSelectedItem();
        if (selectedTransport != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ви впевнені, що хочете видалити транспорт '" + selectedTransport.getName() + "'?",
                    ButtonType.YES, ButtonType.NO);
            Stage alertStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/checkbox.png").toExternalForm()));
            confirmAlert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            confirmAlert.setTitle("Підтвердження видалення");
            confirmAlert.setHeaderText("Видалення транспорту");

            confirmAlert.showAndWait();

            if (confirmAlert.getResult() == ButtonType.YES) {
                try {
                    transportService.delete(selectedTransport.getId());
                    loadTransports();
                    showInfo("Транспорт успішно видалено.");
                    logger.info("Видалено транспорт: {}", selectedTransport.toString());
                } catch (SQLException e) {
                    showError("Помилка видалення транспорту: " + e.getMessage());
                }
            }
        } else {
            showError("Виберіть транспорт для видалення");
        }
    }

    private void showTransportEditDialog(Transport transport) {
        try {
            TransportEditController transportEditController = controllerFactory.createTransportEditController(transport);
            transportEditController.setOnSaveCallback(this::loadTransports);
            transportEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування транспорту: " + e.getMessage());
        }
    }

    private void showTransportTypeEditDialog(TransportType transportType) {
        try {
            TransportTypeEditController transportTypeEditController = controllerFactory.createTransportTypeEditController(transportType);
            transportTypeEditController.setOnSaveCallback(() -> {
                loadFilterData();
                loadTransports();
            });
            transportTypeEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування типу транспорту: " + e.getMessage());
        }
    }

    @FXML
    private void onResetFiltersClicked() {
        resetAllFilters();
        showInfo("Фільтри скинуто до початкових значень");
        loadTransports();
    }

    private void resetAllFilters() {
        FormValidator.clearErrors();

        keywordField.clear();
        transportTypeFilterCombo.setValue("Всі");
        minPriceSpinner.getValueFactory().setValue(0.0);
        maxPriceSpinner.getValueFactory().setValue(5000.0);
    }
}