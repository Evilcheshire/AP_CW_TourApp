package tourapp.service;

import tourapp.dao.AbstractGenericDao;
import tourapp.model.location.Location;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractGenericService<T> {
    protected final AbstractGenericDao<T> dao;

    protected AbstractGenericService(AbstractGenericDao<T> dao) {
        this.dao = dao;
    }

    public List<T> getAll() throws SQLException {
        return dao.findAll();
    }

    public T getById(int id) throws SQLException {
        return dao.findById(id);
    }

    public boolean create(T entity) throws SQLException {
        return dao.create(entity);
    }

    public boolean update(T entity) throws SQLException {
        return dao.update(entity);
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id);
    }

    public List<T> search(Map<String, Object> searchParams) throws SQLException {
        return dao.search(searchParams);
    }
}

