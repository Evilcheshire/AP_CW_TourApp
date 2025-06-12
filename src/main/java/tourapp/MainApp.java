package tourapp;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tourapp.util.AppContext;
import tourapp.util.ControllerFactory;
import tourapp.view.auth_controller.LoginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) {
        AppContext appContext = new AppContext();
        appContext.getServiceLocator().register(Stage.class, primaryStage);

        ControllerFactory controllerFactory = new ControllerFactory(appContext.getServiceLocator());

        LoginController loginController = controllerFactory.createLoginController();
        loginController.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
