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
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.service.meal_service.MealService;
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
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class MealCreateControllerTest {

    @Mock private MealService mealService;
    @Mock private MealTypeService mealTypeService;
    @Mock private SessionManager sessionManager;

    private MealEditController controller;
    private Stage primaryStage;
    private MealType testMealType1;
    private MealType testMealType2;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        testMealType1 = new MealType();
        testMealType1.setId(1);
        testMealType1.setName("Сніданок");

        testMealType2 = new MealType();
        testMealType2.setId(2);
        testMealType2.setName("Обід");

        when(mealTypeService.getAll()).thenReturn(List.of(testMealType1, testMealType2));
        when(mealService.getAll()).thenReturn(Collections.emptyList());

        controller = new MealEditController(primaryStage, sessionManager, mealService, mealTypeService, null);

        controller.setOnSaveCallback(() -> {
        });

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(mealService, mealTypeService, sessionManager);
        when(mealTypeService.getAll()).thenReturn(List.of(testMealType1, testMealType2));
        when(mealService.getAll()).thenReturn(Collections.emptyList());

        controller.setOnSaveCallback(() -> {
        });
    }

    @Test
    void testCreateModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Додавання нового харчування", controller.editStage.getTitle());
    }

    @Test
    void testCreateModeEmptyFields(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
        Spinner<Integer> mealsPerDaySpinner = robot.lookup("#mealsPerDaySpinner").queryAs(Spinner.class);
        ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);
        ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);

        verifyThat(nameField, hasText(""));
        verifyThat(costPerDayField, hasText(""));
        assertEquals(Integer.valueOf(3), mealsPerDaySpinner.getValue());
        assertTrue(selectedMealTypesListView.getItems().isEmpty());
        assertEquals(2, availableMealTypesListView.getItems().size());
    }

    @Test
    void testMealTypesListsPopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

        assertEquals(2, availableMealTypesListView.getItems().size());
        assertEquals("Сніданок", availableMealTypesListView.getItems().get(0).getName());
        assertEquals("Обід", availableMealTypesListView.getItems().get(1).getName());
    }

    @Test
    void testAddMealTypeButton(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);
        ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);

        Platform.runLater(() -> {
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, selectedMealTypesListView.getItems().size());
        assertEquals(1, availableMealTypesListView.getItems().size());
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType1));
        assertFalse(availableMealTypesListView.getItems().contains(testMealType1));
    }

    @Test
    void testRemoveMealTypeButton(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);
        ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);

        Platform.runLater(() -> {
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            selectedMealTypesListView.getSelectionModel().select(testMealType1);
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#removeMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(selectedMealTypesListView.getItems().isEmpty());
        assertEquals(2, availableMealTypesListView.getItems().size());
        assertTrue(availableMealTypesListView.getItems().contains(testMealType1));
    }

    @Test
    void testSuccessfulCreateNewMeal(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).create(any(Meal.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            Spinner<Integer> mealsPerDaySpinner = robot.lookup("#mealsPerDaySpinner").queryAs(Spinner.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Нове харчування");
            costPerDayField.setText("500.0");
            mealsPerDaySpinner.getValueFactory().setValue(3);

            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(mealService, timeout(3000)).create(argThat(meal ->
                meal.getName().equals("Нове харчування") &&
                        meal.getCostPerDay() == 500.0 &&
                        meal.getMealsPerDay() == 3 &&
                        meal.getMealTypes().contains(testMealType1)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyName(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            costPerDayField.setText("500.0");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testValidationErrorForEmptyMealTypes(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);

            nameField.setText("Тестове харчування");
            costPerDayField.setText("500.0");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testValidationErrorForNegativeCost(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Тестове харчування");
            costPerDayField.setText("-100.0");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testValidationErrorForInvalidCostFormat(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Тестове харчування");
            costPerDayField.setText("не число");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testValidationErrorForExcessiveCost(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Тестове харчування");
            costPerDayField.setText("15000.0");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testDuplicateMealValidation(FxRobot robot) throws SQLException {
        Meal existingMeal = new Meal();
        existingMeal.setId(1);
        existingMeal.setName("Існуюче харчування");
        existingMeal.setCostPerDay(300.0);
        existingMeal.setMealsPerDay(2);

        when(mealService.getAll()).thenReturn(List.of(existingMeal));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Існуюче харчування");
            costPerDayField.setText("500.0");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testSQLExceptionHandlingOnCreate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database error"))
                .when(mealService).create(any(Meal.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Валідне харчування");
            costPerDayField.setText("500.0");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, timeout(3000)).create(any(Meal.class));
    }

    @Test
    void testSQLExceptionHandlingOnDuplicateCheck(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(mealService.getAll()).thenThrow(new SQLException("Error checking duplicates"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Валідне харчування");
            costPerDayField.setText("500.0");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testCancelButton(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("Тестове харчування");
            costPerDayField.setText("500.0");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).create(any(Meal.class));
    }

    @Test
    void testWindowProperties() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(controller.editStage.isShowing());
        assertEquals(primaryStage, controller.editStage.getOwner());
        assertEquals("Додавання нового харчування", controller.editStage.getTitle());
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
    void testSpinnerDefaultValue(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Spinner<Integer> mealsPerDaySpinner = robot.lookup("#mealsPerDaySpinner").queryAs(Spinner.class);
        assertEquals(Integer.valueOf(3), mealsPerDaySpinner.getValue());
        assertEquals(Integer.valueOf(1), mealsPerDaySpinner.getValueFactory().getConverter().fromString("1"));
        assertEquals(Integer.valueOf(10), ((SpinnerValueFactory.IntegerSpinnerValueFactory) mealsPerDaySpinner.getValueFactory()).getMax());
    }

    @Test
    void testAddMultipleMealTypes(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);
        ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);

        Platform.runLater(() -> {
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            availableMealTypesListView.getSelectionModel().select(testMealType2);
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(2, selectedMealTypesListView.getItems().size());
        assertTrue(availableMealTypesListView.getItems().isEmpty());
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType1));
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType2));
    }

    @Test
    void testTrimWhitespaceOnCreate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).create(any(Meal.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);

            nameField.setText("  Харчування з пробілами  ");
            costPerDayField.setText("  500.0  ");
            availableMealTypesListView.getSelectionModel().select(testMealType1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(mealService, timeout(3000)).create(argThat(meal ->
                meal.getName().equals("Харчування з пробілами") &&
                        meal.getCostPerDay() == 500.0));

        assertTrue(callbackExecuted.get(), "Trim whitespace callback was not executed");
    }
}
