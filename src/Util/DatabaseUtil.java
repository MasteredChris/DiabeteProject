package Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility per l'inizializzazione e gestione del "database" CSV
 */
public class DatabaseUtil {

    // Percorsi dei file CSV
    public static final String DATA_DIR = "src/Data/";
    public static final String UTENTI_CSV = DATA_DIR + "utenti.csv";
    public static final String RILEVAZIONI_CSV = DATA_DIR + "rilevazioni.csv";
    public static final String TERAPIE_CSV = DATA_DIR + "terapie.csv";
    public static final String ASSUNZIONI_CSV = DATA_DIR + "assunzioni.csv";
    public static final String ALERT_CSV = DATA_DIR + "alert.csv";

    // Headers per i file CSV
    private static final String[] UTENTI_HEADER = {
            "id", "username", "password", "nome", "cognome", "email", "userType",
            "medicoRiferimento", "fattoriRischio", "patologiePregresse", "comorbidita",
            "dataRegistrazione", "numeroAlbo", "specializzazione"
    };

    private static final String[] RILEVAZIONI_HEADER = {
            "id", "idPaziente", "dataOra", "valoreGlicemia", "tipo", "note", "sintomi"
    };

    private static final String[] TERAPIE_HEADER = {
            "id", "idPaziente", "idMedico", "farmaco", "assunzioniGiornaliere",
            "quantitaPerAssunzione", "indicazioni", "dataInizio", "dataFine", "attiva"
    };

    private static final String[] ASSUNZIONI_HEADER = {
            "id", "idPaziente", "idTerapia", "dataOraAssunzione", "dataOraPrevista",
            "farmaco", "quantitaAssunta", "assunta"
    };

    private static final String[] ALERT_HEADER = {
            "id", "idPaziente", "idMedico", "tipo", "dataCreazione",
            "messaggio", "visualizzato", "urgenza"
    };

    /**
     * Inizializza tutti i file CSV se non esistono
     */
    public static void initializeDatabase() {
        System.out.println("Inizializzazione database CSV...");

        // Crea la directory se non esiste
        createDataDirectory();

        // Inizializza tutti i file CSV
        initializeCSVFile(UTENTI_CSV, UTENTI_HEADER);
        initializeCSVFile(RILEVAZIONI_CSV, RILEVAZIONI_HEADER);
        initializeCSVFile(TERAPIE_CSV, TERAPIE_HEADER);
        initializeCSVFile(ASSUNZIONI_CSV, ASSUNZIONI_HEADER);
        initializeCSVFile(ALERT_CSV, ALERT_HEADER);

        System.out.println("Database CSV inizializzato correttamente");
    }

