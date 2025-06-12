package tourapp.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationValidatorTest {

    @Test
    void testValidateLocationTypeName_Valid() {
        assertTrue(LocationValidator.validateLocationTypeName("City").valid());
        assertTrue(LocationValidator.validateLocationTypeName("Mountain").valid());
    }

    @Test
    void testValidateLocationTypeName_Invalid() {
        ValidationResult result = LocationValidator.validateLocationTypeName("");
        assertFalse(result.valid());
        assertEquals("Назва типу локації не може бути порожньою", result.errorMessage());

        result = LocationValidator.validateLocationTypeName(null);
        assertFalse(result.valid());
        assertEquals("Назва типу локації не може бути порожньою", result.errorMessage());

        result = LocationValidator.validateLocationTypeName("A");
        assertFalse(result.valid());
        assertEquals("Назва типу локації повинна містити принаймні 2 символи", result.errorMessage());

        result = LocationValidator.validateLocationTypeName("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("Назва типу локації не може перевищувати 100 символів", result.errorMessage());
    }

    @Test
    void testValidateLocationName_Valid() {
        assertTrue(LocationValidator.validateLocationName("Paris").valid());
        assertTrue(LocationValidator.validateLocationName("New York").valid());
    }

    @Test
    void testValidateLocationName_Invalid() {
        ValidationResult result = LocationValidator.validateLocationName("");
        assertFalse(result.valid());
        assertEquals("Назва локації не може бути порожньою", result.errorMessage());

        result = LocationValidator.validateLocationName(null);
        assertFalse(result.valid());
        assertEquals("Назва локації не може бути порожньою", result.errorMessage());

        result = LocationValidator.validateLocationName("A");
        assertFalse(result.valid());
        assertEquals("Назва локації повинна містити принаймні 2 символи", result.errorMessage());

        result = LocationValidator.validateLocationName("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("Назва локації не може перевищувати 100 символів", result.errorMessage());
    }

    @Test
    void testValidateLocationCountry_Valid() {
        assertTrue(LocationValidator.validateLocationCountry("France").valid());
        assertTrue(LocationValidator.validateLocationCountry("United States").valid());
    }

    @Test
    void testValidateLocationCountry_Invalid() {
        ValidationResult result = LocationValidator.validateLocationCountry("");
        assertFalse(result.valid());
        assertEquals("Країна не може бути порожньою", result.errorMessage());

        result = LocationValidator.validateLocationCountry(null);
        assertFalse(result.valid());
        assertEquals("Країна не може бути порожньою", result.errorMessage());

        result = LocationValidator.validateLocationCountry("A");
        assertFalse(result.valid());
        assertEquals("Назва країни повинна містити принаймні 2 символи", result.errorMessage());

        result = LocationValidator.validateLocationCountry("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("Назва країни не може перевищувати 100 символів", result.errorMessage());
    }

    @Test
    void testValidateLocationDescription_Valid() {
        assertTrue(LocationValidator.validateLocationDescription("").valid());
        assertTrue(LocationValidator.validateLocationDescription(null).valid());
        assertTrue(LocationValidator.validateLocationDescription("Beautiful place").valid());
        assertTrue(LocationValidator.validateLocationDescription("a".repeat(1000)).valid());
    }

    @Test
    void testValidateLocationDescription_Invalid() {
        ValidationResult result = LocationValidator.validateLocationDescription("a".repeat(1001));
        assertFalse(result.valid());
        assertEquals("Опис не може перевищувати 1000 символів", result.errorMessage());
    }
}
