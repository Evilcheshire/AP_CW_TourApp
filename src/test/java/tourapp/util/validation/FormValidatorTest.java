package tourapp.util.validation;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class FormValidatorTest {

    private TextField nameField;
    private TextField emailField;
    private TextField countryField;
    private TextField descriptionField;
    private TextField priceField;
    private TextField keywordField;
    private TextField mealsPerDayField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private ComboBox<String> comboBox;
    private ComboBox<String> userTypeComboBox;
    private ComboBox<String> transportTypeComboBox;
    private ComboBox<String> locationTypeComboBox;
    private ComboBox<String> mealTypeComboBox;
    private ComboBox<String> tourTypeComboBox;
    private ComboBox<String> transportComboBox;
    private ComboBox<String> mealComboBox;
    private TextArea textArea;
    private DatePicker datePicker;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Spinner<Double> spinner;
    private Spinner<Double> minPriceSpinner;
    private Spinner<Double> maxPriceSpinner;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            BaseValidator.clearErrors();

            nameField = new TextField();
            emailField = new TextField();
            countryField = new TextField();
            descriptionField = new TextField();
            priceField = new TextField();
            keywordField = new TextField();
            mealsPerDayField = new TextField();
            passwordField = new PasswordField();
            confirmPasswordField = new PasswordField();
            currentPasswordField = new PasswordField();
            newPasswordField = new PasswordField();
            comboBox = new ComboBox<>();
            userTypeComboBox = new ComboBox<>();
            transportTypeComboBox = new ComboBox<>();
            locationTypeComboBox = new ComboBox<>();
            mealTypeComboBox = new ComboBox<>();
            tourTypeComboBox = new ComboBox<>();
            transportComboBox = new ComboBox<>();
            mealComboBox = new ComboBox<>();
            textArea = new TextArea();
            datePicker = new DatePicker();
            startDatePicker = new DatePicker();
            endDatePicker = new DatePicker();
            spinner = new Spinner<>(0.0, 10000.0, 0.0);
            minPriceSpinner = new Spinner<>(0.0, 10000.0, 0.0);
            maxPriceSpinner = new Spinner<>(0.0, 10000.0, 100.0);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLoginForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            emailField.setText("test@example.com");
            passwordField.setText("Password123!");

            boolean result = FormValidator.validateLoginForm(emailField, passwordField);

            assertTrue(result);
            assertFalse(emailField.getStyleClass().contains("validation-error"));
            assertFalse(passwordField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLoginForm_InvalidEmail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            emailField.setText("invalid-email");
            passwordField.setText("Password123!");

            boolean result = FormValidator.validateLoginForm(emailField, passwordField);

            assertFalse(result);
            assertTrue(emailField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLoginForm_InvalidPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            emailField.setText("test@example.com");
            passwordField.setText("weak");

            boolean result = FormValidator.validateLoginForm(emailField, passwordField);

            assertFalse(result);
            assertTrue(passwordField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateRegisterForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("John Doe");
            emailField.setText("john@example.com");
            passwordField.setText("Password123!");
            confirmPasswordField.setText("Password123!");

            boolean result = FormValidator.validateRegisterForm(
                    nameField, emailField, passwordField, confirmPasswordField);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateRegisterForm_PasswordMismatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("John Doe");
            emailField.setText("john@example.com");
            passwordField.setText("Password123!");
            confirmPasswordField.setText("DifferentPassword!");

            boolean result = FormValidator.validateRegisterForm(
                    nameField, emailField, passwordField, confirmPasswordField);

            assertFalse(result);
            assertTrue(confirmPasswordField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateRegisterForm_NullConfirmPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("John Doe");
            emailField.setText("john@example.com");
            passwordField.setText("Password123!");

            boolean result = FormValidator.validateRegisterForm(
                    nameField, emailField, passwordField, null);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateRegisterForm_InvalidName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText(""); // порожнє ім'я
            emailField.setText("john@example.com");
            passwordField.setText("Password123!");
            confirmPasswordField.setText("Password123!");

            boolean result = FormValidator.validateRegisterForm(
                    nameField, emailField, passwordField, confirmPasswordField);

            assertFalse(result);
            assertTrue(nameField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidatePasswordChangeForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            currentPasswordField.setText("CurrentPassword123!");
            newPasswordField.setText("NewPassword123!");
            confirmPasswordField.setText("NewPassword123!");

            boolean result = FormValidator.validatePasswordChangeForm(
                    currentPasswordField, newPasswordField, confirmPasswordField);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidatePasswordChangeForm_EmptyCurrentPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            currentPasswordField.setText("");
            newPasswordField.setText("NewPassword123!");
            confirmPasswordField.setText("NewPassword123!");

            boolean result = FormValidator.validatePasswordChangeForm(
                    currentPasswordField, newPasswordField, confirmPasswordField);

            assertFalse(result);
            assertTrue(currentPasswordField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidatePasswordChangeForm_WeakNewPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            currentPasswordField.setText("CurrentPassword123!");
            newPasswordField.setText("weak");
            confirmPasswordField.setText("weak");

            boolean result = FormValidator.validatePasswordChangeForm(
                    currentPasswordField, newPasswordField, confirmPasswordField);

            assertFalse(result);
            assertTrue(newPasswordField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidatePasswordChangeForm_PasswordMismatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            currentPasswordField.setText("CurrentPassword123!");
            newPasswordField.setText("NewPassword123!");
            confirmPasswordField.setText("DifferentPassword123!");

            boolean result = FormValidator.validatePasswordChangeForm(
                    currentPasswordField, newPasswordField, confirmPasswordField);

            assertFalse(result);
            assertTrue(confirmPasswordField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateUserEditForm_ValidData_PasswordRequired() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("John Doe");
            emailField.setText("john@example.com");
            userTypeComboBox.setValue("Admin");
            passwordField.setText("Password123!");
            confirmPasswordField.setText("Password123!");

            boolean result = FormValidator.validateUserEditForm(
                    nameField, emailField, userTypeComboBox, passwordField, confirmPasswordField, true);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateUserEditForm_ValidData_PasswordNotRequired() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("John Doe");
            emailField.setText("john@example.com");
            userTypeComboBox.setValue("User");

            boolean result = FormValidator.validateUserEditForm(
                    nameField, emailField, userTypeComboBox, null, null, false);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateUserEditForm_NoUserTypeSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("John Doe");
            emailField.setText("john@example.com");
            userTypeComboBox.setValue(null);

            boolean result = FormValidator.validateUserEditForm(
                    nameField, emailField, userTypeComboBox, null, null, false);

            assertFalse(result);
            assertTrue(userTypeComboBox.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Автобус");
            priceField.setText("100");
            transportTypeComboBox.setValue("Наземний");

            boolean result = FormValidator.validateTransportForm(
                    nameField, priceField, transportTypeComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportForm_InvalidPrice() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Автобус");
            priceField.setText("invalid");
            transportTypeComboBox.setValue("Наземний");

            boolean result = FormValidator.validateTransportForm(
                    nameField, priceField, transportTypeComboBox);

            assertFalse(result);
            assertTrue(priceField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportForm_NoTransportTypeSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Автобус");
            priceField.setText("100");
            transportTypeComboBox.setValue(null);

            boolean result = FormValidator.validateTransportForm(
                    nameField, priceField, transportTypeComboBox);

            assertFalse(result);
            assertTrue(transportTypeComboBox.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportTypeForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Наземний транспорт");

            boolean result = FormValidator.validateTransportTypeForm(nameField);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportTypeForm_InvalidName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("");

            boolean result = FormValidator.validateTransportTypeForm(nameField);

            assertFalse(result);
            assertTrue(nameField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportFilterForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("автобус");
            transportTypeComboBox.setValue("Наземний");
            minPriceSpinner.getValueFactory().setValue(50.0);
            maxPriceSpinner.getValueFactory().setValue(200.0);

            boolean result = FormValidator.validateTransportFilterForm(
                    keywordField, transportTypeComboBox, minPriceSpinner, maxPriceSpinner);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportFilterForm_InvalidPriceRange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("автобус");
            minPriceSpinner.getValueFactory().setValue(200.0);
            maxPriceSpinner.getValueFactory().setValue(50.0);

            boolean result = FormValidator.validateTransportFilterForm(
                    keywordField, transportTypeComboBox, minPriceSpinner, maxPriceSpinner);

            assertFalse(result);
            assertTrue(maxPriceSpinner.getEditor().getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTransportFilterForm_NullFields() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            boolean result = FormValidator.validateTransportFilterForm(
                    null, transportTypeComboBox, null, null);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLocationForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Париж");
            countryField.setText("Франція");
            locationTypeComboBox.setValue("Місто");
            textArea.setText("Столиця Франції, відома своєю архітектурою");

            boolean result = FormValidator.validateLocationForm(
                    nameField, countryField, locationTypeComboBox, textArea);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLocationForm_InvalidName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("");
            countryField.setText("Франція");
            locationTypeComboBox.setValue("Місто");
            textArea.setText("Опис");

            boolean result = FormValidator.validateLocationForm(
                    nameField, countryField, locationTypeComboBox, textArea);

            assertFalse(result);
            assertTrue(nameField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLocationForm_NoLocationTypeSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Париж");
            countryField.setText("Франція");
            locationTypeComboBox.setValue(null);
            textArea.setText("Опис");

            boolean result = FormValidator.validateLocationForm(
                    nameField, countryField, locationTypeComboBox, textArea);

            assertFalse(result);
            assertTrue(locationTypeComboBox.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLocationTypeForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Місто");

            boolean result = FormValidator.validateLocationTypeForm(nameField);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateLocationTypeForm_InvalidName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("");

            boolean result = FormValidator.validateLocationTypeForm(nameField);

            assertFalse(result);
            assertTrue(nameField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Повний пансіон");
            mealsPerDayField.setText("3");
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealForm(
                    nameField, mealsPerDayField, mealTypeComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealForm_InvalidMealsPerDay() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Повний пансіон");
            mealsPerDayField.setText("invalid");
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealForm(
                    nameField, mealsPerDayField, mealTypeComboBox);

            assertFalse(result);
            assertTrue(mealsPerDayField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealForm_NoMealTypeSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Повний пансіон");
            mealsPerDayField.setText("3");
            mealTypeComboBox.setValue(null);

            boolean result = FormValidator.validateMealForm(
                    nameField, mealsPerDayField, mealTypeComboBox);

            assertFalse(result);
            assertTrue(mealTypeComboBox.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealForm_NullMealsPerDay() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Повний пансіон");
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealForm(
                    nameField, null, mealTypeComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealTypeForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("Шведський стіл");

            boolean result = FormValidator.validateMealTypeForm(nameField);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealTypeForm_InvalidName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            nameField.setText("");

            boolean result = FormValidator.validateMealTypeForm(nameField);

            assertFalse(result);
            assertTrue(nameField.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    // ========== TOUR FORM TESTS ==========

    @Test
    void testValidateTourForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            descriptionField.setText("Чудовий тур до Парижа");
            priceField.setText("1500");
            startDatePicker.setValue(LocalDate.now().plusDays(1));
            endDatePicker.setValue(LocalDate.now().plusDays(7));
            tourTypeComboBox.setValue("Екскурсійний");
            transportComboBox.setValue("Автобус");
            mealComboBox.setValue("Повний пансіон");

            boolean result = FormValidator.validateTourForm(
                    descriptionField, priceField, startDatePicker, endDatePicker,
                    tourTypeComboBox, transportComboBox, mealComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourForm_InvalidDateRange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            descriptionField.setText("Чудовий тур до Парижа");
            priceField.setText("1500");
            startDatePicker.setValue(LocalDate.now().plusDays(7));
            endDatePicker.setValue(LocalDate.now().plusDays(1));
            tourTypeComboBox.setValue("Екскурсійний");
            transportComboBox.setValue("Автобус");
            mealComboBox.setValue("Повний пансіон");

            boolean result = FormValidator.validateTourForm(
                    descriptionField, priceField, startDatePicker, endDatePicker,
                    tourTypeComboBox, transportComboBox, mealComboBox);

            assertFalse(result);
            assertTrue(endDatePicker.getEditor().getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourForm_NullStartDate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            descriptionField.setText("Чудовий тур до Парижа");
            priceField.setText("1500");
            startDatePicker.setValue(null);
            endDatePicker.setValue(LocalDate.now().plusDays(7));
            tourTypeComboBox.setValue("Екскурсійний");
            transportComboBox.setValue("Автобус");
            mealComboBox.setValue("Повний пансіон");

            boolean result = FormValidator.validateTourForm(
                    descriptionField, priceField, startDatePicker, endDatePicker,
                    tourTypeComboBox, transportComboBox, mealComboBox);

            assertFalse(result);
            assertTrue(startDatePicker.getEditor().getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourForm_NullEndDate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            descriptionField.setText("Чудовий тур до Парижа");
            priceField.setText("1500");
            startDatePicker.setValue(LocalDate.now().plusDays(1));
            endDatePicker.setValue(null);
            tourTypeComboBox.setValue("Екскурсійний");
            transportComboBox.setValue("Автобус");
            mealComboBox.setValue("Повний пансіон");

            boolean result = FormValidator.validateTourForm(
                    descriptionField, priceField, startDatePicker, endDatePicker,
                    tourTypeComboBox, transportComboBox, mealComboBox);

            assertFalse(result);
            assertTrue(endDatePicker.getEditor().getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourForm_NoTourTypeSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            descriptionField.setText("Чудовий тур до Парижа");
            priceField.setText("1500");
            startDatePicker.setValue(LocalDate.now().plusDays(1));
            endDatePicker.setValue(LocalDate.now().plusDays(7));
            tourTypeComboBox.setValue(null);
            transportComboBox.setValue("Автобус");
            mealComboBox.setValue("Повний пансіон");

            boolean result = FormValidator.validateTourForm(
                    descriptionField, priceField, startDatePicker, endDatePicker,
                    tourTypeComboBox, transportComboBox, mealComboBox);

            assertFalse(result);
            assertTrue(tourTypeComboBox.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourForm_NoTransportSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            descriptionField.setText("Чудовий тур до Парижа");
            priceField.setText("1500");
            startDatePicker.setValue(LocalDate.now().plusDays(1));
            endDatePicker.setValue(LocalDate.now().plusDays(7));
            tourTypeComboBox.setValue("Екскурсійний");
            transportComboBox.setValue(null);
            mealComboBox.setValue("Повний пансіон");

            boolean result = FormValidator.validateTourForm(
                    descriptionField, priceField, startDatePicker, endDatePicker,
                    tourTypeComboBox, transportComboBox, mealComboBox);

            assertFalse(result);
            assertTrue(transportComboBox.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourForm_NoMealSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            descriptionField.setText("Чудовий тур до Парижа");
            priceField.setText("1500");
            startDatePicker.setValue(LocalDate.now().plusDays(1));
            endDatePicker.setValue(LocalDate.now().plusDays(7));
            tourTypeComboBox.setValue("Екскурсійний");
            transportComboBox.setValue("Автобус");
            mealComboBox.setValue(null);

            boolean result = FormValidator.validateTourForm(
                    descriptionField, priceField, startDatePicker, endDatePicker,
                    tourTypeComboBox, transportComboBox, mealComboBox);

            assertFalse(result);
            assertTrue(mealComboBox.getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    // ========== TOUR FILTER FORM TESTS ==========

    @Test
    void testValidateTourFilterForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("Париж");
            minPriceSpinner.getValueFactory().setValue(500.0);
            maxPriceSpinner.getValueFactory().setValue(2000.0);
            startDatePicker.setValue(LocalDate.now().plusDays(1));
            endDatePicker.setValue(LocalDate.now().plusDays(7));

            boolean result = FormValidator.validateTourFilterForm(
                    keywordField, minPriceSpinner, maxPriceSpinner, startDatePicker, endDatePicker);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourFilterForm_InvalidPriceRange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("Париж");
            minPriceSpinner.getValueFactory().setValue(2000.0);
            maxPriceSpinner.getValueFactory().setValue(500.0);
            startDatePicker.setValue(LocalDate.now().plusDays(1));
            endDatePicker.setValue(LocalDate.now().plusDays(7));

            boolean result = FormValidator.validateTourFilterForm(
                    keywordField, minPriceSpinner, maxPriceSpinner, startDatePicker, endDatePicker);

            assertFalse(result);
            assertTrue(maxPriceSpinner.getEditor().getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourFilterForm_InvalidDateRange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("Париж");
            minPriceSpinner.getValueFactory().setValue(500.0);
            maxPriceSpinner.getValueFactory().setValue(2000.0);
            startDatePicker.setValue(LocalDate.now().plusDays(7));
            endDatePicker.setValue(LocalDate.now().plusDays(1));

            boolean result = FormValidator.validateTourFilterForm(
                    keywordField, minPriceSpinner, maxPriceSpinner, startDatePicker, endDatePicker);

            assertFalse(result);
            assertTrue(endDatePicker.getEditor().getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateTourFilterForm_NullFields() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            boolean result = FormValidator.validateTourFilterForm(
                    null, null, null, null, null);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealFilterForm_ValidData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("пансіон");
            minPriceSpinner.getValueFactory().setValue(50.0);
            maxPriceSpinner.getValueFactory().setValue(200.0);
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealFilterForm(
                    keywordField, minPriceSpinner, maxPriceSpinner, mealTypeComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealFilterForm_InvalidPriceRange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("пансіон");
            minPriceSpinner.getValueFactory().setValue(200.0);
            maxPriceSpinner.getValueFactory().setValue(50.0);
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealFilterForm(
                    keywordField, minPriceSpinner, maxPriceSpinner, mealTypeComboBox);

            assertFalse(result);
            assertTrue(maxPriceSpinner.getEditor().getStyleClass().contains("validation-error"));

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealFilterForm_EmptyKeyword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("");
            minPriceSpinner.getValueFactory().setValue(50.0);
            maxPriceSpinner.getValueFactory().setValue(200.0);
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealFilterForm(
                    keywordField, minPriceSpinner, maxPriceSpinner, mealTypeComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealFilterForm_NullKeywordField() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            minPriceSpinner.getValueFactory().setValue(50.0);
            maxPriceSpinner.getValueFactory().setValue(200.0);
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealFilterForm(
                    null, minPriceSpinner, maxPriceSpinner, mealTypeComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidateMealFilterForm_NullPriceSpinners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            keywordField.setText("пансіон");
            mealTypeComboBox.setValue("Шведський стіл");

            boolean result = FormValidator.validateMealFilterForm(
                    keywordField, null, null, mealTypeComboBox);

            assertTrue(result);

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testIsDuplicateName_NoDuplicate() {
        List<TestItem> items = List.of(
                new TestItem(1, "Item 1"),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "Item 3", TestItem::getName, TestItem::getId, null);

        assertFalse(result);
    }

    @Test
    void testIsDuplicateName_HasDuplicate() {
        List<TestItem> items = List.of(
                new TestItem(1, "Item 1"),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "Item 1", TestItem::getName, TestItem::getId, null);

        assertTrue(result);
    }

    @Test
    void testIsDuplicateName_SameItemEditing() {
        List<TestItem> items = List.of(
                new TestItem(1, "Item 1"),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "Item 1", TestItem::getName, TestItem::getId, 1);

        assertFalse(result);
    }

    @Test
    void testIsDuplicateName_EmptyName() {
        List<TestItem> items = List.of(
                new TestItem(1, "Item 1"),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "", TestItem::getName, TestItem::getId, null);

        assertFalse(result);
    }

    @Test
    void testIsDuplicateName_CaseInsensitive() {
        List<TestItem> items = List.of(
                new TestItem(1, "Item 1"),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "ITEM 1", TestItem::getName, TestItem::getId, null);

        assertTrue(result);
    }

    @Test
    void testIsDuplicateName_TrimmedName() {
        List<TestItem> items = List.of(
                new TestItem(1, "Item 1"),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "  Item 1  ", TestItem::getName, TestItem::getId, null);

        assertTrue(result);
    }

    @Test
    void testIsDuplicateName_NullExistingName() {
        List<TestItem> items = List.of(
                new TestItem(1, null),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "Item 1", TestItem::getName, TestItem::getId, null);

        assertFalse(result);
    }

    @Test
    void testIsDuplicateName_EmptyList() {
        List<TestItem> items = List.of();

        boolean result = FormValidator.isDuplicateName(
                items, "Item 1", TestItem::getName, TestItem::getId, null);

        assertFalse(result);
    }

    @Test
    void testIsDuplicateName_DifferentCurrentItemId() {
        List<TestItem> items = List.of(
                new TestItem(1, "Item 1"),
                new TestItem(2, "Item 2")
        );

        boolean result = FormValidator.isDuplicateName(
                items, "Item 1", TestItem::getName, TestItem::getId, 2);

        assertTrue(result);
    }

    private static class TestItem {
        private final Integer id;
        private final String name;

        public TestItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}