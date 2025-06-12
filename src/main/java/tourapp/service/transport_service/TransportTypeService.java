package tourapp.service.transport_service;

import tourapp.dao.transport_dao.TransportTypeDao;
import tourapp.model.transport.TransportType;
import tourapp.service.type_service.AbstractTypeService;

public class TransportTypeService extends AbstractTypeService<TransportType> {
    public TransportTypeService(TransportTypeDao dao){
        super(dao);
    }
}
