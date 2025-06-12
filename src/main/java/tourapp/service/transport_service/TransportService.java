package tourapp.service.transport_service;

import tourapp.dao.transport_dao.TransportDao;
import tourapp.model.transport.Transport;
import tourapp.service.AbstractGenericService;

public class TransportService extends AbstractGenericService<Transport> {
    public TransportService(TransportDao transportDao) { super(transportDao); }
}