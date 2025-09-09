package model;

import java.util.ArrayList;
import java.util.List;

public class Paziente extends Utente {
    private int medicoId;
    private Diabetologo medico;
    private List<Rilevazione> rilevazioni;
    private List<Terapia> terapie = new ArrayList<>();
    private List<Assunzione> assunzioni = new ArrayList<>();
    private SchedaClinica schedaClinica = new SchedaClinica();
    private List<EventoClinico> eventiClinici = new ArrayList<>();
    private List<TerapiaConcomitante> terapieConcomitanti = new ArrayList<>();


    public Paziente(int id, String nome, String cognome, String email, String password, int medicoId) {
        super(id, nome, cognome, email, password);
        this.medicoId = medicoId;
        this.rilevazioni = new ArrayList<>();
    }

    public int getMedicoId() { return medicoId; }
    public Diabetologo getMedico() { return medico; }
    public void setMedico(Diabetologo medico) { this.medico = medico; }
    public List<Terapia> getTerapie() {return terapie;}
    public List<Assunzione> getAssunzioni() {return assunzioni;}

    public List<Rilevazione> getRilevazioni() { return rilevazioni; }

    public SchedaClinica getSchedaClinica() {
        return schedaClinica;
    }

    public void setSchedaClinica(SchedaClinica schedaClinica) {
        this.schedaClinica = schedaClinica;
    }

    public List<EventoClinico> getEventiClinici() {
        return eventiClinici;
    }

    public void aggiungiEventoClinico(EventoClinico e) {
        eventiClinici.add(e);
    }

    public List<TerapiaConcomitante> getTerapieConcomitanti() {
        return terapieConcomitanti;
    }

    public void aggiungiTerapiaConcomitante(TerapiaConcomitante t) {
        terapieConcomitanti.add(t);
    }

    public void aggiungiRilevazione(Rilevazione r) {
        rilevazioni.add(r);
    }

    public void aggiungiTerapia(Terapia t) {
        terapie.add(t);
    }

    public void aggiungiAssunzione(Assunzione a) {
        assunzioni.add(a);
    }

    @Override
    public String getType() {
        return "Paziente";
    }
    @Override
    public String toString() {
        return "(id: "+getId()+") "+getNome() + " " + getCognome();
    }

}
