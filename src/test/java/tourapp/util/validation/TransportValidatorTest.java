package tourapp.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransportValidatorTest {

    @Test
    void testValidateTransportName_Valid() {
        assertTrue(TransportValidator.validateTransportName("Bus").valid());
        assertTrue(TransportValidator.validateTransportName("Train Express").valid());
    }

    @Test
    void testValidateTransportName_Invalid() {
        ValidationResult result = TransportValidator.validateTransportName("");
        assertFalse(result.valid());
        assertEquals("Назва транспорту не може бути порожньою", result.errorMessage());

        result = TransportValidator.validateTransportName(null);
        assertFalse(result.valid());
        assertEquals("Назва транспорту не може бути порожньою", result.errorMessage());

        result = TransportValidator.validateTransportName("A");
        assertFalse(result.valid());
        assertEquals("Назва транспорту повинна містити принаймні 2 символи", result.errorMessage());

        result = TransportValidator.validateTransportName("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("Назва транспорту занадто довга (максимум 100 символів)", result.errorMessage());
    }

    @Test
    void testValidateTransportTypeName_Valid() {
        assertTrue(TransportValidator.validateTransportTypeName("Air").valid());
        assertTrue(TransportValidator.validateTransportTypeName("Ground").valid());
    }

    @Test
    void testValidateTransportTypeName_Invalid() {
        ValidationResult result = TransportValidator.validateTransportTypeName("");
        assertFalse(result.valid());
        assertEquals("Назва типу транспорту не може бути порожньою", result.errorMessage());

        result = TransportValidator.validateTransportTypeName(null);
        assertFalse(result.valid());
        assertEquals("Назва типу транспорту не може бути порожньою", result.errorMessage());

        result = TransportValidator.validateTransportTypeName("A");
        assertFalse(result.valid());
        assertEquals("Назва типу транспорту повинна містити принаймні 2 символи", result.errorMessage());

        result = TransportValidator.validateTransportTypeName("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("Назва типу транспорту не може перевищувати 100 символів", result.errorMessage());
    }

    @Test
    void testValidatePricePerPerson_StringValid() {
        assertTrue(TransportValidator.validatePricePerPerson("100.50").valid());
        assertTrue(TransportValidator.validatePricePerPerson("0.01").valid());
        assertTrue(TransportValidator.validatePricePerPerson("999999.99").valid());
    }

    @Test
    void testValidatePricePerPerson_StringInvalid() {
        ValidationResult result = TransportValidator.validatePricePerPerson("");
        assertFalse(result.valid());
        assertEquals("Ціна за людину не може бути порожньою", result.errorMessage());

        result = TransportValidator.validatePricePerPerson((String) null);
        assertFalse(result.valid());
        assertEquals("Ціна за людину не може бути порожньою", result.errorMessage());

        result = TransportValidator.validatePricePerPerson("abc");
        assertFalse(result.valid());
        assertEquals("Ціна може містити тільки цифри та крапку для десяткових значень", result.errorMessage());

        result = TransportValidator.validatePricePerPerson("100.123");
        assertFalse(result.valid());
        assertEquals("Ціна може містити тільки цифри та крапку для десяткових значень", result.errorMessage());
    }

    @Test
    void testValidatePricePerPerson_DoubleValid() {
        assertTrue(TransportValidator.validatePricePerPerson(100.50).valid());
        assertTrue(TransportValidator.validatePricePerPerson(0.01).valid());
    }

    @Test
    void testValidatePricePerPerson_DoubleInvalid() {
        ValidationResult result = TransportValidator.validatePricePerPerson((Double) null);
        assertFalse(result.valid());
        assertEquals("Ціна за людину не може бути порожньою", result.errorMessage());

        result = TransportValidator.validatePricePerPerson(-1.0);
        assertFalse(result.valid());
        assertEquals("Ціна не може бути від'ємною", result.errorMessage());

        result = TransportValidator.validatePricePerPerson(0.0);
        assertFalse(result.valid());
        assertEquals("Ціна повинна бути більше 0", result.errorMessage());

        result = TransportValidator.validatePricePerPerson(1000000.0);
        assertFalse(result.valid());
        assertEquals("Ціна занадто велика (максимум 999999.99)", result.errorMessage());
    }

    @Test
    void testValidatePriceRange_Valid() {
        assertTrue(TransportValidator.validatePriceRange(100.0, 200.0).valid());
        assertTrue(TransportValidator.validatePriceRange(0.0, 0.0).valid());
    }

    @Test
    void testValidatePriceRange_Invalid() {
        ValidationResult result = TransportValidator.validatePriceRange(null, 200.0);
        assertFalse(result.valid());
        assertEquals("Мінімальна та максимальна ціни не можуть бути порожніми", result.errorMessage());

        result = TransportValidator.validatePriceRange(100.0, null);
        assertFalse(result.valid());
        assertEquals("Мінімальна та максимальна ціни не можуть бути порожніми", result.errorMessage());

        result = TransportValidator.validatePriceRange(-100.0, 200.0);
        assertFalse(result.valid());
        assertEquals("Мінімальна ціна не може бути від'ємною", result.errorMessage());

        result = TransportValidator.validatePriceRange(100.0, -200.0);
        assertFalse(result.valid());
        assertEquals("Максимальна ціна не може бути від'ємною", result.errorMessage());

        result = TransportValidator.validatePriceRange(200.0, 100.0);
        assertFalse(result.valid());
        assertEquals("Максимальна ціна повинна бути більшою за мінімальну", result.errorMessage());

        result = TransportValidator.validatePriceRange(1000000.0, 1000001.0);
        assertFalse(result.valid());
        assertEquals("Ціна занадто велика (максимум 999999.99)", result.errorMessage());
    }

    @Test
    void testValidateSearchKeyword_Valid() {
        assertTrue(TransportValidator.validateSearchKeyword("").valid());
        assertTrue(TransportValidator.validateSearchKeyword(null).valid());
        assertTrue(TransportValidator.validateSearchKeyword("search term").valid());
        assertTrue(TransportValidator.validateSearchKeyword("a".repeat(100)).valid());
    }

    @Test
    void testValidateSearchKeyword_Invalid() {
        ValidationResult result = TransportValidator.validateSearchKeyword("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("Пошуковий запит занадто довгий (максимум 100 символів)", result.errorMessage());
    }
}
