package tourapp.model.tour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TourTypeTest {

    private TourType tourType;
    private static final int SAMPLE_ID = 1;
    private static final String SAMPLE_NAME = "Adventure Tour";

    @BeforeEach
    void setUp() {
        tourType = new TourType();
    }

    @Test
    void testDefaultConstructor() {
        TourType newTourType = new TourType();

        assertEquals(0, newTourType.getId());
        assertNull(newTourType.getName());
    }

    @Test
    void testParameterizedConstructor() {
        TourType tourTypeWithParams = new TourType(SAMPLE_ID, SAMPLE_NAME);

        assertEquals(SAMPLE_ID, tourTypeWithParams.getId());
        assertEquals(SAMPLE_NAME, tourTypeWithParams.getName());
    }

    @Test
    void testParameterizedConstructorWithNullName() {
        TourType tourTypeWithNullName = new TourType(SAMPLE_ID, null);

        assertEquals(SAMPLE_ID, tourTypeWithNullName.getId());
        assertNull(tourTypeWithNullName.getName());
    }

    @Test
    void testParameterizedConstructorWithEmptyName() {
        String emptyName = "";
        TourType tourTypeWithEmptyName = new TourType(SAMPLE_ID, emptyName);

        assertEquals(SAMPLE_ID, tourTypeWithEmptyName.getId());
        assertEquals(emptyName, tourTypeWithEmptyName.getName());
    }

    @Test
    void testSetAndGetId() {
        int expectedId = 123;
        tourType.setId(expectedId);

        assertEquals(expectedId, tourType.getId());
    }

    @Test
    void testSetAndGetIdWithNegativeValue() {
        int negativeId = -1;
        tourType.setId(negativeId);

        assertEquals(negativeId, tourType.getId());
    }

    @Test
    void testSetAndGetIdWithZero() {
        tourType.setId(0);

        assertEquals(0, tourType.getId());
    }

    @Test
    void testSetAndGetName() {
        String expectedName = "Cultural Tour";
        tourType.setName(expectedName);

        assertEquals(expectedName, tourType.getName());
    }

    @Test
    void testSetAndGetNameWithNull() {
        tourType.setName(null);

        assertNull(tourType.getName());
    }

    @Test
    void testSetAndGetNameWithEmptyString() {
        String emptyName = "";
        tourType.setName(emptyName);

        assertEquals(emptyName, tourType.getName());
    }

    @Test
    void testSetAndGetNameWithWhitespace() {
        String whitespaceName = "   ";
        tourType.setName(whitespaceName);

        assertEquals(whitespaceName, tourType.getName());
    }

    @Test
    void testEqualsWithSameObject() {
        assertTrue(tourType.equals(tourType));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(tourType.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertFalse(tourType.equals("not a tour type"));
        assertFalse(tourType.equals(123));
        assertFalse(tourType.equals(new Object()));
    }

    @Test
    void testEqualsWithIdenticalTourTypes() {
        TourType tourType1 = new TourType(SAMPLE_ID, SAMPLE_NAME);
        TourType tourType2 = new TourType(SAMPLE_ID, SAMPLE_NAME);

        assertTrue(tourType1.equals(tourType2));
        assertTrue(tourType2.equals(tourType1));
        assertEquals(tourType1.hashCode(), tourType2.hashCode());
    }

    @Test
    void testEqualsWithDifferentIds() {
        TourType tourType1 = new TourType(1, SAMPLE_NAME);
        TourType tourType2 = new TourType(2, SAMPLE_NAME);

        assertFalse(tourType1.equals(tourType2));
        assertFalse(tourType2.equals(tourType1));
    }

    @Test
    void testEqualsWithDifferentNames() {
        TourType tourType1 = new TourType(SAMPLE_ID, "Adventure Tour");
        TourType tourType2 = new TourType(SAMPLE_ID, "Cultural Tour");

        assertFalse(tourType1.equals(tourType2));
        assertFalse(tourType2.equals(tourType1));
    }

    @Test
    void testEqualsWithBothDifferent() {
        TourType tourType1 = new TourType(1, "Adventure Tour");
        TourType tourType2 = new TourType(2, "Cultural Tour");

        assertFalse(tourType1.equals(tourType2));
        assertFalse(tourType2.equals(tourType1));
    }

    @Test
    void testEqualsWithNullNames() {
        TourType tourType1 = new TourType(SAMPLE_ID, null);
        TourType tourType2 = new TourType(SAMPLE_ID, null);

        assertTrue(tourType1.equals(tourType2));
        assertTrue(tourType2.equals(tourType1));
        assertEquals(tourType1.hashCode(), tourType2.hashCode());
    }

    @Test
    void testEqualsWithOneNullName() {
        TourType tourType1 = new TourType(SAMPLE_ID, SAMPLE_NAME);
        TourType tourType2 = new TourType(SAMPLE_ID, null);

        assertFalse(tourType1.equals(tourType2));
        assertFalse(tourType2.equals(tourType1));
    }

    @Test
    void testEqualsWithEmptyNames() {
        TourType tourType1 = new TourType(SAMPLE_ID, "");
        TourType tourType2 = new TourType(SAMPLE_ID, "");

        assertTrue(tourType1.equals(tourType2));
        assertTrue(tourType2.equals(tourType1));
        assertEquals(tourType1.hashCode(), tourType2.hashCode());
    }

    @Test
    void testHashCodeConsistency() {
        tourType.setId(SAMPLE_ID);
        tourType.setName(SAMPLE_NAME);

        int hashCode1 = tourType.hashCode();
        int hashCode2 = tourType.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeWithDifferentValues() {
        TourType tourType1 = new TourType(1, "Adventure");
        TourType tourType2 = new TourType(2, "Cultural");

        assertNotEquals(tourType1.hashCode(), tourType2.hashCode());
    }

    @Test
    void testHashCodeWithSameValues() {
        TourType tourType1 = new TourType(SAMPLE_ID, SAMPLE_NAME);
        TourType tourType2 = new TourType(SAMPLE_ID, SAMPLE_NAME);

        assertEquals(tourType1.hashCode(), tourType2.hashCode());
    }

    @Test
    void testHashCodeWithNullName() {
        TourType tourType1 = new TourType(SAMPLE_ID, null);
        TourType tourType2 = new TourType(SAMPLE_ID, null);

        assertEquals(tourType1.hashCode(), tourType2.hashCode());
    }

    @Test
    void testToStringWithName() {
        String expectedName = "Beach Resort Tour";
        tourType.setName(expectedName);

        String result = tourType.toString();

        assertEquals(expectedName, result);
    }

    @Test
    void testToStringWithNullName() {
        tourType.setName(null);

        String result = tourType.toString();

        assertEquals(null, result);
    }

    @Test
    void testToStringWithEmptyName() {
        tourType.setName("");

        String result = tourType.toString();

        assertEquals("", result);
    }

    @Test
    void testToStringConsistency() {
        String name = "Mountain Hiking Tour";
        tourType.setName(name);

        String result1 = tourType.toString();
        String result2 = tourType.toString();

        assertEquals(result1, result2);
        assertEquals(name, result1);
    }

    @Test
    void testToStringIgnoresId() {
        tourType.setId(999);
        tourType.setName("Test Tour");

        String result = tourType.toString();

        assertEquals("Test Tour", result);
        assertFalse(result.contains("999"));
    }

    @Test
    void testCompleteObjectLifecycle() {
        TourType tourTypeLifecycle = new TourType();

        assertEquals(0, tourTypeLifecycle.getId());
        assertNull(tourTypeLifecycle.getName());
        assertEquals(null, tourTypeLifecycle.toString());

        tourTypeLifecycle.setId(42);
        tourTypeLifecycle.setName("Eco Tour");

        assertEquals(42, tourTypeLifecycle.getId());
        assertEquals("Eco Tour", tourTypeLifecycle.getName());
        assertEquals("Eco Tour", tourTypeLifecycle.toString());

        tourTypeLifecycle.setId(84);
        tourTypeLifecycle.setName("City Tour");

        assertEquals(84, tourTypeLifecycle.getId());
        assertEquals("City Tour", tourTypeLifecycle.getName());
        assertEquals("City Tour", tourTypeLifecycle.toString());

        tourTypeLifecycle.setName(null);

        assertEquals(84, tourTypeLifecycle.getId());
        assertNull(tourTypeLifecycle.getName());
        assertEquals(null, tourTypeLifecycle.toString());
    }
}