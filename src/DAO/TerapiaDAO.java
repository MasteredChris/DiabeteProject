package DAO;

import Model.Terapia;
import Util.CSVUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TerapiaDAO implements GenericDAO<Terapia> {
    private static final String FILE_PATH = "src/Data/terapie.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Header CSV: id,idPaziente,idMedico,farmaco,assunzioniGiornaliere,quantitaPerAssunzione,indicazioni,dataInizio,dataFine,attiva

    @Override
    public void save(Terapia entity) {
        try {
            List<String[]> existingData = CSVUtil.readCSV(FILE_PATH);

            // Se il file è vuoto, aggiungi l'header
            if (existingData.isEmpty()) {
                String[] header = {"id", "idPaziente", "idMedico", "farmaco", "assunzioniGiornaliere", "quantitaPerAssunzione", "indicazioni", "dataInizio", "dataFine", "attiva"};
                existingData.add(header);
            }

            // Gestisci dataFine nullable
            String dataFineString = entity.getDataFine() != null ?
                    entity.getDataFine().format(DATE_FORMATTER) : "";

            // Converti Terapia in array di stringhe
            String[] terapiaData = {
                    entity.getId(),
                    entity.getIdPaziente(),
                    entity.getIdMedico(),
                    entity.getFarmaco(),
                    String.valueOf(entity.getAssunzioniGiornaliere()),
                    String.valueOf(entity.getQuantitaPerAssunzione()),
                    entity.getIndicazioni() != null ? entity.getIndicazioni() : "",
                    entity.getDataInizio().format(DATE_FORMATTER),
                    dataFineString,
                    String.valueOf(entity.isAttiva())
            };

            existingData.add(terapiaData);
            CSVUtil.writeCSV(FILE_PATH, existingData);

        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio della terapia: " + e.getMessage());
        }
    }

    @Override
    public Terapia findById(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                String[] row = data.get(i);
                if (row[0].equals(id)) {
                    return parseTerapia(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca della terapia: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Terapia> findAll() {
        List<Terapia> terapie = new ArrayList<>();
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                Terapia terapia = parseTerapia(data.get(i));
                if (terapia != null) {
                    terapie.add(terapia);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento delle terapie: " + e.getMessage());
        }
        return terapie;
    }

    @Override
    public void update(Terapia entity) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                if (data.get(i)[0].equals(entity.getId())) {
                    // Aggiorna la riga esistente
                    String dataFineString = entity.getDataFine() != null ?
                            entity.getDataFine().format(DATE_FORMATTER) : "";

                    String[] updatedRow = {
                            entity.getId(),
                            entity.getIdPaziente(),
                            entity.getIdMedico(),
                            entity.getFarmaco(),
                            String.valueOf(entity.getAssunzioniGiornaliere()),
                            String.valueOf(entity.getQuantitaPerAssunzione()),
                            entity.getIndicazioni() != null ? entity.getIndicazioni() : "",
                            entity.getDataInizio().format(DATE_FORMATTER),
                            dataFineString,
                            String.valueOf(entity.isAttiva())
                    };
                    data.set(i, updatedRow);
                    CSVUtil.writeCSV(FILE_PATH, data);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento della terapia: " + e.getMessage());