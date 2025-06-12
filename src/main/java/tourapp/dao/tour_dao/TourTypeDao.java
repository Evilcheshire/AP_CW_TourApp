package tourapp.dao.tour_dao;

import tourapp.dao.AbstractTypeDao;
import tourapp.model.tour.TourType;
import tourapp.util.ConnectionFactory;

import java.util.Optional;

public class TourTypeDao extends AbstractTypeDao<TourType> {
    public TourTypeDao(ConnectionFactory connectionFactory) {
        super(
                connectionFactory,
                "tour_types",
                rs -> new TourType(rs.getInt("id"), rs.getString("name")),
                TourType::getName,
                tourType -> Optional.of(tourType.getId()),
                TourType::setId
        );
    }
}
