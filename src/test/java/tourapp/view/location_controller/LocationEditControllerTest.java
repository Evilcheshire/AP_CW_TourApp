package tourapp.view.location_controller;

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
import tourapp.service.location_service.LocationService;
import tourapp.service.location_service.LocationTypeService;
import tourapp.util.SessionManager;
import tourapp.view.HelperMethods;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class LocationEditControllerTest {

    @Mock private LocationService locationService;
    @Mock private LocationTypeService locationTypeService;
    @Mock private SessionManager sessionManager;

    private LocationEditController controller;
    private Stage primaryStage;
    private LocationType testLocationType1;
    private LocationType testLocationType2;
    private Location locationToEdit;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        testLocationType1 = new LocationType();
        testLocationType1.setId(1);
        testLocationType1.setName("Тип 1");

        testLocationType2 = new LocationType();
        testLocationType2.setId(2);
        testLocationType2.setName("Тип 2");

        locationToEdit = new Location();
        locationToEdit.setId(1);
        locationToEdit.setName("Існуюча локація");
        locationToEdit.setCountry("Україна");
        locationToEdit.setDescription("Опис існуючої локації");
        locationToEdit.setLocationType(testLocationType1);

        when(locationTypeService.getAll()).thenReturn(List.of(testLocationType1, testLocationType2));
        when(locationService.getById(1)).thenReturn(locationToEdit);
        when(locationService.getAll()).thenReturn(List.of(locationToEdit));

        controller = new LocationEditController(primaryStage, sessionManager, locationService, locationTypeService, locationToEdit);

        controller.setOnSaveCallback(() -> {});

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(locationService, locationTypeService, sessionManager);
        when(locationTypeService.getAll()).thenReturn(List.of(testLocationType1, testLocationType2));
        when(locationService.getById(1)).thenReturn(locationToEdit);
        when(locationService.getAll()).thenReturn(List.of(locationToEdit));

        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testEditModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Редагування локації", controller.editStage.getTitle());
    }

    @Test
    void testEditModeFieldsPrePopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
        TextArea descriptionField = robot.lookup("#descriptionField").queryAs(TextArea.class);
        ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

        verifyThat(nameField, hasText("Існуюча локація"));
        verifyThat(countryField, hasText("Україна"));
        verifyThat(descriptionField, hasText("Опис існуючої локації"));
        assertEquals(testLocationType1, locationTypeComboBox.getValue());
    }

    @Test
    void testLocationTypeComboBoxPopulatedInEditMode(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

        assertEquals(2, locationTypeComboBox.getItems().size());
        assertEquals("Тип 1", locationTypeComboBox.getItems().get(0).getName());
        assertEquals("Тип 2", locationTypeComboBox.getItems().get(1).getName());
        assertEquals(testLocationType1, locationTypeComboBox.getValue());
    }

    @Test
    void testSuccessfulUpdateExistingLocation(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).update(any(Location.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            TextArea descriptionField = robot.lookup("#descriptionField").queryAs(TextArea.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Оновлена локація");
            countryField.setText("Польща");
            descriptionField.setText("Оновлений опис локації");
            locationTypeComboBox.setValue(testLocationType2);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationService, timeout(3000)).update(argThat(location ->
                location.getId() == 1 &&
                        location.getName().equals("Оновлена локація") &&
                        location.getCountry().equals("Польща") &&
                        location.getDescription().equals("Оновлений опис локації") &&
                        location.getLocationType().equals(testLocationType2)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testPartialUpdateOfLocation(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).update(any(Location.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Частково оновлена локація");
            locationTypeComboBox.setValue(testLocationType2);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationService, timeout(3000)).update(argThat(location ->
                location.getId() == 1 &&
                        location.getName().equals("Частково оновлена локація") &&
                        location.getCountry().equals("Україна") && // залишається без змін
                        location.getDescription().equals("Опис існуючої локації") && // залишається без змін
                        location.getLocationType().equals(testLocationType2)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyNameInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).update(any(Location.class));
    }

    @Test
    void testValidationErrorForEmptyCountryInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            countryField.setText("");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).update(any(Location.class));
    }

    @Test
    void testValidationErrorForEmptyLocationTypeInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);
            locationTypeComboBox.setValue(null);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).update(any(Location.class));
    }

    @Test
    void testDuplicateLocationValidationExcludesCurrentLocation(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        Location anotherLocation = new Location();
        anotherLocation.setId(2);
        anotherLocation.setName("Інша локація");
        anotherLocation.setCountry("Польща");
        anotherLocation.setLocationType(testLocationType2);

        when(locationService.getAll()).thenReturn(List.of(locationToEdit, anotherLocation));

        when((locationService).update(any(Location.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationService, timeout(3000)).update(any(Location.class));
        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testDuplicateLocationValidationDetectsOtherDuplicates(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Location duplicateLocation = new Location();
        duplicateLocation.setId(2);
        duplicateLocation.setName("Дублююча локація");
        duplicateLocation.setCountry("Україна");
        duplicateLocation.setLocationType(testLocationType2);

        when(locationService.getAll()).thenReturn(List.of(locationToEdit, duplicateLocation));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Дублююча локація");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).update(any(Location.class));
    }

    @Test
    void testSQLExceptionHandlingOnUpdate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database error"))
                .when(locationService).update(any(Location.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Оновлена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, timeout(3000)).update(any(Location.class));
    }

    @Test
    void testTrimWhitespaceOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).update(any(Location.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            TextArea descriptionField = robot.lookup("#descriptionField").queryAs(TextArea.class);

            nameField.setText("  Локація з пробілами  ");
            countryField.setText("  Країна з пробілами  ");
            descriptionField.setText("  Опис з пробілами  ");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(locationService, timeout(3000)).update(argThat(location ->
                location.getName().equals("Локація з пробілами") &&
                        location.getCountry().equals("Країна з пробілами") &&
                        location.getDescription().equals("Опис з пробілами")));

        assertTrue(callbackExecuted.get(), "Trim whitespace callback was not executed");
    }

    @Test
    void testCancelButtonInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Змінена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).update(any(Location.class));
    }

    @Test
    void testLocationIdPreservedOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).update(any(Location.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Нова назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationService, timeout(3000)).update(argThat(location ->
                location.getId() == 1)); // ID повинен залишитися незмінним
    }

    @Test
    void testLoadLocationDataWithNullLocation(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(locationService.getById(1)).thenReturn(null);

        Platform.runLater(() -> {
            controller.editStage.close();
            LocationEditController newController = new LocationEditController(
                    primaryStage, sessionManager, locationService, locationTypeService, locationToEdit);
            newController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, timeout(3000)).getById(1);
    }

    @Test
    void testCallbackExecutionDiagnosticForUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).update(any(Location.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        AtomicBoolean callbackSet = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            System.out.println("=== LOCATION UPDATE CALLBACK EXECUTED ===");
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });
        callbackSet.set(true);

        System.out.println("Location update callback set: " + callbackSet.get());

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Тест callback оновлення");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Location update callback was not executed within timeout");

        verify(locationService, timeout(3000)).update(any(Location.class));
        System.out.println("Location update callback executed: " + callbackExecuted.get());
        System.out.println("Location update stage showing: " + controller.editStage.isShowing());

        assertTrue(callbackExecuted.get(), "Location update callback was not executed even with diagnostic");
    }

    @Test
    void testLocationTypeSelectionCorrectness(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

        assertEquals(testLocationType1, locationTypeComboBox.getValue());
        assertEquals(1, locationTypeComboBox.getValue().getId());
        assertEquals("Тип 1", locationTypeComboBox.getValue().getName());
    }

    @Test
    void testLocationTypeChangePersistsInUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).update(any(Location.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);
            locationTypeComboBox.setValue(testLocationType2);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationService, timeout(3000)).update(argThat(location ->
                location.getLocationType().getId() == 2 &&
                        location.getLocationType().getName().equals("Тип 2")));
    }
}