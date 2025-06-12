package tourapp.model.location;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    private Location location;
    private LocationType cityType;
    private LocationType beachType;

    @BeforeEach
    void setUp() {
        location = new Location();
        cityType = new LocationType(1, "City");
        beachType = new LocationType(2, "Beach");
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(0, location.getId());
        assertNull(location.getName());
        assertNull(location.getCountry());
        assertNull(location.getDescription());
        assertNull(location.getLocationType());
    }

    @Test
    void testParameterizedConstructorWithId() {
        Location loc = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);

        assertEquals(1, loc.getId());
        assertEquals("Kyiv", loc.getName());
        assertEquals("Ukraine", loc.getCountry());
        assertEquals("Capital city", loc.getDescription());
        assertEquals(cityType, loc.getLocationType());
    }

    @Test
    void testParameterizedConstructorWithoutId() {
        Location loc = new Location("Lviv", "Ukraine", "Cultural center", cityType);

        assertEquals(-1, loc.getId());
        assertEquals("Lviv", loc.getName());
        assertEquals("Ukraine", loc.getCountry());
        assertEquals("Cultural center", loc.getDescription());
        assertEquals(cityType, loc.getLocationType());
    }

    @Test
    void testSettersAndGetters() {
        location.setId(10);
        location.setName("Odesa");
        location.setCountry("Ukraine");
        location.setDescription("Port city");
        location.setLocationType(beachType);

        assertEquals(10, location.getId());
        assertEquals("Odesa", location.getName());
        assertEquals("Ukraine", location.getCountry());
        assertEquals("Port city", location.getDescription());
        assertEquals(beachType, location.getLocationType());
    }

    @Test
    void testGetLocationTypeId() {
        // Коли locationType є null
        assertEquals(-1, location.getLocationTypeId());

        // Коли locationType встановлено
        location.setLocationType(cityType);
        assertEquals(1, location.getLocationTypeId());
    }

    @Test
    void testToString() {
        Location loc = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);

        String expected = "Локація: Kyiv\nКраїна: Ukraine\nТип: City\nОпис: Capital city";
        assertEquals(expected, loc.toString());
    }

    @Test
    void testToStringWithNullLocationType() {
        Location loc = new Location(1, "Kyiv", "Ukraine", "Capital city", null);

        String expected = "Локація: Kyiv\nКраїна: Ukraine\nТип: —\nОпис: Capital city";
        assertEquals(expected, loc.toString());
    }

    @Test
    void testToStringWithNullValues() {
        Location loc = new Location();

        String expected = "Локація: null\nКраїна: null\nТип: —\nОпис: null";
        assertEquals(expected, loc.toString());
    }

    @Test
    void testEquals() {
        Location loc1 = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);
        Location loc2 = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);
        Location loc3 = new Location(2, "Lviv", "Ukraine", "Cultural center", cityType);

        // Рефлексивність
        assertEquals(loc1, loc1);

        // Симетричність
        assertEquals(loc1, loc2);
        assertEquals(loc2, loc1);

        // Транзитивність
        Location loc4 = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);
        assertEquals(loc1, loc2);
        assertEquals(loc2, loc4);
        assertEquals(loc1, loc4);

        // Різні об'єкти не рівні
        assertNotEquals(loc1, loc3);

        // Порівняння з null
        assertNotEquals(loc1, null);

        // Порівняння з об'єктом іншого класу
        assertNotEquals(loc1, "string");
    }

    @Test
    void testEqualsWithDifferentFields() {
        Location base = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);

        // Різний ID
        Location diffId = new Location(2, "Kyiv", "Ukraine", "Capital city", cityType);
        assertNotEquals(base, diffId);

        // Різна назва
        Location diffName = new Location(1, "Lviv", "Ukraine", "Capital city", cityType);
        assertNotEquals(base, diffName);

        // Різна country
        Location diffCountry = new Location(1, "Kyiv", "Poland", "Capital city", cityType);
        assertNotEquals(base, diffCountry);

        // Різний опис
        Location diffDescription = new Location(1, "Kyiv", "Ukraine", "Different description", cityType);
        assertNotEquals(base, diffDescription);

        // Різний тип локації
        Location diffType = new Location(1, "Kyiv", "Ukraine", "Capital city", beachType);
        assertNotEquals(base, diffType);
    }

    @Test
    void testEqualsWithNullFields() {
        Location loc1 = new Location(1, null, null, null, null);
        Location loc2 = new Location(1, null, null, null, null);
        Location loc3 = new Location(1, "Kyiv", null, null, null);

        assertEquals(loc1, loc2);
        assertNotEquals(loc1, loc3);
    }

    @Test
    void testHashCode() {
        Location loc1 = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);
        Location loc2 = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);
        Location loc3 = new Location(2, "Lviv", "Ukraine", "Cultural center", cityType);

        // Однакові об'єкти мають однаковий hashCode
        assertEquals(loc1.hashCode(), loc2.hashCode());

        // Різні об'єкти можуть мати різний hashCode (не обов'язково, але бажано)
        assertNotEquals(loc1.hashCode(), loc3.hashCode());
    }

    @Test
    void testHashCodeConsistency() {
        Location loc = new Location(1, "Kyiv", "Ukraine", "Capital city", cityType);
        int hash1 = loc.hashCode();
        int hash2 = loc.hashCode();

        // hashCode повинен бути консистентним
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCodeWithNullFields() {
        Location loc1 = new Location(1, null, null, null, null);
        Location loc2 = new Location(1, null, null, null, null);

        assertEquals(loc1.hashCode(), loc2.hashCode());
    }
}