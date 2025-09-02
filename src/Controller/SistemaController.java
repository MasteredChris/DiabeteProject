package Controller;

import DAO.*;
import Model.Diabetologo;
import Model.Paziente;
import Model.Utente;

public class SistemaController {
    private UtenteDAO utenteDAO;
    private RilevazioneDAO rilevazioneDAO;
    private TerapiaDAO terapiaDAO;
    private AssunzioneFarmacoDAO assunzioneFarmacoDAO;
    private AlertDAO alertDAO;
    private Utente utenteCorrente;

    public SistemaController() {
        this.utenteDAO = new UtenteDAO();
        this.rilevazioneDAO = new RilevazioneDAO();
        this.terapiaDAO = new TerapiaDAO();
        this.assunzioneFarmacoDAO = new AssunzioneFarmacoDAO();
        this.alertDAO = new AlertDAO();
        this.utenteCorrente = null;
    }

    // METODO LOGIN
    public boolean login(String username, String password) {
        try {
            // 1. Cerca l'utente nel database
            Utente utente = utenteDAO.findByUsername(username);

            if (utente == null) {
                System.out.println("Username non trovato");
                return false;
            }

            // 2. Verifica la password
            if (!utente.getPassword().equals(password)) {
                System.out.println("Password errata");
                return false;
            }

            // 3. Login riuscito
            this.utenteCorrente = utente;
            System.out.println("Login riuscito per: " + utente.getNome() + " " + utente.getCognome());

            return true;

        } catch (Exception e) {
            System.err.println("Errore durante il login: " + e.getMessage());
            return false;
        }
    }

    // METODO LOGOUT
    public void logout() {
        if (utenteCorrente != null) {
            System.out.println("Logout di: " + utenteCorrente.getNome());
            this.utenteCorrente = null;
        }
    }

    // VERIFICA SE UTENTE È LOGGATO
    public boolean isLogged() {
        return this.utenteCorrente != null;
    }

    // VERIFICA TIPO UTENTE
    public boolean isPaziente() {
        return isLogged() && utenteCorrente.getUserType().equals("PAZIENTE");
    }

    public boolean isMedico() {
        return isLogged() && utenteCorrente.getUserType().equals("MEDICO");
    }

    public Utente getUtenteCorrente(){
        return this.utenteCorrente;
    }
    // REGISTRAZIONE NUOVO PAZIENTE
    public boolean registraPaziente(Paziente paziente) {
        try {
            // 1. Verifica che non esista già
            if (utenteDAO.findByUsername(paziente.getUsername()) != null) {
                System.out.println("Username già esistente");
                return false;
            }

            // 2. Valida i dati
            if (!validaPaziente(paziente)) {
                return false;
            }

            // 3. Salva nel database
            utenteDAO.save(paziente);
            System.out.println("Paziente registrato: " + paziente.getNome() + " " + paziente.getCognome());

            return true;

        } catch (Exception e) {
            System.err.println("Errore registrazione paziente: " + e.getMessage());
            return false;
        }
    }

    // REGISTRAZIONE NUOVO MEDICO
    public boolean registraMedico(Diabetologo medico) {
        try {
            if (utenteDAO.findByUsername(medico.getUsername()) != null) {
                return false;
            }

            if (!validaMedico(medico)) {
                return false;
            }

            utenteDAO.save(medico);
            System.out.println("Medico registrato: " + medico.getNome() + " " + medico.getCognome());

            return true;

        } catch (Exception e) {
            System.err.println("Errore registrazione medico: " + e.getMessage());
            return false;
        }
    }

    // METODI DI VALIDAZIONE PRIVATI
    private boolean validaPaziente(Utente paziente) {
        return paziente instanceof Paziente;
    }

    private boolean validaMedico(Utente medico) {
        return medico instanceof Diabetologo;
    }
    public UtenteDAO getUtenteDAO() { return utenteDAO; }
    public RilevazioneDAO getRilevazioneDAO() { return rilevazioneDAO; }
    public TerapiaDAO getTerapiaDAO() { return terapiaDAO; }
    public AssunzioneFarmacoDAO getAssunzioneFarmacoDAO() { return assunzioneFarmacoDAO; }
    public AlertDAO getAlertDAO() { return alertDAO; }
}
