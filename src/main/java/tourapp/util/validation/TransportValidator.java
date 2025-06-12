package tourapp.util.validation;

public class TransportValidator {

    public static ValidationResult validateTransportName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Назва транспорту не може бути порожньою");
        }

        name = name.trim();

        if (name.length() < 2) {
            return new ValidationResult(false, "Назва транспорту повинна містити принаймні 2 символи");
        }

        if (name.length() > 100) {
            return new ValidationResult(false, "Назва транспорту занадто довга (максимум 100 символів)");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateTransportTypeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Назва типу транспорту не може бути порожньою");
        }

        name = name.trim();

        if (name.length() < 2) {
            return new ValidationResult(false, "Назва типу транспорту повинна містити принаймні 2 символи");
        }

        if (name.length() > 100) {
            return new ValidationResult(false, "Назва типу транспорту не може перевищувати 100 символів");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePricePerPerson(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return new ValidationResult(false, "Ціна за людину не може бути порожньою");
        }

        String cleanPrice = priceStr.trim();

        if (!cleanPrice.matches("^\\d+(\\.\\d{1,2})?$")) {
            return new ValidationResult(false, "Ціна може містити тільки цифри та крапку для десяткових значень");
        }

        try {
            double price = Double.parseDouble(cleanPrice);
            return validatePricePerPerson(price);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Невірний формат ціни");
        }
    }

    public static ValidationResult validatePricePerPerson(Double price) {
        if (price == null) {
            return new ValidationResult(false, "Ціна за людину не може бути порожньою");
        }

        if (price < 0) {
            return new ValidationResult(false, "Ціна не може бути від'ємною");
        }

        if (price > 999999.99) {
            return new ValidationResult(false, "Ціна занадто велика (максимум 999999.99)");
        }

        if (price == 0) {
            return new ValidationResult(false, "Ціна повинна бути більше 0");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null || maxPrice == null) {
            return new ValidationResult(false, "Мінімальна та максимальна ціни не можуть бути порожніми");
        }

        if (minPrice < 0) {
            return new ValidationResult(false, "Мінімальна ціна не може бути від'ємною");
        }

        if (maxPrice < 0) {
            return new ValidationResult(false, "Максимальна ціна не може бути від'ємною");
        }

        if (minPrice > maxPrice) {
            return new ValidationResult(false, "Максимальна ціна повинна бути більшою за мінімальну");
        }

        if (minPrice > 999999.99 || maxPrice > 999999.99) {
            return new ValidationResult(false, "Ціна занадто велика (максимум 999999.99)");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateSearchKeyword(String keyword) {
        if (keyword != null && keyword.trim().length() > 100) {
            return new ValidationResult(false, "Пошуковий запит занадто довгий (максимум 100 символів)");
        }

        return new ValidationResult(true, null);
    }
}