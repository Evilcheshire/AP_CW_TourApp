package tourapp.util.validation;

public class MealValidator {

    public static ValidationResult validateMealName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Назва харчування не може бути порожньою");
        }

        name = name.trim();

        if (name.length() < 2) {
            return new ValidationResult(false, "Назва харчування повинна містити принаймні 2 символи");
        }

        if (name.length() > 100) {
            return new ValidationResult(false, "Назва харчування занадто довга (максимум 100 символів)");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateMealsPerDay(String mealsPerDayStr) {
        if (mealsPerDayStr == null || mealsPerDayStr.trim().isEmpty()) {
            return new ValidationResult(false, "Кількість прийомів їжі не може бути порожньою");
        }

        if (!mealsPerDayStr.trim().matches("^\\d+$")) {
            return new ValidationResult(false, "Кількість прийомів їжі може містити тільки цифри");
        }

        try {
            int mealsPerDay = Integer.parseInt(mealsPerDayStr.trim());
            return validateMealsPerDay(mealsPerDay);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Невірний формат кількості прийомів їжі");
        }
    }

    public static ValidationResult validateMealsPerDay(Integer mealsPerDay) {
        if (mealsPerDay == null) {
            return new ValidationResult(false, "Кількість прийомів їжі не може бути порожньою");
        }

        if (mealsPerDay <= 0) {
            return new ValidationResult(false, "Кількість прийомів їжі повинна бути більше 0");
        }

        if (mealsPerDay > 10) {
            return new ValidationResult(false, "Кількість прийомів їжі занадто велика (максимум 10)");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateMealTypeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Назва типу харчування не може бути порожньою");
        }

        name = name.trim();

        if (name.length() < 2) {
            return new ValidationResult(false, "Назва типу харчування повинна містити принаймні 2 символи");
        }

        if (name.length() > 50) {
            return new ValidationResult(false, "Назва типу харчування занадто довга (максимум 50 символів)");
        }

        return new ValidationResult(true, null);
    }
}