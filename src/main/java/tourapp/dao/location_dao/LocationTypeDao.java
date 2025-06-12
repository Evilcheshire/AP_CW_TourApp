package tourapp.dao.location_dao;

import tourapp.dao.AbstractTypeDao;
import tourapp.model.location.LocationType;
import tourapp.util.ConnectionFactory;

import java.util.Optional;

public class LocationTypeDao extends AbstractTypeDao<LocationType> {
    public LocationTypeDao(ConnectionFactory connectionFactory) {
        super(
                connectionFactory,
                "location_types",
                rs -> new LocationType(rs.getInt("id"), rs.getString("name")),
                LocationType::getName,
                locationType -> Optional.of(locationType.getId()),
                LocationType::setId
        );
    }
}
