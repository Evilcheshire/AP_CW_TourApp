package tourapp.model.meal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MealTest {

    private Meal meal;
    private MealType breakfast;
    private MealType lunch;

    @BeforeEach
    void setUp() {
        meal = new Meal();
        breakfast = new MealType(1, "Breakfast");
        lunch = new MealType(2, "Lunch");
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(0, meal.getId());
        assertNull(meal.getName());
        assertEquals(0, meal.getMealsPerDay());
        assertEquals(0.0, meal.getCostPerDay());
        assertNotNull(meal.getMealTypes());
        assertTrue(meal.getMealTypes().isEmpty());
    }

    @Test
    void testParameterizedConstructorWithoutMealTypes() {
        Meal m = new Meal(1, "Standard", 3, 50.0);

        assertEquals(1, m.getId());
        assertEquals("Standard", m.getName());
        assertEquals(3, m.getMealsPerDay());
        assertEquals(50.0, m.getCostPerDay());
        assertNotNull(m.getMealTypes());
        assertTrue(m.getMealTypes().isEmpty());
    }

    @Test
    void testParameterizedConstructorWithMealTypes() {
        List<MealType> types = List.of(breakfast, lunch);
        Meal m = new Meal(1, "Premium", 2, 75.0, types);

        assertEquals(1, m.getId());
        assertEquals("Premium", m.getName());
        assertEquals(2, m.getMealsPerDay());
        assertEquals(75.0, m.getCostPerDay());
        assertEquals(2, m.getMealTypes().size());
        assertTrue(m.getMealTypes().contains(breakfast));
        assertTrue(m.getMealTypes().contains(lunch));
    }

    @Test
    void testParameterizedConstructorWithNullMealTypes() {
        Meal m = new Meal(1, "Basic", 1, 25.0, null);

        assertEquals(1, m.getId());
        assertEquals("Basic", m.getName());
        assertEquals(1, m.getMealsPerDay());
        assertEquals(25.0, m.getCostPerDay());
        assertNotNull(m.getMealTypes());
        assertTrue(m.getMealTypes().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        List<MealType> types = new ArrayList<>();
        types.add(breakfast);

        meal.setId(10);
        meal.setName("Deluxe");
        meal.setMealsPerDay(4);
        meal.setCostPerDay(100.0);
        meal.setMealTypes(types);

        assertEquals(10, meal.getId());
        assertEquals("Deluxe", meal.getName());
        assertEquals(4, meal.getMealsPerDay());
        assertEquals(100.0, meal.getCostPerDay());
        assertEquals(1, meal.getMealTypes().size());
        assertEquals(breakfast, meal.getMealTypes().get(0));
    }

    @Test
    void testAddMealType() {
        meal.addMealType(breakfast);
        assertEquals(1, meal.getMealTypes().size());
        assertTrue(meal.getMealTypes().contains(breakfast));

        meal.addMealType(lunch);
        assertEquals(2, meal.getMealTypes().size());
        assertTrue(meal.getMealTypes().contains(lunch));
    }

    @Test
    void testAddMealTypeWhenListIsNull() {
        meal.setMealTypes(null);
        meal.addMealType(breakfast);

        assertNotNull(meal.getMealTypes());
        assertEquals(1, meal.getMealTypes().size());
        assertTrue(meal.getMealTypes().contains(breakfast));
    }

    @Test
    void testEquals() {
        Meal m1 = new Meal(1, "Standard", 3, 50.0);
        Meal m2 = new Meal(1, "Standard", 3, 50.0);
        Meal m3 = new Meal(2, "Premium", 3, 50.0);
        Meal m4 = new Meal(1, "Basic", 3, 50.0);
        Meal m5 = new Meal(1, "Standard", 2, 50.0);
        Meal m6 = new Meal(1, "Standard", 3, 75.0);

        assertEquals(m1, m1);

        assertEquals(m1, m2);
        assertEquals(m2, m1);

        assertNotEquals(m1, m3);
        assertNotEquals(m1, m4);
        assertNotEquals(m1, m5);
        assertNotEquals(m1, m6);

        assertNotEquals(m1, null);
        assertNotEquals(m1, "string");
    }

    @Test
    void testHashCode() {
        Meal m1 = new Meal(1, "Standard", 3, 50.0);
        Meal m2 = new Meal(1, "Standard", 3, 50.0);
        Meal m3 = new Meal(2, "Premium", 3, 50.0);

        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1.hashCode(), m3.hashCode());
    }

    @Test
    void testToString() {
        List<MealType> types = List.of(breakfast, lunch);
        Meal m = new Meal(1, "Premium", 2, 75.50, types);

        String expected = "Meal: Premium\nTypes: Breakfast, Lunch\nCost per day: 75,50\nMeals per day: 2";
        assertEquals(expected, m.toString());
    }

    @Test
    void testToStringWithEmptyMealTypes() {
        Meal m = new Meal(1, "Basic", 1, 25.0);

        String expected = "Meal: Basic\nTypes: \nCost per day: 25,00\nMeals per day: 1";
        assertEquals(expected, m.toString());
    }

    @Test
    void testToStringWithNullMealTypes() {
        Meal m = new Meal(1, "Basic", 1, 25.0);
        m.setMealTypes(null);

        String expected = "Meal: Basic\nTypes: —\nCost per day: 25,00\nMeals per day: 1";
        assertEquals(expected, m.toString());
    }
}