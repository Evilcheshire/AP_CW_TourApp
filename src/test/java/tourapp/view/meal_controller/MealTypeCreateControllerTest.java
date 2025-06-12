package tourapp.view.meal_controller;

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
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class MealTypeCreateControllerTest {

    @Mock private MealTypeService mealTypeService;
    @Mock private SessionManager sessionManager;

    private MealTypeEditController controller;
    private Stage primaryStage;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());

        controller = new MealTypeEditController(primaryStage, sessionManager, mealTypeService, null);

        controller.setOnSaveCallback(() -> {
        });

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(mealTypeService, sessionManager);
        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());

        controller.setOnSaveCallback(() -> {
        });
    }

    @Test
    void testCreateModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Додавання нового типу харчування", controller.editStage.getTitle());
    }

    @Test
    void testCreateModeEmptyNameField(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        verifyThat(nameField, hasText(""));
    }

    @Test
    void testSuccessfulCreateNewMealType(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());
        when(mealTypeService.create(any(MealType.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Новий тип харчування");
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(mealTypeService, timeout(3000)).create(argThat(mealType ->
                mealType.getName().equals("Новий тип харчування")));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyName(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, never()).create(any(MealType.class));
    }

    @Test
    void testDuplicateNameValidation(FxRobot robot) throws SQLException {
        MealType existingType = new MealType();
        existingType.setId(1);
        existingType.setName("Існуючий тип");

        when(mealTypeService.getAll()).thenReturn(List.of(existingType));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Існуючий тип");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, never()).create(any(MealType.class));
    }

    @Test
    void testSQLExceptionHandlingOnCreate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());
        when(mealTypeService.create(any(MealType.class)))
                .thenThrow(new SQLException("Database error"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Валідна назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, timeout(3000)).create(any(MealType.class));
    }

    @Test
    void testSQLExceptionHandlingOnDuplicateCheck(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(mealTypeService.getAll()).thenThrow(new SQLException("Error checking duplicates"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Валідна назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, never()).create(any(MealType.class));
    }

    @Test
    void testCancelButton(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Тестова назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealTypeService, never()).create(any(MealType.class));
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

        when(mealTypeService.getAll()).thenReturn(Collections.emptyList());
        when(mealTypeService.create(any(MealType.class))).thenReturn(true);

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

        verify(mealTypeService, timeout(3000)).create(argThat(mealType ->
                mealType.getName().equals("Назва з пробілами")));

        assertTrue(callbackExecuted.get(), "Multiple spaces test callback was not executed");
    }
}
