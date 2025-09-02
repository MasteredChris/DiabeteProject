package DAO;

import Model.Terapia;

import java.util.List;

public class TerapiaDAO implements GenericDAO<Terapia>{
    @Override
    public void save(Terapia entity) {

    }

    @Override
    public Terapia findById(String id) {
        return null;
    }

    @Override
    public List<Terapia> findAll() {
        return List.of();
    }

    @Override
    public void update(Terapia entity) {

    }

    @Override
    public void delete(String id) {

    }

    public List<Terapia> findByPaziente(String idPaziente){

    }
    public List<Terapia> findTerapieAttive(String idPaziente){

    }
    public Terapia findTerapiaAttivaPaziente(String idPaziente, String farmaco){

    }
}
