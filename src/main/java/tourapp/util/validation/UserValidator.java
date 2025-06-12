package tourapp.util.validation;

import java.util.regex.Pattern;

public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 50;

    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email не може бути порожнім");
        }

        email = email.trim();

        if (email.length() > 100) {
            return new ValidationResult(false, "Email занадто довгий (максимум 100 символів)");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "Невірний формат email адреси");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Пароль не може бути порожнім");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new ValidationResult(false,
                    String.format("Пароль повинен містити принаймні %d символів", MIN_PASSWORD_LENGTH));
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return new ValidationResult(false,
                    String.format("Пароль занадто довгий (максимум %d символів)", MAX_PASSWORD_LENGTH));
        }

        if (!password.equals(password.trim())) {
            return new ValidationResult(false, "Пароль не повинен містити пробіли на початку або в кінці");
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasLetter) {
            return new ValidationResult(false, "Пароль повинен містити принаймні одну літеру");
        }

        if (!hasDigit) {
            return new ValidationResult(false, "Пароль повинен містити принаймні одну цифру");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Ім'я не може бути порожнім");
        }

        name = name.trim();

        if (name.length() < 2) {
            return new ValidationResult(false, "Ім'я повинно містити принаймні 2 символи");
        }

        if (name.length() > 50) {
            return new ValidationResult(false, "Ім'я занадто довге (максимум 50 символів)");
        }

        if (!name.matches("^[a-zA-Zа-яА-ЯіІїЇєЄґҐ\\s'-]+$")) {
            return new ValidationResult(false, "Ім'я може містити тільки літери, пробіли, дефіси та апострофи");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePasswordConfirmation(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return new ValidationResult(false, "Підтвердження паролю не може бути порожнім");
        }

        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Паролі не співпадають");
        }

        return new ValidationResult(true, null);
    }

}