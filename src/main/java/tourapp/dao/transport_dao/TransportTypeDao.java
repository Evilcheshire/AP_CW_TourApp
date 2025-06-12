package tourapp.dao.transport_dao;

import tourapp.dao.AbstractTypeDao;
import tourapp.util.ConnectionFactory;
import tourapp.model.transport.TransportType;

import java.util.Optional;

public class TransportTypeDao extends AbstractTypeDao<TransportType> {
    public TransportTypeDao(ConnectionFactory connectionFactory) {
        super(
                connectionFactory,
                "transport_types",
                rs -> new TransportType(rs.getInt("id"), rs.getString("name")),
                TransportType::getName,
                transportType -> Optional.of(transportType.getId()),
                TransportType::setId
        );
    }
}
