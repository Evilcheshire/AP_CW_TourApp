package tourapp.util.validation;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class TourValidatorTest {

    @Test
    void testValidateTourDescription_ValidDescription() {
        ValidationResult result = TourValidator.validateTourDescription("This is a valid tour description with enough characters.");
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourDescription_EmptyDescription() {
        ValidationResult result = TourValidator.validateTourDescription("");
        assertFalse(result.valid());
        assertEquals("Опис туру не може бути порожнім", result.errorMessage());

        result = TourValidator.validateTourDescription(null);
        assertFalse(result.valid());
        assertEquals("Опис туру не може бути порожнім", result.errorMessage());

        result = TourValidator.validateTourDescription("   ");
        assertFalse(result.valid());
        assertEquals("Опис туру не може бути порожнім", result.errorMessage());
    }

    @Test
    void testValidateTourDescription_TooShort() {
        ValidationResult result = TourValidator.validateTourDescription("Short");
        assertFalse(result.valid());
        assertEquals("Опис туру повинен містити принаймні 10 символів", result.errorMessage());
    }

    @Test
    void testValidateTourDescription_TooLong() {
        String longDescription = "A".repeat(1001);
        ValidationResult result = TourValidator.validateTourDescription(longDescription);
        assertFalse(result.valid());
        assertEquals("Опис туру занадто довгий (максимум 1000 символів)", result.errorMessage());
    }

    @Test
    void testValidateTourDescription_ExactlyMinLength() {
        ValidationResult result = TourValidator.validateTourDescription("1234567890"); // exactly 10 chars
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourDescription_ExactlyMaxLength() {
        String exactDescription = "A".repeat(1000);
        ValidationResult result = TourValidator.validateTourDescription(exactDescription);
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourPrice_StringValidPrice() {
        ValidationResult result = TourValidator.validateTourPrice("1500.50");
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourPrice_StringEmptyPrice() {
        ValidationResult result = TourValidator.validateTourPrice("");
        assertFalse(result.valid());
        assertEquals("Ціна туру не може бути порожньою", result.errorMessage());

        result = TourValidator.validateTourPrice((String) null);
        assertFalse(result.valid());
        assertEquals("Ціна туру не може бути порожньою", result.errorMessage());

        result = TourValidator.validateTourPrice("   ");
        assertFalse(result.valid());
        assertEquals("Ціна туру не може бути порожньою", result.errorMessage());
    }

    @Test
    void testValidateTourPrice_StringInvalidFormat() {
        ValidationResult result = TourValidator.validateTourPrice("invalid");
        assertFalse(result.valid());
        assertEquals("Невірний формат ціни", result.errorMessage());

        result = TourValidator.validateTourPrice("100.50.25");
        assertFalse(result.valid());
        assertEquals("Невірний формат ціни", result.errorMessage());
    }

    @Test
    void testValidateTourPrice_DoubleValidPrice() {
        ValidationResult result = TourValidator.validateTourPrice(1500.50);
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourPrice_DoubleNullPrice() {
        ValidationResult result = TourValidator.validateTourPrice((Double) null);
        assertFalse(result.valid());
        assertEquals("Ціна туру не може бути порожньою", result.errorMessage());
    }

    @Test
    void testValidateTourPrice_DoubleZeroPrice() {
        ValidationResult result = TourValidator.validateTourPrice(0.0);
        assertFalse(result.valid());
        assertEquals("Ціна туру повинна бути більше 0", result.errorMessage());
    }

    @Test
    void testValidateTourPrice_DoubleNegativePrice() {
        ValidationResult result = TourValidator.validateTourPrice(-100.0);
        assertFalse(result.valid());
        assertEquals("Ціна туру повинна бути більше 0", result.errorMessage());
    }

    @Test
    void testValidateTourPrice_DoubleTooHighPrice() {
        ValidationResult result = TourValidator.validateTourPrice(1000001.0);
        assertFalse(result.valid());
        assertEquals("Ціна туру занадто висока (максимум 1,000,000 грн)", result.errorMessage());
    }

    @Test
    void testValidateTourPrice_DoubleExactlyMaxPrice() {
        ValidationResult result = TourValidator.validateTourPrice(1000000.0);
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourPrice_DoubleMinimumValidPrice() {
        ValidationResult result = TourValidator.validateTourPrice(0.01);
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateDateRange_ValidDates() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().minusDays(7);

        ValidationResult result = TourValidator.validateDateRange(startDate, endDate);
        assertFalse(result.valid());
        assertEquals("Дата початку не може бути після дати закінчення", result.errorMessage());
    }

    @Test
    void testValidateDateRange_StartInPast() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);

        ValidationResult result = TourValidator.validateDateRange(startDate, endDate);
        assertFalse(result.valid());
        assertEquals("Дата початку не може бути в минулому", result.errorMessage());
    }

    @Test
    void testValidateDateRange_ExactlyOneYear() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusYears(1);

        ValidationResult result = TourValidator.validateDateRange(startDate, endDate);
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateDateRange_StartToday() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        ValidationResult result = TourValidator.validateDateRange(startDate, endDate);
        assertTrue(result.valid());
    }

    @Test
    void testValidateDateRange_SameDay() {
        LocalDate date = LocalDate.now().plusDays(1);

        ValidationResult result = TourValidator.validateDateRange(date, date);
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourName_ValidName() {
        ValidationResult result = TourValidator.validateTourName("European Adventure");
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourName_EmptyName() {
        ValidationResult result = TourValidator.validateTourName("");
        assertFalse(result.valid());
        assertEquals("Назва туру не може бути порожньою", result.errorMessage());

        result = TourValidator.validateTourName(null);
        assertFalse(result.valid());
        assertEquals("Назва туру не може бути порожньою", result.errorMessage());

        result = TourValidator.validateTourName("   ");
        assertFalse(result.valid());
        assertEquals("Назва туру не може бути порожньою", result.errorMessage());
    }

    @Test
    void testValidateTourName_TooShort() {
        ValidationResult result = TourValidator.validateTourName("AB");
        assertFalse(result.valid());
        assertEquals("Назва туру повинна містити принаймні 3 символи", result.errorMessage());
    }

    @Test
    void testValidateTourName_TooLong() {
        String longName = "A".repeat(101);
        ValidationResult result = TourValidator.validateTourName(longName);
        assertFalse(result.valid());
        assertEquals("Назва туру занадто довга (максимум 100 символів)", result.errorMessage());
    }

    @Test
    void testValidateTourName_ExactlyMinLength() {
        ValidationResult result = TourValidator.validateTourName("ABC");
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourName_ExactlyMaxLength() {
        String exactName = "A".repeat(100);
        ValidationResult result = TourValidator.validateTourName(exactName);
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }

    @Test
    void testValidateTourName_WithWhitespace() {
        ValidationResult result = TourValidator.validateTourName("  European Adventure  ");
        assertTrue(result.valid());
        assertNull(result.errorMessage());
    }
}