    /**
     * Crea la directory dei dati se non esiste
     */
    private static void createDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("Directory " + DATA_DIR + " creata");
            } else {
                System.err.println("Impossibile creare la directory " + DATA_DIR);
            }
        }
    }

    /**
     * Inizializza un singolo file CSV con il suo header se non esiste
     */
    private static void initializeCSVFile(String filePath, String[] header) {
        if (!CSVUtil.csvExists(filePath)) {
            CSVUtil.createCSVWithHeader(filePath, header);
            System.out.println("File " + filePath + " inizializzato");
        } else {
            // Verifica che l'header sia corretto
            if (!CSVUtil.validateCSVHeader(filePath, header)) {
                System.err.println("ATTENZIONE: Header non valido per " + filePath);
            }
        }
    }

    /**
     * Verifica l'integrità di tutti i file del database
     */
    public static boolean validateDatabase() {
        System.out.println("Verifica integrità database...");

        boolean isValid = true;

        // Controlla che tutti i file esistano
        String[] files = {UTENTI_CSV, RILEVAZIONI_CSV, TERAPIE_CSV, ASSUNZIONI_CSV, ALERT_CSV};
        String[][] headers = {UTENTI_HEADER, RILEVAZIONI_HEADER, TERAPIE_HEADER, ASSUNZIONI_HEADER, ALERT_HEADER};

        for (int i = 0; i < files.length; i++) {
            if (!CSVUtil.csvExists(files[i])) {
                System.err.println("ERRORE: File mancante - " + files[i]);
                isValid = false;
            } else if (!CSVUtil.validateCSVHeader(files[i], headers[i])) {
                System.err.println("ERRORE: Header non valido - " + files[i]);
                isValid = false;
            }
        }

        if (isValid) {
            System.out.println("Database integro");
        } else {
            System.err.println("Database corrotto - eseguire initializeDatabase()");
        }

        return isValid;
    }

    /**
     * Ottieni statistiche del database
     */
    public static void printDatabaseStats() {
        System.out.println("\n=== STATISTICHE DATABASE ===");

        int utenti = CSVUtil.countDataRows(UTENTI_CSV);
        int rilevazioni = CSVUtil.countDataRows(RILEVAZIONI_CSV);
        int terapie = CSVUtil.countDataRows(TERAPIE_CSV);
        int assunzioni = CSVUtil.countDataRows(ASSUNZIONI_CSV);
        int alert = CSVUtil.countDataRows(ALERT_CSV);

        System.out.println("Utenti registrati: " + utenti);
        System.out.println("Rilevazioni: " + rilevazioni);
        System.out.println("Terapie: " + terapie);
        System.out.println("Assunzioni farmaci: " + assunzioni);
        System.out.println("Alert generati: " + alert);
        System.out.println("=============================\n");
    }

    /**
     * Pulisce tutti i dati (mantiene solo gli header)
     */
    public static void cleanDatabase() {
        System.out.println("Pulizia database...");

        CSVUtil.createCSVWithHeader(UTENTI_CSV, UTENTI_HEADER);
        CSVUtil.createCSVWithHeader(RILEVAZIONI_CSV, RILEVAZIONI_HEADER);
        CSVUtil.createCSVWithHeader(TERAPIE_CSV, TERAPIE_HEADER);
        CSVUtil.createCSVWithHeader(ASSUNZIONI_CSV, ASSUNZIONI_HEADER);
        CSVUtil.createCSVWithHeader(ALERT_CSV, ALERT_HEADER);

        System.out.println("Database pulito");
    }

    /**
     * Backup del database in una cartella con timestamp
     */
    public static void backupDatabase() {
        String timestamp = java.time.LocalDateTime.now().toString().replaceAll("[:.]", "-");
        String backupDir = "backup/backup_" + timestamp + "/";

        try {
            File backupDirFile = new File(backupDir);
            backupDirFile.mkdirs();

            copyFile(UTENTI_CSV, backupDir + "utenti.csv");
            copyFile(RILEVAZIONI_CSV, backupDir + "rilevazioni.csv");
            copyFile(TERAPIE_CSV, backupDir + "terapie.csv");
            copyFile(ASSUNZIONI_CSV, backupDir + "assunzioni.csv");
            copyFile(ALERT_CSV, backupDir + "alert.csv");

            System.out.println("Backup creato in: " + backupDir);

        } catch (Exception e) {
            System.err.println("Errore durante il backup: " + e.getMessage());
        }
    }

    /**
     * Copia un file
     */
    private static void copyFile(String source, String destination) {
        try {
            java.nio.file.Files.copy(
                    java.nio.file.Paths.get(source),
                    java.nio.file.Paths.get(destination),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        } catch (Exception e) {
            System.err.println("Errore copia file " + source + ": " + e.getMessage());
        }
    }

    /**
     * Verifica se il database è stato inizializzato
     */
    public static boolean isDatabaseInitialized() {
        return CSVUtil.csvExists(UTENTI_CSV) &&
                CSVUtil.csvExists(RILEVAZIONI_CSV) &&
                CSVUtil.csvExists(TERAPIE_CSV) &&
                CSVUtil.csvExists(ASSUNZIONI_CSV) &&
                CSVUtil.csvExists(ALERT_CSV);
    }

    /**
     * Ripara il database ricreando i file mancanti
     */
    public static void repairDatabase() {
        System.out.println("Riparazione database...");

        if (!CSVUtil.csvExists(UTENTI_CSV)) {
            CSVUtil.createCSVWithHeader(UTENTI_CSV, UTENTI_HEADER);
            System.out.println("File utenti ricreato");
        }

        if (!CSVUtil.csvExists(RILEVAZIONI_CSV)) {
            CSVUtil.createCSVWithHeader(RILEVAZIONI_CSV, RILEVAZIONI_HEADER);
            System.out.println("File rilevazioni ricreato");
        }

        if (!CSVUtil.csvExists(TERAPIE_CSV)) {
            CSVUtil.createCSVWithHeader(TERAPIE_CSV, TERAPIE_HEADER);
            System.out.println("File terapie ricreato");
        }

        if (!CSVUtil.csvExists(ASSUNZIONI_CSV)) {
            CSVUtil.createCSVWithHeader(ASSUNZIONI_CSV, ASSUNZIONI_HEADER);
            System.out.println("File assunzioni ricreato");
        }

        if (!CSVUtil.csvExists(ALERT_CSV)) {
            CSVUtil.createCSVWithHeader(ALERT_CSV, ALERT_HEADER);
            System.out.println("File alert ricreato");
        }

        System.out.println("Riparazione completata");
    }
}