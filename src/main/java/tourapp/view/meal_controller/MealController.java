package tourapp.view.meal_controller;

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
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.service.meal_service.MealMealTypeService;
import tourapp.service.meal_service.MealService;
import tourapp.service.meal_service.MealTypeService;
import tourapp.util.ControllerFactory;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseController;
import tourapp.view.NavigationController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MealController extends BaseController {

    @FXML BorderPane mainLayout;
    @FXML TableView<Meal> mealTable;
    @FXML private TableColumn<Meal, Integer> idCol;
    @FXML private TableColumn<Meal, String> nameCol;
    @FXML private TableColumn<Meal, Integer> mealsPerDayCol;
    @FXML private TableColumn<Meal, Double> costCol;
    @FXML private TableColumn<Meal, String> mealTypesCol;
    @FXML private TextField keywordField;
    @FXML private ComboBox<String> mealTypeFilterCombo;
    @FXML private Spinner<Double> minPriceSpinner;
    @FXML private Spinner<Double> maxPriceSpinner;
    @FXML private Spinner<Integer> minMealsSpinner;
    @FXML private Spinner<Integer> maxMealsSpinner;
    @FXML private Button filterButton;
    @FXML private Button addMealButton;
    @FXML private Button addMealTypeButton;
    @FXML private Button editMealButton;
    @FXML private Button editMealTypeButton;
    @FXML private Button deleteMealButton;
    @FXML private StackPane viewContainer;

    private final MealService mealService;
    private final MealTypeService mealTypeService;
    private final MealMealTypeService mealMealTypeService;
    private final ControllerFactory controllerFactory;

    public MealController(Stage stage,
                          SessionManager sessionManager,
                          MealService mealService,
                          MealTypeService mealTypeService,
                          MealMealTypeService mealMealTypeService,
                          ControllerFactory controllerFactory) {
        super(stage, sessionManager);
        this.mealService = mealService;
        this.mealTypeService = mealTypeService;
        this.mealMealTypeService = mealMealTypeService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void show() {
        loadAndShow("/tourapp/view/meal/mealDashboard.fxml", "TourApp - Управління харчуванням");
    }

    @FXML
    public void initialize() {
        initializeTableColumns();
        initializeFilters();
        setupAccessControls();
        initializeNavigationBar();
        setupValidationListeners();
        loadMeals();
    }

    private void setupValidationListeners() {
        keywordField.textProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(keywordField);
        });

        minPriceSpinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(minPriceSpinner.getEditor());
        });

        maxPriceSpinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(maxPriceSpinner.getEditor());
        });

        minMealsSpinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(minMealsSpinner.getEditor());
        });

        maxMealsSpinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(maxMealsSpinner.getEditor());
        });

        mealTypeFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            FormValidator.clearErrorsForControls(mealTypeFilterCombo);
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

    void setupAccessControls() {
        addMealButton.setVisible(isAdmin() || isManager());
        addMealButton.setManaged(isAdmin() || isManager());
        editMealButton.setVisible(isAdmin() || isManager());
        editMealButton.setManaged(isAdmin() || isManager());
        deleteMealButton.setVisible(isAdmin() || isManager());
        deleteMealButton.setManaged(isAdmin() || isManager());
        addMealTypeButton.setVisible(isAdmin());
        addMealTypeButton.setManaged(isAdmin());
        editMealTypeButton.setVisible(isAdmin());
        editMealTypeButton.setManaged(isAdmin());
    }

    private void initializeTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        mealsPerDayCol.setCellValueFactory(new PropertyValueFactory<>("mealsPerDay"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("costPerDay"));

        mealTypesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Meal meal = getTableView().getItems().get(getIndex());
                    try {
                        List<MealType> types = mealMealTypeService.findById1(meal.getId());
                        String typesStr = types.stream()
                                .map(MealType::getName)
                                .collect(Collectors.joining(", "));
                        setText(typesStr);
                    } catch (SQLException e) {
                        setText("Помилка завантаження");
                    }
                }
            }
        });

        costCol.setCellFactory(col -> new TableCell<>() {
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

        mealTable.setRowFactory(tv -> {
            TableRow<Meal> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Meal meal = row.getItem();
                    showMealDetails(meal);
                }
            });
            return row;
        });
    }

    void showMealDetails(Meal meal) {
        try {
            Meal mealWithDetails = mealService.getById(meal.getId());
            List<MealType> mealTypes = meal.getMealTypes();

            StringBuilder details = new StringBuilder();
            details.append("Назва: ").append(mealWithDetails.getName()).append("\n");
            details.append("Кількість прийомів їжі: ").append(mealWithDetails.getMealsPerDay()).append("\n");
            details.append("Вартість за день: ").append(String.format("%.2f грн", mealWithDetails.getCostPerDay())).append("\n");

            if (mealTypes != null && !mealTypes.isEmpty()) {
                details.append("Типи харчування: ");
                String types = mealTypes.stream()
                        .map(MealType::getName)
                        .collect(Collectors.joining(", "));
                details.append(types);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            alert.setTitle("Деталі харчування");
            alert.setHeaderText("Харчування #" + mealWithDetails.getId());
            alert.setContentText(details.toString());
            alert.showAndWait();
        } catch (SQLException e) {
            showError("Помилка при отриманні деталей харчування: " + e.getMessage());
        }
    }

    private void initializeFilters() {
        try {
            SpinnerValueFactory<Double> minFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 0, 50);
            SpinnerValueFactory<Double> maxFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 1000, 50);
            SpinnerValueFactory<Integer> minMealsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1);
            SpinnerValueFactory<Integer> maxMealsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5, 1);

            minPriceSpinner.setValueFactory(minFactory);
            maxPriceSpinner.setValueFactory(maxFactory);
            minMealsSpinner.setValueFactory(minMealsFactory);
            maxMealsSpinner.setValueFactory(maxMealsFactory);

            loadFilterData();
        } catch (Exception e) {
            showError("Помилка ініціалізації фільтрів: " + e.getMessage());
        }
    }

    private void loadFilterData() {
        try {
            ObservableList<String> mealTypes = FXCollections.observableArrayList("Всі");
            List<MealType> types = mealTypeService.getAll();
            types.stream()
                    .map(MealType::getName)
                    .sorted()
                    .forEach(mealTypes::add);

            mealTypeFilterCombo.setItems(mealTypes);
            mealTypeFilterCombo.setValue("Всі");
        } catch (SQLException e) {
            showError("Помилка завантаження даних для фільтрів: " + e.getMessage());
        }
    }

    @FXML
    private void onFilterButtonClicked() {
        if (validateFilters()) {
            loadMeals();
        }
    }

    private boolean validateFilters() {
        boolean isValid = FormValidator.validateMealFilterForm(
                keywordField,
                minPriceSpinner,
                maxPriceSpinner,
                mealTypeFilterCombo
        );

        if (isValid) {
            int minMeals = minMealsSpinner.getValue();
            int maxMeals = maxMealsSpinner.getValue();

            if (minMeals > maxMeals) {
                isValid = false;
            }
        }

        if (!isValid) {
            FormValidator.showValidationErrors(stage);
            resetAllFilters();
        }

        return isValid;
    }

    private void loadMeals() {
        Map<String, Object> filters = new HashMap<>();

        try {
            String keyword = keywordField.getText();
            if (keyword != null && !keyword.isBlank()) {
                filters.put("name", keyword.trim());
            }

            double minPrice = minPriceSpinner.getValue();
            double maxPrice = maxPriceSpinner.getValue();

            if (minPrice >= 0 && maxPrice >= 0 && minPrice <= maxPrice) {
                filters.put("minPrice", minPrice);
                filters.put("maxPrice", maxPrice);
            }

            int minMeals = minMealsSpinner.getValue();
            int maxMeals = maxMealsSpinner.getValue();

            if (minMeals >= 1 && maxMeals >= 1 && minMeals <= maxMeals) {
                filters.put("minMealsPerDay", minMeals);
                filters.put("maxMealsPerDay", maxMeals);
            }

            String mealType = mealTypeFilterCombo.getValue();
            if (mealType != null && !mealType.equals("Всі")) {
                filters.put("meal_type", mealType);
            }

            List<Meal> meals = mealService.search(filters);

            mealTable.setItems(FXCollections.observableArrayList(meals));

            if (meals.isEmpty()) {
                showInfo("За вказаними критеріями харчування не знайдено");
            }

        } catch (SQLException e) {
            showError("Помилка завантаження даних харчування: " + e.getMessage());
            
        }
    }

    @FXML
    public void addNewMeal() {
        showMealEditDialog(null);
    }

    @FXML
    public void addNewMealType() {
        showMealTypeEditDialog(null);
    }

    @FXML
    public void editSelectedMeal() {
        Meal selectedMeal = mealTable.getSelectionModel().getSelectedItem();
        if (selectedMeal != null) {
            try {
                showMealEditDialog(selectedMeal);
            } catch (Exception e) {
                showError("Помилка відкриття форми редагування харчування: " + e.getMessage());
                
            }
        } else {
            showError("Виберіть харчування для редагування");
        }
    }

    @FXML
    public void editSelectedMealType() {
        try {
            List<MealType> types = mealTypeService.getAll();
            if (types.isEmpty()) {
                showInfo("Типи харчування відсутні");
                return;
            }

            ChoiceDialog<MealType> dialog = new ChoiceDialog<>(null, types);
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            dialog.setTitle("Вибір типу харчування");
            dialog.setHeaderText("Виберіть тип харчування для редагування");
            dialog.setContentText("Тип харчування:");

            dialog.showAndWait().ifPresent(selectedType -> {
                try {
                    showMealTypeEditDialog(selectedType);
                    System.out.println(selectedType.getName());
                } catch (Exception e) {
                    showError("Помилка відкриття форми редагування типу харчування: " + e.getMessage());
                    
                }
            });
        } catch (SQLException e) {
            showError("Помилка при отриманні списку типів харчування: " + e.getMessage());
            
        }
    }

    @FXML
    public void deleteSelectedMeal() {
        Meal selectedMeal = mealTable.getSelectionModel().getSelectedItem();
        if (selectedMeal != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ви впевнені, що хочете видалити харчування '" + selectedMeal.getName() + "'?",
                    ButtonType.YES, ButtonType.NO);
            Stage alertStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            confirmAlert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/checkbox.png").toExternalForm()));
            confirmAlert.setTitle("Підтвердження видалення");
            confirmAlert.setHeaderText("Видалення харчування");

            confirmAlert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    try {
                        mealService.delete(selectedMeal.getId());
                        loadMeals();
                        showInfo("Харчування '" + selectedMeal.getName() + "' успішно видалено");
                        logger.info("Видалено харчування: {}", selectedMeal.toString());
                    } catch (SQLException e) {
                        showError("Помилка видалення харчування: " + e.getMessage());
                        
                    }
                }
            });
        } else {
            showError("Виберіть харчування для видалення");
        }
    }

    private void showMealEditDialog(Meal meal) {
        try {
            MealEditController mealEditController = controllerFactory.createMealEditController(meal);
            mealEditController.setOnSaveCallback(() -> {
                loadMeals();
                loadFilterData();
            });
            mealEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування харчування: " + e.getMessage());
            
        }
    }

    private void showMealTypeEditDialog(MealType mealType) {
        try {
            MealTypeEditController mealTypeEditController = controllerFactory.createMealTypeEditController(mealType);
            mealTypeEditController.setOnSaveCallback(() -> {
                loadFilterData();
                loadMeals();
            });
            mealTypeEditController.show();
        } catch (Exception e) {
            showError("Помилка відкриття форми редагування типу харчування: " + e.getMessage());
            
        }
    }

    @FXML
    private void onResetFiltersClicked() {
        resetAllFilters();
        showInfo("Фільтри скинуто до початкових значень");
        loadMeals();
    }

    private void resetAllFilters() {
        FormValidator.clearErrors();

        keywordField.clear();
        mealTypeFilterCombo.setValue("Всі");
        minPriceSpinner.getValueFactory().setValue(0.0);
        maxPriceSpinner.getValueFactory().setValue(1000.0);
        minMealsSpinner.getValueFactory().setValue(1);
        maxMealsSpinner.getValueFactory().setValue(5);
    }
}