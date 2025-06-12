package tourapp.view.location_controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tourapp.model.location.Location;
import tourapp.model.location.LocationType;
import tourapp.service.location_service.LocationService;
import tourapp.service.location_service.LocationTypeService;
import tourapp.util.SessionManager;
import tourapp.util.validation.BaseValidator;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseEditController;

import java.sql.SQLException;
import java.util.List;

public class LocationEditController extends BaseEditController<Location> {

    @FXML private TextField nameField;
    @FXML private TextField countryField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<LocationType> locationTypeComboBox;

    private final LocationService locationService;
    private final LocationTypeService locationTypeService;

    public LocationEditController(Stage stage,
                                  SessionManager sessionManager,
                                  LocationService locationService,
                                  LocationTypeService locationTypeService,
                                  Location locationToEdit) {
        super(stage, sessionManager, locationToEdit);
        this.locationService = locationService;
        this.locationTypeService = locationTypeService;
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/location/locationEdit.fxml";
    }

    @Override
    protected String getWindowTitle() {
        return isCreateMode() ? "Додавання нової локації" : "Редагування локації";
    }

    @Override
    public void initialize() {
        initializeComboBoxes();
        super.initialize();
    }

    private void initializeComboBoxes() {
        try {
            List<LocationType> locationTypes = locationTypeService.getAll();
            initializeComboBox(locationTypeComboBox, locationTypes, LocationType::getName);
        } catch (SQLException e) {
            showError("Помилка завантаження даних: " + e.getMessage());
        }
    }

    @Override
    protected void setupValidationListeners() {
        addValidationListener(nameField);
        addValidationListener(countryField);
        addValidationListener(locationTypeComboBox);

        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            BaseValidator.clearErrorsForControls(descriptionField);
        });
    }

    @Override
    protected void loadEntityData() throws SQLException {
        Location fullLocation = locationService.getById(entityToEdit.getId());

        if (fullLocation != null) {
            nameField.setText(fullLocation.getName());
            countryField.setText(fullLocation.getCountry());
            descriptionField.setText(fullLocation.getDescription());

            LocationType locationType = fullLocation.getLocationType();
            if (locationType != null) {
                locationTypeComboBox.getSelectionModel().select(
                        findItemById(locationTypeComboBox.getItems(), locationType.getId(), LocationType::getId)
                );
            }
        }
    }

    @Override
    protected boolean validateForm() {
        boolean isValid = FormValidator.validateLocationForm(
                nameField,
                countryField,
                locationTypeComboBox,
                descriptionField
        );

        if (isValid) {
            try {
                if (isLocationNameDuplicated(nameField.getText().trim(), countryField.getText().trim())) {
                    String duplicateError = "Локація з такою назвою вже існує в цій країні";
                    BaseValidator.addError(nameField, duplicateError);
                    isValid = false;
                }
            } catch (SQLException e) {
                showError("Помилка перевірки унікальності назви: " + e.getMessage());
                return false;
            }
        }

        return isValid;
    }

    @Override
    protected Location createEntityFromForm() {
        Location location = new Location();
        updateEntityFromForm(location);
        return location;
    }

    @Override
    protected void updateEntityFromForm(Location location) {
        location.setName(nameField.getText().trim());
        location.setCountry(countryField.getText().trim());
        location.setDescription(descriptionField.getText().trim());
        location.setLocationType(locationTypeComboBox.getValue());
    }

    @Override
    protected void saveNewEntity(Location location) throws SQLException {
        locationService.create(location);
    }

    @Override
    protected void updateExistingEntity(Location location) throws SQLException {
        locationService.update(location);
    }

    @Override
    protected String getCreateSuccessMessage() {
        return "Локацію успішно створено!";
    }

    @Override
    protected String getUpdateSuccessMessage() {
        return "Локацію успішно оновлено!";
    }

    private boolean isLocationNameDuplicated(String name, String country) throws SQLException {
        List<Location> existingLocations = locationService.getAll();
        for (Location location : existingLocations) {
            if (location.getName().equalsIgnoreCase(name) &&
                    location.getCountry().equalsIgnoreCase(country)) {
                if (!isCreateMode() && location.getId() == entityToEdit.getId()) {
                    continue;
                }
                return true;
            }
        }
        return false;
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