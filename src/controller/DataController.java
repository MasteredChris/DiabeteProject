package controller;

import model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller responsabile della gestione dei dati (caricamento e salvataggio)
 * per tutte le entità del sistema di gestione diabetologica.
 */
public class DataController {

    private static final Logger LOGGER = Logger.getLogger(DataController.class.getName());
    private static final String CSV_DELIMITER = ",";
    private static final int CSV_SPLIT_LIMIT = -1; // Preserva campi vuoti

    // Costanti per gli indici delle colonne nei CSV
    private static final class ColumnIndex {
        static final int UTENTE_ID = 0;
        static final int UTENTE_TYPE = 1;
        static final int UTENTE_NOME = 2;
        static final int UTENTE_COGNOME = 3;
        static final int UTENTE_EMAIL = 4;
        static final int UTENTE_PASSWORD = 5;
        static final int PAZIENTE_MEDICO_ID = 6;

        static final int RILEVAZIONE_PAZIENTE_ID = 0;
        static final int RILEVAZIONE_DATA = 1;
        static final int RILEVAZIONE_TIPO_PASTO = 2;
        static final int RILEVAZIONE_VALORE = 3;

        static final int TERAPIA_PAZIENTE_ID = 0;
        static final int TERAPIA_FARMACO = 1;
        static final int TERAPIA_ASSUNZIONI = 2;
        static final int TERAPIA_QUANTITA = 3;
        static final int TERAPIA_INDICAZIONI = 4;
        static final int TERAPIA_DATA_INIZIO = 5;
        static final int TERAPIA_DATA_FINE = 6;
        static final int TERAPIA_STATO = 7;
        static final int TERAPIA_MEDICO_ID = 8;
    }

    // Headers CSV
    private static final class CsvHeaders {
        static final String RILEVAZIONI = "pazienteId,data,tipoPasto,valore";
        static final String TERAPIE = "pazienteId,farmaco,assunzioniGiornaliere,quantitaPerAssunzione,indicazioni,dataInizio,dataFine,stato,medicoId";
        static final String ASSUNZIONI = "pazienteId,data,ora,farmaco,quantita";
        static final String SCHEDE_CLINICHE = "pazienteId,fattoriRischio,pregressePatologie,comorbidita";
        static final String EVENTI_CLINICI = "pazienteId,tipo,descrizione,data,ora,note";
        static final String TERAPIE_CONCOMITANTI = "pazienteId,tipoTerapia,descrizione";
    }

    /**
     * Salva dati con strategia di merge: mantiene i dati esistenti di altri pazienti
     * e sostituisce solo quelli dei pazienti modificati.
     */
    private void salvaConMerge(String filePath, List<Paziente> pazientiModificati,
                               Function<String, String> extractPazienteId,
                               Function<Paziente, List<String>> generaRighe,
                               String intestazione) {

        if (pazientiModificati == null || pazientiModificati.isEmpty()) {
            LOGGER.log(Level.WARNING, "Nessun paziente da salvare per il file: {0}", filePath);
            return;
        }

        List<String> righe = new ArrayList<>();
        Set<String> idPazientiModificati = pazientiModificati.stream()
                .map(p -> String.valueOf(p.getId()))
                .collect(Collectors.toSet());

        // Leggi file esistente e mantieni righe di pazienti non modificati
        try {
            leggiFileEsistente(filePath, righe, extractPazienteId, idPazientiModificati);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "File non esistente, verrà creato: {0}", filePath);
            righe.add(intestazione);
        }

        // Aggiungi righe dei pazienti modificati
        for (Paziente paziente : pazientiModificati) {
            try {
                righe.addAll(generaRighe.apply(paziente));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Errore generando righe per paziente ID: " + paziente.getId(), e);
            }
        }

