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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@ExtendWith({ApplicationExtension.class})
class MealEditControllerTest {

    @Mock private MealService mealService;
    @Mock private MealTypeService mealTypeService;
    @Mock private SessionManager sessionManager;

    private MealEditController controller;
    private Stage primaryStage;
    private MealType testMealType1;
    private MealType testMealType2;
    private MealType testMealType3;
    private Meal mealToEdit;

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

        testMealType3 = new MealType();
        testMealType3.setId(3);
        testMealType3.setName("Вечеря");

        mealToEdit = new Meal();
        mealToEdit.setId(1);
        mealToEdit.setName("Стандартне харчування");
        mealToEdit.setMealsPerDay(3);
        mealToEdit.setCostPerDay(150.50);
        mealToEdit.setMealTypes(List.of(testMealType1, testMealType2));

        when(mealTypeService.getAll()).thenReturn(List.of(testMealType1, testMealType2, testMealType3));
        when(mealService.getById(1)).thenReturn(mealToEdit);
        when(mealService.getAll()).thenReturn(List.of(mealToEdit));

        controller = new MealEditController(primaryStage, sessionManager, mealService, mealTypeService, mealToEdit);

        controller.setOnSaveCallback(() -> {
        });

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(mealService, mealTypeService, sessionManager);
        when(mealTypeService.getAll()).thenReturn(List.of(testMealType1, testMealType2, testMealType3));
        when(mealService.getById(1)).thenReturn(mealToEdit);
        when(mealService.getAll()).thenReturn(List.of(mealToEdit));

