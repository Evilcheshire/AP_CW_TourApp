package tourapp.view.tour_controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import tourapp.model.location.Location;
import tourapp.model.location.LocationType;
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
import tourapp.view.HelperMethods;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class TourCreateControllerTest {

    @Mock private TourService tourService;
    @Mock private LocationService locationService;
    @Mock private TourTypeService tourTypeService;
    @Mock private MealService mealService;
    @Mock private TransportService transportService;
    @Mock private SessionManager sessionManager;

    private TourEditController controller;
    private Stage primaryStage;

    private TourType testTourType;
    private Meal testMeal;
    private Transport testTransport;
    private Location testLocation;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        setupTestObjects();

        when(tourTypeService.getAll()).thenReturn(List.of(testTourType));
        when(mealService.getAll()).thenReturn(List.of(testMeal));
        when(transportService.getAll()).thenReturn(List.of(testTransport));
        when(locationService.getAll()).thenReturn(List.of(testLocation));

        controller = new TourEditController(
                primaryStage,
                sessionManager,
                tourService,
                locationService,
                tourTypeService,
                mealService,
                transportService,
                null
        );

        controller.setOnSaveCallback(() -> {});

        controller.show();
    }

    private void setupTestObjects() {
        testTourType = new TourType();
        testTourType.setId(1);
        testTourType.setName("Тестовий тип туру");

        testMeal = new Meal();
        testMeal.setId(1);
        testMeal.setName("Тестове харчування");

        testTransport = new Transport();
        testTransport.setId(1);
        testTransport.setName("Тестовий транспорт");

        LocationType locationType = new LocationType();
        locationType.setId(1);
        locationType.setName("Тестовий тип локації");

        testLocation = new Location();
        testLocation.setId(1);
        testLocation.setName("Тестова локація");
        testLocation.setCountry("Україна");
        testLocation.setLocationType(locationType);
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(tourService, locationService, tourTypeService, mealService, transportService, sessionManager);

        when(tourTypeService.getAll()).thenReturn(List.of(testTourType));
        when(mealService.getAll()).thenReturn(List.of(testMeal));
        when(transportService.getAll()).thenReturn(List.of(testTransport));
        when(locationService.getAll()).thenReturn(List.of(testLocation));

        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testCreateModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Додавання нового туру", controller.editStage.getTitle());
    }

    @Test
    void testCreateModeEmptyFields(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
        TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
        DatePicker startDatePicker = robot.lookup("#startDatePicker").queryAs(DatePicker.class);
        DatePicker endDatePicker = robot.lookup("#endDatePicker").queryAs(DatePicker.class);
        ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);
        CheckBox activeCheckBox = robot.lookup("#activeCheckBox").queryAs(CheckBox.class);

        verifyThat(descriptionField, hasText(""));
        verifyThat(priceField, hasText(""));

        assertNotNull(startDatePicker.getValue());
        assertNotNull(endDatePicker.getValue());
        assertEquals(LocalDate.now(), startDatePicker.getValue());
        assertEquals(LocalDate.now().plusDays(7), endDatePicker.getValue());

        assertNull(tourTypeComboBox.getValue());
        assertNull(mealTypeComboBox.getValue());
        assertNull(transportTypeComboBox.getValue());
        assertTrue(activeCheckBox.isSelected());
    }

    @Test
    void testComboBoxesPopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        assertEquals(1, tourTypeComboBox.getItems().size());
        assertEquals("Тестовий тип туру", tourTypeComboBox.getItems().get(0).getName());

        assertEquals(1, mealTypeComboBox.getItems().size());
        assertEquals("Тестове харчування", mealTypeComboBox.getItems().get(0).getName());

        assertEquals(1, transportTypeComboBox.getItems().size());
        assertEquals("Тестовий транспорт", transportTypeComboBox.getItems().get(0).getName());
    }

    @Test
    void testLocationListsInitialization(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Location> availableLocationsListView = robot.lookup("#availableLocationsListView").queryAs(ListView.class);
        ListView<Location> selectedLocationsListView = robot.lookup("#selectedLocationsListView").queryAs(ListView.class);

        assertEquals(1, availableLocationsListView.getItems().size());
        assertEquals("Тестова локація", availableLocationsListView.getItems().get(0).getName());

        assertEquals(0, selectedLocationsListView.getItems().size());
    }

    @Test
    void testAddRemoveLocationFunctionality(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Location> availableLocationsListView = robot.lookup("#availableLocationsListView").queryAs(ListView.class);
        ListView<Location> selectedLocationsListView = robot.lookup("#selectedLocationsListView").queryAs(ListView.class);

        robot.clickOn(availableLocationsListView);
        Platform.runLater(() -> availableLocationsListView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#addLocationButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, availableLocationsListView.getItems().size());
        assertEquals(1, selectedLocationsListView.getItems().size());

        robot.clickOn(selectedLocationsListView);
        Platform.runLater(() -> selectedLocationsListView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#removeLocationButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, availableLocationsListView.getItems().size());
        assertEquals(0, selectedLocationsListView.getItems().size());
    }

    @Test
    void testSuccessfulCreateNewTour(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((tourService).create(any(Tour.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            fillTourForm(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        verify(tourService, timeout(3000)).create(argThat(tour ->
                tour.getDescription().equals("Новий чудовий тур") &&
                        tour.getPrice() == 1000.0 &&
                        tour.getStartDate().equals(LocalDate.now()) &&
                        tour.getEndDate().equals(LocalDate.now().plusDays(7)) &&
                        tour.isActive() &&
                        tour.getType().equals(testTourType) &&
                        tour.getMeal().equals(testMeal) &&
                        tour.getTransport().equals(testTransport) &&
                        !tour.getLocations().isEmpty()));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyDescription(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
            ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            priceField.setText("1000");
            tourTypeComboBox.setValue(testTourType);
            mealTypeComboBox.setValue(testMeal);
            transportTypeComboBox.setValue(testTransport);

            addLocationToTour(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).create(any(Tour.class));
    }

    @Test
    void testValidationErrorForInvalidPrice(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
            ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            descriptionField.setText("Тестовий тур");
            priceField.setText("не число");
            tourTypeComboBox.setValue(testTourType);
            mealTypeComboBox.setValue(testMeal);
            transportTypeComboBox.setValue(testTransport);

            addLocationToTour(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).create(any(Tour.class));
    }

    @Test
    void testValidationErrorForEmptyLocations(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            fillTourFormWithoutLocations(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).create(any(Tour.class));
    }

    @Test
    void testValidationErrorForEmptyTourType(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
            ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            descriptionField.setText("Тестовий тур");
            priceField.setText("1000");
            mealTypeComboBox.setValue(testMeal);
            transportTypeComboBox.setValue(testTransport);

            addLocationToTour(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).create(any(Tour.class));
    }

    @Test
    void testCancelButton(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            fillTourForm(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).create(any(Tour.class));
    }

    @Test
    void testWindowProperties() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(controller.editStage.isShowing());
        assertEquals(primaryStage, controller.editStage.getOwner());
        assertEquals("Додавання нового туру", controller.editStage.getTitle());
    }

    @Test
    void testTrimWhitespaceOnCreate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

       when((tourService).create(any(Tour.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
            ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            descriptionField.setText("  Тур з пробілами  ");
            priceField.setText("  1000  ");
            tourTypeComboBox.setValue(testTourType);
            mealTypeComboBox.setValue(testMeal);
            transportTypeComboBox.setValue(testTransport);

            addLocationToTour(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(tourService, timeout(3000)).create(argThat(tour ->
                tour.getDescription().equals("Тур з пробілами") &&
                        tour.getPrice() == 1000.0));

        assertTrue(callbackExecuted.get(), "Trim whitespace callback was not executed");
    }

    @Test
    void testDateValidation(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
            DatePicker startDatePicker = robot.lookup("#startDatePicker").queryAs(DatePicker.class);
            DatePicker endDatePicker = robot.lookup("#endDatePicker").queryAs(DatePicker.class);
            ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            descriptionField.setText("Тестовий тур");
            priceField.setText("1000");
            startDatePicker.setValue(LocalDate.now().plusDays(10));
            endDatePicker.setValue(LocalDate.now());

            tourTypeComboBox.setValue(testTourType);
            mealTypeComboBox.setValue(testMeal);
            transportTypeComboBox.setValue(testTransport);

            addLocationToTour(robot);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).create(any(Tour.class));
    }

    private void fillTourForm(FxRobot robot) {
        TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
        TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
        ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        descriptionField.setText("Новий чудовий тур");
        priceField.setText("1000");
        tourTypeComboBox.setValue(testTourType);
        mealTypeComboBox.setValue(testMeal);
        transportTypeComboBox.setValue(testTransport);

        addLocationToTour(robot);
    }

    private void fillTourFormWithoutLocations(FxRobot robot) {
        TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
        TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
        ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        descriptionField.setText("Новий чудовий тур");
        priceField.setText("1000");
        tourTypeComboBox.setValue(testTourType);
        mealTypeComboBox.setValue(testMeal);
        transportTypeComboBox.setValue(testTransport);
    }

    private void addLocationToTour(FxRobot robot) {
        ListView<Location> availableLocationsListView = robot.lookup("#availableLocationsListView").queryAs(ListView.class);

        if (!availableLocationsListView.getItems().isEmpty()) {
            availableLocationsListView.getSelectionModel().select(0);
            Button addLocationButton = robot.lookup("#addLocationButton").queryAs(Button.class);
            addLocationButton.fire();
        }
    }
}