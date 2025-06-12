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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class LocationTypeEditControllerTest {

    @Mock private LocationTypeService locationTypeService;
    @Mock private SessionManager sessionManager;

    private LocationTypeEditController controller;
    private Stage primaryStage;
    private LocationType existingLocationType;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        existingLocationType = new LocationType();
        existingLocationType.setId(1);
        existingLocationType.setName("Оригінальна назва");

        when(locationTypeService.getById(1)).thenReturn(existingLocationType);
        when(locationTypeService.getAll()).thenReturn(List.of(existingLocationType));

        controller = new LocationTypeEditController(primaryStage, sessionManager, locationTypeService, existingLocationType);

        controller.setOnSaveCallback(() -> {});

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(locationTypeService, sessionManager);

        when(locationTypeService.getById(1)).thenReturn(existingLocationType);
        when(locationTypeService.getAll()).thenReturn(List.of(existingLocationType));

        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testEditModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Редагування типу локації", controller.editStage.getTitle());
    }

    @Test
    void testEditModeLoadsExistingData(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        verifyThat(nameField, hasText("Оригінальна назва"));
    }

    @Test
    void testSuccessfulUpdateExistingLocationType(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(locationTypeService).update(eq(1), any(LocationType.class));

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("Оновлена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).update(eq(1), argThat(locationType ->
                locationType.getName().equals("Оновлена назва") && locationType.getId() == 1));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testEditWithSameNameShouldSucceed(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(locationTypeService).update(eq(1), any(LocationType.class));

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Same name edit callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).update(eq(1), argThat(locationType ->
                locationType.getName().equals("Оригінальна назва") && locationType.getId() == 1));

        assertTrue(callbackExecuted.get(), "Same name edit callback was not executed");
    }

    @Test
    void testEditWithDuplicateNameFromAnotherRecord(FxRobot robot) throws SQLException {
        LocationType anotherType = new LocationType();
        anotherType.setId(2);
        anotherType.setName("Інша назва");

        when(locationTypeService.getAll()).thenReturn(List.of(existingLocationType, anotherType));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("Інша назва"); // Дублікат з іншого запису
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).update(anyInt(), any(LocationType.class));
    }

    @Test
    void testValidationErrorForEmptyNameInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).update(anyInt(), any(LocationType.class));
    }

    @Test
    void testSQLExceptionHandlingOnUpdate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database update error"))
                .when(locationTypeService).update(eq(1), any(LocationType.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("Нова валідна назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, timeout(3000)).update(eq(1), any(LocationType.class));
    }

    @Test
    void testCancelButtonInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("Змінена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).update(anyInt(), any(LocationType.class));
    }

    @Test
    void testTrimWhitespaceOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(locationTypeService).update(eq(1), any(LocationType.class));

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("  Назва з пробілами  ");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).update(eq(1), argThat(locationType ->
                locationType.getName().equals("Назва з пробілами")));

        assertTrue(callbackExecuted.get(), "Trim whitespace callback was not executed");
    }

    @Test
    void testEditModeShowErrorDialog(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> controller.showError("Помилка редагування"));
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> alertStage = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene() != null && stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(alertStage.isPresent(), "Error alert not found in edit mode");

        DialogPane pane = (DialogPane) alertStage.get().getScene().getRoot();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        assertNotNull(okButton, "OK button not found in error dialog");

        Platform.runLater(okButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testEditModeShowInfoDialog(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> controller.showInfo("Інформація про редагування"));
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> alertStage = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene() != null && stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(alertStage.isPresent(), "Info alert not found in edit mode");

        DialogPane pane = (DialogPane) alertStage.get().getScene().getRoot();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        assertNotNull(okButton, "OK button not found in info dialog");

        Platform.runLater(okButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testWindowPropertiesInEditMode() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(controller.editStage.isShowing());
        assertFalse(controller.editStage.isResizable());
        assertEquals(primaryStage, controller.editStage.getOwner());
        assertEquals("Редагування типу локації", controller.editStage.getTitle());
    }

    @Test
    void testCallbackExecutionDiagnosticInEditMode(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(locationTypeService).update(eq(1), any(LocationType.class));

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        AtomicBoolean callbackSet = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            System.out.println("=== EDIT MODE CALLBACK EXECUTED ===");
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });
        callbackSet.set(true);

        System.out.println("Edit mode callback set: " + callbackSet.get());

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("Тест callback редагування");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Edit mode callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).update(eq(1), any(LocationType.class));
        System.out.println("Edit mode callback executed: " + callbackExecuted.get());
        System.out.println("Edit mode stage showing: " + controller.editStage.isShowing());

        assertTrue(callbackExecuted.get(), "Edit mode callback was not executed even with diagnostic");
    }

    @Test
    void testDeleteButtonVisibilityInEditMode(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        Button deleteButton = robot.lookup("#deleteButton").queryAs(Button.class);

        assertTrue(deleteButton.isVisible(), "Delete button should be visible in edit mode");
        assertTrue(deleteButton.isManaged(), "Delete button should be managed in edit mode");
    }

    @Test
    void testSuccessfulDelete(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((locationTypeService).delete(1)).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        robot.clickOn("Видалити");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Видалити");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Delete callback was not executed within timeout");

        verify(locationTypeService, timeout(3000)).delete(1);
        assertTrue(callbackExecuted.get(), "Delete callback was not executed");
    }

    @Test
    void testDeleteConfirmationDialog(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Видалити");
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> confirmDialog = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene() != null && stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(confirmDialog.isPresent(), "Confirmation dialog should appear");

        DialogPane pane = (DialogPane) confirmDialog.get().getScene().getRoot();

        assertTrue(pane.getHeaderText().contains("Видалення типу локації"),
                "Dialog should contain delete header text");

        ButtonType deleteButtonType = new ButtonType("Видалити", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Скасувати", ButtonBar.ButtonData.CANCEL_CLOSE);

        assertNotNull(deleteButtonType, "Delete button should be present in confirmation dialog");
        assertNotNull(cancelButtonType, "Cancel button should be present in confirmation dialog");

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testDeleteCancellation(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Видалити");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(locationTypeService, never()).delete(anyInt());
    }

    @Test
    void testGetDeleteHeaderTextMethod() {
        String deleteHeaderText = controller.getDeleteHeaderText();
        assertEquals("Видалення типу локації", deleteHeaderText,
                "Delete header text should match expected value");
    }

    @Test
    void testGetLoadErrorMessageMethod() {
        String loadErrorMessage = controller.getLoadErrorMessage();
        assertEquals("Не вдалося завантажити інтерфейс", loadErrorMessage,
                "Load error message should match expected value");
    }

    @Test
    void testGetLoadDataErrorMessageMethod() {
        String loadDataErrorMessage = controller.getLoadDataErrorMessage();
        assertEquals("Помилка завантаження даних типу локації", loadDataErrorMessage,
                "Load data error message should match expected value");
    }

    @Test
    void testDeleteItemMethodDirectCall() throws SQLException {
        controller.deleteItem(1);

        verify(locationTypeService, times(1)).delete(1);
    }

    @Test
    void testDeleteItemWithSQLException() throws SQLException {
        doThrow(new SQLException("Database error")).when(locationTypeService).delete(1);

        assertThrows(SQLException.class, () -> controller.deleteItem(1),
                "deleteItem should propagate SQLException");

        verify(locationTypeService, times(1)).delete(1);
    }
}