package tourapp.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MealValidatorTest {

    @Test
    void testValidateMealName_Valid() {
        assertTrue(MealValidator.validateMealName("Breakfast").valid());
        assertTrue(MealValidator.validateMealName("Full Board").valid());
    }

    @Test
    void testValidateMealName_Invalid() {
        ValidationResult result = MealValidator.validateMealName("");
        assertFalse(result.valid());
        assertEquals("Назва харчування не може бути порожньою", result.errorMessage());

        result = MealValidator.validateMealName(null);
        assertFalse(result.valid());
        assertEquals("Назва харчування не може бути порожньою", result.errorMessage());

        result = MealValidator.validateMealName("A");
        assertFalse(result.valid());
        assertEquals("Назва харчування повинна містити принаймні 2 символи", result.errorMessage());

        result = MealValidator.validateMealName("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("Назва харчування занадто довга (максимум 100 символів)", result.errorMessage());
    }

    @Test
    void testValidateMealsPerDay_StringValid() {
        assertTrue(MealValidator.validateMealsPerDay("3").valid());
        assertTrue(MealValidator.validateMealsPerDay("1").valid());
        assertTrue(MealValidator.validateMealsPerDay("10").valid());
    }

    @Test
    void testValidateMealsPerDay_StringInvalid() {
        ValidationResult result = MealValidator.validateMealsPerDay("");
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі не може бути порожньою", result.errorMessage());

        result = MealValidator.validateMealsPerDay((String) null);
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі не може бути порожньою", result.errorMessage());

        result = MealValidator.validateMealsPerDay("abc");
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі може містити тільки цифри", result.errorMessage());

        result = MealValidator.validateMealsPerDay("3.5");
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі може містити тільки цифри", result.errorMessage());
    }

    @Test
    void testValidateMealsPerDay_IntegerValid() {
        assertTrue(MealValidator.validateMealsPerDay(3).valid());
        assertTrue(MealValidator.validateMealsPerDay(1).valid());
        assertTrue(MealValidator.validateMealsPerDay(10).valid());
    }

    @Test
    void testValidateMealsPerDay_IntegerInvalid() {
        ValidationResult result = MealValidator.validateMealsPerDay((Integer) null);
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі не може бути порожньою", result.errorMessage());

        result = MealValidator.validateMealsPerDay(0);
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі повинна бути більше 0", result.errorMessage());

        result = MealValidator.validateMealsPerDay(-1);
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі повинна бути більше 0", result.errorMessage());

        result = MealValidator.validateMealsPerDay(11);
        assertFalse(result.valid());
        assertEquals("Кількість прийомів їжі занадто велика (максимум 10)", result.errorMessage());
    }

    @Test
    void testValidateMealTypeName_Valid() {
        assertTrue(MealValidator.validateMealTypeName("All Inclusive").valid());
        assertTrue(MealValidator.validateMealTypeName("BB").valid());
    }

    @Test
    void testValidateMealTypeName_Invalid() {
        ValidationResult result = MealValidator.validateMealTypeName("");
        assertFalse(result.valid());
        assertEquals("Назва типу харчування не може бути порожньою", result.errorMessage());

        result = MealValidator.validateMealTypeName(null);
        assertFalse(result.valid());
        assertEquals("Назва типу харчування не може бути порожньою", result.errorMessage());

        result = MealValidator.validateMealTypeName("A");
        assertFalse(result.valid());
        assertEquals("Назва типу харчування повинна містити принаймні 2 символи", result.errorMessage());

        result = MealValidator.validateMealTypeName("a".repeat(51));
        assertFalse(result.valid());
        assertEquals("Назва типу харчування занадто довга (максимум 50 символів)", result.errorMessage());
    }
}
