package tourapp.service.tour_service;
import tourapp.dao.tour_dao.TourDao;
import tourapp.model.location.Location;
import tourapp.model.tour.Tour;
import tourapp.service.AbstractGenericService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TourService extends AbstractGenericService<Tour> {
    private final TourDao tourDao;

    public TourService(TourDao tourDao) {
        super(tourDao);
        this.tourDao = tourDao;
    }

    public Tour getByIdWithDependencies(int id) throws SQLException {
        return tourDao.findByIdWithDependencies(id);
    }

    public boolean toggleActiveStatus(int tourId, boolean active) throws SQLException {
        Tour tour = getByIdWithDependencies(tourId);
        if (tour == null) {
            return false;
        }

        tour.setActive(active);
        update(tour);
        return true;
    }

    public List<Tour> findActiveTours() throws SQLException {
        return search(Map.of("isActive", true));
    }

    public List<Tour> findInactiveTours() throws SQLException {
        return search(Map.of("isActive", false));
    }


    public List<Location> getLocationsForTour(int tourId) throws SQLException {
        Tour tour = getByIdWithDependencies(tourId);
        if (tour != null && tour.getLocations() != null) {
            return tour.getLocations();
        }
        return List.of();
    }
}