package Controller;

import Model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlertController {
    private SistemaController sistema;

    public AlertController(SistemaController sistema) {
        this.sistema = sistema;
    }

    // VERIFICA GLICEMIE ANOMALE (eseguito periodicamente)
    public void verificaGlicemieAnomale() {
        try {
            LocalDate oggi = LocalDate.now();

            // Trova tutte le rilevazioni di oggi
            List<Rilevazione> rilevazioniOggi = sistema.getRilevazioneDAO().findAll()
                    .stream()
                    .filter(r -> r.getDataOra().toLocalDate().equals(oggi))
                    .collect(Collectors.toList());

            for (Rilevazione rilevazione : rilevazioniOggi) {
                if (!rilevazione.isNormale()) {
                    // Trova il medico del paziente
                    Paziente paziente = (Paziente) sistema.getUtenteDAO().findById(rilevazione.getIdPaziente());
                    if (paziente != null && paziente.getMedicoRiferimento() != null) {

                        // Controlla se non esiste già un alert simile oggi
                        if (!esisteAlertSimile(paziente.getId(), TipoAlert.GLICEMIA_ALTA, oggi)) {

                            String messaggio;
                            GradoAllerta urgenza;

                            if (rilevazione.getGradoAllerta() == GradoAllerta.URGENTE) {
                                messaggio = "URGENTE: " + paziente.getNome() + " " + paziente.getCognome() +
                                        " - Glicemia critica: " + rilevazione.getValoreGlicemia() + " mg/dL";
                                urgenza = GradoAllerta.URGENTE;
                            } else {
                                messaggio = paziente.getNome() + " " + paziente.getCognome() +
                                        " - Glicemia anomala: " + rilevazione.getValoreGlicemia() + " mg/dL";
                                urgenza = GradoAllerta.ATTENZIONE;
                            }

                            creaAlert(paziente.getId(), TipoAlert.GLICEMIA_ALTA, messaggio, urgenza);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Errore verifica glicemie anomale: " + e.getMessage());
        }
    }

    // VERIFICA TERAPIE NON SEGUITE
    public void verificaTerapieNonSeguite() {
        try {
            LocalDate oggi = LocalDate.now();
            LocalDate treGiorniFa = oggi.minusDays(3);

            // Per ogni paziente, controlla l'aderenza terapeutica
            List<Paziente> pazienti = sistema.getUtenteDAO().findAllPazienti();

            for (Paziente paziente : pazienti) {
                List<Terapia> terapieAttive = sistema.getTerapiaDAO().findTerapieAttive(paziente.getId());

                for (Terapia terapia : terapieAttive) {
                    // Conta assunzioni previste e effettive negli ultimi 3 giorni
                    long assunzioniPreviste = 3 * terapia.getAssunzioniGiornaliere();

                    List<AssunzioneFarmaco> assunzioniEffettive = sistema.getAssunzioneFarmacoDAO()
                            .findByTerapia(terapia.getId())
                            .stream()
                            .filter(a -> !a.getDataOraAssunzione().toLocalDate().isBefore(treGiorniFa))
                            .filter(a -> !a.getDataOraAssunzione().toLocalDate().isAfter(oggi))
                            .filter(AssunzioneFarmaco::isAssunta)
                            .collect(Collectors.toList());

                    // Se ha preso meno del 50% delle dosi
                    if (assunzioniEffettive.size() < assunzioniPreviste * 0.5) {

                        // Controlla se non esiste già un alert simile negli ultimi 3 giorni
                        if (!esisteAlertRecente(paziente.getId(), TipoAlert.TERAPIA_NON_SEGUITA, 3)) {

                            String messaggio = paziente.getNome() + " " + paziente.getCognome() +
                                    " non sta seguendo la terapia con " + terapia.getFarmaco() +
                                    " (" + assunzioniEffettive.size() + "/" + assunzioniPreviste + " dosi)";

                            creaAlert(paziente.getId(), TipoAlert.TERAPIA_NON_SEGUITA, messaggio, GradoAllerta.URGENTE);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Errore verifica terapie non seguite: " + e.getMessage());
        }
    }

    // VERIFICA FARMACI DIMENTICATI
    public void verificaFarmaciDimenticati() {
        try {
            LocalDateTime adesso = LocalDateTime.now();
            LocalDateTime duoreOraFa = adesso.minusHours(2);

            List<Paziente> pazienti = sistema.getUtenteDAO().findAllPazienti();

            for (Paziente paziente : pazienti) {
                List<Terapia> terapieAttive = sistema.getTerapiaDAO().findTerapieAttive(paziente.getId());

                for (Terapia terapia : terapieAttive) {
                    List<OrarioAssunzione> orariOggi = terapia.calcolaOrariAssunzione();

                    for (OrarioAssunzione orario : orariOggi) {
                        LocalDateTime orarioPrevisto = LocalDateTime.of(LocalDate.now(), orario.getOra());

                        // Se l'orario è passato da più di 2 ore
                        if (orarioPrevisto.isBefore(duoreOraFa)) {

                            // Verifica se è stata registrata l'assunzione
                            boolean assunzioneRegistrata = sistema.getAssunzioneFarmacoDAO()
                                    .findByTerapia(terapia.getId())
                                    .stream()
                                    .anyMatch(a -> a.getDataOraAssunzione().toLocalDate().equals(LocalDate.now()) &&
                                            Math.abs(ChronoUnit.MINUTES.between(a.getDataOraAssunzione().toLocalTime(),
                                                    orario.getOra())) < 60 &&
                                            a.isAssunta());

                            if (!assunzioneRegistrata) {
                                // Controlla se non esiste già un alert per questo farmaco oggi
                                if (!esisteAlertFarmacoOggi(paziente.getId(), terapia.getFarmaco())) {

                                    String messaggio = paziente.getNome() + " " + paziente.getCognome() +
                                            " ha dimenticato di prendere " + terapia.getFarmaco() +
                                            " alle " + orario.getOra().toString();

                                    creaAlert(paziente.getId(), TipoAlert.FARMACO_DIMENTICATO, messaggio, GradoAllerta.NORMALE);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Errore verifica farmaci dimenticati: " + e.getMessage());
        }
    }

    // CREA NUOVO ALERT
    public void creaAlert(String idPaziente, TipoAlert tipo, String messaggio, GradoAllerta urgenza) {
        try {
            // Trova il medico del paziente
            Paziente paziente = (Paziente) sistema.getUtenteDAO().findById(idPaziente);
            if (paziente == null || paziente.getMedicoRiferimento() == null) {
                System.out.println("Impossibile creare alert: paziente o medico non trovato");
                return;
            }

            Alert alert = new Alert(
                    "ALT_" + System.currentTimeMillis(),
                    idPaziente,
                    paziente.getMedicoRiferimento(),
                    tipo,
                    LocalDateTime.now(),
                    messaggio,
                    false, // non ancora visualizzato
                    urgenza
            );

            // Salva l'alert
            sistema.getAlertDAO().save(alert);

            System.out.println("ALERT CREATO: " + tipo + " - " + messaggio);

            // Se è urgente, potresti inviare anche una notifica immediata
            if (urgenza == GradoAllerta.URGENTE) {
                inviaNotificaUrgente(alert);
            }

        } catch (Exception e) {
            System.err.println("Errore creazione alert: " + e.getMessage());
        }
    }

    // OTTIENI ALERT PER MEDICO
    public List<Alert> getAlertMedico(String idMedico) {
        return sistema.getAlertDAO().findByMedico(idMedico)
                .stream()
                .sorted((a1, a2) -> {
                    // Ordina per urgenza (IMMEDIATA prima) e poi per data (più recente prima)
                    int compareUrgenza = a2.getUrgenza().compareTo(a1.getUrgenza());
                    if (compareUrgenza != 0) {
                        return compareUrgenza;
                    }
                    return a2.getDataCreazione().compareTo(a1.getDataCreazione());
                })
                .collect(Collectors.toList());
    }

    // MARCA ALERT COME VISUALIZZATO
    public boolean marcaAlertVisualizzato(String idAlert) {
        try {
            Alert alert = sistema.getAlertDAO().findById(idAlert);
            if (alert != null) {
                alert.marcaVisualizzato();
                sistema.getAlertDAO().update(alert);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Errore marca alert visualizzato: " + e.getMessage());
            return false;
        }
    }

    // OTTIENI ALERT NON VISUALIZZATI
    public List<Alert> getAlertNonVisualizzati() {
        return sistema.getAlertDAO().findNonVisualizzati();
    }

    // OTTIENI STATISTICHE ALERT PER MEDICO
    public Map<String, Integer> getStatisticheAlert(String idMedico) {
        List<Alert> alertMedico = getAlertMedico(idMedico);
        Map<String, Integer> statistiche = new HashMap<>();

        // Conta per tipo
        for (TipoAlert tipo : TipoAlert.values()) {
            int count = (int) alertMedico.stream()
                    .filter(a -> a.getTipo() == tipo)
                    .count();
            statistiche.put(tipo.toString(), count);
        }

        // Conta per urgenza
        for (GradoAllerta urgenza : GradoAllerta.values()) {
            int count = (int) alertMedico.stream()
                    .filter(a -> a.getUrgenza() == urgenza)
                    .count();
            statistiche.put("urgenza_" + urgenza.toString(), count);
        }

        // Alert non visualizzati
        int nonVisualizzati = (int) alertMedico.stream()
                .filter(a -> !a.isVisualizzato())
                .count();
        statistiche.put("non_visualizzati", nonVisualizzati);

        return statistiche;
    }

    // ELIMINA ALERT VECCHI (pulizia periodica)
    public void eliminaAlertVecchi(int giorniVecchiaia) {
        try {
            LocalDateTime soglia = LocalDateTime.now().minusDays(giorniVecchiaia);

            List<Alert> alertVecchi = sistema.getAlertDAO().findAll()
                    .stream()
                    .filter(a -> a.getDataCreazione().isBefore(soglia))
                    .filter(Alert::isVisualizzato) // Solo quelli già visualizzati
                    .collect(Collectors.toList());

            for (Alert alert : alertVecchi) {
                sistema.getAlertDAO().delete(alert.getId());
            }

            System.out.println("Eliminati " + alertVecchi.size() + " alert vecchi");

        } catch (Exception e) {
            System.err.println("Errore eliminazione alert vecchi: " + e.getMessage());
        }
    }

    // METODI PRIVATI DI SUPPORTO

    private boolean esisteAlertSimile(String idPaziente, TipoAlert tipo, LocalDate data) {
        return sistema.getAlertDAO().findByPaziente(idPaziente)
                .stream()
                .anyMatch(a -> a.getTipo() == tipo &&
                        a.getDataCreazione().toLocalDate().equals(data));
    }

    private boolean esisteAlertRecente(String idPaziente, TipoAlert tipo, int giorni) {
        LocalDateTime soglia = LocalDateTime.now().minusDays(giorni);

        return sistema.getAlertDAO().findByPaziente(idPaziente)
                .stream()
                .anyMatch(a -> a.getTipo() == tipo &&
                        a.getDataCreazione().isAfter(soglia));
    }

    private boolean esisteAlertFarmacoOggi(String idPaziente, String farmaco) {
        LocalDate oggi = LocalDate.now();

        return sistema.getAlertDAO().findByPaziente(idPaziente)
                .stream()
                .anyMatch(a -> a.getTipo() == TipoAlert.FARMACO_DIMENTICATO &&
                        a.getDataCreazione().toLocalDate().equals(oggi) &&
                        a.getMessaggio().contains(farmaco));
    }

    private void inviaNotificaUrgente(Alert alert) {
        // Trova il medico
        Utente medico = sistema.getUtenteDAO().findById(alert.getIdMedico());
        if (medico != null) {
            System.out.println("🚨 NOTIFICA URGENTE inviata al Dr. " + medico.getCognome() +
                    ": " + alert.getMessaggio());

            // In un sistema reale, qui potresti:
            // - Inviare SMS
            // - Inviare email urgente
            // - Notifica push sull'app mobile
            // - Chiamata automatica

            // EmailService.sendUrgent(medico.getEmail(), "ALERT URGENTE", alert.getMessaggio());
            // SmsService.send(medico.getTelefono(), alert.getMessaggio());
        }
    }

    // METODO PER ESEGUIRE TUTTI I CONTROLLI AUTOMATICI
    public void eseguiControlliPeriodici() {
        System.out.println("Esecuzione controlli automatici...");

        verificaGlicemieAnomale();
        verificaTerapieNonSeguite();
        verificaFarmaciDimenticati();

        // Pulizia alert vecchi (una volta al giorno)
        eliminaAlertVecchi(30); // Elimina alert più vecchi di 30 giorni

        System.out.println("Controlli automatici completati");
    }
}