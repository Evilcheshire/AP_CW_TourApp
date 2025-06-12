package tourapp.service.meal_service;

import tourapp.dao.meal_dao.MealMealTypeDao;
import tourapp.model.meal.MealType;
import tourapp.service.link_service.AbstractLinkService;

public class MealMealTypeService extends AbstractLinkService<MealType> {
    public MealMealTypeService(MealMealTypeDao dao) {
        super(dao);
    }
}