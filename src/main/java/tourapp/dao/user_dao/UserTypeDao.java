package tourapp.dao.user_dao;

import tourapp.dao.AbstractTypeDao;
import tourapp.model.user.UserType;
import tourapp.util.ConnectionFactory;

import java.util.Optional;

public class UserTypeDao extends AbstractTypeDao<UserType> {
    public UserTypeDao(ConnectionFactory connectionFactory) {
        super(
                connectionFactory,
                "user_types",
                rs -> new UserType(rs.getInt("id"), rs.getString("name")),
                UserType::getName,
                userType -> Optional.of(userType.getId()),
                UserType::setId
        );
    }

}
