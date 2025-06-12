package tourapp.view.transport_controller;

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
import tourapp.model.transport.Transport;
import tourapp.model.transport.TransportType;
import tourapp.service.transport_service.TransportService;
import tourapp.service.transport_service.TransportTypeService;
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
class TransportEditControllerTest {

    @Mock private TransportService transportService;
    @Mock private TransportTypeService transportTypeService;
    @Mock private SessionManager sessionManager;

    private TransportEditController controller;
    private Stage primaryStage;
    private TransportType testTransportType1;
    private TransportType testTransportType2;
    private Transport transportToEdit;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        testTransportType1 = new TransportType();
        testTransportType1.setId(1);
        testTransportType1.setName("Автобус");

        testTransportType2 = new TransportType();
        testTransportType2.setId(2);
        testTransportType2.setName("Літак");

        transportToEdit = new Transport();
        transportToEdit.setId(1);
        transportToEdit.setName("Існуючий транспорт");
        transportToEdit.setPricePerPerson(150.50);
        transportToEdit.setType(testTransportType1);

        when(transportTypeService.getAll()).thenReturn(List.of(testTransportType1, testTransportType2));
        when(transportService.getById(1)).thenReturn(transportToEdit);
        when(transportService.getAll()).thenReturn(List.of(transportToEdit));

        controller = new TransportEditController(primaryStage, sessionManager, transportService, transportTypeService, transportToEdit);

        controller.setOnSaveCallback(() -> {});

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(transportService, transportTypeService, sessionManager);
        when(transportTypeService.getAll()).thenReturn(List.of(testTransportType1, testTransportType2));
        when(transportService.getById(1)).thenReturn(transportToEdit);
        when(transportService.getAll()).thenReturn(List.of(transportToEdit));

        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testEditModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Редагування транспорту", controller.editStage.getTitle());
    }

    @Test
    void testEditModeFieldsPrePopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
        ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        verifyThat(nameField, hasText("Існуючий транспорт"));
        verifyThat(priceField, hasText("150.5"));
        assertEquals(testTransportType1, transportTypeComboBox.getValue());
    }

    @Test
    void testTransportTypeComboBoxPopulatedInEditMode(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        assertEquals(2, transportTypeComboBox.getItems().size());
        assertEquals("Автобус", transportTypeComboBox.getItems().get(0).getName());
        assertEquals("Літак", transportTypeComboBox.getItems().get(1).getName());
        assertEquals(testTransportType1, transportTypeComboBox.getValue());
    }

    @Test
    void testSuccessfulUpdateExistingTransport(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Оновлений транспорт");
            priceField.setText("200.75");
            transportTypeComboBox.setValue(testTransportType2);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(transportService, timeout(3000)).update(argThat(transport ->
                transport.getId() == 1 &&
                        transport.getName().equals("Оновлений транспорт") &&
                        transport.getPricePerPerson() == 200.75 &&
                        transport.getType().equals(testTransportType2)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testPartialUpdateOfTransport(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Частково оновлений транспорт");
            transportTypeComboBox.setValue(testTransportType2);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(transportService, timeout(3000)).update(argThat(transport ->
                transport.getId() == 1 &&
                        transport.getName().equals("Частково оновлений транспорт") &&
                        transport.getPricePerPerson() == 150.50 &&
                        transport.getType().equals(testTransportType2)));

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

        verify(transportService, never()).update(any(Transport.class));
    }

    @Test
    void testValidationErrorForEmptyPriceInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            priceField.setText("");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).update(any(Transport.class));
    }

    @Test
    void testValidationErrorForInvalidPriceFormatInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            priceField.setText("не число");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).update(any(Transport.class));
    }

    @Test
    void testValidationErrorForNegativePriceInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            priceField.setText("-50.0");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).update(any(Transport.class));
    }

    @Test
    void testValidationErrorForEmptyTransportTypeInEditMode(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);
            transportTypeComboBox.setValue(null);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).update(any(Transport.class));
    }

    @Test
    void testDuplicateTransportValidationExcludesCurrentTransport(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        Transport anotherTransport = new Transport();
        anotherTransport.setId(2);
        anotherTransport.setName("Інший транспорт");
        anotherTransport.setPricePerPerson(300.0);
        anotherTransport.setType(testTransportType2);

        when(transportService.getAll()).thenReturn(List.of(transportToEdit, anotherTransport));
        when(transportService.update(any(Transport.class))).thenReturn(true);

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

        verify(transportService, timeout(3000)).update(any(Transport.class));
        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testDuplicateTransportValidationDetectsOtherDuplicates(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Transport duplicateTransport = new Transport();
        duplicateTransport.setId(2);
        duplicateTransport.setName("Дублюючий транспорт");
        duplicateTransport.setPricePerPerson(100.0);
        duplicateTransport.setType(testTransportType2);

        when(transportService.getAll()).thenReturn(List.of(transportToEdit, duplicateTransport));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Дублюючий транспорт");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).update(any(Transport.class));
    }

    @Test
    void testSQLExceptionHandlingOnUpdate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenThrow(new SQLException("Database error"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Оновлена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, timeout(3000)).update(any(Transport.class));
    }

    @Test
    void testUpdateFailureHandling(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenReturn(false);

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Оновлена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, timeout(3000)).update(any(Transport.class));
        assertTrue(controller.editStage.isShowing(), "Stage should remain open on update failure");
    }

    @Test
    void testTrimWhitespaceOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);

            nameField.setText("  Транспорт з пробілами  ");
            priceField.setText("  250.0  ");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(transportService, timeout(3000)).update(argThat(transport ->
                transport.getName().equals("Транспорт з пробілами") &&
                        transport.getPricePerPerson() == 250.0));

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

        verify(transportService, never()).update(any(Transport.class));
        assertFalse(controller.editStage.isShowing(), "Stage should be closed after cancel");
    }

    @Test
    void testTransportIdPreservedOnUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenReturn(true);

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

        verify(transportService, timeout(3000)).update(argThat(transport ->
                transport.getId() == 1));
    }

    @Test
    void testLoadTransportDataWithNullTransport(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.getById(1)).thenReturn(null);

        Platform.runLater(() -> {
            controller.editStage.close();
            TransportEditController newController = new TransportEditController(
                    primaryStage, sessionManager, transportService, transportTypeService, transportToEdit);
            newController.show();
        });

        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, timeout(3000)).getById(1);
    }

    @Test
    void testTransportTypeSelectionCorrectness(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        assertEquals(testTransportType1, transportTypeComboBox.getValue());
        assertEquals(1, transportTypeComboBox.getValue().getId());
        assertEquals("Автобус", transportTypeComboBox.getValue().getName());
    }

    @Test
    void testTransportTypeChangePersistsInUpdate(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);
            transportTypeComboBox.setValue(testTransportType2);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(transportService, timeout(3000)).update(argThat(transport ->
                transport.getType().getId() == 2 &&
                        transport.getType().getName().equals("Літак")));
    }

    @Test
    void testPriceValidationWithDecimalValues(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when(transportService.update(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        controller.setOnSaveCallback(callbackLatch::countDown);

        Platform.runLater(() -> {
            TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            priceField.setText("199.99");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(transportService, timeout(3000)).update(argThat(transport ->
                transport.getPricePerPerson() == 199.99));
    }

    @Test
    void testPriceValidationWithZeroValue(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField priceField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            priceField.setText("0.0");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).update(any(Transport.class));
    }

    @Test
    void testValidationErrorClearingOnFieldChange(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            nameField.setText("Виправлена назва");
        });

        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        assertEquals("Виправлена назва", nameField.getText());
    }
}