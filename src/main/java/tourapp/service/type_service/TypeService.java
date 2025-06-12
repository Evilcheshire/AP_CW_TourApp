package tourapp.service.type_service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TypeService<T> {
    List<T> getAll() throws SQLException;

    T getById(int id) throws SQLException;

    boolean create(T type) throws SQLException;

    void update(int id, T type) throws SQLException;

    boolean delete(int id) throws SQLException;

    List<T> searchByName(String name) throws SQLException;

    T findByExactName(String name) throws SQLException;
}