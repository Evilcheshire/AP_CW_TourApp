package tourapp.view.tour_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tourapp.model.location.Location;
import tourapp.model.meal.Meal;
import tourapp.model.tour.Tour;
import tourapp.model.tour.TourType;
import tourapp.model.transport.Transport;
import tourapp.service.location_service.LocationService;
import tourapp.service.meal_service.MealService;
import tourapp.service.tour_service.TourService;
import tourapp.service.tour_service.TourTypeService;
import tourapp.service.transport_service.TransportService;
import tourapp.util.SessionManager;
import tourapp.util.validation.BaseValidator;
import tourapp.util.validation.FormValidator;
import tourapp.view.BaseEditController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TourEditController extends BaseEditController<Tour> {

    @FXML private TextField descriptionField;
    @FXML private TextField priceField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<TourType> tourTypeComboBox;
    @FXML private ComboBox<Meal> mealTypeComboBox;
    @FXML private ComboBox<Transport> transportTypeComboBox;
    @FXML private ListView<Location> availableLocationsListView;
    @FXML private ListView<Location> selectedLocationsListView;
    @FXML private Button addLocationButton;
    @FXML private Button removeLocationButton;
    @FXML private CheckBox activeCheckBox;

    private final TourService tourService;
    private final LocationService locationService;
    private final TourTypeService tourTypeService;
    private final MealService mealService;
    private final TransportService transportService;

    private ObservableList<Location> availableLocations;
    private ObservableList<Location> selectedLocations;

    public TourEditController(Stage stage,
                              SessionManager sessionManager,
                              TourService tourService,
                              LocationService locationService,
                              TourTypeService tourTypeService,
                              MealService mealService,
                              TransportService transportService,
                              Tour tourToEdit) {
        super(stage, sessionManager, tourToEdit);
        this.tourService = tourService;
        this.locationService = locationService;
        this.tourTypeService = tourTypeService;
        this.mealService = mealService;
        this.transportService = transportService;
    }

    @Override
    protected String getFxmlPath() {
        return "/tourapp/view/tour/tourEdit.fxml";
    }

    @Override
    protected String getWindowTitle() {
        return isCreateMode() ? "Додавання нового туру" : "Редагування туру";
    }

    @Override
    public void initialize() {
        initializeComboBoxes();
        initializeLocationLists();

        if (isCreateMode()) {
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusDays(7));
            activeCheckBox.setSelected(true);
        }

        super.initialize();
    }

    private void initializeComboBoxes() {
        try {
            List<TourType> tourTypes = tourTypeService.getAll();
            initializeComboBox(tourTypeComboBox, tourTypes, TourType::getName);

            List<Meal> mealTypes = mealService.getAll();
            initializeComboBox(mealTypeComboBox, mealTypes, Meal::getName);

            List<Transport> transportTypes = transportService.getAll();
            initializeComboBox(transportTypeComboBox, transportTypes, Transport::getName);
        } catch (SQLException e) {
            showError("Помилка завантаження даних: " + e.getMessage());
        }
    }

    private void initializeLocationLists() {
        try {
            List<Location> allLocations = locationService.getAll();

            availableLocations = FXCollections.observableArrayList();
            selectedLocations = FXCollections.observableArrayList();

            if (isCreateMode()) {
                availableLocations.addAll(allLocations);
            } else {
                List<Location> tourLocations = tourService.getLocationsForTour(entityToEdit.getId());
                selectedLocations.addAll(tourLocations);

                for (Location location : allLocations) {
                    if (!containsLocationById(tourLocations, location.getId())) {
                        availableLocations.add(location);
                    }
                }
            }

            availableLocationsListView.setItems(availableLocations);
            selectedLocationsListView.setItems(selectedLocations);

            availableLocationsListView.setCellFactory(lv -> new LocationListCell());
            selectedLocationsListView.setCellFactory(lv -> new LocationListCell());

        } catch (SQLException e) {
            showError("Помилка завантаження локацій: " + e.getMessage());
        }
    }

    private boolean containsLocationById(List<Location> locations, int id) {
        return locations.stream().anyMatch(location -> location.getId() == id);
    }

    @Override
    protected void setupValidationListeners() {
        addValidationListener(descriptionField);
        addValidationListener(priceField);
        addValidationListener(startDatePicker);
        addValidationListener(endDatePicker);
        addValidationListener(tourTypeComboBox);
        addValidationListener(mealTypeComboBox);
        addValidationListener(transportTypeComboBox);
    }

    @Override
    protected void loadEntityData() throws SQLException {
        Tour fullTour = tourService.getByIdWithDependencies(entityToEdit.getId());

        if (fullTour != null) {
            descriptionField.setText(fullTour.getDescription());
            priceField.setText(String.valueOf(fullTour.getPrice()));
            startDatePicker.setValue(fullTour.getStartDate());
            endDatePicker.setValue(fullTour.getEndDate());
            activeCheckBox.setSelected(fullTour.isActive());

            TourType tourType = fullTour.getType();
            if (tourType != null) {
                tourTypeComboBox.getSelectionModel().select(
                        findItemById(tourTypeComboBox.getItems(), tourType.getId(), TourType::getId)
                );
            }

            Meal meal = fullTour.getMeal();
            if (meal != null) {
                mealTypeComboBox.getSelectionModel().select(
                        findItemById(mealTypeComboBox.getItems(), meal.getId(), Meal::getId)
                );
            }

            Transport transport = fullTour.getTransport();
            if (transport != null) {
                transportTypeComboBox.getSelectionModel().select(
                        findItemById(transportTypeComboBox.getItems(), transport.getId(), Transport::getId)
                );
            }
        }
    }

    @Override
    protected boolean validateForm() {
        boolean isValid = FormValidator.validateTourForm(
                descriptionField,
                priceField,
                startDatePicker,
                endDatePicker,
                tourTypeComboBox,
                transportTypeComboBox,
                mealTypeComboBox
        );

        if (descriptionField == null || descriptionField.getText().trim().isEmpty()) {
            BaseValidator.addError(descriptionField, "Поле опису туру є обов'язковим");
            isValid = false;
        }

        if (isValid && selectedLocations.isEmpty()) {
            showError("Виберіть хоча б одну локацію для туру");
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected Tour createEntityFromForm() {
        Tour tour = new Tour();
        updateEntityFromForm(tour);
        return tour;
    }

    @Override
    protected void updateEntityFromForm(Tour tour) {
        tour.setDescription(descriptionField.getText().trim());
        tour.setPrice(Double.parseDouble(priceField.getText().trim()));
        tour.setStartDate(startDatePicker.getValue());
        tour.setEndDate(endDatePicker.getValue());
        tour.setActive(activeCheckBox.isSelected());

        tour.setType(tourTypeComboBox.getValue());
        tour.setMeal(mealTypeComboBox.getValue());
        tour.setTransport(transportTypeComboBox.getValue());

        List<Location> locations = new ArrayList<>(selectedLocations);
        tour.setLocations(locations);
    }

    @Override
    protected void saveNewEntity(Tour tour) throws SQLException {
        tourService.create(tour);
    }

    @Override
    protected void updateExistingEntity(Tour tour) throws SQLException {
        tourService.update(tour);
    }

    @Override
    protected String getCreateSuccessMessage() {
        return "Тур успішно створено!";
    }

    @Override
    protected String getUpdateSuccessMessage() {
        return "Тур успішно оновлено!";
    }

    @FXML
    private void handleAddLocation() {
        Location selectedLocation = availableLocationsListView.getSelectionModel().getSelectedItem();
        if (selectedLocation != null) {
            selectedLocations.add(selectedLocation);
            availableLocations.remove(selectedLocation);
        } else {
            showInfo("Спочатку виберіть локацію зі списку доступних");
        }
    }

    @FXML
    private void handleRemoveLocation() {
        Location selectedLocation = selectedLocationsListView.getSelectionModel().getSelectedItem();
        if (selectedLocation != null) {
            availableLocations.add(selectedLocation);
            selectedLocations.remove(selectedLocation);
        } else {
            showInfo("Спочатку виберіть локацію зі списку вибраних");
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

    private static class LocationListCell extends ListCell<Location> {
        @Override
        protected void updateItem(Location location, boolean empty) {
            super.updateItem(location, empty);

            if (empty || location == null) {
                setText(null);
            } else {
                setText(location.getName() + " (" + location.getCountry() + ")");
            }
        }
    }
}