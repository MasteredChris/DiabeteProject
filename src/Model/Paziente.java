package Model;

import Controller.MedicoController;
import Controller.PazienteController;

import java.time.LocalDate;
import java.util.List;

public class Paziente extends Utente {

    private String medicoRiferimento; //id del medico
    private List<String> fattoriRischio;;
    private List<String> patologiePregresse;
    private List<String> comorbidita;
    private LocalDate dataRegistrazione;

    private PazienteController pazienteController;

    public Paziente(String id, String username, String password, String nome, String cognome, String email, List<String> fattoriRichio, List<String> patologiePregresse, List<String> comorbidita, LocalDate dataRegistrazione) {
        super(id, username, password, nome, cognome, email);
        this.fattoriRischio = fattoriRischio;
        this.patologiePregresse = patologiePregresse;
        this.comorbidita = comorbidita;
        this.dataRegistrazione = dataRegistrazione;
    }

    // Setter per il controller (viene impostato dopo il login)
    public void setPazienteController(PazienteController pazienteController) {
        this.pazienteController = pazienteController;
    }

    @Override
    public String getUserType() {
        return "PAZIENTE";
    }

    public String getMedicoRiferimento() {
        return medicoRiferimento;
    }
    public void setMedicoRiferimento(String medicoRiferimento) {
        this.medicoRiferimento = medicoRiferimento;
    }
    public List<String> getFattoriRischio() {
        return fattoriRischio;
    }
    public void setFattoriRischio(List<String> fattoriRischio) {
        this.fattoriRischio = fattoriRischio;
    }
    public List<String> getPatologiePregresse() {
        return patologiePregresse;
    }
    public void setPatologiePregrese(List<String> patologiePregresse) {
        this.patologiePregresse = patologiePregresse;
    }
    public List<String> getComorbidita() {
        return comorbidita;
    }
    public void setComorbidita(List<String> comorbidita) {
        this.comorbidita = comorbidita;
    }
    public LocalDate getDataRegistrazione() {
        return dataRegistrazione;
    }
    public void setDataRegistrazione(LocalDate dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }


    public void aggiungiRilevazione(Rilevazione rilevazione) {
        // Questo metodo è un wrapper che:
        // 1. Valida che la rilevazione appartenga al paziente
        // 2. Chiama il DAO per salvare la rilevazione
        // 3. Può triggerare controlli per alert automatici
    }
    public void aggiungiAssunzione(AssunzioneFarmaco assunzione) {
        // Questo metodo:
        // 1. Verifica che l'assunzione sia coerente con le terapie prescritte
        // 2. Salva l'assunzione tramite DAO
        // 3. Può aggiornare lo stato di completamento della terapia
    }
}
