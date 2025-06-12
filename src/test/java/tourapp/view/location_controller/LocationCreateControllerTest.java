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
class LocationCreateControllerTest {

    @Mock private LocationService locationService;
    @Mock private LocationTypeService locationTypeService;
    @Mock private SessionManager sessionManager;

    private LocationEditController controller;
    private Stage primaryStage;
    private LocationType testLocationType;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        testLocationType = new LocationType();
        testLocationType.setId(1);
        testLocationType.setName("Тестовий тип");

        when(locationTypeService.getAll()).thenReturn(List.of(testLocationType));
        when(locationService.getAll()).thenReturn(Collections.emptyList());

        controller = new LocationEditController(primaryStage, sessionManager, locationService, locationTypeService, null);

        controller.setOnSaveCallback(() -> {});

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(locationService, locationTypeService, sessionManager);
        when(locationTypeService.getAll()).thenReturn(List.of(testLocationType));
        when(locationService.getAll()).thenReturn(Collections.emptyList());

        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testCreateModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Додавання нової локації", controller.editStage.getTitle());
    }

    @Test
    void testCreateModeEmptyFields(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
        TextArea descriptionField = robot.lookup("#descriptionField").queryAs(TextArea.class);
        ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

        verifyThat(nameField, hasText(""));
        verifyThat(countryField, hasText(""));
        verifyThat(descriptionField, hasText(""));
        assertNull(locationTypeComboBox.getValue());
    }

    @Test
    void testLocationTypeComboBoxPopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

        assertEquals(1, locationTypeComboBox.getItems().size());
        assertEquals("Тестовий тип", locationTypeComboBox.getItems().get(0).getName());
    }

    @Test
    void testSuccessfulCreateNewLocation(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).create(any(Location.class))).thenReturn(true);

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

            nameField.setText("Нова локація");
            countryField.setText("Україна");
            descriptionField.setText("Опис нової локації");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationService, timeout(3000)).create(argThat(location ->
                location.getName().equals("Нова локація") &&
                        location.getCountry().equals("Україна") &&
                        location.getDescription().equals("Опис нової локації") &&
                        location.getLocationType().equals(testLocationType)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyName(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            countryField.setText("Україна");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).create(any(Location.class));
    }

    @Test
    void testValidationErrorForEmptyCountry(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Тестова локація");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).create(any(Location.class));
    }

    @Test
    void testValidationErrorForEmptyLocationType(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);

            nameField.setText("Тестова локація");
            countryField.setText("Україна");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).create(any(Location.class));
    }

    @Test
    void testDuplicateLocationValidation(FxRobot robot) throws SQLException {
        Location existingLocation = new Location();
        existingLocation.setId(1);
        existingLocation.setName("Існуюча локація");
        existingLocation.setCountry("Україна");
        existingLocation.setLocationType(testLocationType);

        when(locationService.getAll()).thenReturn(List.of(existingLocation));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Існуюча локація");
            countryField.setText("Україна");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).create(any(Location.class));
    }

    @Test
    void testSQLExceptionHandlingOnCreate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database error"))
                .when(locationService).create(any(Location.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Валідна локація");
            countryField.setText("Україна");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, timeout(3000)).create(any(Location.class));
    }

    @Test
    void testSQLExceptionHandlingOnDuplicateCheck(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(locationService.getAll()).thenThrow(new SQLException("Error checking duplicates"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Валідна локація");
            countryField.setText("Україна");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).create(any(Location.class));
    }

    @Test
    void testCancelButton(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField countryField = robot.lookup("#countryField").queryAs(TextField.class);
            ComboBox<LocationType> locationTypeComboBox = robot.lookup("#locationTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Тестова локація");
            countryField.setText("Україна");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationService, never()).create(any(Location.class));
    }

    @Test
    void testWindowProperties() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(controller.editStage.isShowing());
        assertEquals(primaryStage, controller.editStage.getOwner());
        assertEquals("Додавання нової локації", controller.editStage.getTitle());
    }

    @Test
    void testShowError(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> controller.showError("Помилка з'явилась"));
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> alertStage = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene() != null && stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(alertStage.isPresent(), "Alert not found");

        DialogPane pane = (DialogPane) alertStage.get().getScene().getRoot();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        assertNotNull(okButton, "OK button not found");

        Platform.runLater(okButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testShowInfo(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> controller.showInfo("Це інфо"));
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> alertStage = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene() != null && stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(alertStage.isPresent(), "Alert not found");

        DialogPane pane = (DialogPane) alertStage.get().getScene().getRoot();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        assertNotNull(okButton, "OK button not found");

        Platform.runLater(okButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testTrimWhitespaceOnCreate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationService).create(any(Location.class))).thenReturn(true);

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

            nameField.setText("  Локація з пробілами  ");
            countryField.setText("  Україна  ");
            descriptionField.setText("  Опис з пробілами  ");
            locationTypeComboBox.setValue(testLocationType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        // Натискаємо OK в інформаційному вікні
        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(locationService, timeout(3000)).create(argThat(location ->
                location.getName().equals("Локація з пробілами") &&
                        location.getCountry().equals("Україна") &&
                        location.getDescription().equals("Опис з пробілами")));

        assertTrue(callbackExecuted.get(), "Trim whitespace callback was not executed");
    }


}