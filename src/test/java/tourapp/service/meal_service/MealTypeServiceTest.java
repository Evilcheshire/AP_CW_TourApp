package tourapp.service.meal_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.meal_dao.MealTypeDao;
import tourapp.model.meal.MealType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealTypeServiceTest {

    @Mock private MealTypeDao mealTypeDao;
    @Mock private Function<MealType, String> mockNameExtractor;

    private MealTypeService mealTypeService;

    @BeforeEach
    void setUp() {
        mealTypeService = new MealTypeService(mealTypeDao);
        lenient().when(mealTypeDao.getNameExtractor()).thenReturn(mockNameExtractor);
    }

    @Test
    void getAll_ShouldReturnAllMealTypes() throws SQLException {
        // Given
        List<MealType> expectedTypes = Arrays.asList(new MealType(), new MealType());
        when(mealTypeDao.findAll()).thenReturn(expectedTypes);

        // When
        List<MealType> result = mealTypeService.getAll();

        // Then
        assertEquals(expectedTypes, result);
        verify(mealTypeDao).findAll();
    }

    @Test
    void create_ShouldCreateMealTypeSuccessfully() throws SQLException {
        // Given
        MealType mealType = new MealType();
        when(mockNameExtractor.apply(mealType)).thenReturn("Breakfast");
        when(mealTypeDao.existsWithName("Breakfast")).thenReturn(false);
        when(mealTypeDao.create(mealType)).thenReturn(true);

        // When
        boolean result = mealTypeService.create(mealType);

        // Then
        assertTrue(result);
        verify(mealTypeDao).create(mealType);
    }
}
