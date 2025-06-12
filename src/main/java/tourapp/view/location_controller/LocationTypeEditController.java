package tourapp.view.location_controller;

import javafx.stage.Stage;
import tourapp.model.location.LocationType;
import tourapp.service.location_service.LocationTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseTypeEditController;

import java.sql.SQLException;
import java.util.List;

public class LocationTypeEditController extends BaseTypeEditController<LocationType> {

    private final LocationTypeService locationTypeService;

    public LocationTypeEditController(Stage stage,
                                      SessionManager sessionManager,
                                      LocationTypeService locationTypeService,
                                      LocationType locationTypeToEdit) {
        super(stage, sessionManager, locationTypeToEdit);
        this.locationTypeService = locationTypeService;
    }

    @Override
    protected String getTitle() {
        return (itemToEdit == null) ? "Додавання нового типу локації" : "Редагування типу локації";
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/location/locationTypeEdit.fxml";
    }

    @Override
    protected String getLoadErrorMessage() {
        return "Не вдалося завантажити інтерфейс";
    }

    @Override
    protected String getLoadDataErrorMessage() {
        return "Помилка завантаження даних типу локації";
    }

    @Override
    protected String getItemTypeName() {
        return "Тип локації";
    }

    @Override
    protected String getDeleteHeaderText() {
        return "Видалення типу локації";
    }

    @Override
    protected LocationType createNewItem() {
        return new LocationType();
    }

    @Override
    protected List<LocationType> getAllItems() throws SQLException {
        return locationTypeService.getAll();
    }

    @Override
    protected void createItem(LocationType item) throws SQLException {
        locationTypeService.create(item);
    }

    @Override
    protected void updateItem(int id, LocationType item) throws SQLException {
        locationTypeService.update(id, item);
    }

    @Override
    protected void deleteItem(int id) throws SQLException {
        locationTypeService.delete(id);
    }

    @Override
    protected int getItemId(LocationType item) {
        return item.getId();
    }

    @Override
    protected String getItemName(LocationType item) {
        return item.getName();
    }

    @Override
    protected void setItemName(LocationType item, String name) {
        item.setName(name);
    }

    @Override
    protected boolean validateNameField() {
        return FormValidator.validateLocationTypeForm(nameField);
    }
}