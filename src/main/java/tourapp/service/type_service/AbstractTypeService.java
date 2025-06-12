package tourapp.service.type_service;

import tourapp.dao.AbstractTypeDao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTypeService<T> implements TypeService<T> {
    protected final AbstractTypeDao<T> dao;

    protected AbstractTypeService(AbstractTypeDao<T> dao) {
        this.dao = dao;
    }

    @Override
    public List<T> getAll() throws SQLException {
        return dao.findAll();
    }

    @Override
    public T getById(int id) throws SQLException {
        return dao.findById(id);
    }

    @Override
    public boolean create(T type) throws SQLException {
        String name = dao.getNameExtractor().apply(type);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва не може бути порожньою.");
        }

        if (dao.existsWithName(name)) {
            throw new IllegalArgumentException("Об'єкт з такою назвою вже існує.");
        }

        return dao.create(type);
    }

    @Override
    public void update(int id, T type) throws SQLException {
        String name = dao.getNameExtractor().apply(type);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва не може бути порожньою.");
        }

        dao.update(type, id);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return dao.delete(id);
    }

    @Override
    public List<T> searchByName(String name) throws SQLException {
        return dao.findByName(name);
    }

    @Override
    public T findByExactName(String name) throws SQLException {
        return dao.findByExactName(name);
    }
}