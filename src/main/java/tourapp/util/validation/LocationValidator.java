package tourapp.util.validation;

public class LocationValidator {

    public static ValidationResult validateLocationTypeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Назва типу локації не може бути порожньою");
        }

        name = name.trim();

        if (name.length() < 2) {
            return new ValidationResult(false, "Назва типу локації повинна містити принаймні 2 символи");
        }

        if (name.length() > 100) {
            return new ValidationResult(false, "Назва типу локації не може перевищувати 100 символів");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateLocationName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Назва локації не може бути порожньою");
        }

        name = name.trim();

        if (name.length() < 2) {
            return new ValidationResult(false, "Назва локації повинна містити принаймні 2 символи");
        }

        if (name.length() > 100) {
            return new ValidationResult(false, "Назва локації не може перевищувати 100 символів");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateLocationCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            return new ValidationResult(false, "Країна не може бути порожньою");
        }

        country = country.trim();

        if (country.length() < 2) {
            return new ValidationResult(false, "Назва країни повинна містити принаймні 2 символи");
        }

        if (country.length() > 100) {
            return new ValidationResult(false, "Назва країни не може перевищувати 100 символів");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validateLocationDescription(String description) {
        if (description != null && description.trim().length() > 1000) {
            return new ValidationResult(false, "Опис не може перевищувати 1000 символів");
        }

        return new ValidationResult(true, null);
    }
}