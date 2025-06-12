package tourapp.service.link_service;

import tourapp.dao.AbstractLinkDao;

import java.sql.SQLException;
import java.util.List;

public abstract class AbstractLinkService<T> implements LinkService<T> {
    protected final AbstractLinkDao<T> dao;

    protected AbstractLinkService(AbstractLinkDao<T> dao) {
        this.dao = dao;
    }

    @Override
    public boolean createLink(int id1, int id2) throws SQLException {
        if (dao.existsLink(id1, id2)) {
            throw new IllegalArgumentException("Звʼязок уже існує.");
        }
        return dao.create(id1, id2);
    }

    @Override
    public boolean deleteLink(int id1, int id2) throws SQLException {
        return dao.delete(id1, id2);
    }

    @Override
    public boolean deleteAllById1(int id1) throws SQLException {
        return dao.deleteAllById1(id1);
    }

    @Override
    public boolean deleteAllById2(int id2) throws SQLException {
        return dao.deleteAllById2(id2);
    }

    @Override
    public boolean exists(int id1, int id2) throws SQLException {
        return dao.existsLink(id1, id2);
    }

    @Override
    public List<T> findAll() throws SQLException {
        return dao.findAllLinks();
    }

    @Override
    public List<T> findById1(int id1) throws SQLException {
        return dao.findById1(id1);
    }

    @Override
    public List<T> findById2(int id2) throws SQLException {
        return dao.findById2(id2);
    }
}
