package tourapp.util.validation;

import java.time.LocalDate;

public class TourValidator {

    public static ValidationResult validateTourDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return new ValidationResult(false, "Опис туру не може бути порожнім");
        }

        description = description.trim();

        if (description.length() < 10) {
            return new ValidationResult(false, "Опис туру повинен містити принаймні 10 символів");
        }

        if (description.length() > 1000) {
            return new ValidationResult(false, "Опис туру занадто довгий (максимум 1000 символів)");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateTourPrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return new ValidationResult(false, "Ціна туру не може бути порожньою");
        }

        try {
            double price = Double.parseDouble(priceStr.trim());
            return validateTourPrice(price);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Невірний формат ціни");
        }
    }

    public static ValidationResult validateTourPrice(Double price) {
        if (price == null) {
            return new ValidationResult(false, "Ціна туру не може бути порожньою");
        }

        if (price <= 0) {
            return new ValidationResult(false, "Ціна туру повинна бути більше 0");
        }

        if (price > 1000000) {
            return new ValidationResult(false, "Ціна туру занадто висока (максимум 1,000,000 грн)");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return new ValidationResult(false, "Дати не можуть бути порожніми");
        }

        if (startDate.isAfter(endDate)) {
            return new ValidationResult(false, "Дата початку не може бути після дати закінчення");
        }

        if (startDate.isBefore(LocalDate.now())) {
            return new ValidationResult(false, "Дата початку не може бути в минулому");
        }

        if (startDate.plusYears(1).isBefore(endDate)) {
            return new ValidationResult(false, "Тур не може тривати більше року");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateTourName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Назва туру не може бути порожньою");
        }

        name = name.trim();

        if (name.length() < 3) {
            return new ValidationResult(false, "Назва туру повинна містити принаймні 3 символи");
        }

        if (name.length() > 100) {
            return new ValidationResult(false, "Назва туру занадто довга (максимум 100 символів)");
        }

        return new ValidationResult(true, null);
    }
}