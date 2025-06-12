package tourapp.model.location;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class LocationTypeTest {

    private LocationType locationType;

    @BeforeEach
    void setUp() {
        locationType = new LocationType();
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(0, locationType.getId());
        assertNull(locationType.getName());
    }

    @Test
    void testParameterizedConstructor() {
        LocationType lt = new LocationType(1, "City");
        assertEquals(1, lt.getId());
        assertEquals("City", lt.getName());
    }

    @Test
    void testSettersAndGetters() {
        locationType.setId(5);
        locationType.setName("Mountain");

        assertEquals(5, locationType.getId());
        assertEquals("Mountain", locationType.getName());
    }

    @Test
    void testEquals() {
        LocationType lt1 = new LocationType(1, "Beach");
        LocationType lt2 = new LocationType(1, "Beach");
        LocationType lt3 = new LocationType(2, "City");
        LocationType lt4 = new LocationType(1, "Mountain");

        assertEquals(lt1, lt1);

        assertEquals(lt1, lt2);
        assertEquals(lt2, lt1);

        assertNotEquals(lt1, lt3);
        assertNotEquals(lt1, lt4);

        assertNotEquals(lt1, null);
        assertNotEquals(lt1, "string");
    }

    @Test
    void testHashCode() {
        LocationType lt1 = new LocationType(1, "Beach");
        LocationType lt2 = new LocationType(1, "Beach");
        LocationType lt3 = new LocationType(2, "City");

        assertEquals(lt1.hashCode(), lt2.hashCode());
        assertNotEquals(lt1.hashCode(), lt3.hashCode());
    }

    @Test
    void testToString() {
        LocationType lt = new LocationType(1, "Beach");
        assertEquals("Beach", lt.toString());

        LocationType ltNull = new LocationType(1, null);
        assertEquals(null, ltNull.toString());
    }
}