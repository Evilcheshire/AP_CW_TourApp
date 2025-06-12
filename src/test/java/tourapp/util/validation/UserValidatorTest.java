package tourapp.util.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    @Test
    void testValidateEmail_ValidEmails() {
        assertTrue(UserValidator.validateEmail("test@example.com").valid());
        assertTrue(UserValidator.validateEmail("user.name@domain.co.uk").valid());
        assertTrue(UserValidator.validateEmail("test123@test-domain.com").valid());
    }

    @Test
    void testValidateEmail_InvalidEmails() {
        ValidationResult result = UserValidator.validateEmail("");
        assertFalse(result.valid());
        assertEquals("Email не може бути порожнім", result.errorMessage());

        result = UserValidator.validateEmail(null);
        assertFalse(result.valid());
        assertEquals("Email не може бути порожнім", result.errorMessage());

        result = UserValidator.validateEmail("   ");
        assertFalse(result.valid());
        assertEquals("Email не може бути порожнім", result.errorMessage());

        result = UserValidator.validateEmail("invalid-email");
        assertFalse(result.valid());
        assertEquals("Невірний формат email адреси", result.errorMessage());

        result = UserValidator.validateEmail("@domain.com");
        assertFalse(result.valid());
        assertEquals("Невірний формат email адреси", result.errorMessage());

        result = UserValidator.validateEmail("user@");
        assertFalse(result.valid());
        assertEquals("Невірний формат email адреси", result.errorMessage());

        // Too long email
        String longEmail = "a".repeat(95) + "@test.com";
        result = UserValidator.validateEmail(longEmail);
        assertFalse(result.valid());
        assertEquals("Email занадто довгий (максимум 100 символів)", result.errorMessage());
    }

    @Test
    void testValidatePassword_ValidPasswords() {
        assertTrue(UserValidator.validatePassword("password1").valid());
        assertTrue(UserValidator.validatePassword("Test123").valid());
        assertTrue(UserValidator.validatePassword("MyPass1").valid());
    }

    @Test
    void testValidatePassword_InvalidPasswords() {
        ValidationResult result = UserValidator.validatePassword("");
        assertFalse(result.valid());
        assertEquals("Пароль не може бути порожнім", result.errorMessage());

        result = UserValidator.validatePassword(null);
        assertFalse(result.valid());
        assertEquals("Пароль не може бути порожнім", result.errorMessage());

        result = UserValidator.validatePassword("123");
        assertFalse(result.valid());
        assertEquals("Пароль повинен містити принаймні 6 символів", result.errorMessage());

        result = UserValidator.validatePassword("a".repeat(51));
        assertFalse(result.valid());
        assertEquals("Пароль занадто довгий (максимум 50 символів)", result.errorMessage());

        result = UserValidator.validatePassword(" password1 ");
        assertFalse(result.valid());
        assertEquals("Пароль не повинен містити пробіли на початку або в кінці", result.errorMessage());

        result = UserValidator.validatePassword("123456");
        assertFalse(result.valid());
        assertEquals("Пароль повинен містити принаймні одну літеру", result.errorMessage());

        result = UserValidator.validatePassword("password");
        assertFalse(result.valid());
        assertEquals("Пароль повинен містити принаймні одну цифру", result.errorMessage());
    }

    @Test
    void testValidateName_ValidNames() {
        assertTrue(UserValidator.validateName("John").valid());
        assertTrue(UserValidator.validateName("Mary-Jane").valid());
        assertTrue(UserValidator.validateName("O'Connor").valid());
        assertTrue(UserValidator.validateName("Іван Петрович").valid());
    }

    @Test
    void testValidateName_InvalidNames() {
        ValidationResult result = UserValidator.validateName("");
        assertFalse(result.valid());
        assertEquals("Ім'я не може бути порожнім", result.errorMessage());

        result = UserValidator.validateName(null);
        assertFalse(result.valid());
        assertEquals("Ім'я не може бути порожнім", result.errorMessage());

        result = UserValidator.validateName("   ");
        assertFalse(result.valid());
        assertEquals("Ім'я не може бути порожнім", result.errorMessage());

        result = UserValidator.validateName("A");
        assertFalse(result.valid());
        assertEquals("Ім'я повинно містити принаймні 2 символи", result.errorMessage());

        result = UserValidator.validateName("a".repeat(51));
        assertFalse(result.valid());
        assertEquals("Ім'я занадто довге (максимум 50 символів)", result.errorMessage());

        result = UserValidator.validateName("John123");
        assertFalse(result.valid());
        assertEquals("Ім'я може містити тільки літери, пробіли, дефіси та апострофи", result.errorMessage());
    }

    @Test
    void testValidatePasswordConfirmation() {
        ValidationResult result = UserValidator.validatePasswordConfirmation("password1", "password1");
        assertTrue(result.valid());

        result = UserValidator.validatePasswordConfirmation("password1", "");
        assertFalse(result.valid());
        assertEquals("Підтвердження паролю не може бути порожнім", result.errorMessage());

        result = UserValidator.validatePasswordConfirmation("password1", null);
        assertFalse(result.valid());
        assertEquals("Підтвердження паролю не може бути порожнім", result.errorMessage());

        result = UserValidator.validatePasswordConfirmation("password1", "password2");
        assertFalse(result.valid());
        assertEquals("Паролі не співпадають", result.errorMessage());
    }
}
