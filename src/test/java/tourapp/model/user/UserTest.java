package tourapp.model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private UserType adminType;
    private UserType managerType;
    private UserType customerType;

    @BeforeEach
    void setUp() {
        adminType = createMockUserType("ADMIN");
        managerType = createMockUserType("MANAGER");
        customerType = createMockUserType("CUSTOMER");

        user = new User(1, "John Doe", "john@example.com", "password123", customerType);
    }

    private UserType createMockUserType(String name) {
        return new UserType() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || !(obj instanceof UserType)) return false;
                return name.equals(((UserType) obj).getName());
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    @Test
    void constructorWithAllParameters() {
        User testUser = new User(5, "Jane Smith", "jane@test.com", "secret", adminType);

        assertEquals(5, testUser.getId());
        assertEquals("Jane Smith", testUser.getName());
        assertEquals("jane@test.com", testUser.getEmail());
        assertEquals(adminType, testUser.getUserType());
        assertNotNull(testUser.getPasswordHash());
        assertTrue(User.verifyPassword("secret", testUser.getPasswordHash()));
    }

    @Test
    void constructorWithoutId() {
        User testUser = new User("Bob Wilson", "bob@test.com", "mypass", managerType);

        assertEquals(-1, testUser.getId());
        assertEquals("Bob Wilson", testUser.getName());
        assertEquals("bob@test.com", testUser.getEmail());
        assertEquals(managerType, testUser.getUserType());
        assertTrue(User.verifyPassword("mypass", testUser.getPasswordHash()));
    }

    @Test
    void defaultConstructor() {
        User emptyUser = new User();

        assertEquals(0, emptyUser.getId());
        assertNull(emptyUser.getName());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getUserType());
        assertNull(emptyUser.getPasswordHash());
    }

    @Test
    void hashPassword() {
        String password = "testPassword123";
        String hash = User.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    void verifyPasswordCorrect() {
        String password = "mySecretPassword";
        String hash = User.hashPassword(password);

        assertTrue(User.verifyPassword(password, hash));
    }

    @Test
    void verifyPasswordIncorrect() {
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hash = User.hashPassword(password);

        assertFalse(User.verifyPassword(wrongPassword, hash));
    }

    @Test
    void samePasswordDifferentHashes() {
        String password = "samePassword";
        String hash1 = User.hashPassword(password);
        String hash2 = User.hashPassword(password);

        assertNotEquals(hash1, hash2);
        assertTrue(User.verifyPassword(password, hash1));
        assertTrue(User.verifyPassword(password, hash2));
    }

    @Test
    void getId() {
        assertEquals(1, user.getId());
    }

    @Test
    void setId() {
        user.setId(999);
        assertEquals(999, user.getId());
    }

    @Test
    void getName() {
        assertEquals("John Doe", user.getName());
    }

    @Test
    void setName() {
        user.setName("New Name");
        assertEquals("New Name", user.getName());
    }

    @Test
    void getEmail() {
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void setEmail() {
        user.setEmail("newemail@test.com");
        assertEquals("newemail@test.com", user.getEmail());
    }

    @Test
    void getUserType() {
        assertEquals(customerType, user.getUserType());
    }

    @Test
    void setUserType() {
        user.setUserType(adminType);
        assertEquals(adminType, user.getUserType());
    }

    @Test
    void getPasswordHash() {
        assertNotNull(user.getPasswordHash());
        assertTrue(User.verifyPassword("password123", user.getPasswordHash()));
    }

    @Test
    void setPasswordHash() {
        String newHash = User.hashPassword("newPassword");
        user.setPasswordHash(newHash);
        assertEquals(newHash, user.getPasswordHash());
    }

    @Test
    void isAdmin() {
        user.setUserType(adminType);
        assertTrue(user.isAdmin());

        user.setUserType(customerType);
        assertFalse(user.isAdmin());
    }

    @Test
    void isManager() {
        user.setUserType(managerType);
        assertTrue(user.isManager());

        user.setUserType(customerType);
        assertFalse(user.isManager());
    }

    @Test
    void isCustomer() {
        user.setUserType(customerType);
        assertTrue(user.isCustomer());

        user.setUserType(adminType);
        assertFalse(user.isCustomer());
    }

    @Test
    void roleCheckCaseInsensitive() {
        UserType lowerCaseAdmin = createMockUserType("admin");
        UserType upperCaseAdmin = createMockUserType("ADMIN");
        UserType mixedCaseAdmin = createMockUserType("AdMiN");

        user.setUserType(lowerCaseAdmin);
        assertTrue(user.isAdmin());

        user.setUserType(upperCaseAdmin);
        assertTrue(user.isAdmin());

        user.setUserType(mixedCaseAdmin);
        assertTrue(user.isAdmin());
    }

    @Test
    void testEqualsIdentical() {
        User user2 = new User(1, "John Doe", "john@example.com", "password123", customerType);
        user2.setPasswordHash(user.getPasswordHash());

        assertEquals(user, user2);
    }

    @Test
    void testEqualsDifferent() {
        User user2 = new User(2, "Jane Smith", "jane@example.com", "password456", adminType);

        assertNotEquals(user, user2);
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(user, null);
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals(user, "Not a User object");
    }

    @Test
    void testEqualsReflexive() {
        assertEquals(user, user);
    }

    @Test
    void testHashCodeEqual() {
        User user2 = new User(1, "John Doe", "john@example.com", "password123", customerType);
        user2.setPasswordHash(user.getPasswordHash());

        assertEquals(user.hashCode(), user2.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        User user2 = new User(2, "Jane Smith", "jane@example.com", "password456", adminType);

        assertNotEquals(user.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        String result = user.toString();

        assertTrue(result.contains("User ID: 1"));
        assertTrue(result.contains("Name: John Doe"));
        assertTrue(result.contains("Email: john@example.com"));
        assertTrue(result.contains("User Type: CUSTOMER"));
    }

    @Test
    void nullValues() {
        User nullUser = new User();

        assertDoesNotThrow(() -> {
            nullUser.setName(null);
            nullUser.setEmail(null);
            nullUser.setUserType(null);
            nullUser.setPasswordHash(null);
        });
    }

    @Test
    void emptyStrings() {
        user.setName("");
        user.setEmail("");

        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
    }

    @Test
    void negativeId() {
        user.setId(-100);
        assertEquals(-100, user.getId());
    }
}