package DAO;

import Model.Rilevazione;

import java.time.LocalDate;
import java.util.List;

public class RilevazioneDAO implements GenericDAO<Rilevazione>{
    @Override
    public void save(Rilevazione entity) {

    }

    @Override
    public Rilevazione findById(String id) {
        return null;
    }

    @Override
    public List<Rilevazione> findAll() {
        return List.of();
    }

    @Override
    public void update(Rilevazione entity) {

    }

    @Override
    public void delete(String id) {

    }
    public List<Rilevazione> findByPaziente(String idPaziente){

    }
    public List<Rilevazione> findByPazienteAndPeriodo(String idPaziente, LocalDate inizio, LocalDate fine){

    }
    public List<Rilevazione> findRilevazioniAnomale(){

    }
}
