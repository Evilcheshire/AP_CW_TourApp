package tourapp.service.link_service;

import java.sql.SQLException;
import java.util.List;

public interface LinkService<T> {
    boolean createLink(int id1, int id2) throws SQLException;

    boolean deleteLink(int id1, int id2) throws SQLException;

    boolean deleteAllById1(int id1) throws SQLException;

    boolean deleteAllById2(int id2) throws SQLException;

    boolean exists(int id1, int id2) throws SQLException;

    List<T> findAll() throws SQLException;

    List<T> findById1(int id1) throws SQLException;

    List<T> findById2(int id2) throws SQLException;
}

