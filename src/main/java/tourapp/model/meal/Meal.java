package tourapp.model.meal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Meal {
    private int id;
    private String name;
    private int mealsPerDay;
    private double costPerDay;
    private List<MealType> mealTypes;

    public Meal() {
        this.mealTypes = new ArrayList<>();
    }

    public Meal(int id, String name, int mealsPerDay, double costPerDay) {
        this.id = id;
        this.name = name;
        this.mealsPerDay = mealsPerDay;
        this.costPerDay = costPerDay;
        this.mealTypes = new ArrayList<>();
    }

    public Meal(int id, String name, int mealsPerDay, double costPerDay, List<MealType> mealTypes) {
        this.id = id;
        this.name = name;
        this.mealsPerDay = mealsPerDay;
        this.costPerDay = costPerDay;
        this.mealTypes = mealTypes != null ? mealTypes : new ArrayList<>();
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getMealsPerDay() {
        return mealsPerDay;
    }
    public double getCostPerDay() {
        return costPerDay;
    }
    public List<MealType> getMealTypes() {
        return mealTypes;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setMealsPerDay(int mealsPerDay) {
        this.mealsPerDay = mealsPerDay;
    }
    public void setCostPerDay(double costPerDay) {
        this.costPerDay = costPerDay;
    }
    public void setMealTypes(List<MealType> mealTypes) {
        this.mealTypes = mealTypes;
    }

    public void addMealType(MealType mealType) {
        if (mealTypes == null) {
            mealTypes = new ArrayList<>();
        }
        mealTypes.add(mealType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meal meal = (Meal) o;
        return id == meal.id &&
                mealsPerDay == meal.mealsPerDay &&
                Double.compare(meal.costPerDay, costPerDay) == 0 &&
                Objects.equals(name, meal.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, mealsPerDay, costPerDay);
    }

    @Override
    public String toString() {
        String types = mealTypes != null ? String.join(", ", mealTypes.stream().map(MealType::getName).toList()) : "—";
        return String.format("Meal: %s\nTypes: %s\nCost per day: %.2f\nMeals per day: %d",
                name, types, costPerDay, mealsPerDay);
    }
}