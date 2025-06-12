package tourapp.view.location_controller;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
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
import tourapp.model.location.LocationType;
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
class LocationTypeCreateControllerTest {

    @Mock private LocationTypeService locationTypeService;
    @Mock private SessionManager sessionManager;

    private LocationTypeEditController controller;
    private Stage primaryStage;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());

        controller = new LocationTypeEditController(primaryStage, sessionManager, locationTypeService, null);

        controller.setOnSaveCallback(() -> {});

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(locationTypeService, sessionManager);
        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());

        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testCreateModeWindowTitle() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Додавання нового типу локації", controller.editStage.getTitle());
    }

    @Test
    void testCreateModeEmptyNameField(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        verifyThat(nameField, hasText(""));
    }

    @Test
    void testSuccessfulCreateNewLocationType(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());
        when(locationTypeService.create(any(LocationType.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Новий тип локації");
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).create(argThat(locationType ->
                locationType.getName().equals("Новий тип локації")));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyName(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).create(any(LocationType.class));
    }

    @Test
    void testDuplicateNameValidation(FxRobot robot) throws SQLException {
        LocationType existingType = new LocationType();
        existingType.setId(1);
        existingType.setName("Існуючий тип");

        when(locationTypeService.getAll()).thenReturn(List.of(existingType));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Існуючий тип");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).create(any(LocationType.class));
    }

    @Test
    void testSQLExceptionHandlingOnCreate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());
        when(locationTypeService.create(any(LocationType.class)))
                .thenThrow(new SQLException("Database error"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Валідна назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, timeout(3000)).create(any(LocationType.class));
    }

    @Test
    void testSQLExceptionHandlingOnDuplicateCheck(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(locationTypeService.getAll()).thenThrow(new SQLException("Error checking duplicates"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Валідна назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).create(any(LocationType.class));
    }

    @Test
    void testCancelButton(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Тестова назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).create(any(LocationType.class));
    }

    @Test
    void testWindowProperties() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(controller.editStage.isShowing());
        assertFalse(controller.editStage.isResizable());
        assertEquals(primaryStage, controller.editStage.getOwner());
    }

    @Test
    public void testShowError(FxRobot robot) {
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
    public void testShowInfo(FxRobot robot) {
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
    void testMultipleSpacesInName(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());
        when(locationTypeService.create(any(LocationType.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("  Назва з пробілами  ");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Multiple spaces test callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).create(argThat(locationType ->
                locationType.getName().equals("Назва з пробілами")));

        assertTrue(callbackExecuted.get(), "Multiple spaces test callback was not executed");
    }

    @Test
    void testCallbackExecutionDiagnostic(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(locationTypeService.getAll()).thenReturn(Collections.emptyList());
        when(locationTypeService.create(any(LocationType.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        AtomicBoolean callbackSet = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            System.out.println("=== CALLBACK EXECUTED ===");
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });
        callbackSet.set(true);

        System.out.println("Callback set: " + callbackSet.get());

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Тест callback");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).create(any(LocationType.class));
        System.out.println("Callback executed: " + callbackExecuted.get());
        System.out.println("Stage showing: " + controller.editStage.isShowing());

        assertTrue(callbackExecuted.get(), "Callback was not executed even with diagnostic");
    }
}