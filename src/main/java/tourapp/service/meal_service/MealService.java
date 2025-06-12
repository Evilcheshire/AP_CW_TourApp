package tourapp.service.meal_service;

import tourapp.dao.meal_dao.MealDao;
import tourapp.model.meal.Meal;
import tourapp.service.AbstractGenericService;

public class MealService extends AbstractGenericService<Meal> {
    public MealService(MealDao mealDao) { super(mealDao); }
}
