package tourapp.service.tour_service;

import tourapp.dao.tour_dao.TourTypeDao;
import tourapp.model.tour.TourType;
import tourapp.service.type_service.AbstractTypeService;

public class TourTypeService extends AbstractTypeService<TourType> {
    public TourTypeService(TourTypeDao dao){
        super(dao);
    }
}
