package DAO;

import Model.AssunzioneFarmaco;

import java.time.LocalDate;
import java.util.List;

public class AssunzioneFarmacoDAO implements GenericDAO<AssunzioneFarmaco>{
    @Override
    public void save(AssunzioneFarmaco entity) {

    }

    @Override
    public AssunzioneFarmaco findById(String id) {
        return null;
    }

    @Override
    public List<AssunzioneFarmaco> findAll() {
        return List.of();
    }

    @Override
    public void update(AssunzioneFarmaco entity) {

    }

    @Override
    public void delete(String id) {

    }

    public List<AssunzioneFarmaco> findByPaziente(String idPaziente){

    }
    public List<AssunzioneFarmaco> findByTerapia(String idTerapia){

    }
    public List<AssunzioneFarmaco> findAssunzioniMancanti(String idPaziente, LocalDate data){

    }
}
