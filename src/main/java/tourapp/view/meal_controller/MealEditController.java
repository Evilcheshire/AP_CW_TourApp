package tourapp.view.meal_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.service.meal_service.MealService;
import tourapp.service.meal_service.MealTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.BaseValidator;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseEditController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MealEditController extends BaseEditController<Meal> {

    @FXML private TextField nameField;
    @FXML private Spinner<Integer> mealsPerDaySpinner;
    @FXML private TextField costPerDayField;
    @FXML private ListView<MealType> availableMealTypesListView;
    @FXML private ListView<MealType> selectedMealTypesListView;
    @FXML private Button addMealTypeButton;
    @FXML private Button removeMealTypeButton;

    private ComboBox<String> mealTypeComboBox;

    private final MealService mealService;
    private final MealTypeService mealTypeService;

    private ObservableList<MealType> availableMealTypes;
    private ObservableList<MealType> selectedMealTypes;

    public MealEditController(Stage stage,
                              SessionManager sessionManager,
                              MealService mealService,
                              MealTypeService mealTypeService,
                              Meal mealToEdit) {
        super(stage, sessionManager, mealToEdit);
        this.mealService = mealService;
        this.mealTypeService = mealTypeService;
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/meal/mealEdit.fxml";
    }

    @Override
    protected String getWindowTitle() {
        return isCreateMode() ? "Додавання нового харчування" : "Редагування харчування";
    }

    @Override
    public void initialize() {
        initializeSpinner();
        initializeMealTypesLists();
        initializeVirtualComboBox();
        loadMealTypesData();
        super.initialize();
    }

    private void initializeSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3);
        mealsPerDaySpinner.setValueFactory(valueFactory);
    }

    private void initializeMealTypesLists() {
        availableMealTypes = FXCollections.observableArrayList();
        selectedMealTypes = FXCollections.observableArrayList();

        availableMealTypesListView.setItems(availableMealTypes);
        selectedMealTypesListView.setItems(selectedMealTypes);

        availableMealTypesListView.setCellFactory(lv -> new ListCell<MealType>() {
            @Override
            protected void updateItem(MealType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
            }
        });

        selectedMealTypesListView.setCellFactory(lv -> new ListCell<MealType>() {
            @Override
            protected void updateItem(MealType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
            }
        });

        availableMealTypesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedMealTypesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initializeVirtualComboBox() {
        mealTypeComboBox = new ComboBox<>();
        updateVirtualComboBox();
    }

    private void updateVirtualComboBox() {
        if (selectedMealTypes.isEmpty()) {
            mealTypeComboBox.setValue(null);
        } else {
            mealTypeComboBox.setValue("selected");
        }
    }

    @Override
    protected void setupValidationListeners() {
        addValidationListener(nameField);
        addValidationListener(costPerDayField);

        mealsPerDaySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            BaseValidator.clearErrorsForControls(mealsPerDaySpinner.getEditor());
        });

        selectedMealTypes.addListener((javafx.collections.ListChangeListener<MealType>) change -> {
            BaseValidator.clearErrorsForControls(selectedMealTypesListView);
            updateVirtualComboBox();
        });
    }

    private void loadMealTypesData() {
        try {
            List<MealType> allMealTypes = mealTypeService.getAll();
            availableMealTypes.clear();
            availableMealTypes.addAll(allMealTypes);
        } catch (SQLException e) {
            showError("Помилка завантаження типів харчування: " + e.getMessage());
        }
    }

    @Override
    protected void loadEntityData() throws SQLException {
        Meal fullMeal = mealService.getById(entityToEdit.getId());

        if (fullMeal != null) {
            nameField.setText(fullMeal.getName());
            mealsPerDaySpinner.getValueFactory().setValue(fullMeal.getMealsPerDay());
            costPerDayField.setText(String.valueOf(fullMeal.getCostPerDay()));

            List<MealType> mealTypes = fullMeal.getMealTypes();
            if (mealTypes != null) {
                selectedMealTypes.addAll(mealTypes);
                availableMealTypes.removeAll(mealTypes);
            }
            updateVirtualComboBox();
        }
    }

    @Override
    protected boolean validateForm() {
        TextField mealsPerDayField = new TextField(String.valueOf(mealsPerDaySpinner.getValue()));

        boolean isValid = FormValidator.validateMealForm(
                nameField,
                mealsPerDayField,
                mealTypeComboBox
        );

        if (isValid) {
            try {
                if (FormValidator.isDuplicateName(
                        mealService.getAll(),
                        nameField.getText().trim(),
                        Meal::getName,
                        Meal::getId,
                        isCreateMode() ? null : entityToEdit.getId()
                )) {
                    String duplicateError = "Харчування з такою назвою вже існує";
                    BaseValidator.addError(nameField, duplicateError);
                    isValid = false;
                }
            } catch (SQLException e) {
                showError("Помилка перевірки дублювання назви: " + e.getMessage());
                return false;
            }
        }

        if (isValid && !costPerDayField.getText().trim().isEmpty()) {
            try {
                double cost = Double.parseDouble(costPerDayField.getText().trim());
                if (cost < 0) {
                    BaseValidator.addError(costPerDayField, "Вартість не може бути від'ємною");
                    isValid = false;
                } else if (cost > 10000) {
                    BaseValidator.addError(costPerDayField, "Вартість занадто велика");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                BaseValidator.addError(costPerDayField, "Неправильний формат вартості");
                isValid = false;
            }
        }

        if (isValid && selectedMealTypes.isEmpty()) {
            String mealTypesError = "Виберіть принаймні один тип харчування";
            BaseValidator.addError(selectedMealTypesListView, mealTypesError);
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected Meal createEntityFromForm() {
        Meal meal = new Meal();
        updateEntityFromForm(meal);
        return meal;
    }

    @Override
    protected void updateEntityFromForm(Meal meal) {
        meal.setName(nameField.getText().trim());
        meal.setMealsPerDay(mealsPerDaySpinner.getValue());
        meal.setCostPerDay(Double.parseDouble(costPerDayField.getText().trim()));
        meal.setMealTypes(new ArrayList<>(selectedMealTypes));
    }

    @Override
    protected void saveNewEntity(Meal meal) throws SQLException {
        mealService.create(meal);
    }

    @Override
    protected void updateExistingEntity(Meal meal) throws SQLException {
        mealService.update(meal);
    }

    @Override
    protected String getCreateSuccessMessage() {
        return "Харчування успішно створено!";
    }

    @Override
    protected String getUpdateSuccessMessage() {
        return "Харчування успішно оновлено!";
    }

    @FXML
    private void handleAddMealType() {
        ObservableList<MealType> selected = availableMealTypesListView.getSelectionModel().getSelectedItems();
        if (!selected.isEmpty()) {
            List<MealType> toMove = new ArrayList<>(selected);
            selectedMealTypes.addAll(toMove);
            availableMealTypes.removeAll(toMove);
            availableMealTypesListView.getSelectionModel().clearSelection();
            updateVirtualComboBox();
        }
    }

    @FXML
    private void handleRemoveMealType() {
        ObservableList<MealType> selected = selectedMealTypesListView.getSelectionModel().getSelectedItems();
        if (!selected.isEmpty()) {
            List<MealType> toMove = new ArrayList<>(selected);
            availableMealTypes.addAll(toMove);
            selectedMealTypes.removeAll(toMove);
            selectedMealTypesListView.getSelectionModel().clearSelection();
            updateVirtualComboBox();
        }
    }

    @FXML
    protected void handleSave() {
        super.handleSave();
    }

    @FXML
    protected void handleCancel() {
        super.handleCancel();
    }
}