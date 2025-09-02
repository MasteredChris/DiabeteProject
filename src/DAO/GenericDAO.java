package DAO;

import java.util.List;

public interface GenericDAO<T> {
    public void save(T entity);
    public T findById(String id);
    public List<T> findAll();
    public void update(T entity);
    public void delete(String id);
}
