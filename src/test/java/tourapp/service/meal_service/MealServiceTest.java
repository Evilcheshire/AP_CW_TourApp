package tourapp.service.meal_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tourapp.dao.meal_dao.MealDao;
import tourapp.model.meal.Meal;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock private MealDao mealDao;

    private MealService mealService;

    @BeforeEach
    void setUp() {
        mealService = new MealService(mealDao);
    }

    @Test
    void save_ShouldCallDaoSave() throws SQLException {
        // Given
        Meal meal = new Meal();

        // When
        mealService.create(meal);

        // Then
        verify(mealDao).create(meal);
    }

    @Test
    void update_ShouldCallDaoUpdate() throws SQLException {
        // Given
        Meal meal = new Meal();

        // When
        mealService.update(meal);

        // Then
        verify(mealDao).update(meal);
    }

    @Test
    void save_ShouldThrowException_WhenDaoThrows() throws SQLException {
        // Given
        Meal meal = new Meal();
        doThrow(new SQLException("Database error")).when(mealDao).create(meal);

        // When & Then
        assertThrows(SQLException.class, () -> mealService.create(meal));
        verify(mealDao).create(meal);
    }
}
