package tourapp.view;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.util.Optional;

public class
HelperMethods {

    public static void setFieldValue(Object controller, String fieldName, Object value) throws Exception {
        Field field = controller.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    public static Object getFieldValue(Object controller, String fieldName) throws Exception {
        Field field = controller.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(controller);
    }

    public static void replaceText(FxRobot robot, String fieldSelector, String newText) {
        robot.clickOn(fieldSelector).write(newText);
    }

    public static void clickOnYes(FxRobot robot){
        try {
            robot.clickOn("Yes");
        } catch (Exception e) {
            try {
                robot.clickOn("Так");
            } catch (Exception ex) {
                robot.press(KeyCode.ENTER);
            }
        }
    }

    public static void clickOnNo(FxRobot robot){
        try {
            robot.clickOn("No");
        } catch (Exception e) {
            try {
                robot.clickOn("Ні");
            } catch (Exception ex) {
                robot.press(KeyCode.ESCAPE);
            }
        }
    }

    public static void clickOnOK(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        try {
            robot.clickOn("OK");
        } catch (Exception e1) {
            try {
                robot.clickOn("Гаразд");
            } catch (Exception e2) {
                try {
                    Optional<Stage> alertStage = robot.listTargetWindows().stream()
                            .filter(window -> window instanceof Stage)
                            .map(window -> (Stage) window)
                            .filter(stage -> stage.getScene() != null &&
                                    stage.getScene().getRoot() instanceof DialogPane)
                            .findFirst();

                    if (alertStage.isPresent()) {
                        DialogPane pane = (DialogPane) alertStage.get().getScene().getRoot();
                        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
                        if (okButton != null) {
                            Platform.runLater(okButton::fire);
                            WaitForAsyncUtils.waitForFxEvents();
                            return;
                        }
                    }

                    robot.press(KeyCode.ENTER);
                } catch (Exception e3) {
                    robot.press(KeyCode.ENTER);
                }
            }
        }

        WaitForAsyncUtils.waitForFxEvents();
    }
}