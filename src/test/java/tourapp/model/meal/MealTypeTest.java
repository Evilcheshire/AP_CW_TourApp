package tourapp.model.meal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class MealTypeTest {

    private MealType mealType;

    @BeforeEach
    void setUp() {
        mealType = new MealType();
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(0, mealType.getId());
        assertNull(mealType.getName());
    }

    @Test
    void testParameterizedConstructor() {
        MealType mt = new MealType(1, "Breakfast");
        assertEquals(1, mt.getId());
        assertEquals("Breakfast", mt.getName());
    }

    @Test
    void testSettersAndGetters() {
        mealType.setId(5);
        mealType.setName("Dinner");

        assertEquals(5, mealType.getId());
        assertEquals("Dinner", mealType.getName());
    }

    @Test
    void testEquals() {
        MealType mt1 = new MealType(1, "Breakfast");
        MealType mt2 = new MealType(1, "Breakfast");
        MealType mt3 = new MealType(2, "Lunch");
        MealType mt4 = new MealType(1, "Dinner");

        assertEquals(mt1, mt1);

        assertEquals(mt1, mt2);
        assertEquals(mt2, mt1);

        assertNotEquals(mt1, mt3);
        assertNotEquals(mt1, mt4);

        assertNotEquals(mt1, null);
        assertNotEquals(mt1, "string");
    }

    @Test
    void testHashCode() {
        MealType mt1 = new MealType(1, "Breakfast");
        MealType mt2 = new MealType(1, "Breakfast");
        MealType mt3 = new MealType(2, "Lunch");

        assertEquals(mt1.hashCode(), mt2.hashCode());
        assertNotEquals(mt1.hashCode(), mt3.hashCode());
    }

    @Test
    void testToString() {
        MealType mt = new MealType(1, "Breakfast");
        assertEquals("Breakfast", mt.toString());

        MealType mtNull = new MealType(1, null);
        assertEquals(null, mtNull.toString());
    }
}