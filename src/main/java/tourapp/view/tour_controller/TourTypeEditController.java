package tourapp.view.tour_controller;

import javafx.stage.Stage;
import tourapp.model.tour.TourType;
import tourapp.service.tour_service.TourTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseTypeEditController;

import java.sql.SQLException;
import java.util.List;

public class TourTypeEditController extends BaseTypeEditController<TourType> {

    private final TourTypeService tourTypeService;

    public TourTypeEditController(Stage stage,
                                  SessionManager sessionManager,
                                  TourTypeService tourTypeService,
                                  TourType tourTypeToEdit) {
        super(stage, sessionManager, tourTypeToEdit);
        this.tourTypeService = tourTypeService;
    }

    @Override
    protected String getTitle() {
        return (itemToEdit == null) ? "Додавання нового типу туру" : "Редагування типу туру";
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/tour/tourTypeEdit.fxml";
    }

    @Override
    protected String getLoadErrorMessage() {
        return "Не вдалося завантажити інтерфейс";
    }

    @Override
    protected String getLoadDataErrorMessage() {
        return "Помилка завантаження даних типу турів";
    }

    @Override
    protected String getItemTypeName() {
        return "Тип туру";
    }

    @Override
    protected String getDeleteHeaderText() {
        return "Видалення типу туру";
    }

    @Override
    protected TourType createNewItem() {
        return new TourType();
    }

    @Override
    protected List<TourType> getAllItems() throws SQLException {
        return tourTypeService.getAll();
    }

    @Override
    protected void createItem(TourType item) throws SQLException {
        tourTypeService.create(item);
    }

    @Override
    protected void updateItem(int id, TourType item) throws SQLException {
        tourTypeService.update(id, item);
    }

    @Override
    protected void deleteItem(int id) throws SQLException {
        tourTypeService.delete(id);
    }

    @Override
    protected int getItemId(TourType item) {
        return item.getId();
    }

    @Override
    protected String getItemName(TourType item) {
        return item.getName();
    }

    @Override
    protected void setItemName(TourType item, String name) {
        item.setName(name);
    }

    @Override
    protected boolean validateNameField() {
        return FormValidator.validateTransportTypeForm(nameField);
    }
}