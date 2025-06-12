package tourapp.model.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class UserTypeTest {

    private UserType userType;

    @BeforeEach
    void setUp() {
        userType = new UserType();
    }

    @Test
    void testDefaultConstructor() {
        UserType ut = new UserType();
        assertEquals(0, ut.getId());
        assertNull(ut.getName());
    }

    @Test
    void testParameterizedConstructor() {
        UserType ut = new UserType(1, "Admin");
        assertEquals(1, ut.getId());
        assertEquals("Admin", ut.getName());
    }

    @Test
    void testParameterizedConstructorWithNullName() {
        UserType ut = new UserType(2, null);
        assertEquals(2, ut.getId());
        assertNull(ut.getName());
    }

    @Test
    void testGetId() {
        userType.setId(5);
        assertEquals(5, userType.getId());
    }

    @Test
    void testSetId() {
        userType.setId(10);
        assertEquals(10, userType.getId());
    }

    @Test
    void testSetIdNegative() {
        userType.setId(-1);
        assertEquals(-1, userType.getId());
    }

    @Test
    void testSetIdZero() {
        userType.setId(0);
        assertEquals(0, userType.getId());
    }

    @Test
    void testGetName() {
        userType.setName("Customer");
        assertEquals("Customer", userType.getName());
    }

    @Test
    void testSetName() {
        userType.setName("Manager");
        assertEquals("Manager", userType.getName());
    }

    @Test
    void testSetNameNull() {
        userType.setName(null);
        assertNull(userType.getName());
    }

    @Test
    void testSetNameEmpty() {
        userType.setName("");
        assertEquals("", userType.getName());
    }

    @Test
    void testSetNameWithWhitespace() {
        userType.setName("  Admin  ");
        assertEquals("  Admin  ", userType.getName());
    }

    @Test
    void testToString() {
        userType.setName("Tourist");
        assertEquals("Tourist", userType.toString());
    }

    @Test
    void testToStringWithNullName() {
        userType.setName(null);
        assertNull(userType.toString());
    }

    @Test
    void testToStringWithEmptyName() {
        userType.setName("");
        assertEquals("", userType.toString());
    }

    @Test
    void testEqualsSameObject() {
        assertTrue(userType.equals(userType));
    }

    @Test
    void testEqualsNull() {
        assertFalse(userType.equals(null));
    }

    @Test
    void testEqualsDifferentClass() {
        assertFalse(userType.equals("string"));
    }

    @Test
    void testEqualsSameId() {
        UserType ut1 = new UserType(1, "Admin");
        UserType ut2 = new UserType(1, "Manager");
        assertTrue(ut1.equals(ut2));
    }

    @Test
    void testEqualsDifferentId() {
        UserType ut1 = new UserType(1, "Admin");
        UserType ut2 = new UserType(2, "Admin");
        assertFalse(ut1.equals(ut2));
    }

    @Test
    void testEqualsZeroId() {
        UserType ut1 = new UserType(0, "Admin");
        UserType ut2 = new UserType(0, "Manager");
        assertTrue(ut1.equals(ut2));
    }

    @Test
    void testEqualsNegativeId() {
        UserType ut1 = new UserType(-1, "Admin");
        UserType ut2 = new UserType(-1, "Manager");
        assertTrue(ut1.equals(ut2));
    }

    @Test
    void testHashCode() {
        userType.setId(42);
        assertEquals(42, userType.hashCode());
    }

    @Test
    void testHashCodeConsistent() {
        UserType ut1 = new UserType(5, "Admin");
        UserType ut2 = new UserType(5, "Manager");
        assertEquals(ut1.hashCode(), ut2.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        UserType ut1 = new UserType(1, "Admin");
        UserType ut2 = new UserType(2, "Admin");
        assertNotEquals(ut1.hashCode(), ut2.hashCode());
    }

    @Test
    void testHashCodeZero() {
        userType.setId(0);
        assertEquals(0, userType.hashCode());
    }

    @Test
    void testHashCodeNegative() {
        userType.setId(-10);
        assertEquals(-10, userType.hashCode());
    }

    @Test
    void testCompleteWorkflow() {
        UserType ut = new UserType(1, "Admin");

        assertEquals(1, ut.getId());
        assertEquals("Admin", ut.getName());
        assertEquals("Admin", ut.toString());

        ut.setId(2);
        ut.setName("Manager");

        assertEquals(2, ut.getId());
        assertEquals("Manager", ut.getName());
        assertEquals("Manager", ut.toString());
        assertEquals(2, ut.hashCode());

        UserType ut2 = new UserType(2, "Customer");
        assertTrue(ut.equals(ut2));
        assertEquals(ut.hashCode(), ut2.hashCode());
    }
}