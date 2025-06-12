package tourapp.dao.meal_dao;

import tourapp.dao.AbstractTypeDao;
import tourapp.model.meal.MealType;
import tourapp.util.ConnectionFactory;

import java.util.Optional;

public class MealTypeDao extends AbstractTypeDao<MealType> {
    public MealTypeDao(ConnectionFactory connectionFactory) {
        super(
                connectionFactory,
                "meal_types",
                rs -> new MealType(rs.getInt("id"), rs.getString("name")),
                MealType::getName,
                mealType -> Optional.of(mealType.getId()),
                MealType::setId
        );
    }
}
