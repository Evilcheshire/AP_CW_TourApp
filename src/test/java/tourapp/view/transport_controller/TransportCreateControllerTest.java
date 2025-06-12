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
class TransportCreateControllerTest {

    @Mock private TransportService transportService;
    @Mock private TransportTypeService transportTypeService;
    @Mock private SessionManager sessionManager;

    private TransportEditController controller;
    private Stage primaryStage;
    private TransportType testTransportType;

    @Start
    void start(Stage stage) throws SQLException {
        MockitoAnnotations.openMocks(this);
        this.primaryStage = stage;

        testTransportType = new TransportType();
        testTransportType.setId(1);
        testTransportType.setName("Автобус");

        when(transportTypeService.getAll()).thenReturn(List.of(testTransportType));
        when(transportService.getAll()).thenReturn(Collections.emptyList());

        controller = new TransportEditController(primaryStage, sessionManager, transportService, transportTypeService, null);

        controller.setOnSaveCallback(() -> {});

        controller.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        reset(transportService, transportTypeService, sessionManager);
        when(transportTypeService.getAll()).thenReturn(List.of(testTransportType));
        when(transportService.getAll()).thenReturn(Collections.emptyList());

        controller.setOnSaveCallback(() -> {});
    }

    @Test
    void testCreateModeWindowTitle(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Додавання нового транспорту", controller.editStage.getTitle());
    }

    @Test
    void testCreateModeEmptyFields(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
        TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
        ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        verifyThat(nameField, hasText(""));
        verifyThat(pricePerPersonField, hasText(""));
        assertNull(transportTypeComboBox.getValue());
    }

    @Test
    void testTransportTypeComboBoxPopulated(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

        assertEquals(1, transportTypeComboBox.getItems().size());
        assertEquals("Автобус", transportTypeComboBox.getItems().getFirst().getName());
    }

    @Test
    void testSuccessfulCreateNewTransport(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((transportService).create(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Новий автобус");
            pricePerPersonField.setText("150.50");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Callback was not executed within timeout");

        verify(transportService, timeout(3000)).create(argThat(transport ->
                transport.getName().equals("Новий автобус") &&
                        transport.getPricePerPerson() == 150.50 &&
                        transport.getType().equals(testTransportType)));

        assertTrue(callbackExecuted.get(), "Callback was not executed");
    }

    @Test
    void testValidationErrorForEmptyName(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            pricePerPersonField.setText("100.0");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testValidationErrorForEmptyPrice(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Тестовий транспорт");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testValidationErrorForEmptyTransportType(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);

            nameField.setText("Тестовий транспорт");
            pricePerPersonField.setText("100.0");
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testValidationErrorForInvalidPriceFormat(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Тестовий транспорт");
            pricePerPersonField.setText("неправильна ціна");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testValidationErrorForNegativePrice(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Тестовий транспорт");
            pricePerPersonField.setText("-50.0");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testDuplicateTransportValidation(FxRobot robot) throws SQLException {
        Transport existingTransport = new Transport();
        existingTransport.setId(1);
        existingTransport.setName("Існуючий автобус");
        existingTransport.setPricePerPerson(100.0);
        existingTransport.setType(testTransportType);

        when(transportService.getAll()).thenReturn(List.of(existingTransport));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Існуючий автобус");
            pricePerPersonField.setText("200.0");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testSQLExceptionHandlingOnCreate(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new SQLException("Database error"))
                .when(transportService).create(any(Transport.class));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Валідний транспорт");
            pricePerPersonField.setText("100.0");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, timeout(3000)).create(any(Transport.class));
    }

    @Test
    void testSQLExceptionHandlingOnDuplicateCheck(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();
        when(transportService.getAll()).thenThrow(new SQLException("Error checking duplicates"));

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Валідний транспорт");
            pricePerPersonField.setText("100.0");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testCancelButton(FxRobot robot) throws SQLException {
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Тестовий транспорт");
            pricePerPersonField.setText("100.0");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Скасувати");
        WaitForAsyncUtils.waitForFxEvents();

        verify(transportService, never()).create(any(Transport.class));
    }

    @Test
    void testWindowProperties() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(controller.editStage.isShowing());
        assertEquals(primaryStage, controller.editStage.getOwner());
        assertEquals("Додавання нового транспорту", controller.editStage.getTitle());
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

        when((transportService).create(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("  Транспорт з пробілами  ");
            pricePerPersonField.setText("  123.45  ");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Trim whitespace callback was not executed within timeout");

        verify(transportService, timeout(3000)).create(argThat(transport ->
                transport.getName().equals("Транспорт з пробілами") &&
                        transport.getPricePerPerson() == 123.45));

        assertTrue(callbackExecuted.get(), "Trim whitespace callback was not executed");
    }

    @Test
    void testValidDecimalPriceFormats(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((transportService).create(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Транспорт з десятковою ціною");
            pricePerPersonField.setText("99.99");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Decimal price callback was not executed within timeout");

        verify(transportService, timeout(3000)).create(argThat(transport ->
                transport.getName().equals("Транспорт з десятковою ціною") &&
                        transport.getPricePerPerson() == 99.99));

        assertTrue(callbackExecuted.get(), "Decimal price callback was not executed");
    }

    @Test
    void testCallbackExecutionDiagnostic(FxRobot robot) throws SQLException, InterruptedException {
        WaitForAsyncUtils.waitForFxEvents();

        when((transportService).create(any(Transport.class))).thenReturn(true);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        AtomicBoolean callbackSet = new AtomicBoolean(false);

        controller.setOnSaveCallback(() -> {
            System.out.println("=== TRANSPORT CREATE CALLBACK EXECUTED ===");
            callbackExecuted.set(true);
            callbackLatch.countDown();
        });
        callbackSet.set(true);

        System.out.println("Transport create callback set: " + callbackSet.get());

        Platform.runLater(() -> {
            TextField nameField = robot.lookup("#nameField").queryAs(TextField.class);
            TextField pricePerPersonField = robot.lookup("#pricePerPersonField").queryAs(TextField.class);
            ComboBox<TransportType> transportTypeComboBox = robot.lookup("#transportTypeComboBox").queryAs(ComboBox.class);

            nameField.setText("Тест callback транспорту");
            pricePerPersonField.setText("150.0");
            transportTypeComboBox.setValue(testTransportType);
        });

        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("Зберегти");
        WaitForAsyncUtils.waitForFxEvents();

        HelperMethods.clickOnOK(robot);

        assertTrue(callbackLatch.await(5, TimeUnit.SECONDS), "Transport create callback was not executed within timeout");

        verify(transportService, timeout(3000)).create(any(Transport.class));
        System.out.println("Transport create callback executed: " + callbackExecuted.get());
        System.out.println("Transport create stage showing: " + controller.editStage.isShowing());

        assertTrue(callbackExecuted.get(), "Transport create callback was not executed even with diagnostic");
    }
}