        // Scrivi file
        scriviFile(filePath, righe);
    }

    private void leggiFileEsistente(String filePath, List<String> righe,
                                    Function<String, String> extractPazienteId,
                                    Set<String> idPazientiModificati) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header != null) {
                righe.add(header);
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    String pazienteId = extractPazienteId.apply(line);
                    if (!idPazientiModificati.contains(pazienteId)) {
                        righe.add(line);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Errore processando riga: " + line, e);
                }
            }
        }
    }

    private void scriviFile(String filePath, List<String> righe) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String riga : righe) {
                bw.write(riga);
                bw.newLine();
            }
            LOGGER.log(Level.INFO, "File salvato con successo: {0}", filePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore salvando file: " + filePath, e);
        }
    }

    // ============ GESTIONE UTENTI ============

    public List<Utente> caricaUtenti(String utentiFile) {
        List<Utente> utenti = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(utentiFile))) {
            String riga = br.readLine(); // Salta header
            if (riga == null) {
                LOGGER.log(Level.WARNING, "File utenti vuoto: {0}", utentiFile);
                return utenti;
            }

            while ((riga = br.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;

                try {
                    Utente utente = parseUtente(riga);
                    if (utente != null) {
                        utenti.add(utente);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Errore parsing utente: " + riga, e);
                }
            }

            associaPazientiAiMedici(utenti);
            LOGGER.log(Level.INFO, "Caricati {0} utenti", utenti.size());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricando utenti da: " + utentiFile, e);
        }

        return utenti;
    }

    private Utente parseUtente(String riga) {
        String[] campi = riga.split(CSV_DELIMITER);
        if (campi.length < 6) {
            LOGGER.log(Level.WARNING, "Riga utente con campi insufficienti: {0}", riga);
            return null;
        }

        try {
            int id = Integer.parseInt(campi[ColumnIndex.UTENTE_ID].trim());
            String type = campi[ColumnIndex.UTENTE_TYPE].trim();
            String nome = campi[ColumnIndex.UTENTE_NOME].trim();
            String cognome = campi[ColumnIndex.UTENTE_COGNOME].trim();
            String email = campi[ColumnIndex.UTENTE_EMAIL].trim();
            String password = campi[ColumnIndex.UTENTE_PASSWORD].trim();

            if (type.equalsIgnoreCase("Paziente")) {
                if (campi.length <= ColumnIndex.PAZIENTE_MEDICO_ID) {
                    LOGGER.log(Level.WARNING, "Paziente senza medico ID: {0}", riga);
                    return null;
                }
                int medicoId = Integer.parseInt(campi[ColumnIndex.PAZIENTE_MEDICO_ID].trim());
                return new Paziente(id, nome, cognome, email, password, medicoId);
            } else if (type.equalsIgnoreCase("Diabetologo")) {
                return new Diabetologo(id, nome, cognome, email, password);
            } else {
                LOGGER.log(Level.WARNING, "Tipo utente sconosciuto: {0}", type);
                return null;
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Errore parsing numeri in riga: " + riga, e);
            return null;
        }
    }

    private void associaPazientiAiMedici(List<Utente> utenti) {
        Map<Integer, Diabetologo> mediciMap = utenti.stream()
                .filter(u -> u instanceof Diabetologo)
                .map(u -> (Diabetologo) u)
                .collect(Collectors.toMap(Diabetologo::getId, d -> d));

        long pazientiAssociati = utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .peek(paziente -> {
                    Diabetologo medico = mediciMap.get(paziente.getMedicoId());
                    if (medico != null) {
                        paziente.setMedico(medico);
                        medico.addPaziente(paziente);
                    } else {
                        LOGGER.log(Level.WARNING, "Medico non trovato per paziente ID: {0}", paziente.getId());
                    }
                })
                .filter(p -> p.getMedico() != null)
                .count();

        LOGGER.log(Level.INFO, "Associati {0} pazienti ai rispettivi medici", pazientiAssociati);
    }

    // ============ GESTIONE RILEVAZIONI ============

    public void caricaRilevazioni(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int rilevazioniCaricate = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Salta intestazione
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    if (parseRilevazione(line, pazientiMap)) {
                        rilevazioniCaricate++;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Errore parsing rilevazione: " + line, e);
                }
            }

            LOGGER.log(Level.INFO, "Caricate {0} rilevazioni", rilevazioniCaricate);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricando rilevazioni da: " + file, e);
        }
    }

    private boolean parseRilevazione(String line, Map<Integer, Paziente> pazientiMap) {
        String[] campi = line.split(CSV_DELIMITER);
        if (campi.length < 4) return false;

        int pazienteId = Integer.parseInt(campi[ColumnIndex.RILEVAZIONE_PAZIENTE_ID].trim());
        Paziente paziente = pazientiMap.get(pazienteId);

        if (paziente != null) {
            Rilevazione rilevazione = new Rilevazione(
                    LocalDate.parse(campi[ColumnIndex.RILEVAZIONE_DATA].trim()),
                    campi[ColumnIndex.RILEVAZIONE_TIPO_PASTO].trim(),
                    Integer.parseInt(campi[ColumnIndex.RILEVAZIONE_VALORE].trim())
            );
            paziente.aggiungiRilevazione(rilevazione);
            return true;
        }

        return false;
    }

    public void salvaRilevazioni(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(CSV_DELIMITER)[0],
                p -> p.getRilevazioni().stream()
                        .map(r -> String.join(CSV_DELIMITER,
                                String.valueOf(p.getId()),
                                r.getData().toString(),
                                r.getTipoPasto(),
                                String.valueOf(r.getValore())))
                        .toList(),
                CsvHeaders.RILEVAZIONI);
    }

    // ============ GESTIONE TERAPIE ============

    public void caricaTerapie(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int terapieCaricate = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Salta intestazione
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    if (parseTerapia(line, pazientiMap)) {
                        terapieCaricate++;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Errore parsing terapia: " + line, e);
                }
            }

            LOGGER.log(Level.INFO, "Caricate {0} terapie", terapieCaricate);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricando terapie da: " + file, e);
        }
    }

    private boolean parseTerapia(String line, Map<Integer, Paziente> pazientiMap) {
        String[] campi = line.split(CSV_DELIMITER);
        if (campi.length < 9) return false;

        int pazienteId = Integer.parseInt(campi[ColumnIndex.TERAPIA_PAZIENTE_ID].trim());
        Paziente paziente = pazientiMap.get(pazienteId);

        if (paziente != null) {
            Terapia terapia = new Terapia(
                    campi[ColumnIndex.TERAPIA_FARMACO].trim(),
                    Integer.parseInt(campi[ColumnIndex.TERAPIA_ASSUNZIONI].trim()),
                    Double.parseDouble(campi[ColumnIndex.TERAPIA_QUANTITA].trim()),
                    campi[ColumnIndex.TERAPIA_INDICAZIONI].trim(),
                    LocalDate.parse(campi[ColumnIndex.TERAPIA_DATA_INIZIO].trim()),
                    LocalDate.parse(campi[ColumnIndex.TERAPIA_DATA_FINE].trim()),
                    Terapia.Stato.valueOf(campi[ColumnIndex.TERAPIA_STATO].trim()),
                    Integer.parseInt(campi[ColumnIndex.TERAPIA_MEDICO_ID].trim())
            );
            paziente.aggiungiTerapia(terapia);
            return true;
        }

        return false;
    }

    public void salvaTerapie(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(CSV_DELIMITER)[0],
                p -> p.getTerapie().stream()
                        .map(t -> String.join(CSV_DELIMITER,
                                String.valueOf(p.getId()),
                                t.getFarmaco(),
                                String.valueOf(t.getAssunzioniGiornaliere()),
                                String.valueOf(t.getQuantitaPerAssunzione()),
                                t.getIndicazioni(),
                                t.getDataInizio().toString(),
                                t.getDataFine().toString(),
                                t.getStato().toString(),
                                String.valueOf(t.getMedicoId())))
                        .toList(),
                CsvHeaders.TERAPIE);
    }

    // ============ GESTIONE ASSUNZIONI ============

    public void caricaAssunzioni(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        processaCsvFile(file, "assunzioni", line -> parseAssunzione(line, pazientiMap));
    }

    private boolean parseAssunzione(String line, Map<Integer, Paziente> pazientiMap) {
        String[] campi = line.split(CSV_DELIMITER);
        if (campi.length < 5) return false;

        int pazienteId = Integer.parseInt(campi[0].trim());
        Paziente paziente = pazientiMap.get(pazienteId);

        if (paziente != null) {
            Assunzione assunzione = new Assunzione(
                    LocalDate.parse(campi[1].trim()),
                    LocalTime.parse(campi[2].trim()),
                    campi[3].trim(),
                    Double.parseDouble(campi[4].trim())
            );
            paziente.aggiungiAssunzione(assunzione);
            return true;
        }

        return false;
    }

    public void salvaAssunzioni(String filePath, List<Paziente> pazientiModificati) {
        salvaConMerge(filePath, pazientiModificati,
                riga -> riga.split(CSV_DELIMITER)[0],
                p -> p.getAssunzioni().stream()
                        .map(a -> String.join(CSV_DELIMITER,
                                String.valueOf(p.getId()),
                                a.getData().toString(),
                                a.getOra().toString(),
                                a.getFarmaco(),
                                String.valueOf(a.getQuantita())))
                        .toList(),
                CsvHeaders.ASSUNZIONI);
    }

    // ============ GESTIONE SCHEDE CLINICHE ============

    public void caricaSchedeCliniche(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        processaCsvFile(file, "schede cliniche", line -> parseSchedaClinica(line, pazientiMap));
    }

    private boolean parseSchedaClinica(String line, Map<Integer, Paziente> pazientiMap) {
        String[] campi = line.split(CSV_DELIMITER, CSV_SPLIT_LIMIT);
        if (campi.length < 4) return false;

        int pazienteId = Integer.parseInt(campi[0].trim());
        Paziente paziente = pazientiMap.get(pazienteId);

        if (paziente != null) {
            SchedaClinica scheda = new SchedaClinica(
                    campi[1].trim(),
                    campi[2].trim(),
                    campi[3].trim()
            );
            paziente.setSchedaClinica(scheda);
            return true;
        }

        return false;
    }

    public void salvaSchedeCliniche(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(CSV_DELIMITER)[0],
                p -> {
                    SchedaClinica scheda = p.getSchedaClinica();
                    if (scheda == null) scheda = new SchedaClinica();

                    return List.of(String.join(CSV_DELIMITER,
                            String.valueOf(p.getId()),
                            nullSafe(scheda.getFattoriRischio()),
                            nullSafe(scheda.getPregressePatologie()),
                            nullSafe(scheda.getComorbidita())));
                },
                CsvHeaders.SCHEDE_CLINICHE);
    }

    // ============ GESTIONE EVENTI CLINICI ============

    public void caricaEventiClinici(String filePath, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        processaCsvFile(filePath, "eventi clinici", line -> parseEventoClinico(line, pazientiMap));
    }

    private boolean parseEventoClinico(String line, Map<Integer, Paziente> pazientiMap) {
        String[] campi = line.split(CSV_DELIMITER, CSV_SPLIT_LIMIT);
        if (campi.length < 6) return false;

        int pazienteId = Integer.parseInt(campi[0].trim());
        Paziente paziente = pazientiMap.get(pazienteId);

        if (paziente != null) {
            EventoClinico evento = new EventoClinico(
                    campi[1].trim(),
                    campi[2].trim(),
                    LocalDate.parse(campi[3].trim()),
                    campi[4].trim().isEmpty() ? null : LocalTime.parse(campi[4].trim()),
                    campi[5].trim()
            );
            paziente.aggiungiEventoClinico(evento);
            return true;
        }

        return false;
    }

    public void salvaEventiClinici(String file, List<Paziente> pazienti) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        salvaConMerge(file, pazienti,
                riga -> riga.split(CSV_DELIMITER)[0],
                p -> p.getEventiClinici().stream()
                        .map(e -> String.join(CSV_DELIMITER,
                                String.valueOf(p.getId()),
                                e.getTipo(),
                                e.getDescrizione(),
                                e.getData().toString(),
                                e.getOra() != null ? e.getOra().format(formatter) : "",
                                nullSafe(e.getNote())))
                        .toList(),
                CsvHeaders.EVENTI_CLINICI);
    }
    // ============ GESTIONE TERAPIE CONCOMITANTI ============

    public void caricaTerapieConcomitanti(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        processaCsvFile(file, "terapie concomitanti", line -> parseTerapiaConcomitante(line, pazientiMap));
    }

    private boolean parseTerapiaConcomitante(String line, Map<Integer, Paziente> pazientiMap) {
        String[] campi = line.split(CSV_DELIMITER, CSV_SPLIT_LIMIT);
        if (campi.length < 3) return false;

        int pazienteId = Integer.parseInt(campi[0].trim());
        Paziente paziente = pazientiMap.get(pazienteId);

        if (paziente != null) {
            TerapiaConcomitante terapia = new TerapiaConcomitante(
                    campi[1].trim(),
                    campi[2].trim()
            );
            paziente.aggiungiTerapiaConcomitante(terapia);
            return true;
        }

        return false;
    }

    public void salvaTerapieConcomitanti(String file, List<Paziente> pazienti) {
        salvaConMerge(file, pazienti,
                riga -> riga.split(CSV_DELIMITER)[0],
                p -> p.getTerapieConcomitanti().stream()
                        .map(t -> String.join(CSV_DELIMITER,
                                String.valueOf(p.getId()),
                                t.getTipoTerapia(),
                                nullSafe(t.getDescrizione())))
                        .toList(),
                CsvHeaders.TERAPIE_CONCOMITANTI);
    }

    // ============ METODI UTILITY ============

    private Map<Integer, Paziente> createPazientiMap(List<Utente> utenti) {
        return utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .collect(Collectors.toMap(Paziente::getId, p -> p));
    }

    private void processaCsvFile(String file, String tipoEntita, Function<String, Boolean> lineParser) {
        int entitaCaricate = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Salta intestazione
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    if (lineParser.apply(line)) {
                        entitaCaricate++;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Errore parsing " + tipoEntita + ": " + line, e);
                }
            }

            LOGGER.log(Level.INFO, "Caricate {0} {1}", new Object[]{entitaCaricate, tipoEntita});

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricando " + tipoEntita + " da: " + file, e);
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}