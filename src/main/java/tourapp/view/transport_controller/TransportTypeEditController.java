package tourapp.view.transport_controller;

import javafx.stage.Stage;
import tourapp.model.transport.TransportType;
import tourapp.service.transport_service.TransportTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseTypeEditController;

import java.sql.SQLException;
import java.util.List;

public class TransportTypeEditController extends BaseTypeEditController<TransportType> {

    private final TransportTypeService transportTypeService;

    public TransportTypeEditController(Stage stage,
                                       SessionManager sessionManager,
                                       TransportTypeService transportTypeService,
                                       TransportType transportTypeToEdit) {
        super(stage, sessionManager, transportTypeToEdit);
        this.transportTypeService = transportTypeService;
    }

    @Override
    protected String getTitle() {
        return (itemToEdit == null) ? "Додавання нового типу транспорту" : "Редагування типу транспорту";
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/transport/transportTypeEdit.fxml";
    }

    @Override
    protected String getLoadErrorMessage() {
        return "Не вдалося завантажити інтерфейс";
    }

    @Override
    protected String getLoadDataErrorMessage() {
        return "Помилка завантаження даних типу транспорту";
    }

    @Override
    protected String getItemTypeName() {
        return "Тип транспорту";
    }

    @Override
    protected String getDeleteHeaderText() {
        return "Видалення типу транспорту";
    }

    @Override
    protected TransportType createNewItem() {
        return new TransportType();
    }

    @Override
    protected List<TransportType> getAllItems() throws SQLException {
        return transportTypeService.getAll();
    }

    @Override
    protected void createItem(TransportType item) throws SQLException {
        transportTypeService.create(item);
    }

    @Override
    protected void updateItem(int id, TransportType item) throws SQLException {
        transportTypeService.update(id, item);
    }

    @Override
    protected void deleteItem(int id) throws SQLException {
        transportTypeService.delete(id);
    }

    @Override
    protected int getItemId(TransportType item) {
        return item.getId();
    }

    @Override
    protected String getItemName(TransportType item) {
        return item.getName();
    }

    @Override
    protected void setItemName(TransportType item, String name) {
        item.setName(name);
    }

    @Override
    protected boolean validateNameField() {
        return FormValidator.validateTransportTypeForm(nameField);
    }
}