package tourapp.service.user_service;

import tourapp.dao.user_dao.UserTypeDao;
import tourapp.model.user.UserType;
import tourapp.service.type_service.AbstractTypeService;

public class UserTypeService extends AbstractTypeService<UserType> {
    public UserTypeService(UserTypeDao dao){
        super(dao);
    }
}
