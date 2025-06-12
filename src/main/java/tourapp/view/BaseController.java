package tourapp.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import tourapp.util.SessionManager;

import java.io.IOException;
import org.slf4j.Logger;


public abstract class BaseController {
    public static Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected final Stage stage;
    protected final SessionManager sessionManager;

    protected BaseController(Stage stage, SessionManager sessionManager) {
        this.stage = stage;
        this.sessionManager = sessionManager;
    }

    public void loadAndShow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setController(this);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            Image icon = new Image(getClass().getResourceAsStream("/tourapp/images/icon.png"));
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            showError("Не вдалося завантажити інтерфейс: " + e.getMessage());
        }
    }

    public abstract void show();

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/cross.png").toExternalForm()));
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logger.error(message);
    }

    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
        alert.setTitle("Інформація");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected boolean isAuthenticated() {
        return sessionManager.hasActiveSession();
    }

    public boolean isAdmin() {
        return isAuthenticated() && sessionManager.getCurrentSession().isAdmin();
    }

    public boolean isManager() {
        return isAuthenticated() && sessionManager.getCurrentSession().isManager();
    }

    public boolean isCustomer() {
        return isAuthenticated() && sessionManager.getCurrentSession().isCustomer();
    }
}
