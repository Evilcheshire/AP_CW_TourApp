package tourapp.service.meal_service;

import tourapp.dao.meal_dao.MealTypeDao;
import tourapp.model.meal.MealType;
import tourapp.service.type_service.AbstractTypeService;

public class MealTypeService extends AbstractTypeService<MealType> {
    public MealTypeService(MealTypeDao dao) {
        super(dao);
    }
}
