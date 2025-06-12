package tourapp.view.meal_controller;

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
import tourapp.model.meal.MealType;
import tourapp.service.meal_service.MealTypeService;
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
class MealTypeEditControllerTest {

    @Mock private MealTypeService mealTypeService;
    @Mock private SessionManager sessionManager;

    private MealTypeEditController controller;
    private Stage primaryStage;
    private MealType existingMealType;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        existingMealType = new MealType();
        existingMealType.setId(1);
        existingMealType.setName("Оригінальна назва");

        when(mealTypeService.getById(1)).thenReturn(existingMealType);
        when(mealTypeService.getAll()).thenReturn(List.of(existingMealType));

        controller = new MealTypeEditController(primaryStage, sessionManager, mealTypeService, existingMealType);

        controller.setOnSaveCallback(() -> {
        });

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(mealTypeService, sessionManager);

        when(mealTypeService.getById(1)).thenReturn(existingMealType);
        when(mealTypeService.getAll()).thenReturn(List.of(existingMealType));

        controller.setOnSaveCallback(() -> {
        });
    }

    @Test
    void testEditModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Редагування типу харчування", controller.editStage.getTitle());
    }

    @Test
    void testEditModeLoadsExistingData(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        verifyThat(nameField, hasText("Оригінальна назва"));
    }

    @Test
    void testSuccessfulUpdateExistingMealType(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(mealTypeService).update(eq(1), any(MealType.class));

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

        verify(mealTypeService, timeout(3000)).update(eq(1), argThat(mealType ->
                mealType.getName().equals("Оновлена назва") && mealType.getId() == 1));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testEditWithSameNameShouldSucceed(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(mealTypeService).update(eq(1), any(MealType.class));

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

        verify(mealTypeService, timeout(3000)).update(eq(1), argThat(mealType ->
                mealType.getName().equals("Оригінальна назва") && mealType.getId() == 1));

        assertTrue(callbackExecuted.get(), "Same name edit callback was not executed");
    }

    @Test
    void testEditWithDuplicateNameFromAnotherRecord(FxRobot robot) throws SQLException {
        MealType anotherType = new MealType();
        anotherType.setId(2);
        anotherType.setName("Інша назва");

        when(mealTypeService.getAll()).thenReturn(List.of(existingMealType, anotherType));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("Інша назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, never()).update(anyInt(), any(MealType.class));
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

        verify(mealTypeService, never()).update(anyInt(), any(MealType.class));
    }

    @Test
    void testSQLExceptionHandlingOnUpdate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database update error"))
                .when(mealTypeService).update(eq(1), any(MealType.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.clear();
            nameField.setText("Нова валідна назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, timeout(3000)).update(eq(1), any(MealType.class));
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

        verify(mealTypeService, never()).update(anyInt(), any(MealType.class));
    }

    @Test
    void testTrimWhitespaceOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(mealTypeService).update(eq(1), any(MealType.class));

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

        verify(mealTypeService, timeout(3000)).update(eq(1), argThat(mealType ->
                mealType.getName().equals("Назва з пробілами")));

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
        assertEquals("Редагування типу харчування", controller.editStage.getTitle());
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

        when((mealTypeService).delete(1)).thenReturn(true);

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

        verify(mealTypeService, timeout(3000)).delete(1);
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

        assertTrue(pane.getHeaderText().contains("Видалення типу харчування"),
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

        verify(mealTypeService, never()).delete(anyInt());
    }

    @Test
    void testDeleteWithGeneralSQLException(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("General database error"))
                .when(mealTypeService).delete(1);

        robot.clickOn("Видалити");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Видалити");
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> errorDialog = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene() != null && stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(errorDialog.isPresent(), "Error dialog should appear for general SQL exception");

        DialogPane pane = (DialogPane) errorDialog.get().getScene().getRoot();
        assertTrue(pane.getContentText().contains("Помилка при видаленні"),
                "Should show general delete error message");

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        Platform.runLater(okButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, timeout(3000)).delete(1);
    }

    @Test
    void testGetDeleteHeaderTextMethod() {
        String deleteHeaderText = controller.getDeleteHeaderText();
        assertEquals("Видалення типу харчування", deleteHeaderText,
                "Delete header text should match expected value");
    }

    @Test
    void testGetLoadErrorMessageMethod() {
        String loadErrorMessage = controller.getLoadErrorMessage();
        assertEquals("Не вдалося завантажити інтерфейс редагування типу харчування", loadErrorMessage,
                "Load error message should match expected value");
    }

    @Test
    void testGetLoadDataErrorMessageMethod() {
        String loadDataErrorMessage = controller.getLoadDataErrorMessage();
        assertEquals("Помилка завантаження даних типу харчування", loadDataErrorMessage,
                "Load data error message should match expected value");
    }

    @Test
    void testDeleteItemMethodDirectCall() throws SQLException {
        controller.deleteItem(1);

        verify(mealTypeService, times(1)).delete(1);
    }

    @Test
    void testDeleteItemWithSQLException() throws SQLException {
        doThrow(new SQLException("Database error")).when(mealTypeService).delete(1);

        assertThrows(SQLException.class, () -> controller.deleteItem(1),
                "deleteItem should propagate SQLException");

        verify(mealTypeService, times(1)).delete(1);
    }
}
