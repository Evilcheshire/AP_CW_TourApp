package tourapp.model.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class TransportTypeTest {

    private TransportType transportType;
    private static final int TEST_ID = 1;
    private static final String TEST_NAME = "Bus";

    @BeforeEach
    void setUp() {
        transportType = new TransportType(TEST_ID, TEST_NAME);
    }


    @Test
    void defaultConstructor() {
        TransportType emptyTransportType = new TransportType();

        assertEquals(0, emptyTransportType.getId());
        assertNull(emptyTransportType.getName());
    }

    @Test
    void constructorWithParameters() {
        TransportType testTransportType = new TransportType(5, "Airplane");

        assertEquals(5, testTransportType.getId());
        assertEquals("Airplane", testTransportType.getName());
    }

    @Test
    void constructorWithNullName() {
        TransportType testTransportType = new TransportType(10, null);

        assertEquals(10, testTransportType.getId());
        assertNull(testTransportType.getName());
    }

    @Test
    void constructorWithEmptyName() {
        TransportType testTransportType = new TransportType(15, "");

        assertEquals(15, testTransportType.getId());
        assertEquals("", testTransportType.getName());
    }

    @Test
    void getId() {
        assertEquals(TEST_ID, transportType.getId());
    }

    @Test
    void setId() {
        transportType.setId(999);
        assertEquals(999, transportType.getId());
    }

    @Test
    void setIdNegative() {
        transportType.setId(-1);
        assertEquals(-1, transportType.getId());
    }

    @Test
    void setIdZero() {
        transportType.setId(0);
        assertEquals(0, transportType.getId());
    }

    @Test
    void getName() {
        assertEquals(TEST_NAME, transportType.getName());
    }

    @Test
    void setName() {
        transportType.setName("Train");
        assertEquals("Train", transportType.getName());
    }

    @Test
    void setNameNull() {
        transportType.setName(null);
        assertNull(transportType.getName());
    }

    @Test
    void setNameEmpty() {
        transportType.setName("");
        assertEquals("", transportType.getName());
    }

    @Test
    void setNameWithSpaces() {
        transportType.setName("  Car  ");
        assertEquals("  Car  ", transportType.getName());
    }

    @Test
    void testEqualsIdentical() {
        TransportType transportType2 = new TransportType(TEST_ID, TEST_NAME);

        assertEquals(transportType, transportType2);
    }

    @Test
    void testEqualsDifferentId() {
        TransportType transportType2 = new TransportType(999, TEST_NAME);

        assertNotEquals(transportType, transportType2);
    }

    @Test
    void testEqualsDifferentName() {
        TransportType transportType2 = new TransportType(TEST_ID, "Ship");

        assertNotEquals(transportType, transportType2);
    }

    @Test
    void testEqualsDifferentIdAndName() {
        TransportType transportType2 = new TransportType(888, "Bicycle");

        assertNotEquals(transportType, transportType2);
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(transportType, null);
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals(transportType, "Not a TransportType");
    }

    @Test
    void testEqualsReflexive() {
        assertEquals(transportType, transportType);
    }

    @Test
    void testEqualsSymmetric() {
        TransportType transportType2 = new TransportType(TEST_ID, TEST_NAME);

        assertEquals(transportType, transportType2);
        assertEquals(transportType2, transportType);
    }

    @Test
    void testEqualsTransitive() {
        TransportType transportType2 = new TransportType(TEST_ID, TEST_NAME);
        TransportType transportType3 = new TransportType(TEST_ID, TEST_NAME);

        assertEquals(transportType, transportType2);
        assertEquals(transportType2, transportType3);
        assertEquals(transportType, transportType3);
    }

    @Test
    void testEqualsWithNullFields() {
        TransportType transportType1 = new TransportType(1, null);
        TransportType transportType2 = new TransportType(1, null);
        TransportType transportType3 = new TransportType(1, "Bus");

        assertEquals(transportType1, transportType2);
        assertNotEquals(transportType1, transportType3);
    }

    @Test
    void testHashCodeEqual() {
        TransportType transportType2 = new TransportType(TEST_ID, TEST_NAME);

        assertEquals(transportType.hashCode(), transportType2.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        TransportType transportType2 = new TransportType(999, "Train");

        assertNotEquals(transportType.hashCode(), transportType2.hashCode());
    }

    @Test
    void testHashCodeConsistent() {
        int hash1 = transportType.hashCode();
        int hash2 = transportType.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCodeWithNullFields() {
        TransportType transportType1 = new TransportType(1, null);
        TransportType transportType2 = new TransportType(1, null);

        assertEquals(transportType1.hashCode(), transportType2.hashCode());
    }

    @Test
    void testToString() {
        assertEquals(TEST_NAME, transportType.toString());
    }

    @Test
    void testToStringWithNull() {
        transportType.setName(null);
        assertEquals(null, transportType.toString());
    }

    @Test
    void testToStringWithEmpty() {
        transportType.setName("");
        assertEquals("", transportType.toString());
    }

    @Test
    void testToStringWithSpaces() {
        transportType.setName("  Metro  ");
        assertEquals("  Metro  ", transportType.toString());
    }

    @Test
    void maxId() {
        transportType.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, transportType.getId());
    }

    @Test
    void minId() {
        transportType.setId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, transportType.getId());
    }

    @Test
    void longName() {
        String longName = "A".repeat(1000);
        transportType.setName(longName);
        assertEquals(longName, transportType.getName());
    }

    @Test
    void nameWithSpecialCharacters() {
        String specialName = "Автобус-№1@#$%^&*()";
        transportType.setName(specialName);
        assertEquals(specialName, transportType.getName());
    }

    @Test
    void multipleFieldChanges() {
        assertEquals(TEST_ID, transportType.getId());
        assertEquals(TEST_NAME, transportType.getName());

        transportType.setId(100);
        transportType.setName("Car");
        assertEquals(100, transportType.getId());
        assertEquals("Car", transportType.getName());

        transportType.setId(200);
        transportType.setName("Plane");
        assertEquals(200, transportType.getId());
        assertEquals("Plane", transportType.getName());

        transportType.setName(null);
        assertEquals(200, transportType.getId());
        assertNull(transportType.getName());
    }
}