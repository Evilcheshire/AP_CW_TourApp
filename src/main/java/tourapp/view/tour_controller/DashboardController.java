package tourapp.view.tour_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tourapp.model.location.Location;
import tourapp.model.meal.MealType;
import tourapp.model.tour.Tour;
import tourapp.model.tour.TourType;
import tourapp.model.transport.Transport;
import tourapp.service.location_service.*;
import tourapp.service.meal_service.*;
import tourapp.service.tour_service.*;
import tourapp.service.transport_service.*;
import tourapp.service.user_service.UserTourService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.util.validation.TourValidator;
import tourapp.util.validation.ValidationResult;
import tourapp.view.BaseController;
import tourapp.view.NavigationController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController extends BaseController {

    @FXML BorderPane mainLayout;
    @FXML TableView<Tour> tourTable;
    @FXML private TableColumn<Tour, Integer> idCol;
    @FXML private TableColumn<Tour, String> descCol;
    @FXML private TableColumn<Tour, Double> priceCol;
    @FXML private TableColumn<Tour, LocalDate> startCol;
    @FXML private TableColumn<Tour, LocalDate> endCol;
    @FXML private TableColumn<Tour, Boolean> activeCol;
    @FXML private TextField keywordField;
    @FXML Spinner<Double> minPriceSpinner;
    @FXML Spinner<Double> maxPriceSpinner;
    @FXML DatePicker startDatePicker;
    @FXML DatePicker endDatePicker;
    @FXML private Button filterButton;
    @FXML private ComboBox<String> countryFilterCombo;
    @FXML private ComboBox<String> tourTypeFilterCombo;
    @FXML private ListView<String> mealTypeFilterList;
    @FXML private ComboBox<String> transportTypeFilterCombo;
    @FXML Button addTourButton;
    @FXML Button addTourTypeButton;
    @FXML Button editTourButton;
    @FXML Button deleteTourButton;
    @FXML private Button editTourTypeButton;
    @FXML private Button toggleStatusButton;
    @FXML ScrollPane cardScrollPane;
    @FXML FlowPane tourCardContainer;
    @FXML private Button resetFiltersButton;

    private final TourService tourService;
    private final ControllerFactory controllerFactory;
    private final UserTourService userTourService;

    private final LocationService locationService;
    private final TourTypeService tourTypeService;
    private final MealTypeService mealTypeService;
    private final TransportService transportService;

    public DashboardController(Stage stage,
                               SessionManager sessionManager,
                               TourService tourService,
                               LocationService locationService,
                               TourTypeService tourTypeService,
                               MealTypeService mealTypeService,
                               TransportService transportService,
                               UserTourService userTourService,
                               ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.tourService = tourService;
        this.locationService = locationService;
        this.tourTypeService = tourTypeService;
        this.mealTypeService = mealTypeService;
        this.transportService = transportService;
        this.userTourService = userTourService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/tour/mainDashboard.fxml", "TourApp - Головна сторінка");
    }

    @FXML
    public void initialize() {
        initializeTableColumns();
        initializeFilters();
        setupAccessControls();
        initializeNavigationBar();
        switchViewMode(isCustomer());
        setupValidationListeners();
        loadTours();
    }

    void setupValidationListeners() {
        keywordField.textProperty().addListener((observable, oldValue, newValue) -> {
            FormValidator.clearErrorsForControls(keywordField);
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            FormValidator.clearErrorsForControls(startDatePicker.getEditor());
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            FormValidator.clearErrorsForControls(endDatePicker.getEditor());
        });

        minPriceSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            FormValidator.clearErrorsForControls(minPriceSpinner.getEditor());
        });

        maxPriceSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            FormValidator.clearErrorsForControls(maxPriceSpinner.getEditor());
        });
    }

    private boolean validateFilters() {

        boolean isValid = FormValidator.validateTourFilterForm(
                keywordField,
                minPriceSpinner,
                maxPriceSpinner,
                startDatePicker,
                endDatePicker
        );

        if (!isValid) {
            FormValidator.showValidationErrors(stage);
        }

        return isValid;
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
        addTourButton.setVisible(isAdmin() || isManager());
        addTourButton.setManaged(isAdmin() || isManager());
        editTourButton.setVisible(isAdmin() || isManager());
        editTourButton.setManaged(isAdmin() || isManager());
        deleteTourButton.setVisible(isAdmin() || isManager());
        deleteTourButton.setManaged(isAdmin() || isManager());
        toggleStatusButton.setVisible(isAdmin() || isManager());
        toggleStatusButton.setManaged(isAdmin() || isManager());
        addTourTypeButton.setVisible(isAdmin());
        addTourTypeButton.setManaged(isAdmin());
        editTourTypeButton.setVisible(isAdmin());
        editTourTypeButton.setManaged(isAdmin());
    }

    void switchViewMode(boolean isCustomer) {
        tourTable.setVisible(!isCustomer);
        tourTable.setManaged(!isCustomer);
        cardScrollPane.setVisible(isCustomer);
        cardScrollPane.setManaged(isCustomer);
    }

    void displayToursAsCards(List<Tour> tours) {
        tourCardContainer.getChildren().clear();

        for (Tour tour : tours) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(10));
            card.setPrefWidth(250);
            card.getStyleClass().add("user-dashboard-card");

            Label title = new Label(tour.getDescription());
            title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            title.setWrapText(true);

            Label price = new Label(String.format("Ціна: %.2f грн", tour.getPrice()));
            Label dates = new Label(String.format("Дата: %s - %s",
                    tour.getStartDate().toString(), tour.getEndDate().toString()));
            dates.setWrapText(true);

            Button bookButton = new Button("Забронювати");
            bookButton.setOnAction(e -> handleBooking(tour));

            card.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    showTourDetails(tour);
                }
            });

            card.getChildren().addAll(title, price, dates, bookButton);
            tourCardContainer.getChildren().add(card);
        }
    }

    void initializeTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Активний" : "Неактивний");
                }
            }
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

        tourTable.setRowFactory(tv -> {
            TableRow<Tour> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Tour tour = row.getItem();
                    showTourDetails(tour);
                }
            });
            return row;
        });
    }

    void showTourDetails(Tour tour) {
        try {
            Tour tourWithDetails = tourService.getByIdWithDependencies(tour.getId());
            System.out.println(tour);
            if (tourWithDetails != null) {
                StringBuilder details = new StringBuilder();
                details.append("Опис: ").append(tourWithDetails.getDescription()).append("\n");
                details.append("Ціна: ").append(String.format("%.2f грн", tourWithDetails.getPrice())).append("\n");
                details.append("Період: ").append(tourWithDetails.getStartDate()).append(" - ").append(tourWithDetails.getEndDate()).append("\n");
                details.append("Статус: ").append(tourWithDetails.isActive() ? "Активний" : "Неактивний").append("\n");

                if (tourWithDetails.getType() != null) {
                    details.append("Тип туру: ").append(tourWithDetails.getType().getName()).append("\n");
                }

                if (tourWithDetails.getMeal() != null) {
                    details.append("Харчування: ").append(tourWithDetails.getMeal().getName()).append("\n");
                }

                if (tourWithDetails.getTransport() != null) {
                    details.append("Транспорт: ").append(tourWithDetails.getTransport().getName()).append("\n");
                }

                List<Location> tourLocations;
                if (tourWithDetails.getLocations() != null && !tourWithDetails.getLocations().isEmpty()) {
                    tourLocations = tourWithDetails.getLocations();
                } else {
                    tourLocations = tourService.getLocationsForTour(tourWithDetails.getId());
                }

                if (tourLocations != null && !tourLocations.isEmpty()) {
                    details.append("Локації: ");
                    String locations = tourLocations.stream()
                            .map(location -> location.getName() + " (" + location.getCountry() + ")")
                            .collect(Collectors.joining(", "));
                    details.append(locations);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
                alert.setTitle("Деталі туру");
                alert.setHeaderText("Тур #" + tourWithDetails.getId());
                alert.setContentText(details.toString());
                alert.showAndWait();
            } else {
                showError("Не вдалося знайти тур з ID: " + tour.getId());
            }
        } catch (SQLException e) {
            showError("Помилка при отриманні деталей туру: " + e.getMessage());
            
        }
    }

    void initializeFilters() {
        try {
            SpinnerValueFactory<Double> minFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100000, 0, 500);
            SpinnerValueFactory<Double> maxFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100000, 50000, 500);
            minPriceSpinner.setValueFactory(minFactory);
            maxPriceSpinner.setValueFactory(maxFactory);

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

            ObservableList<String> tourTypes = FXCollections.observableArrayList("Всі");
            List<TourType> types = tourTypeService.getAll();
            types.stream()
                    .map(TourType::getName)
                    .sorted()
                    .forEach(tourTypes::add);
            tourTypeFilterCombo.setItems(tourTypes);
            tourTypeFilterCombo.setValue("Всі");

            ObservableList<String> mealTypes = FXCollections.observableArrayList();
            List<MealType> meal_type_list = mealTypeService.getAll();
            meal_type_list.stream()
                    .map(MealType::getName)
                    .sorted()
                    .forEach(mealTypes::add);
            mealTypeFilterList.setItems(mealTypes);
            mealTypeFilterList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            ObservableList<String> transportTypes = FXCollections.observableArrayList("Всі");
            List<Transport> transports = transportService.getAll();
            transports.stream()
                    .map(Transport::getName)
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
            loadTours();
        }
    }

    @FXML
    void handleBooking(Tour tour) {
        if (!validateTourForBooking(tour)) {
            FormValidator.showValidationErrors(stage);
            resetAllFilters();
            return;
        }

        try {
            int userId = sessionManager.getCurrentSession().user().getId();
            int tourId = tour.getId();

            if (userTourService.exists(userId, tourId)) {
                showInfo("Ви вже забронювали цей тур");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            Stage alertStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            confirmAlert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/checkbox.png").toExternalForm()));
            confirmAlert.setTitle("Підтвердження бронювання");
            confirmAlert.setHeaderText("Бронювання туру");

            StringBuilder confirmText = new StringBuilder();
            confirmText.append("Ви збираєтесь забронювати тур:\n\n");
            confirmText.append("Назва: ").append(tour.getDescription()).append("\n");
            confirmText.append("Ціна: ").append(String.format("%.2f грн", tour.getPrice())).append("\n");
            confirmText.append("Дати: ").append(tour.getStartDate()).append(" - ").append(tour.getEndDate()).append("\n\n");
            confirmText.append("Підтвердити бронювання?");

            confirmAlert.setContentText(confirmText.toString());
            confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        boolean success = userTourService.createLink(userId, tourId);

                        if (success) {
                            showSuccessBookingDialog(tour);
                        } else {
                            showError("Помилка при створенні бронювання. Спробуйте ще раз.");
                        }
                    } catch (SQLException e) {
                        if (e.getMessage().contains("уже існує")) {
                            showInfo("Ви вже забронювали цей тур");
                        } else {
                            showError("Помилка при бронюванні туру: " + e.getMessage());
                        }
                        
                    } catch (IllegalArgumentException e) {
                        showInfo("Ви вже забронювали цей тур");
                    }
                }
            });

        } catch (Exception e) {
            showError("Помилка при бронюванні туру: " + e.getMessage());
            
        }
    }

    boolean validateTourForBooking(Tour tour) {
        if (tour == null) {
            showError("Помилка: тур не знайдено");
            return false;
        }

        LocalDate now = LocalDate.now();
        if (tour.getStartDate().isBefore(now)) {
            showError("Неможливо забронювати тур, який вже почався");
            return false;
        }

        if (tour.getEndDate().isBefore(now)) {
            showError("Неможливо забронювати тур, який вже закінчився");
            return false;
        }

        ValidationResult priceResult = FormValidator.validatePriceRange(String.valueOf(tour.getPrice()));
        if (!priceResult.valid()) {
            showError("Помилка з ціною туру: " + priceResult.errorMessage());
            return false;
        }

        ValidationResult descResult = TourValidator.validateTourName(tour.getDescription());
        if (!descResult.valid()) {
            showError("Помилка з описом туру: " + descResult.errorMessage());
            return false;
        }

        ValidationResult dateResult = TourValidator.validateDateRange(tour.getStartDate(), tour.getEndDate());
        if (!dateResult.valid()) {
            showError("Помилка з датами туру: " + dateResult.errorMessage());
            return false;
        }

        return true;
    }

    void showSuccessBookingDialog(Tour tour) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        Stage alertStage = (Stage) successAlert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
        successAlert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
        successAlert.setTitle("Успішне бронювання");
        successAlert.setHeaderText("Тур успішно заброньовано!");

        StringBuilder successText = new StringBuilder();
        successText.append("Ви успішно забронювали тур:\n\n");
        successText.append("Назва: ").append(tour.getDescription()).append("\n");
        successText.append("Ціна: ").append(String.format("%.2f грн", tour.getPrice())).append("\n");
        successText.append("Дати: ").append(tour.getStartDate()).append(" - ").append(tour.getEndDate()).append("\n\n");
        successText.append("Ви можете переглянути всі свої бронювання в розділі 'Заброньовані тури'.");

        successAlert.setContentText(successText.toString());

        ButtonType viewBookedButton = new ButtonType("Переглянути заброньовані");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        successAlert.getButtonTypes().setAll(viewBookedButton, okButton);

        successAlert.showAndWait().ifPresent(response -> {
            if (response == viewBookedButton) {
                try {
                    FormValidator.clearErrors();
                    controllerFactory.createBookedToursController().show();
                } catch (Exception e) {
                    showError("Помилка переходу до заброньованих турів: " + e.getMessage());
                }
            }
        });
    }

    void loadTours() {
        Map<String, Object> filters = new HashMap<>();

        String keyword = keywordField.getText();
        if (keyword != null && !keyword.isBlank()) {
            filters.put("description", keyword);
        }

        double minPrice = minPriceSpinner.getValue();
        double maxPrice = maxPriceSpinner.getValue();
        filters.put("minPrice", minPrice);
        filters.put("maxPrice", maxPrice);

        if (startDatePicker.getValue() != null) {
            filters.put("startDate", startDatePicker.getValue());
        }

        if (endDatePicker.getValue() != null) {
            filters.put("endDate", endDatePicker.getValue());
        }

        String country = countryFilterCombo.getValue();
        if (country != null && !country.equals("Всі")) {
            filters.put("country", country);
        }

        String tourType = tourTypeFilterCombo.getValue();
        if (tourType != null && !tourType.equals("Всі")) {
            filters.put("tour_type", tourType);
        }

        List<String> selectedMealTypes = mealTypeFilterList.getSelectionModel().getSelectedItems();
        if (selectedMealTypes != null && !selectedMealTypes.isEmpty()) {
            filters.put("meal_types", selectedMealTypes);
        }

        String transportType = transportTypeFilterCombo.getValue();
        if (transportType != null && !transportType.equals("Всі")) {
            filters.put("transport_type", transportType);
        }

        if (isCustomer()) {
            filters.put("is_active", true);
        }

        try {
            List<Tour> tours = tourService.search(filters);
            if (isCustomer()) {
                displayToursAsCards(tours);
            } else {
                tourTable.setItems(FXCollections.observableArrayList(tours));
            }
            if (tours.isEmpty()) {
                showInfo("За вказаними критеріями турів не знайдено");
            }
        } catch (SQLException e) {
            showError("Помилка завантаження турів: " + e.getMessage());
        }
    }

    @FXML
    public void addNewTour() {
        showTourEditDialog(null);
    }

    @FXML
    public void editSelectedTour() {
        Tour selectedTour = tourTable.getSelectionModel().getSelectedItem();
        if (selectedTour != null) {
            showTourEditDialog(selectedTour);
        } else {
            showInfo("Виберіть тур для редагування");
        }

    }

    private void showTourEditDialog(Tour tour) {
        try {
            TourEditController tourEditController = controllerFactory.createTourEditController(tour);
            tourEditController.setOnSaveCallback(this::loadTours);
            tourEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування туру: " + e.getMessage());
        }
    }

    @FXML
    public void addNewTourType() {
        try {
            showTourTypeEditDialog(null);
        } catch (Exception e) {
            showError("Помилка відкриття форми створення типу туру: " + e.getMessage());
        }
    }

    @FXML
    public void editSelectedTourType() {
        if (isAdmin() || isManager()) {
            try {
                List<TourType> types = tourTypeService.getAll();
                if (types.isEmpty()) {
                    showInfo("Типи турів відсутні");
                    return;
                }

                ChoiceDialog<TourType> dialog = new ChoiceDialog<>(types.getFirst(), types);
                Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
                dialog.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
                dialog.setTitle("Вибір типу туру");
                dialog.setHeaderText("Виберіть тип туру для редагування");
                dialog.setContentText("Тип туру:");

                dialog.showAndWait().ifPresent(selectedType -> {
                    try {
                        showTourTypeEditDialog(selectedType);
                    } catch (Exception e) {
                        showError("Помилка відкриття форми редагування типу туру: " + e.getMessage());
                    }
                });
            } catch (SQLException e) {
                showError("Помилка при отриманні списку типів турів: " + e.getMessage());
            }
        } else {
            showInfo("У вас немає прав для редагування типів турів");
        }
    }

    private void showTourTypeEditDialog(TourType tourType) {
        try {
            TourTypeEditController tourTypeEditController = controllerFactory.createTourTypeEditController(tourType);
            tourTypeEditController.setOnSaveCallback(() -> {
                loadFilterData();
                loadTours();
            });
            tourTypeEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування типу харчування: " + e.getMessage());
        }
    }

    @FXML
    public void deleteSelectedTour() {
        if (isAdmin() || isManager()) {
            Tour selectedTour = tourTable.getSelectionModel().getSelectedItem();
            if (selectedTour != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Ви впевнені, що хочете видалити тур '" + selectedTour.getDescription() + "'?",
                        ButtonType.YES, ButtonType.NO);
                Stage alertStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
                confirmAlert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
                alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/checkbox.png").toExternalForm()));
                confirmAlert.showAndWait();

                if (confirmAlert.getResult() == ButtonType.YES) {
                    try {
                        tourService.delete(selectedTour.getId());
                        loadTours();
                        showInfo("Тур успішно видалено.");
                    } catch (SQLException e) {
                        showError("Помилка видалення туру: " + e.getMessage());
                    }
                }
            } else {
                showError("Виберіть тур для видалення");
            }
        }
    }

    @FXML
    public void toggleTourStatus() {
        if (isAdmin() || isManager()) {
            Tour selectedTour = tourTable.getSelectionModel().getSelectedItem();
            if (selectedTour != null) {
                try {
                    boolean newStatus = !selectedTour.isActive();
                    tourService.toggleActiveStatus(selectedTour.getId(), newStatus);
                    System.out.println(selectedTour);
                    loadTours();
                    showInfo("Статус туру змінено на " + (newStatus ? "активний" : "неактивний") + ".");
                } catch (SQLException e) {
                    showError("Помилка зміни статусу туру: " + e.getMessage());
                }
            } else {
                showError("Виберіть тур для зміни статусу");
            }
        }
    }

    @FXML
    private void onResetFiltersClicked() {
        resetAllFilters();
        showInfo("Фільтри скинуто до початкових значень");
        loadTours();
    }

    void resetAllFilters() {
        FormValidator.clearErrors();

        keywordField.clear();
        countryFilterCombo.setValue("Всі");
        tourTypeFilterCombo.setValue("Всі");
        transportTypeFilterCombo.setValue("Всі");
        minPriceSpinner.getValueFactory().setValue(0.0);
        maxPriceSpinner.getValueFactory().setValue(50000.0);
        mealTypeFilterList.getSelectionModel().clearSelection();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }
}