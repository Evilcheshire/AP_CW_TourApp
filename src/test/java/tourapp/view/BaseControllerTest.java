package tourapp.view;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import tourapp.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class BaseControllerTest extends ApplicationTest {

    private TestableBaseController controller;
    private SessionManager mockSessionManager;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Override
    public void start(Stage stage) {
        mockSessionManager = mock(SessionManager.class);
        controller = new TestableBaseController(stage, mockSessionManager);
    }

    @BeforeEach
    public void setupStreams() {
        System.setOut(new PrintStream(outContent));
        System.setProperty("testfx.headless", "true");
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.clearProperty("testfx.headless");
    }

    @Test
    public void testShowError(){
        Platform.runLater(() -> controller.showError("Помилка з'явилась"));

        FxRobot robot = new FxRobot();
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> alertStage = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(alertStage.isPresent(), "Alert not found");

        DialogPane pane = (DialogPane) alertStage.get().getScene().getRoot();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        assertNotNull(okButton, "OK button not found");

        Platform.runLater(okButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testShowInfo() {
        Platform.runLater(() -> controller.showInfo("Це інфо"));

        FxRobot robot = new FxRobot();
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Stage> alertStage = robot.listTargetWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getScene().getRoot() instanceof DialogPane)
                .findFirst();

        assertTrue(alertStage.isPresent(), "Alert not found");

        DialogPane pane = (DialogPane) alertStage.get().getScene().getRoot();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        assertNotNull(okButton, "OK button not found");

        Platform.runLater(okButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testIsAuthenticated_returnsTrueIfSessionExists() {
        when(mockSessionManager.hasActiveSession()).thenReturn(true);
        assertTrue(controller.isAuthenticated());
    }

    @Test
    public void testIsAuthenticated_returnsFalseIfNoSession() {
        when(mockSessionManager.hasActiveSession()).thenReturn(false);
        assertFalse(controller.isAuthenticated());
    }

    @Test
    public void testIsAdmin_trueIfSessionAndRoleAdmin() {
        mockRole(true, false, false);
        assertTrue(controller.isAdmin());
    }

    @Test
    public void testIsManager_trueIfSessionAndRoleManager() {
        mockRole(false, true, false);
        assertTrue(controller.isManager());
    }

    @Test
    public void testIsCustomer_trueIfSessionAndRoleCustomer() {
        mockRole(false, false, true);
        assertTrue(controller.isCustomer());
    }

    private void mockRole(boolean isAdmin, boolean isManager, boolean isCustomer) {
        SessionManager.UserSession mockSession = mock(SessionManager.UserSession.class);
        when(mockSession.isAdmin()).thenReturn(isAdmin);
        when(mockSession.isManager()).thenReturn(isManager);
        when(mockSession.isCustomer()).thenReturn(isCustomer);
        when(mockSessionManager.hasActiveSession()).thenReturn(true);
        when(mockSessionManager.getCurrentSession()).thenReturn(mockSession);
    }

    private static class TestableBaseController extends BaseController {
        public TestableBaseController(Stage stage, SessionManager sessionManager) {
            super(stage, sessionManager);
        }

        @Override
        public void show() {
            StackPane pane = new StackPane(new Label("Dummy View"));
            Scene scene = new Scene(pane, 300, 200);
            stage.setScene(scene);
            stage.show();
        }
    }
}
