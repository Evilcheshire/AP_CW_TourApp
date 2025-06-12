package tourapp.view.meal_controller;

import javafx.stage.Stage;
import tourapp.model.meal.MealType;
import tourapp.service.meal_service.MealTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseTypeEditController;

import java.sql.SQLException;
import java.util.List;

public class MealTypeEditController extends BaseTypeEditController<MealType> {

    private final MealTypeService mealTypeService;

    public MealTypeEditController(Stage stage,
                                  SessionManager sessionManager,
                                  MealTypeService mealTypeService,
                                  MealType mealTypeToEdit) {
        super(stage, sessionManager, mealTypeToEdit);
        this.mealTypeService = mealTypeService;
    }

    @Override
    protected String getTitle() {
        return (itemToEdit == null) ? "Додавання нового типу харчування" : "Редагування типу харчування";
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/meal/mealTypeEdit.fxml";
    }

    @Override
    protected String getLoadErrorMessage() {
        return "Не вдалося завантажити інтерфейс редагування типу харчування";
    }

    @Override
    protected String getLoadDataErrorMessage() {
        return "Помилка завантаження даних типу харчування";
    }

    @Override
    protected String getItemTypeName() {
        return "Тип харчування";
    }

    @Override
    protected String getDeleteHeaderText() {
        return "Видалення типу харчування";
    }

    @Override
    protected MealType createNewItem() {
        return new MealType();
    }

    @Override
    protected List<MealType> getAllItems() throws SQLException {
        return mealTypeService.getAll();
    }

    @Override
    protected void createItem(MealType item) throws SQLException {
        mealTypeService.create(item);
    }

    @Override
    protected void updateItem(int id, MealType item) throws SQLException {
        mealTypeService.update(id, item);
    }

    @Override
    protected void deleteItem(int id) throws SQLException {
        mealTypeService.delete(id);
    }

    @Override
    protected int getItemId(MealType item) {
        return item.getId();
    }

    @Override
    protected String getItemName(MealType item) {
        return item.getName();
    }

    @Override
    protected void setItemName(MealType item, String name) {
        item.setName(name);
    }

    @Override
    protected boolean validateNameField() {
        return FormValidator.validateMealTypeForm(nameField);
    }

}