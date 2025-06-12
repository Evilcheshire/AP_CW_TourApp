package tourapp.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tourapp.util.SessionManager;
import tourapp.util.validation.BaseValidator;
import tourapp.util.validation.FormValidator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public abstract class BaseTypeEditController<T> extends BaseController {

    @FXML protected TextField nameField;
    @FXML protected Button saveButton;
    @FXML protected Button cancelButton;
    @FXML protected Button deleteButton;

    protected T itemToEdit;
    protected Runnable onSaveCallback;
    public final Stage editStage;

    protected BaseTypeEditController(Stage stage, SessionManager sessionManager, T itemToEdit) {
        super(stage, sessionManager);
        this.itemToEdit = itemToEdit;

        this.editStage = new Stage();
        this.editStage.initModality(Modality.WINDOW_MODAL);
        this.editStage.initOwner(stage);
    }

    @Override
    public void show() {
        String title = getTitle();
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
            editStage.setResizable(false);
            editStage.show();
        } catch (IOException e) {
            showError(getLoadErrorMessage() + ": " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            BaseValidator.clearErrorsForControls(nameField);
        });

        deleteButton.setVisible(itemToEdit != null);
        deleteButton.setManaged(itemToEdit != null);

        if (itemToEdit != null){
            nameField.setText(getItemName(itemToEdit));
        }
    }

    @FXML
    protected void handleDelete() {
        if (itemToEdit == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage alertStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResource("/tourapp/images/checkbox.png").toExternalForm()));
        confirmAlert.setTitle("Підтвердження видалення");
        confirmAlert.setHeaderText(getDeleteHeaderText());
        confirmAlert.setContentText("Ви впевнені, що хочете видалити " + getItemTypeName().toLowerCase() + " \"" +
                getItemName(itemToEdit) + "\"?\n\nЦю дію неможливо скасувати.");
        confirmAlert.initOwner(editStage);

        ButtonType deleteButtonType = new ButtonType("Видалити", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Скасувати", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == deleteButtonType) {
                try {
                    deleteItem(getItemId(itemToEdit));
                    showInfo(getItemTypeName() + " успішно видалено!");
                    logger.info("Видалено {}: {}", getItemTypeName().toLowerCase(), itemToEdit.toString());
                    executeCallbackAndClose();
                } catch (SQLException e) {
                    if (e.getMessage().contains("foreign key constraint") ||
                            e.getMessage().contains("FOREIGN KEY constraint")) {
                        showError("Неможливо видалити " + getItemTypeName().toLowerCase() +
                                ", оскільки він використовується в інших записах.");
                    } else {
                        showError("Помилка при видаленні " + getItemTypeName().toLowerCase() + ": " + e.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    protected void handleSave() {
        if (!validateForm()) {
            FormValidator.showValidationErrors(editStage);
            return;
        }

        T item = (itemToEdit != null) ? itemToEdit : createNewItem();
        setItemName(item, nameField.getText().trim());

        if (itemToEdit == null) {
            try {
                createItem(item);
                showInfo(getItemTypeName() + " успішно створено!");
                logger.info("Створено {}: {}", getItemTypeName().toLowerCase(), item.toString());
                executeCallbackAndClose();
            } catch (SQLException e) {
                showError("Помилка при створенні " + getItemTypeName().toLowerCase() + ": " + e.getMessage());
            }
        } else {
            try {
                updateItem(getItemId(item), item);
                showInfo(getItemTypeName() + " успішно оновлено!");
                logger.info("Оновлено {}: {}", getItemTypeName().toLowerCase(), item.toString());
                executeCallbackAndClose();
            } catch (SQLException e) {
                showError("Помилка при оновленні " + getItemTypeName().toLowerCase() + ": " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleCancel() {
        FormValidator.clearErrors();
        editStage.close();
    }

    protected boolean validateForm() {
        boolean isValid = validateNameField();

        if (isValid) {
            try {
                if (FormValidator.isDuplicateName(
                        getAllItems(),
                        nameField.getText().trim(),
                        this::getItemName,
                        this::getItemId,
                        (itemToEdit != null) ? getItemId(itemToEdit) : null
                )) {
                    String duplicateError = getItemTypeName() + " з такою назвою вже існує";
                    BaseValidator.addError(nameField, duplicateError);
                    isValid = false;
                }
            } catch (SQLException e) {
                showError("Помилка перевірки унікальності назви: " + e.getMessage());
                return false;
            }
        }

        return isValid;
    }

    protected void executeCallbackAndClose() {
        onSaveCallback.run();
        editStage.close();
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

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    protected abstract String getTitle();

    protected abstract String getFxmlPath();

    protected abstract String getLoadErrorMessage();

    protected abstract String getLoadDataErrorMessage();

    protected abstract String getItemTypeName();

    protected abstract String getDeleteHeaderText();

    protected abstract T createNewItem();

    protected abstract List<T> getAllItems() throws SQLException;

    protected abstract void createItem(T item) throws SQLException;

    protected abstract void updateItem(int id, T item) throws SQLException;

    protected abstract void deleteItem(int id) throws SQLException;

    protected abstract int getItemId(T item);

    protected abstract String getItemName(T item);

    protected abstract void setItemName(T item, String name);

    protected abstract boolean validateNameField();

}