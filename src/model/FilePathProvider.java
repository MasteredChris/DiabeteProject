package model;

/**
 * Centralized file path configuration for the application.
 * This class provides a single point of configuration for all data file paths.
 */
public class FilePathProvider {

    private static final String RESOURCES_PATH = "src/resources/";

    private final String utenti;
    private final String schedeFile;
    private final String rilevazioniFile;
    private final String terapieFile;
    private final String assunzioniFile;
    private final String eventiCliniciFile;
    private final String terapieConcomitantiFile;
    private final String schedeClinicheFile;

    public FilePathProvider() {
        this.schedeFile = RESOURCES_PATH + "schede_cliniche.csv";
        this.rilevazioniFile = RESOURCES_PATH + "rilevazioni.csv";
        this.terapieFile = RESOURCES_PATH + "terapie.csv";
        this.assunzioniFile = RESOURCES_PATH + "assunzioni.csv";
        this.eventiCliniciFile = RESOURCES_PATH + "eventi_clinici.csv";
        this.terapieConcomitantiFile = RESOURCES_PATH + "terapie_concomitanti.csv";
        this.utenti = RESOURCES_PATH + "utenti.csv";
        this.schedeClinicheFile = RESOURCES_PATH + "schede_cliniche.csv";
    }

    public String getSchedeFile() {
        return schedeFile;
    }

    public String getRilevazioniFile() {
        return rilevazioniFile;
    }

    public String getTerapieFile() {
        return terapieFile;
    }

    public String getAssunzioniFile() {
        return assunzioniFile;
    }

    public String getEventiCliniciFile() {
        return eventiCliniciFile;
    }

    public String getTerapieConcomitantiFile() {
        return terapieConcomitantiFile;
    }

    public String getUtenti() { return utenti; }

    public String getSchedeClinicheFile() {
        return schedeClinicheFile;
    }
}