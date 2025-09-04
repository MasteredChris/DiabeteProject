package DAO;

import Model.Alert;
import Model.GradoAllerta;
import Model.TipoAlert;
import Util.CSVUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlertDAO implements GenericDAO<Alert> {
    private static final String FILE_PATH = "src/Data/alert.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Header CSV: id,idPaziente,idMedico,tipo,dataCreazione,messaggio,visualizzato,urgenza

    @Override
    public void save(Alert entity) {
        try {
            List<String[]> existingData = CSVUtil.readCSV(FILE_PATH);

            // Se il file è vuoto, aggiungi l'header
            if (existingData.isEmpty()) {
                String[] header = {"id", "idPaziente", "idMedico", "tipo", "dataCreazione", "messaggio", "visualizzato", "urgenza"};
                existingData.add(header);
            }

            // Converti Alert in array di stringhe
            String[] alertData = {
                    entity.getId(),
                    entity.getIdPaziente(),
                    entity.getIdMedico(),
                    entity.getTipo().toString(),
                    entity.getDataCreazione().format(DATE_FORMATTER),
                    entity.getMessaggio(),
                    String.valueOf(entity.isVisualizzato()),
                    entity.getUrgenza().toString()
            };

            existingData.add(alertData);
            CSVUtil.writeCSV(FILE_PATH, existingData);

        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio dell'alert: " + e.getMessage());
        }
    }

    @Override
    public Alert findById(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                String[] row = data.get(i);
                if (row[0].equals(id)) {
                    return parseAlert(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dell'alert: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Alert> findAll() {
        List<Alert> alerts = new ArrayList<>();
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                Alert alert = parseAlert(data.get(i));
                if (alert != null) {
                    alerts.add(alert);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento degli alert: " + e.getMessage());
        }
        return alerts;
    }

    @Override
    public void update(Alert entity) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                if (data.get(i)[0].equals(entity.getId())) {
                    // Aggiorna la riga esistente
                    String[] updatedRow = {
                            entity.getId(),
                            entity.getIdPaziente(),
                            entity.getIdMedico(),
                            entity.getTipo().toString(),
                            entity.getDataCreazione().format(DATE_FORMATTER),
                            entity.getMessaggio(),
                            String.valueOf(entity.isVisualizzato()),
                            entity.getUrgenza().toString()
                    };
                    data.set(i, updatedRow);
                    CSVUtil.writeCSV(FILE_PATH, data);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dell'alert: " + e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);
            data.removeIf(row -> row.length > 0 && row[0].equals(id));
            CSVUtil.writeCSV(FILE_PATH, data);
        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione dell'alert: " + e.getMessage());
        }
    }

    public List<Alert> findByMedico(String idMedico) {
        return findAll().stream()
                .filter(alert -> alert.getIdMedico().equals(idMedico))
                .collect(Collectors.toList());
    }

    public List<Alert> findNonVisualizzati() {
        return findAll().stream()
                .filter(alert -> !alert.isVisualizzato())
                .collect(Collectors.toList());
    }

    public List<Alert> findByPaziente(String idPaziente) {
        return findAll().stream()
                .filter(alert -> alert.getIdPaziente().equals(idPaziente))
                .collect(Collectors.toList());
    }

    private Alert parseAlert(String[] row) {
        try {
            if (row.length < 8) {
                System.err.println("Riga CSV alert incompleta: " + String.join(",", row));
                return null;
            }

            return new Alert(
                    row[0], // id
                    row[1], // idPaziente
                    row[2], // idMedico
                    TipoAlert.valueOf(row[3]), // tipo
                    LocalDateTime.parse(row[4], DATE_FORMATTER), // dataCreazione
                    row[5], // messaggio
                    Boolean.parseBoolean(row[6]), // visualizzato
                    GradoAllerta.valueOf(row[7]) // urgenza
            );
        } catch (Exception e) {
            System.err.println("Errore nel parsing dell'alert: " + e.getMessage());
            return null;
        }
    }
}