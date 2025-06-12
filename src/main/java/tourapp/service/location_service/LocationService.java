package tourapp.service.location_service;

import tourapp.dao.location_dao.LocationDao;
import tourapp.model.location.Location;
import tourapp.service.AbstractGenericService;

public class LocationService extends AbstractGenericService<Location> {
    public LocationService(LocationDao locationDao) { super(locationDao); }
}
