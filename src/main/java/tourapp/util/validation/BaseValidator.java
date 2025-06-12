package tourapp.util.validation;

import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.SystemColor.control;

public abstract class BaseValidator {

    private static final Map<Control, String> originalStyles = new HashMap<>();
    private static final List<String> validationErrors = new ArrayList<>();

    public static ValidationResult validatePriceRange(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return new ValidationResult(false, "Ціна не може бути порожньою");
        }

        String cleanPrice = priceStr.trim();

        if (!cleanPrice.matches("^\\d+(\\.\\d{1,2})?$")) {
            return new ValidationResult(false, "Ціна може містити тільки цифри та крапку для десяткових значень");
        }

        try {
            double price = Double.parseDouble(cleanPrice);
            if (price < 0) {
                return new ValidationResult(false, "Ціна не може бути від'ємною");
            }
            if (price > 999999.99) {
                return new ValidationResult(false, "Ціна занадто велика (максимум 999999.99)");
            }
            if (price == 0) {
                return new ValidationResult(false, "Ціна повинна бути більше 0");
            }
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Невірний формат ціни");
        }

        return new ValidationResult(true, null);
    }

    public static void addError(Control control, String message) {
        if (control == null || message == null || message.trim().isEmpty()) return;

        if (!validationErrors.contains(message)) {
            validationErrors.add(message);
        }
        control.getStyleClass().add("validation-error");

        Tooltip errorTooltip = new Tooltip(message);
        Tooltip.install(control, errorTooltip);
    }

    public static void clearErrors() {
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> {
            for (Map.Entry<Control, String> entry : originalStyles.entrySet()) {
                Control control = entry.getKey();
                control.getStyleClass().remove("validation-error");
                Tooltip.uninstall(control, control.getTooltip());
            }
            originalStyles.clear();
            validationErrors.clear();
        });
        pause.play();
    }

    public static void clearErrorsForControls(Control... controls) {
        for (Control control : controls) {
            if (originalStyles.containsKey(control)) {
                String originalStyle = originalStyles.get(control);
                control.setStyle(originalStyle != null ? originalStyle : "");
                Tooltip.uninstall(control, control.getTooltip());
                originalStyles.remove(control);
            }
        }
    }

    public static String getValidationErrorsFormatted() {
        if (validationErrors.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Знайдено наступні помилки:\n\n");

        for (int i = 0; i < validationErrors.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, validationErrors.get(i)));
        }

        return sb.toString().trim();
    }

    public static boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }

    public static void showValidationErrors(Window owner) {
        if (!BaseValidator.hasValidationErrors()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Помилки валідації");
        alert.setHeaderText("Будь ласка, виправте наступні помилки:");
        alert.setContentText(BaseValidator.getValidationErrorsFormatted());

        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.showAndWait();
    }
}