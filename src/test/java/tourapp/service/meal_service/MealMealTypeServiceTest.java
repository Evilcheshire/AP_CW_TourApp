package tourapp.service.meal_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.meal_dao.MealMealTypeDao;
import tourapp.model.meal.MealType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealMealTypeServiceTest {

    @Mock
    private MealMealTypeDao mealMealTypeDao;

    private MealMealTypeService mealMealTypeService;

    @BeforeEach
    void setUp() {
        mealMealTypeService = new MealMealTypeService(mealMealTypeDao);
    }

    @Test
    void createLink_ShouldCreateLinkSuccessfully() throws SQLException {
        // Given
        when(mealMealTypeDao.existsLink(1, 2)).thenReturn(false);
        when(mealMealTypeDao.create(1, 2)).thenReturn(true);

        // When
        boolean result = mealMealTypeService.createLink(1, 2);

        // Then
        assertTrue(result);
        verify(mealMealTypeDao).existsLink(1, 2);
        verify(mealMealTypeDao).create(1, 2);
    }

    @Test
    void findById1_ShouldReturnMealTypes() throws SQLException {
        // Given
        List<MealType> expectedMealTypes = Arrays.asList(new MealType(), new MealType());
        when(mealMealTypeDao.findById1(1)).thenReturn(expectedMealTypes);

        // When
        List<MealType> result = mealMealTypeService.findById1(1);

        // Then
        assertEquals(expectedMealTypes, result);
        verify(mealMealTypeDao).findById1(1);
    }
}
