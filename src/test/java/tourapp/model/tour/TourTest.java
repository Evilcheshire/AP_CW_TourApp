package tourapp.model.tour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.model.location.Location;
import tourapp.model.meal.Meal;
import tourapp.model.transport.Transport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourTest {

    @Mock private TourType mockTourType;
    @Mock private Transport mockTransport;
    @Mock private Meal mockMeal;
    @Mock private Location mockLocation1;
    @Mock private Location mockLocation2;

    private Tour tour;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        tour = new Tour();
        startDate = LocalDate.of(2024, 6, 1);
        endDate = LocalDate.of(2024, 6, 10);

        // Налаштування mock об'єктів
        lenient().when(mockTourType.getName()).thenReturn("Adventure Tour");
        lenient().when(mockLocation1.getName()).thenReturn("Paris");
        lenient().when(mockLocation1.getCountry()).thenReturn("France");
        lenient().when(mockLocation2.getName()).thenReturn("Rome");
        lenient().when(mockLocation2.getCountry()).thenReturn("Italy");
    }

    @Test
    void testDefaultConstructor() {
        Tour newTour = new Tour();

        assertNotNull(newTour.getLocations());
        assertTrue(newTour.getLocations().isEmpty());
        assertTrue(newTour.isActive());
    }

    @Test
    void testSetAndGetId() {
        int expectedId = 123;
        tour.setId(expectedId);

        assertEquals(expectedId, tour.getId());
    }

    @Test
    void testSetAndGetName() {
        String expectedName = "European Adventure";
        tour.setName(expectedName);

        assertEquals(expectedName, tour.getName());
    }

    @Test
    void testSetAndGetDescription() {
        String expectedDescription = "Amazing tour through Europe";
        tour.setDescription(expectedDescription);

        assertEquals(expectedDescription, tour.getDescription());
    }

    @Test
    void testSetAndGetType() {
        tour.setType(mockTourType);

        assertEquals(mockTourType, tour.getType());
    }

    @Test
    void testSetAndGetTransport() {
        tour.setTransport(mockTransport);

        assertEquals(mockTransport, tour.getTransport());
    }

    @Test
    void testSetAndGetMeal() {
        tour.setMeal(mockMeal);

        assertEquals(mockMeal, tour.getMeal());
    }

    @Test
    void testSetAndGetStartDate() {
        tour.setStartDate(startDate);

        assertEquals(startDate, tour.getStartDate());
    }

    @Test
    void testSetAndGetEndDate() {
        tour.setEndDate(endDate);

        assertEquals(endDate, tour.getEndDate());
    }

    @Test
    void testSetAndGetPrice() {
        double expectedPrice = 1500.50;
        tour.setPrice(expectedPrice);

        assertEquals(expectedPrice, tour.getPrice(), 0.001);
    }

    @Test
    void testSetAndGetLocations() {
        List<Location> locations = Arrays.asList(mockLocation1, mockLocation2);
        tour.setLocations(locations);

        assertEquals(locations, tour.getLocations());
        assertEquals(2, tour.getLocations().size());
    }

    @Test
    void testSetAndIsActive() {
        tour.setActive(false);
        assertFalse(tour.isActive());

        tour.setActive(true);
        assertTrue(tour.isActive());
    }

    @Test
    void testEqualsWithSameObject() {
        assertTrue(tour.equals(tour));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(tour.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertFalse(tour.equals("not a tour"));
    }

    @Test
    void testEqualsWithIdenticalTours() {
        Tour tour1 = createSampleTour();
        Tour tour2 = createSampleTour();

        assertTrue(tour1.equals(tour2));
        assertEquals(tour1.hashCode(), tour2.hashCode());
    }

    @Test
    void testEqualsWithDifferentIds() {
        Tour tour1 = createSampleTour();
        Tour tour2 = createSampleTour();
        tour2.setId(999);

        assertFalse(tour1.equals(tour2));
    }

    @Test
    void testEqualsWithDifferentNames() {
        Tour tour1 = createSampleTour();
        Tour tour2 = createSampleTour();
        tour2.setName("Different Name");

        assertFalse(tour1.equals(tour2));
    }

    @Test
    void testEqualsWithDifferentPrices() {
        Tour tour1 = createSampleTour();
        Tour tour2 = createSampleTour();
        tour2.setPrice(9999.99);

        assertFalse(tour1.equals(tour2));
    }

    @Test
    void testHashCodeConsistency() {
        Tour tour1 = createSampleTour();
        int hashCode1 = tour1.hashCode();
        int hashCode2 = tour1.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testToStringWithLocations() {
        when(mockTourType.getName()).thenReturn("Adventure Tour");
        when(mockLocation1.getName()).thenReturn("Paris");
        when(mockLocation1.getCountry()).thenReturn("France");
        when(mockLocation2.getName()).thenReturn("Rome");
        when(mockLocation2.getCountry()).thenReturn("Italy");

        tour.setId(1);
        tour.setName("Test Tour");
        tour.setStartDate(startDate);
        tour.setEndDate(endDate);
        tour.setType(mockTourType);
        tour.setPrice(1000.50);

        List<Location> locations = Arrays.asList(mockLocation1, mockLocation2);
        tour.setLocations(locations);

        String result = tour.toString();

        assertNotNull(result);
        assertTrue(result.contains("Tour ID: 1"));
        assertTrue(result.contains("Name: Test Tour"));
        assertTrue(result.contains("Paris, France"));
        assertTrue(result.contains("Rome, Italy"));
        assertTrue(result.contains("Adventure Tour"));
        assertTrue(result.contains("1000,50"));

        verify(mockLocation1, atLeastOnce()).getName();
        verify(mockLocation1, atLeastOnce()).getCountry();
        verify(mockLocation2, atLeastOnce()).getName();
        verify(mockLocation2, atLeastOnce()).getCountry();
        verify(mockTourType, atLeastOnce()).getName();
    }

    @Test
    void testToStringWithEmptyLocations() {
        tour.setId(1);
        tour.setName("Test Tour");
        tour.setStartDate(startDate);
        tour.setEndDate(endDate);
        tour.setPrice(1000.50);
        tour.setLocations(new ArrayList<>());

        String result = tour.toString();

        assertNotNull(result);
        assertTrue(result.contains("Tour ID: 1"));
        assertTrue(result.contains("Name: Test Tour"));
        assertTrue(result.contains("Type: -")); // null type should show as "-"
    }

    @Test
    void testToStringWithNullLocations() {
        tour.setId(1);
        tour.setName("Test Tour");
        tour.setStartDate(startDate);
        tour.setEndDate(endDate);
        tour.setPrice(1000.50);
        tour.setLocations(null);

        String result = tour.toString();

        assertNotNull(result);
        assertTrue(result.contains("Tour ID: 1"));
        assertTrue(result.contains("Name: Test Tour"));
    }

    private Tour createSampleTour() {
        Tour sampleTour = new Tour();
        sampleTour.setId(1);
        sampleTour.setName("Sample Tour");
        sampleTour.setDescription("Sample Description");
        sampleTour.setType(mockTourType);
        sampleTour.setTransport(mockTransport);
        sampleTour.setMeal(mockMeal);
        sampleTour.setStartDate(startDate);
        sampleTour.setEndDate(endDate);
        sampleTour.setPrice(1000.0);
        sampleTour.setLocations(Arrays.asList(mockLocation1));
        sampleTour.setActive(true);
        return sampleTour;
    }
}