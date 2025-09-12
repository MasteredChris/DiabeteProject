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
 * Refactored DataController following SOLID principles and clean code practices.
 * Handles all data persistence operations for the diabetes management system.
 */
public class DataController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger(DataController.class.getName());
    private static final String CSV_DELIMITER = ",";
    private static final int CSV_SPLIT_LIMIT = -1;

    private final FilePathProvider filePathProvider;
    private final CsvProcessor csvProcessor;
    private final EntityParsers entityParsers;
    private final EntitySerializers entitySerializers;

    public DataController() {
        this.filePathProvider = new FilePathProvider();
        this.csvProcessor = new CsvProcessor();
        this.entityParsers = new EntityParsers();
        this.entitySerializers = new EntitySerializers();
    }

    // ============ PUBLIC API ============

    public List<Utente> caricaUtenti(String utentiFile) {
        try {
            List<Utente> utenti = csvProcessor.loadEntities(utentiFile, entityParsers::parseUtente);
            associaPazientiAiMedici(utenti);
            LOGGER.log(Level.INFO, "Caricati {0} utenti", utenti.size());
            return utenti;
        } catch (DataProcessingException e) {
            handleException("Errore nel caricamento degli utenti", e);
            return new ArrayList<>();
        }
    }

    public void caricaRilevazioni(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int loaded = csvProcessor.loadAndProcessEntities(file,
                line -> entityParsers.parseRilevazione(line, pazientiMap));
        LOGGER.log(Level.INFO, "Caricate {0} rilevazioni", loaded);
    }

    public void caricaTerapie(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int loaded = csvProcessor.loadAndProcessEntities(file,
                line -> entityParsers.parseTerapia(line, pazientiMap));
        LOGGER.log(Level.INFO, "Caricate {0} terapie", loaded);
    }

    public void caricaAssunzioni(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int loaded = csvProcessor.loadAndProcessEntities(file,
                line -> entityParsers.parseAssunzione(line, pazientiMap));
        LOGGER.log(Level.INFO, "Caricate {0} assunzioni", loaded);
    }

    public void caricaSchedeCliniche(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int loaded = csvProcessor.loadAndProcessEntities(file,
                line -> entityParsers.parseSchedaClinica(line, pazientiMap));
        LOGGER.log(Level.INFO, "Caricate {0} schede cliniche", loaded);
    }

    public void caricaEventiClinici(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int loaded = csvProcessor.loadAndProcessEntities(file,
                line -> entityParsers.parseEventoClinico(line, pazientiMap));
        LOGGER.log(Level.INFO, "Caricati {0} eventi clinici", loaded);
    }

    public void caricaTerapieConcomitanti(String file, List<Utente> utenti) {
        Map<Integer, Paziente> pazientiMap = createPazientiMap(utenti);
        int loaded = csvProcessor.loadAndProcessEntities(file,
                line -> entityParsers.parseTerapiaConcomitante(line, pazientiMap));
        LOGGER.log(Level.INFO, "Caricate {0} terapie concomitanti", loaded);
    }

    // Save methods
    public void salvaRilevazioni(List<Paziente> pazienti) {
        csvProcessor.saveWithMerge(
                filePathProvider.getRilevazioniFile(),
                pazienti,
                CsvHeaders.RILEVAZIONI,
                entitySerializers::serializeRilevazioni
        );
    }

    public void salvaTerapie(List<Paziente> pazienti) {
        csvProcessor.saveWithMerge(
                filePathProvider.getTerapieFile(),
                pazienti,
                CsvHeaders.TERAPIE,
                entitySerializers::serializeTerapie
        );
    }

    public void salvaAssunzioni(List<Paziente> pazienti) {
        csvProcessor.saveWithMerge(
                filePathProvider.getAssunzioniFile(),
                pazienti,
                CsvHeaders.ASSUNZIONI,
                entitySerializers::serializeAssunzioni
        );
    }

    public void salvaSchedeCliniche(List<Paziente> pazienti) {
        csvProcessor.saveWithMerge(
                filePathProvider.getSchedeFile(),
                pazienti,
                CsvHeaders.SCHEDE_CLINICHE,
                entitySerializers::serializeSchedeCliniche
        );
    }

    public void salvaEventiClinici(List<Paziente> pazienti) {
        csvProcessor.saveWithMerge(
                filePathProvider.getEventiCliniciFile(),
                pazienti,
                CsvHeaders.EVENTI_CLINICI,
                entitySerializers::serializeEventiClinici
        );
    }

    public void salvaTerapieConcomitanti(List<Paziente> pazienti) {
        csvProcessor.saveWithMerge(
                filePathProvider.getTerapieConcomitantiFile(),
                pazienti,
                CsvHeaders.TERAPIE_CONCOMITANTI,
                entitySerializers::serializeTerapieConcomitanti
        );
    }

    // ============ PRIVATE HELPERS ============

    private Map<Integer, Paziente> createPazientiMap(List<Utente> utenti) {
        return utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .collect(Collectors.toMap(Paziente::getId, p -> p));
    }

    private void associaPazientiAiMedici(List<Utente> utenti) {
        Map<Integer, Diabetologo> mediciMap = utenti.stream()
                .filter(u -> u instanceof Diabetologo)
                .map(u -> (Diabetologo) u)
                .collect(Collectors.toMap(Diabetologo::getId, d -> d));

        long pazientiAssociati = utenti.stream()
                .filter(u -> u instanceof Paziente)
                .map(u -> (Paziente) u)
                .peek(paziente -> associaPazienteAMedico(paziente, mediciMap))
                .filter(p -> p.getMedico() != null)
                .count();

        LOGGER.log(Level.INFO, "Associati {0} pazienti ai rispettivi medici", pazientiAssociati);
    }

    private void associaPazienteAMedico(Paziente paziente, Map<Integer, Diabetologo> mediciMap) {
        Diabetologo medico = mediciMap.get(paziente.getMedicoId());
        if (medico != null) {
            paziente.setMedico(medico);
            medico.addPaziente(paziente);
        } else {
            LOGGER.log(Level.WARNING, "Medico non trovato per paziente ID: {0}", paziente.getId());
        }
    }

    // ============ INNER CLASSES ============

    private static final class CsvHeaders {
        static final String RILEVAZIONI = "pazienteId,data,tipoPasto,valore";
        static final String TERAPIE = "pazienteId,farmaco,assunzioniGiornaliere,quantitaPerAssunzione,indicazioni,dataInizio,dataFine,stato,medicoId";
        static final String ASSUNZIONI = "pazienteId,data,ora,farmaco,quantita";
        static final String SCHEDE_CLINICHE = "pazienteId,fattoriRischio,pregressePatologie,comorbidita";
        static final String EVENTI_CLINICI = "pazienteId,tipo,descrizione,data,ora,note";
        static final String TERAPIE_CONCOMITANTI = "pazienteId,tipoTerapia,descrizione";
    }

    private static final class ColumnIndex {
        static final int UTENTE_ID = 0;
        static final int UTENTE_TYPE = 1;
        static final int UTENTE_NOME = 2;
        static final int UTENTE_COGNOME = 3;
        static final int UTENTE_EMAIL = 4;
        static final int UTENTE_PASSWORD = 5;
        static final int PAZIENTE_MEDICO_ID = 6;
    }

    /**
     * Handles CSV file processing operations with merge capability.
     */
    private class CsvProcessor {

        public <T> List<T> loadEntities(String filePath, Function<String, T> parser) {
            List<T> entities = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String header = br.readLine();
                if (header == null) {
                    LOGGER.log(Level.WARNING, "File vuoto: {0}", filePath);
                    return entities;
                }

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    try {
                        T entity = parser.apply(line);
                        if (entity != null) {
                            entities.add(entity);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Errore parsing linea: " + line, e);
                    }
                }
            } catch (IOException e) {
                throw new DataProcessingException("Errore lettura file: " + filePath, e);
            }

            return entities;
        }

        public int loadAndProcessEntities(String filePath, Function<String, Boolean> processor) {
            int processedCount = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                br.readLine(); // Skip header
                String line;

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    try {
                        if (processor.apply(line)) {
                            processedCount++;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Errore processing linea: " + line, e);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Errore caricamento da: " + filePath, e);
            }

            return processedCount;
        }

        public void saveWithMerge(String filePath, List<Paziente> pazientiModificati,
                                  String header, Function<Paziente, List<String>> serializer) {
            if (pazientiModificati == null || pazientiModificati.isEmpty()) {
                LOGGER.log(Level.WARNING, "Nessun paziente da salvare per: {0}", filePath);
                return;
            }

            List<String> lines = new ArrayList<>();
            Set<String> modifiedPatientIds = pazientiModificati.stream()
                    .map(p -> String.valueOf(p.getId()))
                    .collect(Collectors.toSet());

            // Merge existing data
            try {
                mergeExistingData(filePath, lines, modifiedPatientIds);
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "File non esistente, verrà creato: {0}", filePath);
                lines.add(header);
            }

            // Add modified patient data
            for (Paziente paziente : pazientiModificati) {
                try {
                    lines.addAll(serializer.apply(paziente));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Errore serializzazione paziente ID: " + paziente.getId(), e);
                }
            }

            // Write file
            writeFile(filePath, lines);
        }

        private void mergeExistingData(String filePath, List<String> lines, Set<String> excludePatientIds)
                throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String header = br.readLine();
                if (header != null) {
                    lines.add(header);
                }

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    try {
                        String patientId = line.split(CSV_DELIMITER)[0];
                        if (!excludePatientIds.contains(patientId)) {
                            lines.add(line);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Errore processing existing line: " + line, e);
                    }
                }
            }
        }

        private void writeFile(String filePath, List<String> lines) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
                LOGGER.log(Level.INFO, "File salvato: {0}", filePath);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Errore scrittura file: " + filePath, e);
            }
        }
    }

    /**
     * Handles parsing of different entity types from CSV lines.
     */
    private class EntityParsers {

        public Utente parseUtente(String line) {
            String[] fields = line.split(CSV_DELIMITER);
            if (fields.length < 6) {
                LOGGER.log(Level.WARNING, "Campi insufficienti per utente: {0}", line);
                return null;
            }

            try {
                int id = Integer.parseInt(fields[ColumnIndex.UTENTE_ID].trim());
                String type = fields[ColumnIndex.UTENTE_TYPE].trim();
                String nome = fields[ColumnIndex.UTENTE_NOME].trim();
                String cognome = fields[ColumnIndex.UTENTE_COGNOME].trim();
                String email = fields[ColumnIndex.UTENTE_EMAIL].trim();
                String password = fields[ColumnIndex.UTENTE_PASSWORD].trim();

                return createUtente(type, id, nome, cognome, email, password, fields);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Errore parsing numeri in: " + line, e);
                return null;
            }
        }

        private Utente createUtente(String type, int id, String nome, String cognome,
                                    String email, String password, String[] fields) {
            if (type.equalsIgnoreCase("Paziente")) {
                if (fields.length <= ColumnIndex.PAZIENTE_MEDICO_ID) {
                    LOGGER.log(Level.WARNING, "Paziente senza medico ID");
                    return null;
                }
                int medicoId = Integer.parseInt(fields[ColumnIndex.PAZIENTE_MEDICO_ID].trim());
                return new Paziente(id, nome, cognome, email, password, medicoId);
            } else if (type.equalsIgnoreCase("Diabetologo")) {
                return new Diabetologo(id, nome, cognome, email, password);
            } else {
                LOGGER.log(Level.WARNING, "Tipo utente sconosciuto: {0}", type);
                return null;
            }
        }

        public Boolean parseRilevazione(String line, Map<Integer, Paziente> pazientiMap) {
            String[] fields = line.split(CSV_DELIMITER);
            if (fields.length < 4) return false;

            try {
                int pazienteId = Integer.parseInt(fields[0].trim());
                Paziente paziente = pazientiMap.get(pazienteId);

                if (paziente != null) {
                    Rilevazione rilevazione = new Rilevazione(
                            LocalDate.parse(fields[1].trim()),
                            fields[2].trim(),
                            Integer.parseInt(fields[3].trim())
                    );
                    paziente.aggiungiRilevazione(rilevazione);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Errore parsing rilevazione: " + line, e);
            }
            return false;
        }

        public Boolean parseTerapia(String line, Map<Integer, Paziente> pazientiMap) {
            String[] fields = line.split(CSV_DELIMITER);
            if (fields.length < 9) return false;

            try {
                int pazienteId = Integer.parseInt(fields[0].trim());
                Paziente paziente = pazientiMap.get(pazienteId);

                if (paziente != null) {
                    Terapia terapia = new Terapia(
                            fields[1].trim(),
                            Integer.parseInt(fields[2].trim()),
                            Double.parseDouble(fields[3].trim()),
                            fields[4].trim(),
                            LocalDate.parse(fields[5].trim()),
                            LocalDate.parse(fields[6].trim()),
                            Terapia.Stato.valueOf(fields[7].trim()),
                            Integer.parseInt(fields[8].trim())
                    );
                    paziente.aggiungiTerapia(terapia);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Errore parsing terapia: " + line, e);
            }
            return false;
        }

        public Boolean parseAssunzione(String line, Map<Integer, Paziente> pazientiMap) {
            String[] fields = line.split(CSV_DELIMITER);
            if (fields.length < 5) return false;

            try {
                int pazienteId = Integer.parseInt(fields[0].trim());
                Paziente paziente = pazientiMap.get(pazienteId);

                if (paziente != null) {
                    Assunzione assunzione = new Assunzione(
                            LocalDate.parse(fields[1].trim()),
                            LocalTime.parse(fields[2].trim()),
                            fields[3].trim(),
                            Double.parseDouble(fields[4].trim())
                    );
                    paziente.aggiungiAssunzione(assunzione);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Errore parsing assunzione: " + line, e);
            }
            return false;
        }

        public Boolean parseSchedaClinica(String line, Map<Integer, Paziente> pazientiMap) {
            String[] fields = line.split(CSV_DELIMITER, CSV_SPLIT_LIMIT);
            if (fields.length < 4) return false;

            try {
                int pazienteId = Integer.parseInt(fields[0].trim());
                Paziente paziente = pazientiMap.get(pazienteId);

                if (paziente != null) {
                    SchedaClinica scheda = new SchedaClinica(
                            fields[1].trim(),
                            fields[2].trim(),
                            fields[3].trim()
                    );
                    paziente.setSchedaClinica(scheda);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Errore parsing scheda clinica: " + line, e);
            }
            return false;
        }

        public Boolean parseEventoClinico(String line, Map<Integer, Paziente> pazientiMap) {
            String[] fields = line.split(CSV_DELIMITER, CSV_SPLIT_LIMIT);
            if (fields.length < 6) return false;

            try {
                int pazienteId = Integer.parseInt(fields[0].trim());
                Paziente paziente = pazientiMap.get(pazienteId);

                if (paziente != null) {
                    EventoClinico evento = new EventoClinico(
                            fields[1].trim(),
                            fields[2].trim(),
                            LocalDate.parse(fields[3].trim()),
                            fields[4].trim().isEmpty() ? null : LocalTime.parse(fields[4].trim()),
                            fields[5].trim()
                    );
                    paziente.aggiungiEventoClinico(evento);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Errore parsing evento clinico: " + line, e);
            }
            return false;
        }

        public Boolean parseTerapiaConcomitante(String line, Map<Integer, Paziente> pazientiMap) {
            String[] fields = line.split(CSV_DELIMITER, CSV_SPLIT_LIMIT);
            if (fields.length < 3) return false;

            try {
                int pazienteId = Integer.parseInt(fields[0].trim());
                Paziente paziente = pazientiMap.get(pazienteId);

                if (paziente != null) {
                    TerapiaConcomitante terapia = new TerapiaConcomitante(
                            fields[1].trim(),
                            fields[2].trim()
                    );
                    paziente.aggiungiTerapiaConcomitante(terapia);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Errore parsing terapia concomitante: " + line, e);
            }
            return false;
        }
    }

    /**
     * Handles serialization of different entity types to CSV format.
     */
    private class EntitySerializers {

        public List<String> serializeRilevazioni(Paziente paziente) {
            return paziente.getRilevazioni().stream()
                    .map(r -> String.join(CSV_DELIMITER,
                            String.valueOf(paziente.getId()),
                            r.getData().toString(),
                            r.getTipoPasto(),
                            String.valueOf(r.getValore())))
                    .toList();
        }

        public List<String> serializeTerapie(Paziente paziente) {
            return paziente.getTerapie().stream()
                    .map(t -> String.join(CSV_DELIMITER,
                            String.valueOf(paziente.getId()),
                            t.getFarmaco(),
                            String.valueOf(t.getAssunzioniGiornaliere()),
                            String.valueOf(t.getQuantitaPerAssunzione()),
                            t.getIndicazioni(),
                            t.getDataInizio().toString(),
                            t.getDataFine().toString(),
                            t.getStato().toString(),
                            String.valueOf(t.getMedicoId())))
                    .toList();
        }

        public List<String> serializeAssunzioni(Paziente paziente) {
            return paziente.getAssunzioni().stream()
                    .map(a -> String.join(CSV_DELIMITER,
                            String.valueOf(paziente.getId()),
                            a.getData().toString(),
                            a.getOra().toString(),
                            a.getFarmaco(),
                            String.valueOf(a.getQuantita())))
                    .toList();
        }

        public List<String> serializeSchedeCliniche(Paziente paziente) {
            SchedaClinica scheda = paziente.getSchedaClinica();
            if (scheda == null) scheda = new SchedaClinica();

            return List.of(String.join(CSV_DELIMITER,
                    String.valueOf(paziente.getId()),
                    nullSafe(scheda.getFattoriRischio()),
                    nullSafe(scheda.getPregressePatologie()),
                    nullSafe(scheda.getComorbidita())));
        }

        public List<String> serializeEventiClinici(Paziente paziente) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            return paziente.getEventiClinici().stream()
                    .map(e -> String.join(CSV_DELIMITER,
                            String.valueOf(paziente.getId()),
                            e.getTipo(),
                            e.getDescrizione(),
                            e.getData().toString(),
                            e.getOra() != null ? e.getOra().format(formatter) : "",
                            nullSafe(e.getNote())))
                    .toList();
        }

        public List<String> serializeTerapieConcomitanti(Paziente paziente) {
            return paziente.getTerapieConcomitanti().stream()
                    .map(t -> String.join(CSV_DELIMITER,
                            String.valueOf(paziente.getId()),
                            t.getTipoTerapia(),
                            nullSafe(t.getDescrizione())))
                    .toList();
        }

        private String nullSafe(String value) {
            return value != null ? value : "";
        }
    }

    /**
     * Custom exception for data processing errors.
     */
    private static class DataProcessingException extends RuntimeException {
        public DataProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}