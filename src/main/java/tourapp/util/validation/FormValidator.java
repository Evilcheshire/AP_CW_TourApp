package tourapp.util.validation;

import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FormValidator extends BaseValidator {

    public static boolean validateLoginForm(TextField emailField, PasswordField passwordField) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult emailResult = UserValidator.validateEmail(emailField.getText());
        if (!emailResult.valid()) {
            addError(emailField, emailResult.errorMessage());
            errors.add(emailResult.errorMessage());
        }

        ValidationResult passwordResult = UserValidator.validatePassword(passwordField.getText());
        if (!passwordResult.valid()) {
            addError(passwordField, passwordResult.errorMessage());
            errors.add(passwordResult.errorMessage());
        }

        return errors.isEmpty();
    }

    public static boolean validateRegisterForm(TextField nameField, TextField emailField,
                                               PasswordField passwordField, PasswordField confirmPasswordField) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = UserValidator.validateName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        ValidationResult emailResult = UserValidator.validateEmail(emailField.getText());
        if (!emailResult.valid()) {
            addError(emailField, emailResult.errorMessage());
            errors.add(emailResult.errorMessage());
        }

        if (passwordField != null) {
            ValidationResult passwordResult = UserValidator.validatePassword(passwordField.getText());
            if (!passwordResult.valid()) {
                addError(passwordField, passwordResult.errorMessage());
                errors.add(passwordResult.errorMessage());
            }
        }

        if (confirmPasswordField != null) {
            ValidationResult confirmResult = UserValidator.validatePasswordConfirmation(
                    passwordField.getText(), confirmPasswordField.getText());
            if (!confirmResult.valid()) {
                addError(confirmPasswordField, confirmResult.errorMessage());
                errors.add(confirmResult.errorMessage());
            }
        }

        return errors.isEmpty();
    }

    public static boolean validatePasswordChangeForm(PasswordField currentPasswordField,
                                                     PasswordField newPasswordField,
                                                     PasswordField confirmPasswordField) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        if (currentPasswordField.getText().isEmpty()) {
            addError(currentPasswordField, "Введіть поточний пароль");
            errors.add("Введіть поточний пароль");
        }

        ValidationResult passwordResult = UserValidator.validatePassword(newPasswordField.getText());
        if (!passwordResult.valid()) {
            addError(newPasswordField, passwordResult.errorMessage());
            errors.add(passwordResult.errorMessage());
        }

        ValidationResult confirmResult = UserValidator.validatePasswordConfirmation(
                newPasswordField.getText(), confirmPasswordField.getText());
        if (!confirmResult.valid()) {
            addError(confirmPasswordField, confirmResult.errorMessage());
            errors.add(confirmResult.errorMessage());
        }

        return errors.isEmpty();
    }

    public static boolean validateUserEditForm(TextField nameField, TextField emailField,
                                               ComboBox<?> userTypeComboBox,
                                               PasswordField passwordField,
                                               PasswordField confirmPasswordField,
                                               boolean isPasswordRequired) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = UserValidator.validateName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        ValidationResult emailResult = UserValidator.validateEmail(emailField.getText());
        if (!emailResult.valid()) {
            addError(emailField, emailResult.errorMessage());
            errors.add(emailResult.errorMessage());
        }

        if (userTypeComboBox.getValue() == null) {
            addError(userTypeComboBox, "Будь ласка, оберіть тип користувача");
            errors.add("Будь ласка, оберіть тип користувача");
        }

        if (isPasswordRequired && passwordField != null && confirmPasswordField != null) {
            ValidationResult passwordResult = UserValidator.validatePassword(passwordField.getText());
            if (!passwordResult.valid()) {
                addError(passwordField, passwordResult.errorMessage());
                errors.add(passwordResult.errorMessage());
            }

            ValidationResult confirmResult = UserValidator.validatePasswordConfirmation(
                    passwordField.getText(), confirmPasswordField.getText());
            if (!confirmResult.valid()) {
                addError(confirmPasswordField, confirmResult.errorMessage());
                errors.add(confirmResult.errorMessage());
            }
        }

        return errors.isEmpty();
    }

    public static boolean validateTransportForm(TextField nameField,
                                                TextField pricePerPersonField,
                                                ComboBox<?> transportTypeComboBox) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = TransportValidator.validateTransportName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        ValidationResult priceResult = TransportValidator.validatePricePerPerson(pricePerPersonField.getText());
        if (!priceResult.valid()) {
            addError(pricePerPersonField, priceResult.errorMessage());
            errors.add(priceResult.errorMessage());
        }

        if (transportTypeComboBox.getValue() == null) {
            addError(transportTypeComboBox, "Виберіть тип транспорту");
            errors.add("Виберіть тип транспорту");
        }

        return errors.isEmpty();
    }

    public static boolean validateTransportTypeForm(TextField nameField) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = TransportValidator.validateTransportTypeName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        return errors.isEmpty();
    }

    public static boolean validateTransportFilterForm(TextField keywordField,
                                                      ComboBox<String> transportTypeFilterCombo,
                                                      Spinner<Double> minPriceSpinner,
                                                      Spinner<Double> maxPriceSpinner) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        if (keywordField != null) {
            ValidationResult keywordResult = TransportValidator.validateSearchKeyword(keywordField.getText());
            if (!keywordResult.valid()) {
                addError(keywordField, keywordResult.errorMessage());
                errors.add(keywordResult.errorMessage());
            }
        }

        if (minPriceSpinner != null && maxPriceSpinner != null) {
            double minPrice = minPriceSpinner.getValue();
            double maxPrice = maxPriceSpinner.getValue();

            ValidationResult priceRangeResult = TransportValidator.validatePriceRange(minPrice, maxPrice);
            if (!priceRangeResult.valid()) {
                addError(maxPriceSpinner.getEditor(), priceRangeResult.errorMessage());
                errors.add(priceRangeResult.errorMessage());
            }
        }

        return errors.isEmpty();
    }

    public static boolean validateLocationForm(TextField nameField,
                                               TextField countryField,
                                               ComboBox<?> locationTypeComboBox,
                                               TextArea descriptionField) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = LocationValidator.validateLocationName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        ValidationResult countryResult = LocationValidator.validateLocationCountry(countryField.getText());
        if (!countryResult.valid()) {
            addError(countryField, countryResult.errorMessage());
            errors.add(countryResult.errorMessage());
        }

        if (locationTypeComboBox != null && locationTypeComboBox.getValue() == null) {
            addError(locationTypeComboBox, "Виберіть тип локації");
            errors.add("Виберіть тип локації");
        }


        ValidationResult descriptionResult = LocationValidator.validateLocationDescription(descriptionField.getText());
        if (!descriptionResult.valid()) {
            addError(descriptionField, descriptionResult.errorMessage());
            errors.add(descriptionResult.errorMessage());
        }

        return errors.isEmpty();
    }

    public static boolean validateLocationTypeForm(TextField nameField) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = LocationValidator.validateLocationTypeName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        return errors.isEmpty();
    }

    public static boolean validateMealForm(TextField nameField,
                                           TextField mealsPerDayField,
                                           ComboBox<?> mealTypeComboBox) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = MealValidator.validateMealName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        if (mealsPerDayField != null) {
            ValidationResult mealsResult = MealValidator.validateMealsPerDay(mealsPerDayField.getText());
            if (!mealsResult.valid()) {
                addError(mealsPerDayField, mealsResult.errorMessage());
                errors.add(mealsResult.errorMessage());
            }
        }

        if (mealTypeComboBox != null && mealTypeComboBox.getValue() == null) {
            addError(mealTypeComboBox, "Виберіть тип харчування");
            errors.add("Виберіть тип харчування");
        }

        return errors.isEmpty();
    }

    public static boolean validateMealTypeForm(TextField nameField) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = MealValidator.validateMealTypeName(nameField.getText());
        if (!nameResult.valid()) {
            addError(nameField, nameResult.errorMessage());
            errors.add(nameResult.errorMessage());
        }

        return errors.isEmpty();
    }

    public static boolean validateTourForm(TextField descriptionField,
                                           TextField priceField,
                                           DatePicker startDatePicker,
                                           DatePicker endDatePicker,
                                           ComboBox<?> tourTypeComboBox,
                                           ComboBox<?> transportComboBox,
                                           ComboBox<?> mealComboBox) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        // Валідація опису туру
        if (descriptionField != null) {
            ValidationResult descriptionResult = TourValidator.validateTourDescription(descriptionField.getText());
            if (!descriptionResult.valid()) {
                addError(descriptionField, descriptionResult.errorMessage());
                errors.add(descriptionResult.errorMessage());
            }
        }

        // Валідація ціни туру
        if (priceField != null) {
            ValidationResult priceResult = TourValidator.validateTourPrice(priceField.getText());
            if (!priceResult.valid()) {
                addError(priceField, priceResult.errorMessage());
                errors.add(priceResult.errorMessage());
            }
        }

        // Валідація дат
        if (startDatePicker != null && endDatePicker != null) {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate != null && endDate != null) {
                ValidationResult dateRangeResult = TourValidator.validateDateRange(startDate, endDate);
                if (!dateRangeResult.valid()) {
                    addError(endDatePicker.getEditor(), dateRangeResult.errorMessage());
                    errors.add(dateRangeResult.errorMessage());
                }
            } else if (startDate == null) {
                addError(startDatePicker.getEditor(), "Виберіть дату початку туру");
                errors.add("Виберіть дату початку туру");
            } else {
                addError(endDatePicker.getEditor(), "Виберіть дату закінчення туру");
                errors.add("Виберіть дату закінчення туру");
            }
        }

        if (tourTypeComboBox != null && tourTypeComboBox.getValue() == null) {
            addError(tourTypeComboBox, "Виберіть тип туру");
            errors.add("Виберіть тип туру");
        }

        if (transportComboBox != null && transportComboBox.getValue() == null) {
            addError(transportComboBox, "Виберіть транспорт для туру");
            errors.add("Виберіть транспорт для туру");
        }

        if (mealComboBox != null && mealComboBox.getValue() == null) {
            addError(mealComboBox, "Виберіть харчування для туру");
            errors.add("Виберіть харчування для туру");
        }

        return errors.isEmpty();
    }

    public static boolean validateTourFilterForm(TextField keywordField,
                                                 Spinner<Double> minPriceSpinner,
                                                 Spinner<Double> maxPriceSpinner,
                                                 DatePicker startDatePicker,
                                                 DatePicker endDatePicker) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        if (keywordField != null) {
            ValidationResult keywordResult = TransportValidator.validateSearchKeyword(keywordField.getText());
            if (!keywordResult.valid()) {
                addError(keywordField, keywordResult.errorMessage());
                errors.add(keywordResult.errorMessage());
            }
        }

        if (minPriceSpinner != null && maxPriceSpinner != null) {
            double minPrice = minPriceSpinner.getValue();
            double maxPrice = maxPriceSpinner.getValue();

            ValidationResult priceRangeResult = TransportValidator.validatePriceRange(minPrice, maxPrice);
            if (!priceRangeResult.valid()) {
                addError(maxPriceSpinner.getEditor(), priceRangeResult.errorMessage());
                errors.add(priceRangeResult.errorMessage());
            }
        }

        if (startDatePicker != null && endDatePicker != null) {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    addError(endDatePicker.getEditor(), "Дата закінчення повинна бути після дати початку");
                    errors.add("Дата закінчення повинна бути після дати початку");
                }
            }
        }

        return errors.isEmpty();
    }

    public static boolean validateMealFilterForm(TextField keywordField,
                                                 Spinner<Double> minPriceSpinner,
                                                 Spinner<Double> maxPriceSpinner,
                                                 ComboBox<String> mealTypeFilterCombo) {
        clearErrors();
        List<String> errors = new ArrayList<>();

        if (keywordField != null && keywordField.getText() != null && !keywordField.getText().trim().isEmpty()) {
            ValidationResult keywordResult = TransportValidator.validateSearchKeyword(keywordField.getText());
            if (!keywordResult.valid()) {
                addError(keywordField, keywordResult.errorMessage());
                errors.add(keywordResult.errorMessage());
            }
        }

        if (minPriceSpinner != null && maxPriceSpinner != null) {
            double minPrice = minPriceSpinner.getValue();
            double maxPrice = maxPriceSpinner.getValue();

            ValidationResult priceRangeResult = TransportValidator.validatePriceRange(minPrice, maxPrice);
            if (!priceRangeResult.valid()) {
                addError(maxPriceSpinner.getEditor(), priceRangeResult.errorMessage());
                errors.add(priceRangeResult.errorMessage());
            }
        }

        return errors.isEmpty();
    }

    public static <T> boolean isDuplicateName(List<T> items,
                                              String newName,
                                              Function<T, String> nameExtractor,
                                              Function<T, Integer> idExtractor,
                                              Integer currentItemId) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }

        String trimmedName = newName.trim();

        for (T item : items) {
            String existingName = nameExtractor.apply(item);
            if (existingName != null && existingName.equalsIgnoreCase(trimmedName)) {
                if (currentItemId != null && idExtractor.apply(item).equals(currentItemId)) {
                    continue;
                }
                return true;
            }
        }

        return false;
    }

}