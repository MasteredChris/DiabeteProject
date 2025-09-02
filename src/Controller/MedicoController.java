package Controller;

import Model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MedicoController {
    private SistemaController sistema;

    public MedicoController(SistemaController sistema) {
        this.sistema = sistema;
    }

    public SistemaController getSistema() {
        return sistema;
    }

    // OTTIENI LISTA PAZIENTI DEL MEDICO
    public List<Paziente> getPazienti() {
        if (!sistema.isMedico()) {
            return new ArrayList<>();
        }

        Diabetologo medico = (Diabetologo) sistema.getUtenteCorrente();

        // Trova tutti i pazienti che hanno questo medico come riferimento
        return sistema.getUtenteDAO().findAllPazienti()
                .stream()
                .filter(p -> medico.getId().equals(p.getMedicoRiferimento()))
                .collect(Collectors.toList());
    }

    // PRESCRIVI NUOVA TERAPIA
    public boolean prescriviTerapia(String idPaziente, String farmaco, int assunzioniGiornaliere,
                                    double quantitaPerAssunzione, String indicazioni,
                                    LocalDate dataInizio, LocalDate dataFine) {
        try {
            if (!sistema.isMedico()) {
                System.out.println("Solo i medici possono prescrivere terapie");
                return false;
            }

            Diabetologo medico = (Diabetologo) sistema.getUtenteCorrente();

            // 1. Verifica che il paziente sia assegnato al medico
            Paziente paziente = (Paziente) sistema.getUtenteDAO().findById(idPaziente);
            if (paziente == null || !medico.getId().equals(paziente.getMedicoRiferimento())) {
                System.out.println("Paziente non autorizzato per questo medico");
                return false;
            }

            // 2. Crea la terapia
            Terapia terapia = new Terapia(
                    "TER_" + System.currentTimeMillis(),
                    idPaziente,
                    medico.getId(),
                    farmaco,
                    assunzioniGiornaliere,
                    quantitaPerAssunzione,
                    indicazioni,
                    dataInizio,
                    dataFine,
                    true
            );

            // 3. Valida la terapia
            if (!terapia.isValidTerapia()) {
                System.out.println("Dati terapia non validi");
                return false;
            }

            // 4. Controlla se esiste già una terapia attiva per lo stesso farmaco
            List<Terapia> terapieAttive = sistema.getTerapiaDAO().findTerapieAttive(idPaziente);
            for (Terapia t : terapieAttive) {
                if (t.getFarmaco().equalsIgnoreCase(farmaco)) {
                    // Disattiva la vecchia terapia
                    t.sospendiTerapia("Sostituita da nuova prescrizione");
                    sistema.getTerapiaDAO().update(t);
                }
            }

            // 5. Salva la nuova terapia
            sistema.getTerapiaDAO().save(terapia);

            System.out.println("Terapia prescritta: " + farmaco + " per " + paziente.getNome());
            return true;

        } catch (Exception e) {
            System.err.println("Errore prescrizione terapia: " + e.getMessage());
            return false;
        }
    }

    // MODIFICA TERAPIA ESISTENTE
    public boolean modificaTerapia(String idTerapia, Double nuovaQuantita, Integer nuoveAssunzioni, String motivazione) {
        try {
            if (!sistema.isMedico()) {
                return false;
            }

            // 1. Trova la terapia
            Terapia terapia = sistema.getTerapiaDAO().findById(idTerapia);
            if (terapia == null) {
                System.out.println("Terapia non trovata");
                return false;
            }

            // 2. Verifica che sia del medico loggato
            Diabetologo medico = (Diabetologo) sistema.getUtenteCorrente();
            if (!terapia.getIdMedico().equals(medico.getId())) {
                System.out.println("Non autorizzato a modificare questa terapia");
                return false;
            }

            // 3. Applica le modifiche
            if (nuovaQuantita != null) {
                terapia.modificaDosaggio(nuovaQuantita, motivazione);
            }

            if (nuoveAssunzioni != null) {
                terapia.modificaFrequenza(nuoveAssunzioni, motivazione);
            }

            // 4. Salva le modifiche
            sistema.getTerapiaDAO().update(terapia);

            System.out.println("Terapia modificata con successo");
            return true;

        } catch (Exception e) {
            System.err.println("Errore modifica terapia: " + e.getMessage());
            return false;
        }
    }

    // OTTIENI RILEVAZIONI DI UN PAZIENTE
    public List<Rilevazione> getRilevazioniPaziente(String idPaziente, LocalDate inizio, LocalDate fine) {
        if (!sistema.isMedico()) {
            return new ArrayList<>();
        }

        // Verifica che il paziente sia del medico
        Diabetologo medico = (Diabetologo) sistema.getUtenteCorrente();
        Paziente paziente = (Paziente) sistema.getUtenteDAO().findById(idPaziente);

        if (paziente == null || !medico.getId().equals(paziente.getMedicoRiferimento())) {
            return new ArrayList<>();
        }

        return sistema.getRilevazioneDAO().findByPazienteAndPeriodo(idPaziente, inizio, fine);
    }

    // OTTIENI ALERT DEL MEDICO
    public List<Alert> getAlert() {
        if (!sistema.isMedico()) {
            return new ArrayList<>();
        }

        Diabetologo medico = (Diabetologo) sistema.getUtenteCorrente();
        return sistema.getAlertDAO().findByMedico(medico.getId());
    }

    // AGGIORNA INFORMAZIONI PAZIENTE
    public boolean aggiornaInfoPaziente(String idPaziente, List<String> fattoriRischio, List<String> comorbidita) {
        try {
            if (!sistema.isMedico()) {
                return false;
            }

            Diabetologo medico = (Diabetologo) sistema.getUtenteCorrente();
            Paziente paziente = (Paziente) sistema.getUtenteDAO().findById(idPaziente);

            if (paziente == null || !medico.getId().equals(paziente.getMedicoRiferimento())) {
                System.out.println("Paziente non autorizzato");
                return false;
            }

            // Aggiorna i dati
            if (fattoriRischio != null) {
                paziente.setFattoriRischio(fattoriRischio);
            }

            if (comorbidita != null) {
                paziente.setComorbidita(comorbidita);
            }

            // Salva le modifiche
            sistema.getUtenteDAO().update(paziente);

            System.out.println("Informazioni paziente aggiornate");
            return true;

        } catch (Exception e) {
            System.err.println("Errore aggiornamento paziente: " + e.getMessage());
            return false;
        }
    }

    // OTTIENI STATISTICHE PAZIENTE
    public Map<String, Object> getStatistichePaziente(String idPaziente) {
        Map<String, Object> statistiche = new HashMap<>();

        if (!sistema.isMedico()) {
            return statistiche;
        }

        // Verifica autorizzazione
        Diabetologo medico = (Diabetologo) sistema.getUtenteCorrente();
        Paziente paziente = (Paziente) sistema.getUtenteDAO().findById(idPaziente);

        if (paziente == null || !medico.getId().equals(paziente.getMedicoRiferimento())) {
            return statistiche;
        }

        // Calcola statistiche ultimo mese
        LocalDate finePeriodo = LocalDate.now();
        LocalDate inizioPeriodo = finePeriodo.minusMonths(1);

        List<Rilevazione> rilevazioni = sistema.getRilevazioneDAO()
                .findByPazienteAndPeriodo(idPaziente, inizioPeriodo, finePeriodo);

        if (!rilevazioni.isEmpty()) {
            // Media glicemie pre-pasto
            double mediaPrePasto = rilevazioni.stream()
                    .filter(r -> r.getTipo() == TipoRilevazione.PRIMA_PASTO)
                    .mapToInt(Rilevazione::getValoreGlicemia)
                    .average()
                    .orElse(0.0);

            // Media glicemie post-pasto
            double mediaPostPasto = rilevazioni.stream()
                    .filter(r -> r.getTipo() == TipoRilevazione.DOPO_PASTO)
                    .mapToInt(Rilevazione::getValoreGlicemia)
                    .average()
                    .orElse(0.0);

            // Conta episodi anomali
            long episodiAnomali = rilevazioni.stream()
                    .filter(r -> !r.isNormale())
                    .count();

            statistiche.put("mediaPrePasto", mediaPrePasto);
            statistiche.put("mediaPostPasto", mediaPostPasto);
            statistiche.put("totalerilevazioni", rilevazioni.size());
            statistiche.put("episodiAnomali", episodiAnomali);
            statistiche.put("percentualeNormali",
                    (double)(rilevazioni.size() - episodiAnomali) / rilevazioni.size() * 100);
        }

        return statistiche;
    }
}