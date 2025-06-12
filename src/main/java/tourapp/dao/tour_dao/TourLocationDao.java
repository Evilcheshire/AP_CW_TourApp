package tourapp.dao.tour_dao;

import tourapp.dao.AbstractLinkDao;
import tourapp.dao.location_dao.LocationDao;
import tourapp.model.location.Location;
import tourapp.model.tour.TourLocation;
import tourapp.util.ConnectionFactory;

import java.sql.SQLException;

public class TourLocationDao extends AbstractLinkDao<TourLocation> {

    private final LocationDao locationDao;

    public TourLocationDao(ConnectionFactory connectionFactory, LocationDao locationDao) {
        super(connectionFactory);
        this.locationDao = locationDao;
    }

    @Override
    protected String getTableName() {
        return "tour_locations";
    }

    @Override
    protected String getId1Column() {
        return "tour_id";
    }

    @Override
    protected String getId2Column() {
        return "location_id";
    }

    @Override
    protected TourLocation mapWithAdditionalData(int tourId, int locationId) throws SQLException {
        Location location = locationDao.findById(locationId);
        return new TourLocation(tourId, location);
    }
}
