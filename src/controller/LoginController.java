package controller;

import model.FilePathProvider;
import model.Utente;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller responsabile dell'autenticazione degli utenti e dell'inizializzazione
 * del sistema con il caricamento di tutti i dati.
 */
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());


    private FilePathProvider filePaths= new FilePathProvider();
    private final List<Utente> utenti;
    private final DataController dataController;

    /**
     * Inizializza il controller caricando tutti i dati del sistema.
     * L'ordine di caricamento è importante: prima gli utenti, poi i loro dati associati.
     */
    public LoginController() {
        LOGGER.info("Inizializzazione LoginController...");

        this.dataController = new DataController();

        // Carica utenti (base del sistema)
        this.utenti = dataController.caricaUtenti(filePaths.getUtenti());

        if (utenti.isEmpty()) {
            LOGGER.warning("Nessun utente caricato. Verificare il file utenti.csv");
            return;
        }

        // Carica tutti i dati associati ai pazienti
        caricaDatiPazienti();

        LOGGER.info("Inizializzazione LoginController completata");
    }

    /**
     * Carica tutti i tipi di dati associati ai pazienti.
     * Questo metodo centralizza il caricamento per una migliore gestione degli errori.
     */
    private void caricaDatiPazienti() {
        try {
            LOGGER.info("Caricamento dati pazienti in corso...");

            // Carica dati clinici
            dataController.caricaRilevazioni(filePaths.getRilevazioniFile(), utenti);
            dataController.caricaTerapie(filePaths.getTerapieFile(), utenti);
            dataController.caricaAssunzioni(filePaths.getAssunzioniFile(), utenti);
            dataController.caricaSchedeCliniche(filePaths.getSchedeClinicheFile(), utenti);
            dataController.caricaEventiClinici(filePaths.getEventiCliniciFile(), utenti);
            dataController.caricaTerapieConcomitanti(filePaths.getTerapieConcomitantiFile(), utenti);

            LOGGER.info("Caricamento dati pazienti completato");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento dei dati pazienti", e);
            throw new RuntimeException("Errore critico nell'inizializzazione dei dati", e);
        }
    }

    /**
     * Autentica un utente con email e password.
     *
     * @param email Email dell'utente (case-insensitive)
     * @param password Password dell'utente (case-sensitive)
     * @return L'utente autenticato, null se le credenziali non sono valide
     */
    public Utente login(String email, String password) {
        if (email == null || password == null) {
            LOGGER.warning("Tentativo di login con credenziali null");
            return null;
        }

        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            LOGGER.warning("Tentativo di login con credenziali vuote");
            return null;
        }

        String emailNormalized = email.trim().toLowerCase();

        for (Utente utente : utenti) {
            if (utente.getEmail().toLowerCase().equals(emailNormalized) &&
                    utente.getPassword().equals(password)) {

                LOGGER.log(Level.INFO, "Login successful per utente: {0} (ID: {1})",
                        new Object[]{utente.getEmail(), utente.getId()});
                return utente;
            }
        }

        LOGGER.log(Level.WARNING, "Login fallito per email: {0}", emailNormalized);
        return null;
    }

    /**
     * Verifica se un utente con la data email esiste nel sistema.
     *
     * @param email Email da verificare
     * @return true se l'utente esiste, false altrimenti
     */
    public boolean esisteUtente(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailNormalized = email.trim().toLowerCase();
        return utenti.stream()
                .anyMatch(u -> u.getEmail().toLowerCase().equals(emailNormalized));
    }

    /**
     * Ottiene statistiche di base sul sistema.
     *
     * @return Stringa con informazioni statistiche
     */
    public String getStatisticheSistema() {
        long pazienti = utenti.stream().filter(u -> u.getClass().getSimpleName().equals("Paziente")).count();
        long diabetologi = utenti.stream().filter(u -> u.getClass().getSimpleName().equals("Diabetologo")).count();

        return String.format("Sistema caricato con %d utenti totali (%d pazienti, %d diabetologi)",
                utenti.size(), pazienti, diabetologi);
    }

    // ============ GETTERS ============

    /**
     * Restituisce la lista degli utenti caricati.
     *
     * @return Lista immutabile degli utenti
     */
    public List<Utente> getUtenti() {
        return List.copyOf(utenti); // Restituisce copia immutabile per sicurezza
    }

    /**
     * Restituisce il controller per la gestione dei dati.
     *
     * @return Il DataController associato
     */
    public DataController getDataController() {
        return dataController;
    }
}