package tourapp.model.tour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.model.location.Location;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourLocationTest {

    @Mock
    private Location mockLocation;

    private TourLocation tourLocation;
    private static final int TOUR_ID = 1;
    private static final int LOCATION_ID = 2;

    @BeforeEach
    void setUp() {
        tourLocation = new TourLocation();

        lenient().when(mockLocation.getId()).thenReturn(LOCATION_ID);
    }

    @Test
    void testDefaultConstructor() {
        TourLocation newTourLocation = new TourLocation();

        assertEquals(0, newTourLocation.getTourId());
        assertEquals(0, newTourLocation.getLocationId());
        assertNull(newTourLocation.getLocation());
    }

    @Test
    void testConstructorWithTourIdAndLocationId() {
        TourLocation tourLoc = new TourLocation(TOUR_ID, LOCATION_ID);

        assertEquals(TOUR_ID, tourLoc.getTourId());
        assertEquals(LOCATION_ID, tourLoc.getLocationId());
        assertNull(tourLoc.getLocation());
    }

    @Test
    void testConstructorWithTourIdAndLocation() {
        TourLocation tourLoc = new TourLocation(TOUR_ID, mockLocation);

        assertEquals(TOUR_ID, tourLoc.getTourId());
        assertEquals(LOCATION_ID, tourLoc.getLocationId());
        assertEquals(mockLocation, tourLoc.getLocation());

        verify(mockLocation).getId();
    }

    @Test
    void testSetAndGetTourId() {
        int expectedTourId = 123;
        tourLocation.setTourId(expectedTourId);

        assertEquals(expectedTourId, tourLocation.getTourId());
    }

    @Test
    void testSetAndGetLocationId() {
        int expectedLocationId = 456;
        tourLocation.setLocationId(expectedLocationId);

        assertEquals(expectedLocationId, tourLocation.getLocationId());
    }

    @Test
    void testSetAndGetLocation() {
        tourLocation.setLocation(mockLocation);

        assertEquals(mockLocation, tourLocation.getLocation());
        assertEquals(LOCATION_ID, tourLocation.getLocationId());

        verify(mockLocation).getId();
    }

    @Test
    void testSetLocationWithNull() {
        tourLocation.setLocationId(999); // встановлюємо якийсь ID
        tourLocation.setLocation(null);

        assertNull(tourLocation.getLocation());
        assertEquals(999, tourLocation.getLocationId()); // ID не повинен змінитися
    }

    @Test
    void testSetLocationUpdatesLocationId() {
        Location anotherMockLocation = mock(Location.class);
        when(anotherMockLocation.getId()).thenReturn(789);

        tourLocation.setLocation(anotherMockLocation);

        assertEquals(anotherMockLocation, tourLocation.getLocation());
        assertEquals(789, tourLocation.getLocationId());

        verify(anotherMockLocation).getId();
    }

    @Test
    void testEqualsWithSameObject() {
        assertTrue(tourLocation.equals(tourLocation));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(tourLocation.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertFalse(tourLocation.equals("not a tour location"));
    }

    @Test
    void testEqualsWithIdenticalTourLocations() {
        TourLocation tourLoc1 = new TourLocation(TOUR_ID, LOCATION_ID);
        TourLocation tourLoc2 = new TourLocation(TOUR_ID, LOCATION_ID);

        assertTrue(tourLoc1.equals(tourLoc2));
        assertTrue(tourLoc2.equals(tourLoc1));
        assertEquals(tourLoc1.hashCode(), tourLoc2.hashCode());
    }

    @Test
    void testEqualsWithDifferentTourIds() {
        TourLocation tourLoc1 = new TourLocation(1, LOCATION_ID);
        TourLocation tourLoc2 = new TourLocation(2, LOCATION_ID);

        assertFalse(tourLoc1.equals(tourLoc2));
        assertFalse(tourLoc2.equals(tourLoc1));
    }

    @Test
    void testEqualsWithDifferentLocationIds() {
        TourLocation tourLoc1 = new TourLocation(TOUR_ID, 1);
        TourLocation tourLoc2 = new TourLocation(TOUR_ID, 2);

        assertFalse(tourLoc1.equals(tourLoc2));
        assertFalse(tourLoc2.equals(tourLoc1));
    }

    @Test
    void testEqualsWithBothDifferentIds() {
        TourLocation tourLoc1 = new TourLocation(1, 1);
        TourLocation tourLoc2 = new TourLocation(2, 2);

        assertFalse(tourLoc1.equals(tourLoc2));
        assertFalse(tourLoc2.equals(tourLoc1));
    }

    @Test
    void testHashCodeConsistency() {
        tourLocation.setTourId(TOUR_ID);
        tourLocation.setLocationId(LOCATION_ID);

        int hashCode1 = tourLocation.hashCode();
        int hashCode2 = tourLocation.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeWithDifferentValues() {
        TourLocation tourLoc1 = new TourLocation(1, 1);
        TourLocation tourLoc2 = new TourLocation(2, 2);

        assertNotEquals(tourLoc1.hashCode(), tourLoc2.hashCode());
    }

    @Test
    void testHashCodeWithSameValues() {
        TourLocation tourLoc1 = new TourLocation(TOUR_ID, LOCATION_ID);
        TourLocation tourLoc2 = new TourLocation(TOUR_ID, LOCATION_ID);

        assertEquals(tourLoc1.hashCode(), tourLoc2.hashCode());
    }

    @Test
    void testLocationInteractionAfterConstruction() {
        TourLocation tourLoc = new TourLocation(TOUR_ID, mockLocation);

        assertEquals(mockLocation, tourLoc.getLocation());

        Location anotherMockLocation = mock(Location.class);
        when(anotherMockLocation.getId()).thenReturn(999);

        tourLoc.setLocation(anotherMockLocation);

        assertEquals(anotherMockLocation, tourLoc.getLocation());
        assertEquals(999, tourLoc.getLocationId());

        verify(mockLocation).getId();
        verify(anotherMockLocation).getId();
    }

    @Test
    void testEqualsIgnoresLocationObject() {
        Location mockLocation1 = mock(Location.class);
        Location mockLocation2 = mock(Location.class);
        when(mockLocation1.getId()).thenReturn(LOCATION_ID);
        when(mockLocation2.getId()).thenReturn(LOCATION_ID);

        TourLocation tourLoc1 = new TourLocation(TOUR_ID, mockLocation1);
        TourLocation tourLoc2 = new TourLocation(TOUR_ID, mockLocation2);

        assertTrue(tourLoc1.equals(tourLoc2));
        assertEquals(tourLoc1.hashCode(), tourLoc2.hashCode());
    }
}