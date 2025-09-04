package DAO;

import Model.Diabetologo;
import Model.Paziente;
import Model.Utente;
import Util.CSVUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UtenteDAO implements GenericDAO<Utente> {
    private static final String FILE_PATH = "src/Data/utenti.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Header CSV: id,username,password,nome,cognome,email,userType,medicoRiferimento,fattoriRischio,patologiePregresse,comorbidita,dataRegistrazione,numeroAlbo,specializzazione

    @Override
    public void save(Utente entity) {
        try {
            List<String[]> existingData = CSVUtil.readCSV(FILE_PATH);

            // Se il file è vuoto, aggiungi l'header
            if (existingData.isEmpty()) {
                String[] header = {"id", "username", "password", "nome", "cognome", "email", "userType",
                        "medicoRiferimento", "fattoriRischio", "patologiePregresse", "comorbidita",
                        "dataRegistrazione", "numeroAlbo", "specializzazione"};
                existingData.add(header);
            }

            String[] utenteData = createUserRow(entity);
            existingData.add(utenteData);
            CSVUtil.writeCSV(FILE_PATH, existingData);

        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio dell'utente: " + e.getMessage());
        }
    }

    @Override
    public Utente findById(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                String[] row = data.get(i);
                if (row[0].equals(id)) {
                    return parseUtente(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dell'utente: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Utente> findAll() {
        List<Utente> utenti = new ArrayList<>();
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                Utente utente = parseUtente(data.get(i));
                if (utente != null) {
                    utenti.add(utente);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento degli utenti: " + e.getMessage());
        }
        return utenti;
    }

    @Override
    public void update(Utente entity) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);

            for (int i = 1; i < data.size(); i++) { // Salta l'header
                if (data.get(i)[0].equals(entity.getId())) {
                    // Aggiorna la riga esistente
                    String[] updatedRow = createUserRow(entity);
                    data.set(i, updatedRow);
                    CSVUtil.writeCSV(FILE_PATH, data);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dell'utente: " + e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        try {
            List<String[]> data = CSVUtil.readCSV(FILE_PATH);
            data.removeIf(row -> row.length > 0 && row[0].equals(id));
            CSVUtil.writeCSV(FILE_PATH, data);
        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione dell'utente: " + e.getMessage());
        }
    }

    public Utente findByUsername(String username) {
        return findAll().stream()
                .filter(utente -> utente.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public List<Paziente> findAllPazienti() {
        return findAll().stream()
                .filter(utente -> utente instanceof Paziente)
                .map(utente -> (Paziente) utente)
                .collect(Collectors.toList());
    }

    public List<Diabetologo> findAllMedici() {
        return findAll().stream()
                .filter(utente -> utente instanceof Diabetologo)
                .map(utente -> (Diabetologo) utente)
                .collect(Collectors.toList());
    }

    public List<Paziente> findPazientiByMedico(String idMedico) {
        return findAllPazienti().stream()
                .filter(paziente -> idMedico.equals(paziente.getMedicoRiferimento()))
                .collect(Collectors.toList());
    }

    public List<Utente> findByEmail(String email) {
        return findAll().stream()
                .filter(utente -> utente.getEmail().equalsIgnoreCase(email))
                .collect(Collectors.toList());
    }

    private String[] createUserRow(Utente entity) {
        String[] row = new String[14]; // 14 colonne

        // Dati base comuni
        row[0] = entity.getId();
        row[1] = entity.getUsername();
        row[2] = entity.getPassword();
        row[3] = entity.getNome();
        row[4] = entity.getCognome();
        row[5] = entity.getEmail();
        row[6] = entity.getUserType();

        // Inizializza i campi specifici come vuoti
        row[7] = ""; // medicoRiferimento
        row[8] = ""; // fattoriRischio
        row[9] = ""; // patologiePregresse
        row[10] = ""; // comorbidita
        row[11] = ""; // dataRegistrazione
        row[12] = ""; // numeroAlbo
        row[13] = ""; // specializzazione

        if (entity instanceof Paziente) {
            Paziente paziente = (Paziente) entity;

            row[7] = paziente.getMedicoRiferimento() != null ? paziente.getMedicoRiferimento() : "";
            row[8] = paziente.getFattoriRischio() != null ? String.join(";", paziente.getFattoriRischio()) : "";
            row[9] = paziente.getPatologiePregresse() != null ? String.join(";", paziente.getPatologiePregresse()) : "";
            row[10] = paziente.getComorbidita() != null ? String.join(";", paziente.getComorbidita()) : "";
            row[11] = paziente.getDataRegistrazione() != null ? paziente.getDataRegistrazione().format(DATE_FORMATTER) : "";

        } else if (entity instanceof Diabetologo) {
            Diabetologo medico = (Diabetologo) entity;

            // Per il medico, i campi specifici del paziente restano vuoti
            // e popoliamo i campi specifici del medico (se esistono nelle future implementazioni)
        }

        return row;
    }

    private Utente parseUtente(String[] row) {
        try {
            if (row.length < 7) {
                System.err.println("Riga CSV utente incompleta: " + String.join(",", row));
                return null;
            }

            String userType = row[6];

            if ("PAZIENTE".equals(userType)) {
                return parsePaziente(row);
            } else if ("MEDICO".equals(userType)) {
                return parseMedico(row);
            } else {
                System.err.println("Tipo utente sconosciuto: " + userType);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Errore nel parsing dell'utente: " + e.getMessage());
            return null;
        }
    }

    private Paziente parsePaziente(String[] row) {
        try {
            // Parse delle liste da stringhe separate da ";"
            List<String> fattoriRischio = parseStringList(row.length > 8 ? row[8] : "");
            List<String> patologiePregresse = parseStringList(row.length > 9 ? row[9] : "");
            List<String> comorbidita = parseStringList(row.length > 10 ? row[10] : "");

            // Parse data registrazione
            LocalDate dataRegistrazione = null;
            if (row.length > 11 && !row[11].trim().isEmpty()) {
                dataRegistrazione = LocalDate.parse(row[11], DATE_FORMATTER);
            }

            Paziente paziente = new Paziente(
                    row[0], // id
                    row[1], // username
                    row[2], // password
                    row[3], // nome
                    row[4], // cognome
                    row[5], // email
                    fattoriRischio,
                    patologiePregresse,
                    comorbidita,
                    dataRegistrazione
            );

            // Imposta medico di riferimento
            if (row.length > 7 && !row[7].trim().isEmpty()) {
                paziente.setMedicoRiferimento(row[7]);
            }

            return paziente;

        } catch (Exception e) {
            System.err.println("Errore nel parsing del paziente: " + e.getMessage());
            return null;
        }
    }

    private Diabetologo parseMedico(String[] row) {
        try {
            Diabetologo medico = new Diabetologo(
                    row[0], // id
                    row[1], // username
                    row[2], // password
                    row[3], // nome
                    row[4], // cognome
                    row[5]  // email
            );

            // Campi aggiuntivi del medico (se presenti in futuro)
            // if (row.length > 12 && !row[12].trim().isEmpty()) {
            //     medico.setNumeroAlbo(row[12]);
            // }
            // if (row.length > 13 && !row[13].trim().isEmpty()) {
            //     medico.setSpecializzazione(row[13]);
            // }

            return medico;

        } catch (Exception e) {
            System.err.println("Errore nel parsing del medico: " + e.getMessage());
            return null;
        }
    }

    private List<String> parseStringList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(str.split(";")));
    }
}