        controller.setOnSaveCallback(() -> {
        });
    }

    @Test
    void testEditModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Редагування харчування", controller.editStage.getTitle());
    }

    @Test
    void testEditModeFieldsPrePopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        Spinner<Integer> mealsPerDaySpinner = robot.lookup("#mealsPerDaySpinner").queryAs(Spinner.class);
        TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
        ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);

        verifyThat(nameField, hasText("Стандартне харчування"));
        assertEquals(3, (int) mealsPerDaySpinner.getValue());
        verifyThat(costPerDayField, hasText("150.5"));
        assertEquals(2, selectedMealTypesListView.getItems().size());
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType1));
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType2));
    }

    @Test
    void testMealTypesListsPopulatedCorrectlyInEditMode(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);
        ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);

        assertEquals(1, availableMealTypesListView.getItems().size());
        assertTrue(availableMealTypesListView.getItems().contains(testMealType3));

        assertEquals(2, selectedMealTypesListView.getItems().size());
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType1));
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType2));
    }

    @Test
    void testSuccessfulUpdateExistingMeal(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).update(any(Meal.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            Spinner<Integer> mealsPerDaySpinner = robot.lookup("#mealsPerDaySpinner").queryAs(Spinner.class);
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);

            nameField.setText("Оновлене харчування");
            mealsPerDaySpinner.getValueFactory().setValue(4);
            costPerDayField.setText("200.75");
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(mealService, timeout(3000)).update(argThat(meal ->
                meal.getId() == 1 &&
                        meal.getName().equals("Оновлене харчування") &&
                        meal.getMealsPerDay() == 4 &&
                        meal.getCostPerDay() == 200.75));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testPartialUpdateOfMeal(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).update(any(Meal.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Частково оновлене харчування");
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(mealService, timeout(3000)).update(argThat(meal ->
                meal.getId() == 1 &&
                        meal.getName().equals("Частково оновлене харчування") &&
                        meal.getMealsPerDay() == 3 &&
                        meal.getCostPerDay() == 150.5));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testUpdateMealWithMealTypesChanges(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).update(any(Meal.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);
            availableMealTypesListView.getSelectionModel().select(testMealType3);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(mealService, timeout(3000)).update(argThat(meal ->
                meal.getMealTypes().size() == 3 &&
                        meal.getMealTypes().contains(testMealType1) &&
                        meal.getMealTypes().contains(testMealType2) &&
                        meal.getMealTypes().contains(testMealType3)));

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

        verify(mealService, never()).update(any(Meal.class));
    }

    @Test
    void testValidationErrorForInvalidCostInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            costPerDayField.setText("invalid_cost");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).update(any(Meal.class));
    }

    @Test
    void testValidationErrorForNegativeCostInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            costPerDayField.setText("-50.0");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).update(any(Meal.class));
    }

    @Test
    void testValidationErrorForTooHighCostInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField costPerDayField = robot.lookup("#costPerDayField").queryAs(TextField.class);
            costPerDayField.setText("15000.0");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).update(any(Meal.class));
    }

    @Test
    void testValidationErrorForEmptyMealTypesInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);
            selectedMealTypesListView.getSelectionModel().selectAll();
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#removeMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).update(any(Meal.class));
    }

    @Test
    void testDuplicateMealValidationExcludesCurrentMeal(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        Meal anotherMeal = new Meal();
        anotherMeal.setId(2);
        anotherMeal.setName("Інше харчування");
        anotherMeal.setMealsPerDay(2);
        anotherMeal.setCostPerDay(100.0);
        anotherMeal.setMealTypes(List.of(testMealType1));

        when(mealService.getAll()).thenReturn(List.of(mealToEdit, anotherMeal));

        when((mealService).update(any(Meal.class))).thenReturn(true);

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

        verify(mealService, timeout(3000)).update(any(Meal.class));
        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testDuplicateMealValidationDetectsOtherDuplicates(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Meal duplicateMeal = new Meal();
        duplicateMeal.setId(2);
        duplicateMeal.setName("Дублююче харчування");
        duplicateMeal.setMealsPerDay(2);
        duplicateMeal.setCostPerDay(100.0);
        duplicateMeal.setMealTypes(List.of(testMealType1));

        when(mealService.getAll()).thenReturn(List.of(mealToEdit, duplicateMeal));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Дублююче харчування");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, never()).update(any(Meal.class));
    }

    @Test
    void testSQLExceptionHandlingOnUpdate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database error"))
                .when(mealService).update(any(Meal.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Оновлена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, timeout(3000)).update(any(Meal.class));
    }

    @Test
    void testTrimWhitespaceOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).update(any(Meal.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("  Харчування з пробілами  ");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(mealService, timeout(3000)).update(argThat(meal ->
                meal.getName().equals("Харчування з пробілами")));

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

        verify(mealService, never()).update(any(Meal.class));
    }

    @Test
    void testMealIdPreservedOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).update(any(Meal.class))).thenReturn(true);

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

        verify(mealService, timeout(3000)).update(argThat(meal ->
                meal.getId() == 1));
    }

    @Test
    void testLoadMealDataWithNullMeal(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(mealService.getById(1)).thenReturn(null);

        Platform.runLater(() -> {
            controller.editStage.close();
            MealEditController newController = new MealEditController(
                    primaryStage, sessionManager, mealService, mealTypeService, mealToEdit);
            newController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        verify(mealService, timeout(3000)).getById(1);
    }

    @Test
    void testMealTypesMovementBetweenLists(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ListView<MealType> availableMealTypesListView = robot.lookup("#availableMealTypesListView").queryAs(ListView.class);
        ListView<MealType> selectedMealTypesListView = robot.lookup("#selectedMealTypesListView").queryAs(ListView.class);

        assertEquals(1, availableMealTypesListView.getItems().size());
        assertEquals(2, selectedMealTypesListView.getItems().size());

        Platform.runLater(() -> {
            availableMealTypesListView.getSelectionModel().select(testMealType3);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#addMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(0, availableMealTypesListView.getItems().size());
        assertEquals(3, selectedMealTypesListView.getItems().size());
        assertTrue(selectedMealTypesListView.getItems().contains(testMealType3));

        Platform.runLater(() -> {
            selectedMealTypesListView.getSelectionModel().select(testMealType3);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#removeMealTypeButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, availableMealTypesListView.getItems().size());
        assertEquals(2, selectedMealTypesListView.getItems().size());
        assertFalse(selectedMealTypesListView.getItems().contains(testMealType3));
        assertTrue(availableMealTypesListView.getItems().contains(testMealType3));
    }

    @Test
    void testSpinnerValuePersistence(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((mealService).update(any(Meal.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            Spinner<Integer> mealsPerDaySpinner = robot.lookup("#mealsPerDaySpinner").queryAs(Spinner.class);
            mealsPerDaySpinner.getValueFactory().setValue(5);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(mealService, timeout(3000)).update(argThat(meal ->
                meal.getMealsPerDay() == 5));
    }
}