package DAO;

import Model.Rilevazione;
import Model.TipoRilevazione;
import Util.CSVUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RilevazioneDAO implements GenericDAO<Rilevazione> {
    private static final String FILE_PATH = "src/Data/rilevazioni.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Header CSV: id,idPaziente,dataOra,valoreGlicemia,tipo,note,sintomi

    @Override
    public void save(Rilevazione entity) {
        try {
            List<String[]> existingData = CSVUtil.readCSV(FILE_PATH);

            // Se il file è vuoto, aggiungi l'header
            if (existingData.isEmpty()) {
                String[] header = {"id", "idPaziente", "dataOra", "valoreGlicemia", "tipo", "note", "sintomi"};
                existingData.add(header);
            }

            // Converti la lista di sintomi in stringa separata da ";"
            String sintomiString = entity.getSintomi() != null ?
                    String.join(";", entity.getSintomi()) : "";

            // Converti Rilevazione in array di stringhe
            String[] rilevazioneData = {
                    entity.getId(),
                    entity.getIdPaziente(),
                    entity.getDataOra().format(DATE_FORMATTER),
                    String.valueOf(entity.getValoreGlicemia()),
                    entity.getTipo().toString(),
                    entity.getNote() != null ? entity.getNote() : "",
                    sintomiString
            };

            existingData.add(rilevazioneData);
            CSVUtil.writeCSV(FILE_PATH, existingData);

        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio della rilevazione: " + e.getMessage());
        }
    }

    @Override
    public Rilevazione findById(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                String[] row = data.get(i);
                if (row[0].equals(id)) {
                    return parseRilevazione(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca della rilevazione: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Rilevazione> findAll() {
        List<Rilevazione> rilevazioni = new ArrayList<>();
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                Rilevazione rilevazione = parseRilevazione(data.get(i));
                if (rilevazione != null) {
                    rilevazioni.add(rilevazione);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento delle rilevazioni: " + e.getMessage());
        }
        return rilevazioni;
    }

    @Override
    public void update(Rilevazione entity) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                if (data.get(i)[0].equals(entity.getId())) {
                    // Aggiorna la riga esistente
                    String sintomiString = entity.getSintomi() != null ?
                            String.join(";", entity.getSintomi()) : "";

                    String[] updatedRow = {
                            entity.getId(),
                            entity.getIdPaziente(),
                            entity.getDataOra().format(DATE_FORMATTER),
                            String.valueOf(entity.getValoreGlicemia()),
                            entity.getTipo().toString(),
                            entity.getNote() != null ? entity.getNote() : "",
                            sintomiString
                    };
                    data.set(i, updatedRow);
                    CSVUtil.writeCSV(FILE_PATH, data);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento della rilevazione: " + e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);
            data.removeIf(row -> row.length > 0 && row[0].equals(id));
            CSVUtil.writeCSV(FILE_PATH, data);
        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione della rilevazione: " + e.getMessage());
        }
    }

    public List<Rilevazione> findByPaziente(String idPaziente) {
        return findAll().stream()
                .filter(rilevazione -> rilevazione.getIdPaziente().equals(idPaziente))
                .sorted((r1, r2) -> r2.getDataOra().compareTo(r1.getDataOra())) // Più recenti prima
                .collect(Collectors.toList());
    }

    public List<Rilevazione> findByPazienteAndPeriodo(String idPaziente, LocalDate inizio, LocalDate fine) {
        return findAll().stream()
                .filter(rilevazione -> rilevazione.getIdPaziente().equals(idPaziente))
                .filter(rilevazione -> {
                    LocalDate dataRilevazione = rilevazione.getDataOra().toLocalDate();
                    return !dataRilevazione.isBefore(inizio) && !dataRilevazione.isAfter(fine);
                })
                .sorted((r1, r2) -> r2.getDataOra().compareTo(r1.getDataOra())) // Più recenti prima
                .collect(Collectors.toList());
    }

    public List<Rilevazione> findRilevazioniAnomale() {
        return findAll().stream()
                .filter(rilevazione -> !rilevazione.isNormale())
                .sorted((r1, r2) -> r2.getDataOra().compareTo(r1.getDataOra())) // Più recenti prima
                .collect(Collectors.toList());
    }

    public List<Rilevazione> findByTipo(TipoRilevazione tipo) {
        return findAll().stream()
                .filter(rilevazione -> rilevazione.getTipo() == tipo)
                .sorted((r1, r2) -> r2.getDataOra().compareTo(r1.getDataOra()))
                .collect(Collectors.toList());
    }

    public List<Rilevazione> findByPazienteAndTipo(String idPaziente, TipoRilevazione tipo) {
        return findAll().stream()
                .filter(rilevazione -> rilevazione.getIdPaziente().equals(idPaziente))
                .filter(rilevazione -> rilevazione.getTipo() == tipo)
                .sorted((r1, r2) -> r2.getDataOra().compareTo(r1.getDataOra()))
                .collect(Collectors.toList());
    }

    public List<Rilevazione> findRilevazioniUrgenti() {
        return findAll().stream()
                .filter(rilevazione -> rilevazione.getGradoAllerta().toString().equals("URGENTE"))
                .sorted((r1, r2) -> r2.getDataOra().compareTo(r1.getDataOra()))
                .collect(Collectors.toList());
    }

    private Rilevazione parseRilevazione(String[] row) {
        try {
            if (row.length < 7) {
                System.err.println("Riga CSV rilevazione incompleta: " + String.join(",", row));
                return null;
            }

            // Parse sintomi dalla stringa separata da ";"
            List<String> sintomi = new ArrayList<>();
            if (row[6] != null && !row[6].trim().isEmpty()) {
                sintomi = Arrays.asList(row[6].split(";"));
            }

            return new Rilevazione(
                    row[0], // id
                    row[1], // idPaziente
                    LocalDateTime.parse(row[2], DATE_FORMATTER), // dataOra
                    Integer.parseInt(row[3]), // valoreGlicemia
                    TipoRilevazione.valueOf(row[4]), // tipo
                    row[5], // note
                    sintomi // sintomi
            );
        } catch (Exception e) {
            System.err.println("Errore nel parsing della rilevazione: " + e.getMessage());
            return null;
        }
    }
}