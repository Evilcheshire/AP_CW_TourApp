package tourapp.model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tourapp.model.tour.Tour;

import static org.junit.jupiter.api.Assertions.*;

class UserTourTest {

    private UserTour userTour;
    private Tour mockTour;
    private static final int TEST_USER_ID = 1;
    private static final int TEST_TOUR_ID = 101;

    @BeforeEach
    void setUp() {
        mockTour = createMockTour(TEST_TOUR_ID, "Test Tour");
        userTour = new UserTour(TEST_USER_ID, TEST_TOUR_ID);
    }

    private Tour createMockTour(int id, String name) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setName(name);
        return tour;
    }

    @Test
    void defaultConstructor() {
        UserTour emptyUserTour = new UserTour();

        assertEquals(0, emptyUserTour.getUserId());
        assertEquals(0, emptyUserTour.getTourId());
        assertNull(emptyUserTour.getTour());
    }

    @Test
    void constructorWithIds() {
        UserTour testUserTour = new UserTour(5, 200);

        assertEquals(5, testUserTour.getUserId());
        assertEquals(200, testUserTour.getTourId());
        assertNull(testUserTour.getTour());
    }

    @Test
    void constructorWithTourObject() {
        Tour tour = createMockTour(300, "Adventure Tour");
        UserTour testUserTour = new UserTour(10, tour);

        assertEquals(10, testUserTour.getUserId());
        assertEquals(300, testUserTour.getTourId());
        assertEquals(tour, testUserTour.getTour());
    }

    @Test
    void constructorAutoSetsTourId() {
        Tour tour = createMockTour(999, "Luxury Tour");
        UserTour testUserTour = new UserTour(15, tour);

        assertEquals(999, testUserTour.getTourId());
        assertEquals(tour.getId(), testUserTour.getTourId());
    }

    @Test
    void getUserId() {
        assertEquals(TEST_USER_ID, userTour.getUserId());
    }

    @Test
    void setUserId() {
        userTour.setUserId(999);
        assertEquals(999, userTour.getUserId());
    }

    @Test
    void setUserIdNegative() {
        userTour.setUserId(-1);
        assertEquals(-1, userTour.getUserId());
    }

    @Test
    void getTourId() {
        assertEquals(TEST_TOUR_ID, userTour.getTourId());
    }

    @Test
    void setTourId() {
        userTour.setTourId(500);
        assertEquals(500, userTour.getTourId());
    }

    @Test
    void setTourIdNegative() {
        userTour.setTourId(-100);
        assertEquals(-100, userTour.getTourId());
    }

    @Test
    void getTour() {
        assertNull(userTour.getTour());

        userTour.setTour(mockTour);
        assertEquals(mockTour, userTour.getTour());
    }

    @Test
    void setTourUpdatesTourId() {
        Tour newTour = createMockTour(777, "Beach Tour");
        userTour.setTour(newTour);

        assertEquals(newTour, userTour.getTour());
        assertEquals(777, userTour.getTourId());
    }

    @Test
    void setTourNullDoesNotChangeTourId() {
        int originalTourId = userTour.getTourId();
        userTour.setTour(null);

        assertNull(userTour.getTour());
        assertEquals(originalTourId, userTour.getTourId());
    }

    @Test
    void setTourOverwriteUpdatesId() {
        Tour firstTour = createMockTour(100, "First Tour");
        Tour secondTour = createMockTour(200, "Second Tour");

        userTour.setTour(firstTour);
        assertEquals(100, userTour.getTourId());

        userTour.setTour(secondTour);
        assertEquals(200, userTour.getTourId());
        assertEquals(secondTour, userTour.getTour());
    }

    @Test
    void testEqualsIdentical() {
        UserTour userTour2 = new UserTour(TEST_USER_ID, TEST_TOUR_ID);

        assertEquals(userTour, userTour2);
    }

    @Test
    void testEqualsDifferentUserId() {
        UserTour userTour2 = new UserTour(999, TEST_TOUR_ID);

        assertNotEquals(userTour, userTour2);
    }

    @Test
    void testEqualsDifferentTourId() {
        UserTour userTour2 = new UserTour(TEST_USER_ID, 999);

        assertNotEquals(userTour, userTour2);
    }

    @Test
    void testEqualsWithDifferentTourObjectsSameIds() {
        Tour tour1 = createMockTour(TEST_TOUR_ID, "Tour 1");
        Tour tour2 = createMockTour(TEST_TOUR_ID, "Tour 2");

        UserTour userTour1 = new UserTour(TEST_USER_ID, tour1);
        UserTour userTour2 = new UserTour(TEST_USER_ID, tour2);

        assertEquals(userTour1, userTour2);
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(userTour, null);
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals(userTour, "Not a UserTour object");
    }

    @Test
    void testEqualsReflexive() {
        assertEquals(userTour, userTour);
    }

    @Test
    void testEqualsSymmetric() {
        UserTour userTour2 = new UserTour(TEST_USER_ID, TEST_TOUR_ID);

        assertEquals(userTour, userTour2);
        assertEquals(userTour2, userTour);
    }

    @Test
    void testEqualsTransitive() {
        UserTour userTour2 = new UserTour(TEST_USER_ID, TEST_TOUR_ID);
        UserTour userTour3 = new UserTour(TEST_USER_ID, TEST_TOUR_ID);

        assertEquals(userTour, userTour2);
        assertEquals(userTour2, userTour3);
        assertEquals(userTour, userTour3);
    }

    @Test
    void testHashCodeEqual() {
        UserTour userTour2 = new UserTour(TEST_USER_ID, TEST_TOUR_ID);

        assertEquals(userTour.hashCode(), userTour2.hashCode());
    }

    @Test
    void testHashCodeDifferent() {
        UserTour userTour2 = new UserTour(999, 888);

        assertNotEquals(userTour.hashCode(), userTour2.hashCode());
    }

    @Test
    void testHashCodeConsistent() {
        int hash1 = userTour.hashCode();
        int hash2 = userTour.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void tourIdSynchronization() {
        Tour tour1 = createMockTour(100, "Mountain Tour");
        Tour tour2 = createMockTour(200, "City Tour");

        userTour.setTour(tour1);
        assertEquals(100, userTour.getTourId());

        userTour.setTour(tour2);
        assertEquals(200, userTour.getTourId());
    }

    @Test
    void manualTourIdChange() {
        Tour tour = createMockTour(300, "Desert Tour");
        userTour.setTour(tour);

        userTour.setTourId(999);

        assertEquals(tour, userTour.getTour());
        assertEquals(999, userTour.getTourId());
        assertNotEquals(tour.getId(), userTour.getTourId());
    }

    @Test
    void setTourWithZeroId() {
        Tour tour = createMockTour(0, "Free Tour");
        userTour.setTour(tour);

        assertEquals(0, userTour.getTourId());
        assertEquals(tour, userTour.getTour());
    }

    @Test
    void zeroIds() {
        UserTour zeroUserTour = new UserTour(0, 0);

        assertEquals(0, zeroUserTour.getUserId());
        assertEquals(0, zeroUserTour.getTourId());
    }

    @Test
    void maxIds() {
        UserTour maxUserTour = new UserTour(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, maxUserTour.getUserId());
        assertEquals(Integer.MAX_VALUE, maxUserTour.getTourId());
    }

    @Test
    void minIds() {
        UserTour minUserTour = new UserTour(Integer.MIN_VALUE, Integer.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, minUserTour.getUserId());
        assertEquals(Integer.MIN_VALUE, minUserTour.getTourId());
    }

    @Test
    void multipleTourOperations() {
        Tour tour1 = createMockTour(100, "Tour 1");
        Tour tour2 = createMockTour(200, "Tour 2");

        userTour.setTour(tour1);
        assertEquals(tour1, userTour.getTour());
        assertEquals(100, userTour.getTourId());

        userTour.setTour(null);
        assertNull(userTour.getTour());
        assertEquals(100, userTour.getTourId());

        userTour.setTour(tour2);
        assertEquals(tour2, userTour.getTour());
        assertEquals(200, userTour.getTourId());
    }
}