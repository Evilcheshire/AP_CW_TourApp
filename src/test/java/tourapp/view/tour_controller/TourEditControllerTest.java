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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class TourEditControllerTest {

    @Mock private TourService tourService;
    @Mock private LocationService locationService;
    @Mock private TourTypeService tourTypeService;
    @Mock private MealService mealService;
    @Mock private TransportService transportService;
    @Mock private SessionManager sessionManager;

    private TourEditController controller;
    private Stage primaryStage;
    private Tour tourToEdit;
    private TourType testTourType1, testTourType2;
    private Meal testMeal1, testMeal2;
    private Transport testTransport1, testTransport2;
    private Location testLocation1, testLocation2, testLocation3;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        setupTestData();
        setupMocks();

        controller = spy(new TourEditController(primaryStage, sessionManager, tourService,
                locationService, tourTypeService, mealService, transportService, tourToEdit));
        controller.setOnSaveCallback(() -> {});
        controller.show();
    }

    private void setupTestData() {
        testTourType1 = new TourType();
        testTourType1.setId(1);
        testTourType1.setName("Пляжний відпочинок");

        testTourType2 = new TourType();
        testTourType2.setId(2);
        testTourType2.setName("Екскурсійний тур");

        testMeal1 = new Meal();
        testMeal1.setId(1);
        testMeal1.setName("Все включено");

        testMeal2 = new Meal();
        testMeal2.setId(2);
        testMeal2.setName("Сніданок");

        testTransport1 = new Transport();
        testTransport1.setId(1);
        testTransport1.setName("Автобус");

        testTransport2 = new Transport();
        testTransport2.setId(2);
        testTransport2.setName("Літак");

        testLocation1 = new Location();
        testLocation1.setId(1);
        testLocation1.setName("Київ");
        testLocation1.setCountry("Україна");

        testLocation2 = new Location();
        testLocation2.setId(2);
        testLocation2.setName("Львів");
        testLocation2.setCountry("Україна");

        testLocation3 = new Location();
        testLocation3.setId(3);
        testLocation3.setName("Одеса");
        testLocation3.setCountry("Україна");

        tourToEdit = new Tour();
        tourToEdit.setId(1);
        tourToEdit.setDescription("Чудовий тур по Україні");
        tourToEdit.setPrice(1500.0);
        tourToEdit.setStartDate(LocalDate.of(2025, 7, 15));
        tourToEdit.setEndDate(LocalDate.of(2025, 7, 25));
        tourToEdit.setActive(true);
        tourToEdit.setType(testTourType1);
        tourToEdit.setMeal(testMeal1);
        tourToEdit.setTransport(testTransport1);
        tourToEdit.setLocations(Arrays.asList(testLocation1, testLocation2));
    }

    private void setupMocks() throws SQLException {
        when(tourTypeService.getAll()).thenReturn(Arrays.asList(testTourType1, testTourType2));
        when(mealService.getAll()).thenReturn(Arrays.asList(testMeal1, testMeal2));
        when(transportService.getAll()).thenReturn(Arrays.asList(testTransport1, testTransport2));
        when(locationService.getAll()).thenReturn(Arrays.asList(testLocation1, testLocation2, testLocation3));
        when(tourService.getByIdWithDependencies(1)).thenReturn(tourToEdit);
        when(tourService.getLocationsForTour(1)).thenReturn(Arrays.asList(testLocation1, testLocation2));
        when(tourService.update(any(Tour.class))).thenReturn(true);
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(tourService, locationService, tourTypeService, mealService, transportService, sessionManager);
        setupMocks();
        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testEditModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Редагування туру", controller.editStage.getTitle());
    }

    @Test
    void testEditModeFieldsPrePopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
        TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
        DatePicker startDatePicker = robot.lookup("#startDatePicker").queryAs(DatePicker.class);
        DatePicker endDatePicker = robot.lookup("#endDatePicker").queryAs(DatePicker.class);
        CheckBox activeCheckBox = robot.lookup("#activeCheckBox").queryAs(CheckBox.class);
        ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        verifyThat(descriptionField, hasText("Чудовий тур по Україні"));
        verifyThat(priceField, hasText("1500.0"));
        assertEquals(LocalDate.of(2025, 7, 15), startDatePicker.getValue());
        assertEquals(LocalDate.of(2025, 7, 25), endDatePicker.getValue());
        assertTrue(activeCheckBox.isSelected());
        assertEquals(testTourType1, tourTypeComboBox.getValue());
        assertEquals(testMeal1, mealTypeComboBox.getValue());
        assertEquals(testTransport1, transportTypeComboBox.getValue());
    }

    @Test
    void testComboBoxesPopulatedInEditMode(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
        ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        assertEquals(2, tourTypeComboBox.getItems().size());
        assertEquals(2, mealTypeComboBox.getItems().size());
        assertEquals(2, transportTypeComboBox.getItems().size());

        assertEquals("Пляжний відпочинок", tourTypeComboBox.getItems().get(0).getName());
        assertEquals("Все включено", mealTypeComboBox.getItems().get(0).getName());
        assertEquals("Автобус", transportTypeComboBox.getItems().get(0).getName());
    }

    @Test
    void testLocationListsInitializedCorrectly(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Location> availableLocationsListView = robot.lookup("#availableLocationsListView").queryAs(ListView.class);
        ListView<Location> selectedLocationsListView = robot.lookup("#selectedLocationsListView").queryAs(ListView.class);

        assertEquals(1, availableLocationsListView.getItems().size());
        assertEquals(2, selectedLocationsListView.getItems().size());

        assertTrue(selectedLocationsListView.getItems().contains(testLocation1));
        assertTrue(selectedLocationsListView.getItems().contains(testLocation2));
        assertTrue(availableLocationsListView.getItems().contains(testLocation3));
    }

    @Test
    void testSuccessfulUpdateExistingTour(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
            DatePicker startDatePicker = robot.lookup("#startDatePicker").queryAs(DatePicker.class);
            DatePicker endDatePicker = robot.lookup("#endDatePicker").queryAs(DatePicker.class);
            CheckBox activeCheckBox = robot.lookup("#activeCheckBox").queryAs(CheckBox.class);

            descriptionField.setText("Оновлений опис туру");
            priceField.setText("2000.0");
            startDatePicker.setValue(LocalDate.of(2025, 8, 1));
            endDatePicker.setValue(LocalDate.of(2025, 8, 10));
            activeCheckBox.setSelected(false);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(tourService, timeout(3000)).update(argThat(tour ->
                tour.getId() == 1 &&
                        tour.getDescription().equals("Оновлений опис туру") &&
                        tour.getPrice() == 2000.0 &&
                        tour.getStartDate().equals(LocalDate.of(2025, 8, 1)) &&
                        tour.getEndDate().equals(LocalDate.of(2025, 8, 10)) &&
                        !tour.isActive()));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testPartialUpdateOfTour(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);

            descriptionField.setText("Частково оновлений тур");
            tourTypeComboBox.setValue(testTourType2);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(tourService, timeout(3000)).update(argThat(tour ->
                tour.getId() == 1 &&
                        tour.getDescription().equals("Частково оновлений тур") &&
                        tour.getPrice() == 1500.0 &&
                        tour.getType().equals(testTourType2)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyDescriptionInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            descriptionField.setText("");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).update(any(Tour.class));
    }

    @Test
    void testValidationErrorForInvalidPriceInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);
            priceField.setText("не число");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).update(any(Tour.class));
    }

    @Test
    void testValidationErrorForEmptyTourTypeInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
            tourTypeComboBox.setValue(null);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).update(any(Tour.class));
    }

    @Test
    void testValidationErrorForInvalidDateRangeInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            DatePicker startDatePicker = robot.lookup("#startDatePicker").queryAs(DatePicker.class);
            DatePicker endDatePicker = robot.lookup("#endDatePicker").queryAs(DatePicker.class);

            startDatePicker.setValue(LocalDate.of(2025, 8, 10));
            endDatePicker.setValue(LocalDate.of(2025, 8, 5));
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).update(any(Tour.class));
    }

    @Test
    void testValidationErrorForEmptyLocationsInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ListView<Location> selectedLocationsListView = robot.lookup("#selectedLocationsListView").queryAs(ListView.class);
            selectedLocationsListView.getItems().clear();
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).update(any(Tour.class));
    }

    @Test
    void testLocationManipulationInEditMode(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            ListView<Location> availableLocationsListView = robot.lookup("#availableLocationsListView").queryAs(ListView.class);
            availableLocationsListView.getSelectionModel().select(testLocation3);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addLocationButton");
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ListView<Location> selectedLocationsListView = robot.lookup("#selectedLocationsListView").queryAs(ListView.class);
            selectedLocationsListView.getSelectionModel().select(testLocation1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#removeLocationButton");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(tourService, timeout(3000)).update(argThat(tour ->
                tour.getLocations().size() == 2 &&
                        tour.getLocations().contains(testLocation2) &&
                        tour.getLocations().contains(testLocation3) &&
                        !tour.getLocations().contains(testLocation1)));
    }

    @Test
    void testComboBoxChangesInEditMode(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            ComboBox<TourType> tourTypeComboBox = robot.lookup("#tourTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Meal> mealTypeComboBox = robot.lookup("#mealTypeComboBox").queryAs(ComboBox.class);
            ComboBox<Transport> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            tourTypeComboBox.setValue(testTourType2);
            mealTypeComboBox.setValue(testMeal2);
            transportTypeComboBox.setValue(testTransport2);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(tourService, timeout(3000)).update(argThat(tour ->
                tour.getType().equals(testTourType2) &&
                        tour.getMeal().equals(testMeal2) &&
                        tour.getTransport().equals(testTransport2)));
    }

    @Test
    void testSQLExceptionHandlingOnUpdate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(tourService.update(any(Tour.class))).thenThrow(new SQLException("Database error"));

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            descriptionField.setText("Оновлена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, timeout(3000)).update(any(Tour.class));
    }

    @Test
    void testTrimWhitespaceOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#priceField").queryAs(TextField.class);

            descriptionField.setText("  Тур з пробілами  ");
            priceField.setText("  2500.0  ");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(tourService, timeout(3000)).update(argThat(tour ->
                tour.getDescription().equals("Тур з пробілами") &&
                        tour.getPrice() == 2500.0));

        assertTrue(callbackExecuted.get(), "Trim whitespace callback was not executed");
    }

    @Test
    void testCancelButtonInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            descriptionField.setText("Змінений опис");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, never()).update(any(Tour.class));
    }

    @Test
    void testTourIdPreservedOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            descriptionField.setText("Новий опис туру");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(tourService, timeout(3000)).update(argThat(tour ->
                tour.getId() == 1));
    }

    @Test
    void testLoadTourDataWithNullTour() throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(tourService.getByIdWithDependencies(1)).thenReturn(null);

        Platform.runLater(() -> {
            controller.editStage.close();
            TourEditController newController = new TourEditController(
                    primaryStage, sessionManager, tourService, locationService,
                    tourTypeService, mealService, transportService, tourToEdit);
            newController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, timeout(3000)).getByIdWithDependencies(1);
    }

    @Test
    void testUpdateWithServiceReturningFalse(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(tourService.update(any(Tour.class))).thenReturn(false);

        Platform.runLater(() -> {
            TextField descriptionField = robot.lookup("#descriptionField").queryAs(TextField.class);
            descriptionField.setText("Оновлений опис");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(tourService, timeout(3000)).update(any(Tour.class));
    }

    @Test
    void testAddLocationWithoutSelection(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#addLocationButton");
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Location> selectedLocationsListView = robot.lookup("#selectedLocationsListView").queryAs(ListView.class);
        assertEquals(2, selectedLocationsListView.getItems().size());
    }

    @Test
    void testRemoveLocationWithoutSelection(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#removeLocationButton");
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Location> selectedLocationsListView = robot.lookup("#selectedLocationsListView").queryAs(ListView.class);
        assertEquals(2, selectedLocationsListView.getItems().size());
    }
}