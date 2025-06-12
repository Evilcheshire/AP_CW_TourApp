package tourapp.util.validation;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class BaseValidatorTest {

    private TextField testTextField;

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

            testTextField = new TextField();

            latch.countDown();
        });

        latch.await();
    }

    @Test
    void testValidatePriceRange_ValidPrice() {
        ValidationResult result = BaseValidator.validatePriceRange("100.50");
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidatePriceRange_EmptyPrice() {
        ValidationResult result = BaseValidator.validatePriceRange("");
        assertFalse(result.valid());
        assertEquals("Ціна не може бути порожньою", result.errorMessage());

        result = BaseValidator.validatePriceRange(null);
        assertFalse(result.valid());
        assertEquals("Ціна не може бути порожньою", result.errorMessage());

        result = BaseValidator.validatePriceRange("   ");
        assertFalse(result.valid());
        assertEquals("Ціна не може бути порожньою", result.errorMessage());
    }

    @Test
    void testValidatePriceRange_InvalidFormat() {
        ValidationResult result = BaseValidator.validatePriceRange("abc");
        assertFalse(result.valid());
        assertEquals("Ціна може містити тільки цифри та крапку для десяткових значень", result.errorMessage());

        result = BaseValidator.validatePriceRange("100.123");
        assertFalse(result.valid());
        assertEquals("Ціна може містити тільки цифри та крапку для десяткових значень", result.errorMessage());

        result = BaseValidator.validatePriceRange("100..50");
        assertFalse(result.valid());
        assertEquals("Ціна може містити тільки цифри та крапку для десяткових значень", result.errorMessage());
    }

    @Test
    void testValidatePriceRange_NegativePrice() {
        ValidationResult result = BaseValidator.validatePriceRange("-100");
        assertFalse(result.valid());
        assertEquals("Ціна може містити тільки цифри та крапку для десяткових значень", result.errorMessage());
    }

    @Test
    void testValidatePriceRange_ZeroPrice() {
        ValidationResult result = BaseValidator.validatePriceRange("0");
        assertFalse(result.valid());
        assertEquals("Ціна повинна бути більше 0", result.errorMessage());

        result = BaseValidator.validatePriceRange("0.00");
        assertFalse(result.valid());
        assertEquals("Ціна повинна бути більше 0", result.errorMessage());
    }

    @Test
    void testValidatePriceRange_TooLargePrice() {
        ValidationResult result = BaseValidator.validatePriceRange("1000000");
        assertFalse(result.valid());
        assertEquals("Ціна занадто велика (максимум 999999.99)", result.errorMessage());
    }

    @Test
    void testValidatePriceRange_NumberFormatException() {
        ValidationResult result = BaseValidator.validatePriceRange("100.5.0");
        assertFalse(result.valid());
        assertEquals("Ціна може містити тільки цифри та крапку для десяткових значень", result.errorMessage());
    }

    @Test
    void testAddError_NullControl() {
        assertDoesNotThrow(() -> BaseValidator.addError(null, "Error message"));
    }

    @Test
    void testAddError_ValidControl() {
        BaseValidator.addError(testTextField, "Test error");

        assertTrue(testTextField.getStyleClass().contains("validation-error"));
    }
}
