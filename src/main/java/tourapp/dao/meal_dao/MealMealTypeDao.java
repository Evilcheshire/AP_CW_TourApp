package tourapp.dao.meal_dao;

import tourapp.dao.AbstractLinkDao;
import tourapp.model.meal.MealType;
import tourapp.util.ConnectionFactory;

import java.sql.SQLException;

public class MealMealTypeDao extends AbstractLinkDao<MealType> {

    public MealMealTypeDao(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    protected String getTableName() {
        return "meal_meal_types";
    }

    @Override
    protected String getId1Column() {
        return "meal_id";
    }

    @Override
    protected String getId2Column() {
        return "meal_type_id";
    }

    @Override
    protected MealType mapWithAdditionalData(int mealId, int mealTypeId) throws SQLException {
        return new MealType(mealTypeId, fetchMealTypeName(mealTypeId));
    }

    private String fetchMealTypeName(int id) throws SQLException {
        String sql = "SELECT name FROM meal_types WHERE id = ?";
        try (var conn = connectionFactory.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return null;
    }
}
