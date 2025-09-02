package DAO;

import Model.Alert;

import java.util.List;

public class AlertDAO implements GenericDAO<Alert>{
    @Override
    public void save(Alert entity) {

    }

    @Override
    public Alert findById(String id) {
        return null;
    }

    @Override
    public List<Alert> findAll() {
        return List.of();
    }

    @Override
    public void update(Alert entity) {

    }

    @Override
    public void delete(String id) {

    }

    public List<Alert> findByMedico(String idMedico){

    }
    public List<Alert> findNonVisualizzati(){

    }
    public List<Alert> findByPaziente(String idPaziente){

    }
}
