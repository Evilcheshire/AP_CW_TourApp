package tourapp.view.transport_controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.service.transport_service.TransportService;
import tourapp.service.transport_service.TransportTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.BaseValidator;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseEditController;

import java.sql.SQLException;
import java.util.List;

public class TransportEditController extends BaseEditController<Transport> {

    @FXML private TextField nameField;
    @FXML private TextField pricePerPersonField;
    @FXML private ComboBox<TransportType> transportTypeComboBox;

    private final TransportService transportService;
    private final TransportTypeService transportTypeService;

    public TransportEditController(Stage stage,
                                   SessionManager sessionManager,
                                   TransportService transportService,
                                   TransportTypeService transportTypeService,
                                   Transport transportToEdit) {
        super(stage, sessionManager, transportToEdit);
        this.transportService = transportService;
        this.transportTypeService = transportTypeService;
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/transport/transportEdit.fxml";
    }

    @Override
    protected String getWindowTitle() {
        return isCreateMode() ? "Додавання нового транспорту" : "Редагування транспорту";
    }

    @Override
    public void initialize() {
        initializeComboBoxes();
        super.initialize();
    }

    private void initializeComboBoxes() {
        try {
            List<TransportType> transportTypes = transportTypeService.getAll();
            initializeComboBox(transportTypeComboBox, transportTypes, TransportType::getName);
        } catch (SQLException e) {
            showError("Помилка завантаження даних: " + e.getMessage());
        }
    }

    @Override
    protected void setupValidationListeners() {
        addValidationListener(nameField);
        addValidationListener(pricePerPersonField);
        addValidationListener(transportTypeComboBox);
    }

    @Override
    protected void loadEntityData() throws SQLException {
        Transport fullTransport = transportService.getById(entityToEdit.getId());

        if (fullTransport != null) {
            nameField.setText(fullTransport.getName());
            pricePerPersonField.setText(String.valueOf(fullTransport.getPricePerPerson()));

            TransportType transportType = fullTransport.getType();
            if (transportType != null) {
                transportTypeComboBox.getSelectionModel().select(
                        findItemById(transportTypeComboBox.getItems(), transportType.getId(), TransportType::getId)
                );
            }
        }
    }

    @Override
    protected boolean validateForm() {
        boolean isValid = FormValidator.validateTransportForm(nameField, pricePerPersonField, transportTypeComboBox);

        if (isValid) {
            try {
                if (FormValidator.isDuplicateName(
                        transportService.getAll(),
                        nameField.getText().trim(),
                        Transport::getName,
                        Transport::getId,
                        isCreateMode() ? null : entityToEdit.getId()
                )) {
                    String duplicateError = "Транспорт з такою назвою вже існує";
                    BaseValidator.addError(nameField, duplicateError);
                    isValid = false;
                }
            } catch (SQLException e) {
                showError("Помилка перевірки дублювання назви: " + e.getMessage());
                return false;
            }
        }

        return isValid;
    }

    @Override
    protected Transport createEntityFromForm() {
        Transport transport = new Transport();
        updateEntityFromForm(transport);
        return transport;
    }

    @Override
    protected void updateEntityFromForm(Transport transport) {
        transport.setName(nameField.getText().trim());
        transport.setPricePerPerson(Double.parseDouble(pricePerPersonField.getText().trim()));
        transport.setType(transportTypeComboBox.getValue());
    }

    @Override
    protected void saveNewEntity(Transport transport) throws SQLException {
        transportService.create(transport);
    }

    @Override
    protected void updateExistingEntity(Transport transport) throws SQLException {
        transportService.update(transport);
    }

    @Override
    protected String getCreateSuccessMessage() {
        return "Транспорт успішно створено!";
    }

    @Override
    protected String getUpdateSuccessMessage() {
        return "Транспорт успішно оновлено!";
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