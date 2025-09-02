package Model;

import Controller.MedicoController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Diabetologo extends Utente {

    private MedicoController medicoController;

    public Diabetologo(String id, String username, String password, String nome, String cognome, String email) {
        super(id, username, password, nome, cognome, email);
    }

    // Setter per il controller (viene impostato dopo il login)
    public void setMedicoController(MedicoController medicoController) {
        this.medicoController = medicoController;
    }
        @Override
    public String getUserType() {
        return "MEDICO";
    }
    public boolean prescriviTerapia(String idPaziente, Terapia terapia) {
        try {
            // 1. Controllo di sicurezza - verifica che il controller sia disponibile
            if (medicoController == null) {
                System.err.println("Errore: Controller non inizializzato");
                return false;
            }

            // 2. Controllo autorizzazione - verifica che il paziente sia assegnato a questo medico
            if (!verificaAutorizzazionePaziente(idPaziente)) {
                System.out.println("Errore: Paziente non autorizzato per questo medico");
                return false;
            }

            // 3. Validazione dati terapia
            if (terapia == null) {
                System.out.println("Errore: Terapia non può essere null");
                return false;
            }

            if (!terapia.isValidTerapia()) {
                System.out.println("Errore: Dati terapia non validi");
                return false;
            }

            // 4. Imposta il medico prescrittore
            terapia.setIdMedico(this.getId());
            terapia.setIdPaziente(idPaziente);

            // 5. Verifica compatibilità con terapie esistenti
            if (!verificaCompatibilitaFarmacologica(idPaziente, terapia)) {
                System.out.println("Errore: Incompatibilità farmacologica rilevata");
                return false;
            }

            // 6. Delega al MedicoController per l'operazione effettiva
            boolean risultato = medicoController.prescriviTerapia(
                    idPaziente,
                    terapia.getFarmaco(),
                    terapia.getAssunzioniGiornaliere(),
                    terapia.getQuantitaPerAssunzione(),
                    terapia.getIndicazioni(),
                    terapia.getDataInizio(),
                    terapia.getDataFine()
            );

            if (risultato) {
                System.out.println("Dr. " + this.getCognome() + " ha prescritto " +
                        terapia.getFarmaco() + " al paziente " + idPaziente);

                // 7. Log dell'operazione per audit
                logOperazioneMedica("PRESCRIZIONE", idPaziente,
                        "Prescritto " + terapia.getFarmaco() + " " +
                                terapia.getQuantitaPerAssunzione() + "mg " +
                                terapia.getAssunzioniGiornaliere() + " volte/giorno");
            }

            return risultato;

        } catch (Exception e) {
            System.err.println("Errore durante prescrizione terapia: " + e.getMessage());
            return false;
        }
    }
    public boolean modificaTerapia(String idTerapia, Terapia nuovaTerapia) {
        try {
            // 1. Controllo controller disponibile
            if (medicoController == null) {
                System.err.println("Errore: Controller non inizializzato");
                return false;
            }

            // 2. Trova la terapia esistente
            Terapia terapiaEsistente = medicoController.getSistema().getTerapiaDAO().findById(idTerapia);
            if (terapiaEsistente == null) {
                System.out.println("Errore: Terapia non trovata");
                return false;
            }

            // 3. Verifica autorizzazione - solo il medico prescrittore può modificare
            if (!terapiaEsistente.getIdMedico().equals(this.getId())) {
                System.out.println("Errore: Non autorizzato a modificare questa terapia");
                return false;
            }

            // 4. Verifica che il paziente sia ancora assegnato al medico
            if (!verificaAutorizzazionePaziente(terapiaEsistente.getIdPaziente())) {
                System.out.println("Errore: Paziente non più assegnato a questo medico");
                return false;
            }

            // 5. Validazione nuovi dati
            if (nuovaTerapia == null || !nuovaTerapia.isValidTerapia()) {
                System.out.println("Errore: Nuovi dati terapia non validi");
                return false;
            }

            // 6. Prepara le modifiche e motivazioni
            List<String> modificheApplicate = new ArrayList<>();
            String motivazione = "Aggiornamento terapia da " + this.getNome() + " " + this.getCognome();

            // 7. Applica le modifiche rilevate
            boolean modificaEffettuata = false;

            // Verifica cambio dosaggio
            if (terapiaEsistente.getQuantitaPerAssunzione() != nuovaTerapia.getQuantitaPerAssunzione()) {
                double vecchioDosaggio = terapiaEsistente.getQuantitaPerAssunzione();

                boolean risultatoModifica = medicoController.modificaTerapia(
                        idTerapia,
                        nuovaTerapia.getQuantitaPerAssunzione(), // nuovo dosaggio
                        null, // frequenza invariata
                        "Modifica dosaggio: da " + vecchioDosaggio + "mg a " + nuovaTerapia.getQuantitaPerAssunzione() + "mg"
                );

                if (risultatoModifica) {
                    modificheApplicate.add("Dosaggio: " + vecchioDosaggio + "mg → " +
                            nuovaTerapia.getQuantitaPerAssunzione() + "mg");
                    modificaEffettuata = true;
                }
            }

            // Verifica cambio frequenza
            if (terapiaEsistente.getAssunzioniGiornaliere() != nuovaTerapia.getAssunzioniGiornaliere()) {
                int vecchiaFrequenza = terapiaEsistente.getAssunzioniGiornaliere();

                boolean risultatoModifica = medicoController.modificaTerapia(
                        idTerapia,
                        null, // dosaggio invariato
                        nuovaTerapia.getAssunzioniGiornaliere(), // nuova frequenza
                        "Modifica frequenza: da " + vecchiaFrequenza + " a " + nuovaTerapia.getAssunzioniGiornaliere() + " volte/giorno"
                );

                if (risultatoModifica) {
                    modificheApplicate.add("Frequenza: " + vecchiaFrequenza + " → " +
                            nuovaTerapia.getAssunzioniGiornaliere() + " volte/giorno");
                    modificaEffettuata = true;
                }
            }

            // Verifica cambio indicazioni
            if (!terapiaEsistente.getIndicazioni().equals(nuovaTerapia.getIndicazioni())) {
                // Aggiorna le indicazioni direttamente
                terapiaEsistente.setIndicazioni(nuovaTerapia.getIndicazioni());
                medicoController.getSistema().getTerapiaDAO().update(terapiaEsistente);

                modificheApplicate.add("Indicazioni aggiornate");
                modificaEffettuata = true;
            }

            // Verifica cambio date (inizio/fine terapia)
            if ((nuovaTerapia.getDataFine() != null && !nuovaTerapia.getDataFine().equals(terapiaEsistente.getDataFine())) ||
                    (nuovaTerapia.getDataFine() == null && terapiaEsistente.getDataFine() != null)) {

                terapiaEsistente.setDataFine(nuovaTerapia.getDataFine());
                medicoController.getSistema().getTerapiaDAO().update(terapiaEsistente);

                if (nuovaTerapia.getDataFine() != null) {
                    modificheApplicate.add("Data fine: " + nuovaTerapia.getDataFine());
                } else {
                    modificheApplicate.add("Terapia resa a tempo indeterminato");
                }
                modificaEffettuata = true;
            }

            // 8. Log e notifica se ci sono state modifiche
            if (modificaEffettuata) {
                String descrizioneModifiche = String.join(", ", modificheApplicate);

                System.out.println("Dr. " + this.getCognome() + " ha modificato la terapia " +
                        terapiaEsistente.getFarmaco() + ": " + descrizioneModifiche);

                // Log per audit
                logOperazioneMedica("MODIFICA_TERAPIA", terapiaEsistente.getIdPaziente(),
                        "Terapia " + terapiaEsistente.getFarmaco() + " modificata: " + descrizioneModifiche);

                // Notifica al paziente
                notificaPazienteModificaTerapia(terapiaEsistente.getIdPaziente(), terapiaEsistente.getFarmaco(), descrizioneModifiche);

                return true;
            } else {
                System.out.println("Nessuna modifica rilevata nella terapia");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Errore durante modifica terapia: " + e.getMessage());
            return false;
        }
    }

    // VERIFICA AUTORIZZAZIONE PAZIENTE
    private boolean verificaAutorizzazionePaziente(String idPaziente) {
        try {
            if (medicoController == null) {
                return false;
            }

            // Trova il paziente
            Paziente paziente = (Paziente) medicoController.getSistema().getUtenteDAO().findById(idPaziente);

            if (paziente == null) {
                System.out.println("Paziente non trovato: " + idPaziente);
                return false;
            }

            // Verifica che sia assegnato a questo medico
            if (!this.getId().equals(paziente.getMedicoRiferimento())) {
                System.out.println("Paziente " + paziente.getNome() + " non assegnato al Dr. " + this.getCognome());
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Errore verifica autorizzazione: " + e.getMessage());
            return false;
        }
    }

    // VERIFICA COMPATIBILITÀ FARMACOLOGICA
    private boolean verificaCompatibilitaFarmacologica(String idPaziente, Terapia nuovaTerapia) {
        try {
            // Ottieni tutte le terapie attive del paziente
            List<Terapia> terapieAttive = medicoController.getSistema().getTerapiaDAO().findTerapieAttive(idPaziente);

            for (Terapia terapiaEsistente : terapieAttive) {
                // Controlla stesso farmaco
                if (terapiaEsistente.getFarmaco().equalsIgnoreCase(nuovaTerapia.getFarmaco())) {
                    System.out.println("Attenzione: Esiste già una terapia attiva con " + nuovaTerapia.getFarmaco());
                    return false;
                }

                // Controlli specifici per interazioni note
                if (hasInterazioneFarmacologica(terapiaEsistente.getFarmaco(), nuovaTerapia.getFarmaco())) {
                    System.out.println("Attenzione: Possibile interazione tra " +
                            terapiaEsistente.getFarmaco() + " e " + nuovaTerapia.getFarmaco());
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Errore verifica compatibilità: " + e.getMessage());
            return false;
        }
    }

    // CONTROLLA INTERAZIONI FARMACOLOGICHE
    private boolean hasInterazioneFarmacologica(String farmaco1, String farmaco2) {
        // Database semplificato di interazioni note
        Map<String, List<String>> interazioni = new HashMap<>();

        // Metformina
        interazioni.put("metformina", Arrays.asList("warfarin", "furosemide"));

        // Sulfaniluree
        interazioni.put("glibenclamide", Arrays.asList("warfarin", "fenilbutazone"));
        interazioni.put("gliclazide", Arrays.asList("warfarin", "miconazolo"));

        // Insulina (generalmente compatibile, ma controlli specifici)
        interazioni.put("insulina", Arrays.asList("corticosteroidi"));

        String f1 = farmaco1.toLowerCase();
        String f2 = farmaco2.toLowerCase();

        // Controlla in entrambe le direzioni
        return (interazioni.containsKey(f1) && interazioni.get(f1).contains(f2)) ||
                (interazioni.containsKey(f2) && interazioni.get(f2).contains(f1));
    }

    // LOG OPERAZIONI MEDICHE
    private void logOperazioneMedica(String tipoOperazione, String idPaziente, String descrizione) {
        try {
            // Trova il nome del paziente per il log
            Paziente paziente = (Paziente) medicoController.getSistema().getUtenteDAO().findById(idPaziente);
            String nomePaziente = paziente != null ? paziente.getNome() + " " + paziente.getCognome() : idPaziente;

            // Log formato: [TIMESTAMP] DR.COGNOME - OPERAZIONE - PAZIENTE - DESCRIZIONE
            String logEntry = String.format("[%s] Dr.%s - %s - %s - %s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    this.getCognome(),
                    tipoOperazione,
                    nomePaziente,
                    descrizione);

            System.out.println("AUDIT LOG: " + logEntry);

            // In un sistema reale, qui salveresti su file o database
            // AuditLogger.log(logEntry);
            // oppure LogDAO.save(new LogEntry(this.getId(), tipoOperazione, idPaziente, descrizione));

        } catch (Exception e) {
            System.err.println("Errore durante logging: " + e.getMessage());
        }
    }

    // NOTIFICA PAZIENTE PER MODIFICA TERAPIA
    private void notificaPazienteModificaTerapia(String idPaziente, String farmaco, String modifiche) {
        try {
            Paziente paziente = (Paziente) medicoController.getSistema().getUtenteDAO().findById(idPaziente);

            if (paziente != null) {
                String messaggio = String.format(
                        "Il Dr. %s ha modificato la tua terapia con %s. Modifiche: %s. " +
                                "Controlla la nuova posologia nell'app.",
                        this.getCognome(),
                        farmaco,
                        modifiche
                );

                System.out.println("NOTIFICA A " + paziente.getNome() + ": " + messaggio);

                // In un sistema reale:
                // EmailService.send(paziente.getEmail(), "Modifica Terapia", messaggio);
                // PushNotificationService.send(paziente.getId(), messaggio);
            }

        } catch (Exception e) {
            System.err.println("Errore invio notifica: " + e.getMessage());
        }
    }
}
