package tourapp.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tourapp.util.SessionManager;
import tourapp.util.validation.BaseValidator;
import tourapp.util.validation.FormValidator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public abstract class BaseEditController<T> extends BaseController {

    public final Stage editStage;
    protected final T entityToEdit;
    protected Runnable onSaveCallback;

    protected BaseEditController(Stage parentStage, SessionManager sessionManager, T entityToEdit) {
        super(parentStage, sessionManager);
        this.entityToEdit = entityToEdit;
        this.editStage = new Stage();
        this.editStage.initModality(Modality.WINDOW_MODAL);
        this.editStage.initOwner(parentStage);
    }

    @Override
    public void show() {
        String title = getWindowTitle();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getFxmlPath()));
            loader.setController(this);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
            Image icon = new Image(getClass().getResourceAsStream("/tourapp/images/question.png"));
            editStage.getIcons().add(icon);
            editStage.setScene(scene);
            editStage.setTitle(title);
            editStage.show();

        } catch (IOException e) {
            showError("Не вдалося завантажити інтерфейс: " + e.getMessage());
        }
    }

    protected abstract String getFxmlPath();

    protected abstract String getWindowTitle();

    protected abstract void setupValidationListeners();

    protected abstract void loadEntityData() throws SQLException;

    protected abstract boolean validateForm();

    protected abstract T createEntityFromForm();

    protected abstract void updateEntityFromForm(T entity);

    protected abstract void saveNewEntity(T entity) throws SQLException;

    protected abstract void updateExistingEntity(T entity) throws SQLException;

    protected abstract String getCreateSuccessMessage();

    protected abstract String getUpdateSuccessMessage();

    protected <E> void initializeComboBox(ComboBox<E> comboBox, List<E> items, Function<E, String> nameExtractor) {
        comboBox.setItems(FXCollections.observableArrayList(items));
        comboBox.setConverter(new StringConverter<E>() {
            @Override
            public String toString(E item) {
                return (item != null) ? nameExtractor.apply(item) : "";
            }

            @Override
            public E fromString(String string) {
                return null;
            }
        });
    }

    protected <E> E findItemById(List<E> items, int id, Function<E, Integer> idExtractor) {
        for (E item : items) {
            if (idExtractor.apply(item) == id) {
                return item;
            }
        }
        return null;
    }

    protected void addValidationListener(javafx.scene.control.Control control) {
        if (control instanceof javafx.scene.control.TextField) {
            ((javafx.scene.control.TextField) control).textProperty().addListener((obs, oldVal, newVal) -> {
                BaseValidator.clearErrorsForControls(control);
            });
        } else if (control instanceof javafx.scene.control.ComboBox) {
            ((javafx.scene.control.ComboBox<?>) control).valueProperty().addListener((obs, oldVal, newVal) -> {
                BaseValidator.clearErrorsForControls(control);
            });
        } else if (control instanceof javafx.scene.control.DatePicker) {
            ((javafx.scene.control.DatePicker) control).valueProperty().addListener((obs, oldVal, newVal) -> {
                BaseValidator.clearErrorsForControls(((javafx.scene.control.DatePicker) control).getEditor());
            });
        }
    }

    protected void handleSave() {
        if (!validateForm()) {
            FormValidator.showValidationErrors(editStage);
            return;
        }

        try {
            if (isCreateMode()) {
                T entity = createEntityFromForm();
                saveNewEntity(entity);
                showInfo(getCreateSuccessMessage());
                logger.info("Створено сутність: {}", entity.toString());
            } else {
                updateEntityFromForm(entityToEdit);
                updateExistingEntity(entityToEdit);
                showInfo(getUpdateSuccessMessage());
                logger.info("Оновлено сутність: {}", entityToEdit.toString());
            }
            executeCallbackAndClose();
        } catch (SQLException e) {
            String errorMessage = isCreateMode() ? "Помилка при створенні: " : "Помилка при оновленні: ";
            showError(errorMessage + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Неправильний формат числових полів. Будь ласка, перевірте введені дані.");
        }
    }

    protected void handleCancel() {
        FormValidator.clearErrors();
        editStage.close();
    }

    protected boolean isCreateMode() {
        return entityToEdit == null;
    }

    protected void executeCallbackAndClose() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        editStage.close();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @Override
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/cross.png").toExternalForm()));
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(editStage);
        alert.showAndWait();
        logger.error(message);
    }

    @Override
    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/info.png").toExternalForm()));
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/tourapp/css/styles.css").toExternalForm());
        alert.setTitle("Інформація");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(editStage);
        alert.showAndWait();
    }

    public void initialize() {
        setupValidationListeners();
        if (!isCreateMode()) {
            try {
                loadEntityData();
            } catch (SQLException e) {
                showError("Помилка завантаження даних: " + e.getMessage());
            }
        }
    }
}