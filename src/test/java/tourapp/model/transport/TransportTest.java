package tourapp.model.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class TransportTest {

    private Transport transport;
    private TransportType transportType;
    private static final int TEST_ID = 1;
    private static final String TEST_NAME = "Luxury Bus";
    private static final double TEST_PRICE = 150.50;

    @BeforeEach
    void setUp() {
        transportType = new TransportType(1, "Bus");
        transport = new Transport(TEST_ID, TEST_NAME, transportType, TEST_PRICE);
    }

    @Test
    void defaultConstructor() {
        Transport emptyTransport = new Transport();

        assertEquals(0, emptyTransport.getId());
        assertNull(emptyTransport.getName());
        assertNull(emptyTransport.getType());
        assertEquals(0.0, emptyTransport.getPricePerPerson());
    }

    @Test
    void constructorWithParameters() {
        TransportType airplaneType = new TransportType(2, "Airplane");
        Transport testTransport = new Transport(5, "Boeing 737", airplaneType, 500.0);

        assertEquals(5, testTransport.getId());
        assertEquals("Boeing 737", testTransport.getName());
        assertEquals(airplaneType, testTransport.getType());
        assertEquals(500.0, testTransport.getPricePerPerson());
    }

    @Test
    void constructorWithNullValues() {
        Transport testTransport = new Transport(10, null, null, 75.0);

        assertEquals(10, testTransport.getId());
        assertNull(testTransport.getName());
        assertNull(testTransport.getType());
        assertEquals(75.0, testTransport.getPricePerPerson());
    }

    @Test
    void constructorWithZeroPrice() {
        Transport testTransport = new Transport(15, "Free Bus", transportType, 0.0);

        assertEquals(15, testTransport.getId());
        assertEquals("Free Bus", testTransport.getName());
        assertEquals(transportType, testTransport.getType());
        assertEquals(0.0, testTransport.getPricePerPerson());
    }

    @Test
    void constructorWithNegativePrice() {
        Transport testTransport = new Transport(20, "Discount Transport", transportType, -50.0);

        assertEquals(20, testTransport.getId());
        assertEquals("Discount Transport", testTransport.getName());
        assertEquals(transportType, testTransport.getType());
        assertEquals(-50.0, testTransport.getPricePerPerson());
    }


    @Test
    void getId() {
        assertEquals(TEST_ID, transport.getId());
    }

    @Test
    void setId() {
        transport.setId(999);
        assertEquals(999, transport.getId());
    }

    @Test
    void setIdNegative() {
        transport.setId(-1);
        assertEquals(-1, transport.getId());
    }

    @Test
    void getName() {
        assertEquals(TEST_NAME, transport.getName());
    }

    @Test
    void setName() {
        transport.setName("Express Train");
        assertEquals("Express Train", transport.getName());
    }

    @Test
    void setNameNull() {
        transport.setName(null);
        assertNull(transport.getName());
    }

    @Test
    void getType() {
        assertEquals(transportType, transport.getType());
    }

    @Test
    void setType() {
        TransportType newType = new TransportType(3, "Train");
        transport.setType(newType);
        assertEquals(newType, transport.getType());
    }

    @Test
    void setTypeNull() {
        transport.setType(null);
        assertNull(transport.getType());
    }

    @Test
    void getPricePerPerson() {
        assertEquals(TEST_PRICE, transport.getPricePerPerson());
    }

    @Test
    void setPricePerPerson() {
        transport.setPricePerPerson(299.99);
        assertEquals(299.99, transport.getPricePerPerson());
    }

    @Test
    void setPricePerPersonZero() {
        transport.setPricePerPerson(0.0);
        assertEquals(0.0, transport.getPricePerPerson());
    }

    @Test
    void setPricePerPersonNegative() {
        transport.setPricePerPerson(-100.0);
        assertEquals(-100.0, transport.getPricePerPerson());
    }

    @Test
    void setPricePerPersonLarge() {
        double largePrice = Double.MAX_VALUE;
        transport.setPricePerPerson(largePrice);
        assertEquals(largePrice, transport.getPricePerPerson());
    }

    @Test
    void testEqualsIdentical() {
        Transport transport2 = new Transport(TEST_ID, TEST_NAME, transportType, TEST_PRICE);

        assertEquals(transport, transport2);
    }

    @Test
    void testEqualsDifferentId() {
        Transport transport2 = new Transport(999, TEST_NAME, transportType, TEST_PRICE);

        assertNotEquals(transport, transport2);
    }

    @Test
    void testEqualsDifferentName() {
        Transport transport2 = new Transport(TEST_ID, "Different Name", transportType, TEST_PRICE);

        assertNotEquals(transport, transport2);
    }

    @Test
    void testEqualsDifferentType() {
        TransportType differentType = new TransportType(2, "Airplane");
        Transport transport2 = new Transport(TEST_ID, TEST_NAME, differentType, TEST_PRICE);

        assertNotEquals(transport, transport2);
    }

    @Test
    void testEqualsDifferentPrice() {
        Transport transport2 = new Transport(TEST_ID, TEST_NAME, transportType, 999.99);

        assertNotEquals(transport, transport2);
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(transport, null);
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals(transport, "Not a Transport");
    }

    @Test
    void testEqualsReflexive() {
        assertEquals(transport, transport);
    }

    @Test
    void testEqualsWithNullFields() {
        Transport transport1 = new Transport(1, null, null, 100.0);
        Transport transport2 = new Transport(1, null, null, 100.0);
        Transport transport3 = new Transport(1, "Bus", null, 100.0);

        assertEquals(transport1, transport2);
        assertNotEquals(transport1, transport3);
    }

    @Test
    void testEqualsWithDoubles() {
        Transport transport1 = new Transport(1, "Bus", transportType, 100.123456789);
        Transport transport2 = new Transport(1, "Bus", transportType, 100.123456789);
        Transport transport3 = new Transport(1, "Bus", transportType, 100.123456788);

        assertEquals(transport1, transport2);
        assertNotEquals(transport1, transport3);
    }

    @Test
    void testHashCodeEqual() {
        Transport transport2 = new Transport(TEST_ID, TEST_NAME, transportType, TEST_PRICE);

        assertEquals(transport.hashCode(), transport2.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        Transport transport2 = new Transport(999, "Different", transportType, 888.88);

        assertNotEquals(transport.hashCode(), transport2.hashCode());
    }

    @Test
    void testHashCodeConsistent() {
        int hash1 = transport.hashCode();
        int hash2 = transport.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void testToString() {
        String result = transport.toString();

        assertTrue(result.contains(TEST_NAME));
        assertTrue(result.contains("Bus"));
        assertTrue(result.contains("150,50"));
        assertTrue(result.contains("Type:"));
        assertTrue(result.contains("Price per person:"));
    }

    @Test
    void testToStringWithNullType() {
        transport.setType(null);
        String result = transport.toString();

        assertTrue(result.contains(TEST_NAME));
        assertTrue(result.contains("—"));
        assertTrue(result.contains("150,50"));
    }

    @Test
    void testToStringWithNullName() {
        transport.setName(null);
        String result = transport.toString();

        assertTrue(result.contains("null"));
        assertTrue(result.contains("Bus"));
        assertTrue(result.contains("150,50"));
    }

    @Test
    void testToStringPriceFormatting() {
        transport.setPricePerPerson(100.0);
        String result = transport.toString();

        assertTrue(result.contains("100,00"));
    }

    @Test
    void testToStringWithDecimalPrice() {
        transport.setPricePerPerson(123.456);
        String result = transport.toString();

        assertTrue(result.contains("123,46"));
    }

    @Test
    void maxValues() {
        transport.setId(Integer.MAX_VALUE);
        transport.setPricePerPerson(Double.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, transport.getId());
        assertEquals(Double.MAX_VALUE, transport.getPricePerPerson());
    }

    @Test
    void minValues() {
        transport.setId(Integer.MIN_VALUE);
        transport.setPricePerPerson(Double.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, transport.getId());
        assertEquals(Double.MIN_VALUE, transport.getPricePerPerson());
    }

    @Test
    void specialDoubleValues() {
        transport.setPricePerPerson(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, transport.getPricePerPerson());

        transport.setPricePerPerson(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, transport.getPricePerPerson());

        transport.setPricePerPerson(Double.NaN);
        assertTrue(Double.isNaN(transport.getPricePerPerson()));
    }

    @Test
    void longName() {
        String longName = "Very ".repeat(100) + "Long Transport Name";
        transport.setName(longName);
        assertEquals(longName, transport.getName());
    }

    @Test
    void nameWithSpecialCharacters() {
        String specialName = "Супер-Автобус №1 (Люкс) @#$%";
        transport.setName(specialName);
        assertEquals(specialName, transport.getName());
    }

    @Test
    void multipleObjectChanges() {
        TransportType trainType = new TransportType(2, "Train");
        TransportType airplaneType = new TransportType(3, "Airplane");

        assertEquals(TEST_ID, transport.getId());
        assertEquals(TEST_NAME, transport.getName());
        assertEquals(transportType, transport.getType());
        assertEquals(TEST_PRICE, transport.getPricePerPerson());

        transport.setId(100);
        transport.setName("Express Train");
        transport.setType(trainType);
        transport.setPricePerPerson(200.0);

        assertEquals(100, transport.getId());
        assertEquals("Express Train", transport.getName());
        assertEquals(trainType, transport.getType());
        assertEquals(200.0, transport.getPricePerPerson());

        transport.setId(200);
        transport.setName("Boeing 747");
        transport.setType(airplaneType);
        transport.setPricePerPerson(1000.0);

        assertEquals(200, transport.getId());
        assertEquals("Boeing 747", transport.getName());
        assertEquals(airplaneType, transport.getType());
        assertEquals(1000.0, transport.getPricePerPerson());
    }
}