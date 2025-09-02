package Controller;

import Model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PazienteController {
    private SistemaController sistema;

    public PazienteController(SistemaController sistema) {
        this.sistema = sistema;
    }

    public boolean aggiungiRilevazione(int glicemia, TipoRilevazione tipo, List<String> sintomi, String note) {
        try {
            // 1. Verifica che sia loggato un paziente
            if (!sistema.isPaziente()) {
                System.out.println("Solo i pazienti possono aggiungere rilevazioni");
                return false;
            }

            Paziente paziente = (Paziente) sistema.getUtenteCorrente();

            // 2. Valida i dati
            if (glicemia < 20 || glicemia > 600) {
                System.out.println("Valore glicemia non valido (20-600 mg/dL)");
                return false;
            }

            // 3. Crea la rilevazione
            Rilevazione rilevazione = new Rilevazione(
                    "RIL_" + System.currentTimeMillis(),
                    paziente.getId(),
                    LocalDateTime.now(),
                    glicemia,
                    tipo,
                    note != null ? note : "",
                    sintomi != null ? sintomi : new ArrayList<>()
            );

            // 4. Salva nel database
            sistema.getRilevazioneDAO().save(rilevazione);

            // 5. Controlla se serve generare alert
            verificaAlert(rilevazione);

            System.out.println("Rilevazione registrata: " + glicemia + " mg/dL");
            return true;

        } catch (Exception e) {
            System.err.println("Errore durante registrazione rilevazione: " + e.getMessage());
            return false;
        }
    }

    // AGGIUNGI ASSUNZIONE FARMACO
    public boolean aggiungiAssunzioneFarmaco(String idTerapia, LocalDateTime dataOraAssunzione, double quantita) {
        try {
            if (!sistema.isPaziente()) {
                return false;
            }

            Paziente paziente = (Paziente) sistema.getUtenteCorrente();

            // 1. Verifica che la terapia esista e sia del paziente
            Terapia terapia = sistema.getTerapiaDAO().findById(idTerapia);
            if (terapia == null || !terapia.getIdPaziente().equals(paziente.getId())) {
                System.out.println("Terapia non trovata o non autorizzata");
                return false;
            }

            // 2. Verifica che la terapia sia attiva
            if (!terapia.isAttiva()) {
                System.out.println("La terapia non è più attiva");
                return false;
            }

            // 3. Crea l'assunzione
            AssunzioneFarmaco assunzione = new AssunzioneFarmaco(
                    "ASS_" + System.currentTimeMillis(),
                    paziente.getId(),
                    idTerapia,
                    dataOraAssunzione,
                    dataOraAssunzione, // Per semplicità, ora prevista = ora effettiva
                    terapia.getFarmaco(),
                    quantita,
                    true
            );

            // 4. Salva
            sistema.getAssunzioneFarmacoDAO().save(assunzione);

            System.out.println("Assunzione registrata: " + terapia.getFarmaco() + " - " + quantita + "mg");
            return true;

        } catch (Exception e) {
            System.err.println("Errore registrazione assunzione: " + e.getMessage());
            return false;
        }
    }

    // OTTIENI RILEVAZIONI IN UN PERIODO
    public List<Rilevazione> getRilevazioniPeriodo(LocalDate inizio, LocalDate fine) {
        if (!sistema.isPaziente()) {
            return new ArrayList<>();
        }

        Paziente paziente = (Paziente) sistema.getUtenteCorrente();
        return sistema.getRilevazioneDAO().findByPazienteAndPeriodo(paziente.getId(), inizio, fine);
    }

    // OTTIENI TERAPIE ATTIVE
    public List<Terapia> getTerapieAttive() {
        if (!sistema.isPaziente()) {
            return new ArrayList<>();
        }

        Paziente paziente = (Paziente) sistema.getUtenteCorrente();
        return sistema.getTerapiaDAO().findTerapieAttive(paziente.getId());
    }

    // OTTIENI ASSUNZIONI DI UN GIORNO
    public List<AssunzioneFarmaco> getAssunzioniGiorno(LocalDate giorno) {
        if (!sistema.isPaziente()) {
            return new ArrayList<>();
        }

        Paziente paziente = (Paziente) sistema.getUtenteCorrente();

        // Filtra per giorno specifico
        return sistema.getAssunzioneFarmacoDAO().findByPaziente(paziente.getId())
                .stream()
                .filter(a -> a.getDataOraAssunzione().toLocalDate().equals(giorno))
                .collect(Collectors.toList());
    }

    // INVIA EMAIL AL MEDICO (simulata)
    public void inviaEmailMedico(String messaggio) {
        if (!sistema.isPaziente()) {
            return;
        }

        Paziente paziente = (Paziente) sistema.getUtenteCorrente();
        String idMedico = paziente.getMedicoRiferimento();

        if (idMedico != null) {
            Utente medico = sistema.getUtenteDAO().findById(idMedico);
            if (medico != null) {
                System.out.println("EMAIL INVIATA al Dr. " + medico.getCognome() + ": " + messaggio);

                // In un sistema reale, qui invieresti davvero l'email
                // EmailService.send(medico.getEmail(), "Messaggio da paziente", messaggio);
            }
        }
    }
        // METODO PRIVATO PER VERIFICA ALERT
        private void verificaAlert(Rilevazione rilevazione){
            // Se la glicemia è fuori norma, crea un alert
            if (!rilevazione.isNormale()) {
                AlertController alertController = new AlertController(sistema);

                String messaggio;
                GradoAllerta urgenza;

                if (rilevazione.getGradoAllerta() == GradoAllerta.URGENTE) {
                    messaggio = "URGENTE: Glicemia critica " + rilevazione.getValoreGlicemia() + " mg/dL";
                    urgenza = GradoAllerta.URGENTE;
                } else {
                    messaggio = "Glicemia fuori norma: " + rilevazione.getValoreGlicemia() + " mg/dL";
                    urgenza = GradoAllerta.ATTENZIONE;
                }

                alertController.creaAlert(
                        rilevazione.getIdPaziente(),
                        TipoAlert.GLICEMIA_ALTA,
                        messaggio,
                        urgenza
                );
            }
        }

}
