package DAO;

import Model.AssunzioneFarmaco;
import Util.CSVUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssunzioneFarmacoDAO implements GenericDAO<AssunzioneFarmaco> {
    private static final String FILE_PATH = "src/Data/assunzioni.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Header CSV: id,idPaziente,idTerapia,dataOraAssunzione,dataOraPrevista,farmaco,quantitaAssunta,assunta

    @Override
    public void save(AssunzioneFarmaco entity) {
        try {
            List<String[]> existingData = CSVUtil.readCSV(FILE_PATH);

            // Se il file è vuoto, aggiungi l'header
            if (existingData.isEmpty()) {
                String[] header = {"id", "idPaziente", "idTerapia", "dataOraAssunzione", "dataOraPrevista", "farmaco", "quantitaAssunta", "assunta"};
                existingData.add(header);
            }

            // Converti AssunzioneFarmaco in array di stringhe
            String[] assunzioneData = {
                    entity.getId(),
                    entity.getIdPaziente(),
                    entity.getIdTerapia(),
                    entity.getDataOraAssunzione().format(DATE_FORMATTER),
                    entity.getDataOraPrevista().format(DATE_FORMATTER),
                    entity.getFarmaco(),
                    String.valueOf(entity.getQuantitaAssunta()),
                    String.valueOf(entity.isAssunta())
            };

            existingData.add(assunzioneData);
            CSVUtil.writeCSV(FILE_PATH, existingData);

        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio dell'assunzione: " + e.getMessage());
        }
    }

    @Override
    public AssunzioneFarmaco findById(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                String[] row = data.get(i);
                if (row[0].equals(id)) {
                    return parseAssunzioneFarmaco(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dell'assunzione: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<AssunzioneFarmaco> findAll() {
        List<AssunzioneFarmaco> assunzioni = new ArrayList<>();
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                AssunzioneFarmaco assunzione = parseAssunzioneFarmaco(data.get(i));
                if (assunzione != null) {
                    assunzioni.add(assunzione);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento delle assunzioni: " + e.getMessage());
        }
        return assunzioni;
    }

    @Override
    public void update(AssunzioneFarmaco entity) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                if (data.get(i)[0].equals(entity.getId())) {
                    // Aggiorna la riga esistente
                    String[] updatedRow = {
                            entity.getId(),
                            entity.getIdPaziente(),
                            entity.getIdTerapia(),
                            entity.getDataOraAssunzione().format(DATE_FORMATTER),
                            entity.getDataOraPrevista().format(DATE_FORMATTER),
                            entity.getFarmaco(),
                            String.valueOf(entity.getQuantitaAssunta()),
                            String.valueOf(entity.isAssunta())
                    };
                    data.set(i, updatedRow);
                    CSVUtil.writeCSV(FILE_PATH, data);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dell'assunzione: " + e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);
            data.removeIf(row -> row.length > 0 && row[0].equals(id));
            CSVUtil.writeCSV(FILE_PATH, data);
        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione dell'assunzione: " + e.getMessage());
        }
    }

    public List<AssunzioneFarmaco> findByPaziente(String idPaziente) {
        return findAll().stream()
                .filter(assunzione -> assunzione.getIdPaziente().equals(idPaziente))
                .collect(Collectors.toList());
    }

    public List<AssunzioneFarmaco> findByTerapia(String idTerapia) {
        return findAll().stream()
                .filter(assunzione -> assunzione.getIdTerapia().equals(idTerapia))
                .collect(Collectors.toList());
    }

    public List<AssunzioneFarmaco> findAssunzioniMancanti(String idPaziente, LocalDate data) {
        return findAll().stream()
                .filter(assunzione -> assunzione.getIdPaziente().equals(idPaziente))
                .filter(assunzione -> assunzione.getDataOraPrevista().toLocalDate().equals(data))
                .filter(assunzione -> !assunzione.isAssunta())
                .collect(Collectors.toList());
    }

    public List<AssunzioneFarmaco> findByPazienteAndDate(String idPaziente, LocalDate data) {
        return findAll().stream()
                .filter(assunzione -> assunzione.getIdPaziente().equals(idPaziente))
                .filter(assunzione -> assunzione.getDataOraAssunzione().toLocalDate().equals(data))
                .collect(Collectors.toList());
    }

    public List<AssunzioneFarmaco> findByTerapiaAndDateRange(String idTerapia, LocalDate inizio, LocalDate fine) {
        return findAll().stream()
                .filter(assunzione -> assunzione.getIdTerapia().equals(idTerapia))
                .filter(assunzione -> {
                    LocalDate dataAssunzione = assunzione.getDataOraAssunzione().toLocalDate();
                    return !dataAssunzione.isBefore(inizio) && !dataAssunzione.isAfter(fine);
                })
                .collect(Collectors.toList());
    }

    private AssunzioneFarmaco parseAssunzioneFarmaco(String[] row) {
        try {
            if (row.length < 8) {
                System.err.println("Riga CSV assunzione incompleta: " + String.join(",", row));
                return null;
            }

            return new AssunzioneFarmaco(
                    row[0], // id
                    row[1], // idPaziente
                    row[2], // idTerapia
                    LocalDateTime.parse(row[3], DATE_FORMATTER), // dataOraAssunzione
                    LocalDateTime.parse(row[4], DATE_FORMATTER), // dataOraPrevista
                    row[5], // farmaco
                    Double.parseDouble(row[6]), // quantitaAssunta
                    Boolean.parseBoolean(row[7]) // assunta
            );
        } catch (Exception e) {
            System.err.println("Errore nel parsing dell'assunzione farmaco: " + e.getMessage());
            return null;
        }
    }
}