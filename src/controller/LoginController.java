package controller;

import model.Utente;

import java.util.List;

public class LoginController {

    private List<Utente> utenti;
    private DataController dataController;
    private final String utentiFile = "src/resources/utenti.csv";
    private final String rilevazioniFile = "src/resources/rilevazioni.csv";
    private final String terapieFile = "src/resources/terapie.csv";
    private final String assunzioniFile = "src/resources/assunzioni.csv";
    private final String schedeFile = "src/resources/schede_cliniche.csv";

    public LoginController() {
        dataController = new DataController();

        // Carica utenti
        utenti = dataController.caricaUtenti(utentiFile);

        // Carica rilevazioni e associa ai pazienti
        dataController.caricaRilevazioni(rilevazioniFile, utenti);

        dataController.caricaTerapie(terapieFile, utenti);
        dataController.caricaAssunzioni(assunzioniFile,utenti);
        dataController.caricaSchedeCliniche(schedeFile,utenti);
    }

    /**
     * Effettua il login di un utente tramite email e password
     * @param email email inserita
     * @param password password inserita
     * @return utente loggato oppure null se non trovato
     */
    public Utente login(String email, String password) {
        for (Utente u : utenti) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Restituisce la lista di tutti gli utenti (utile per dashboard diabetologo)
     */
    public List<Utente> getUtenti() {
        return utenti;
    }

    /**
     * Restituisce il DataController per leggere/scrivere rilevazioni
     */
    public DataController getDataController() {
        return dataController;
    }
}
