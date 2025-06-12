package tourapp.service.tour_service;

import tourapp.dao.tour_dao.TourLocationDao;
import tourapp.model.tour.TourLocation;
import tourapp.service.link_service.AbstractLinkService;

public class TourLocationService extends AbstractLinkService<TourLocation> {
    public TourLocationService(TourLocationDao tourLocationDao) {
        super(tourLocationDao);
    }
}
