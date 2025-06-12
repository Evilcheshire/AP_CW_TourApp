package tourapp.service.location_service;

import tourapp.dao.location_dao.LocationTypeDao;
import tourapp.model.location.LocationType;
import tourapp.service.type_service.AbstractTypeService;

public class LocationTypeService extends AbstractTypeService<LocationType> {
    public LocationTypeService(LocationTypeDao dao) {
        super(dao);
    }
}
