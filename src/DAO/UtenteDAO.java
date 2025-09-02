package DAO;

import Model.Diabetologo;
import Model.Paziente;
import Model.Utente;

import java.util.List;

public class UtenteDAO implements GenericDAO<Utente> {
    @Override
    public void save(Utente entity) {

    }

    @Override
    public Utente findById(String id) {
        return null;
    }

    @Override
    public List<Utente> findAll() {
        return List.of();
    }

    @Override
    public void update(Utente entity) {

    }

    @Override
    public void delete(String id) {

    }

    public Utente findByUsername(String username){

    }
    public <Paziente> findAllPazienti(){

    }
    public List<Diabetologo> findAllMedici(){

    }
    public Paziente findPazienteByMedico(String idMedico){

    }
}
