package tourapp.dao.meal_dao;

import tourapp.dao.AbstractGenericDao;
import tourapp.model.meal.Meal;
import tourapp.model.meal.MealType;
import tourapp.util.ConnectionFactory;
import tourapp.util.DaoUtils.JoinInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealDao extends AbstractGenericDao<Meal> {

    private final MealMealTypeDao mealTypeLinkDao;

    public MealDao(ConnectionFactory connectionFactory) {
        super(connectionFactory, "meals", rs -> new Meal(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("meals_per_day"),
                rs.getDouble("cost_per_day")
        ));
        this.mealTypeLinkDao = new MealMealTypeDao(connectionFactory);
    }

    @Override
    public String getBaseAlias() {
        return "m";
    }

    @Override
    public List<JoinInfo> initJoinInfos() {
        List<JoinInfo> joins = new ArrayList<>();

        joins.add(new JoinInfo(
                "LEFT JOIN", "meal_meal_types", "mmt",
                getBaseAlias() + ".id = mmt.meal_id",
                "meal_types"));

        joins.add(new JoinInfo(
                "LEFT JOIN", "meal_types", "mt",
                "mmt.meal_type_id = mt.id",
                "meal_type"));

        return joins;
    }

    @Override
    public Map<String, String> initColumnMappings() {
        Map<String, String> columnMappings = new HashMap<>();
        String baseAlias = getBaseAlias();
        columnMappings.put("id", baseAlias + ".id");
        columnMappings.put("name", baseAlias + ".name");
        columnMappings.put("mealsPerDay", baseAlias + ".meals_per_day");
        columnMappings.put("minCostPerDay", baseAlias + ".cost_per_day");
        columnMappings.put("maxCostPerDay", baseAlias + ".cost_per_day");
        columnMappings.put("mealType", "mt.name");
        return columnMappings;
    }

    @Override
    public Meal findById(int id) throws SQLException {
        Meal meal = super.findById(id);
        if (meal != null) {
            meal.setMealTypes(mealTypeLinkDao.findById1(id));
        }
        return meal;
    }

    @Override
    public List<Meal> findAll() throws SQLException {
        List<Meal> meals = super.findAll();
        for (Meal meal : meals) {
            meal.setMealTypes(mealTypeLinkDao.findById1(meal.getId()));
        }
        return meals;
    }

    public boolean create(Meal meal) throws SQLException {
        String sql = "INSERT INTO meals (name, meals_per_day, cost_per_day) VALUES (?, ?, ?)";

        try (var conn = connectionFactory.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, meal.getName());
            stmt.setInt(2, meal.getMealsPerDay());
            stmt.setDouble(3, meal.getCostPerDay());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return false;

            try (var keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    meal.setId(keys.getInt(1));
                }
            }

            for (MealType type : meal.getMealTypes()) {
                mealTypeLinkDao.create(meal.getId(), type.getId());
            }

            return true;
        }
    }

    public boolean update(Meal meal) throws SQLException {
        String sql = "UPDATE meals SET name = ?, meals_per_day = ?, cost_per_day = ? WHERE id = ?";

        try (var conn = connectionFactory.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, meal.getName());
            stmt.setInt(2, meal.getMealsPerDay());
            stmt.setDouble(3, meal.getCostPerDay());
            stmt.setInt(4, meal.getId());

            mealTypeLinkDao.deleteAllById1(meal.getId());

            for (MealType type : meal.getMealTypes()) {
                mealTypeLinkDao.create(meal.getId(), type.getId());
            }

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        return super.delete(id) && mealTypeLinkDao.deleteAllById1(id);
    }
}