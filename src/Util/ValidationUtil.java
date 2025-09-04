package Util;

import Model.TipoRilevazione;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    private static final Pattern NOME_COGNOME_PATTERN = Pattern.compile(
            "^[a-zA-ZÀ-ÿ\\s']{2,50}$"
    );

    private static final Pattern FARMACO_PATTERN = Pattern.compile(
            "^[a-zA-ZÀ-ÿ\\s0-9.-]{2,100}$"
    );

    // Costanti per validazione glicemia
    private static final int MIN_GLICEMIA = 20;
    private static final int MAX_GLICEMIA = 600;

    // Range normali glicemia
    private static final int GLICEMIA_PRIMA_PASTO_MIN = 80;
    private static final int GLICEMIA_PRIMA_PASTO_MAX = 130;
    private static final int GLICEMIA_DOPO_PASTO_MAX = 180;

    // Costanti per password
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 50;

    // Costanti per quantità farmaci
    private static final double MIN_QUANTITA_FARMACO = 0.1;
    private static final double MAX_QUANTITA_FARMACO = 1000.0;

    /**
     * Valida un indirizzo email
     * @param email l'email da validare
     * @return true se l'email è valida
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailTrimmed = email.trim();

        // Controlla lunghezza
        if (emailTrimmed.length() > 254) {
            return false;
        }

        // Controlla pattern
        return EMAIL_PATTERN.matcher(emailTrimmed).matches();
    }

    /**
     * Valida un valore di glicemia
     * @param valore il valore della glicemia in mg/dL
     * @param tipo il tipo di rilevazione
     * @return true se il valore è tecnicamente valido (non necessariamente normale)
     */
    public static boolean isValidGlicemia(int valore, TipoRilevazione tipo) {
        return valore >= MIN_GLICEMIA && valore <= MAX_GLICEMIA;
    }

    /**
     * Verifica se un valore di glicemia è normale
     * @param valore il valore della glicemia in mg/dL
     * @param tipo il tipo di rilevazione
     * @return true se il valore è nel range normale
     */
    public static boolean isNormalGlicemia(int valore, TipoRilevazione tipo) {
        if (!isValidGlicemia(valore, tipo)) {
            return false;
        }

        switch (tipo) {
            case PRIMA_PASTO:
                return valore >= GLICEMIA_PRIMA_PASTO_MIN && valore <= GLICEMIA_PRIMA_PASTO_MAX;
            case DOPO_PASTO:
                return valore <= GLICEMIA_DOPO_PASTO_MAX;
            default:
                return false;
        }
    }

    /**
     * Valida una password
     * @param password la password da validare
     * @return true se la password è valida
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        // Controlla lunghezza
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }

        // La password deve contenere almeno:
        // - una lettera minuscola
        // - una lettera maiuscola o un numero
        boolean hasLowerCase = false;
        boolean hasUpperCaseOrDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                hasUpperCaseOrDigit = true;
            }
        }

        return hasLowerCase && hasUpperCaseOrDigit;
    }

    /**
     * Valida uno username
     * @param username lo username da validare
     * @return true se lo username è valido
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String usernameTrimmed = username.trim();
        return USERNAME_PATTERN.matcher(usernameTrimmed).matches();
    }

    /**
     * Valida un nome o cognome
     * @param name il nome da validare
     * @return true se il nome è valido
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String nameTrimmed = name.trim();
        return NOME_COGNOME_PATTERN.matcher(nameTrimmed).matches();
    }

    /**
     * Valida un nome di farmaco
     * @param farmaco il nome del farmaco da validare
     * @return true se il nome del farmaco è valido
     */
    public static boolean isValidFarmaco(String farmaco) {
        if (farmaco == null || farmaco.trim().isEmpty()) {
            return false;
        }

        String farmacoTrimmed = farmaco.trim();
        return FARMACO_PATTERN.matcher(farmacoTrimmed).matches();
    }

    /**
     * Valida una quantità di farmaco
     * @param quantita la quantità da validare
     * @return true se la quantità è valida
     */
    public static boolean isValidQuantitaFarmaco(double quantita) {
        return quantita >= MIN_QUANTITA_FARMACO && quantita <= MAX_QUANTITA_FARMACO;
    }

    /**
     * Valida il numero di assunzioni giornaliere
     * @param assunzioni il numero di assunzioni
     * @return true se il numero è valido
     */
    public static boolean isValidAssunzioniGiornaliere(int assunzioni) {
        return assunzioni >= 1 && assunzioni <= 6;
    }

    /**
     * Valida una stringa generica (non null e non vuota)
     * @param str la stringa da validare
     * @return true se la stringa è valida
     */
    public static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Valida una stringa con lunghezza minima e massima
     * @param str la stringa da validare
     * @param minLength lunghezza minima
     * @param maxLength lunghezza massima
     * @return true se la stringa è valida
     */
    public static boolean isValidString(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }

        String trimmed = str.trim();
        return trimmed.length() >= minLength && trimmed.length() <= maxLength;
    }

    /**
     * Valida un ID (deve essere non null e non vuoto)
     * @param id l'ID da validare
     * @return true se l'ID è valido
     */
    public static boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    /**
     * Valida un numero intero in un range
     * @param value il valore da validare
     * @param min valore minimo (inclusive)
     * @param max valore massimo (inclusive)
     * @return true se il valore è nel range
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Valida un numero double in un range
     * @param value il valore da validare
     * @param min valore minimo (inclusive)
     * @param max valore massimo (inclusive)
     * @return true se il valore è nel range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Valida note/indicazioni (possono essere vuote ma non null)
     * @param note le note da validare
     * @return true se le note sono valide
     */
    public static boolean isValidNote(String note) {
        if (note == null) {
            return false;
        }

        // Le note possono essere vuote, ma se presenti non devono superare 500 caratteri
        return note.length() <= 500;
    }

    /**
     * Ottiene un messaggio di errore per una password non valida
     * @param password la password da controllare
     * @return messaggio di errore o null se la password è valida
     */
    public static String getPasswordErrorMessage(String password) {
        if (password == null) {
            return "La password non può essere null";
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "La password deve essere di almeno " + MIN_PASSWORD_LENGTH + " caratteri";
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "La password non può superare " + MAX_PASSWORD_LENGTH + " caratteri";
        }

        boolean hasLowerCase = false;
        boolean hasUpperCaseOrDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                hasUpperCaseOrDigit = true;
            }
        }

        if (!hasLowerCase) {
            return "La password deve contenere almeno una lettera minuscola";
        }

        if (!hasUpperCaseOrDigit) {
            return "La password deve contenere almeno una lettera maiuscola o un numero";
        }

        return null; // Password valida
    }

    /**
     * Ottiene un messaggio di errore per una glicemia non valida
     * @param valore il valore della glicemia
     * @param tipo il tipo di rilevazione
     * @return messaggio di errore o null se la glicemia è valida
     */
    public static String getGlicemiaErrorMessage(int valore, TipoRilevazione tipo) {
        if (valore < MIN_GLICEMIA) {
            return "Il valore di glicemia non può essere inferiore a " + MIN_GLICEMIA + " mg/dL";
        }

        if (valore > MAX_GLICEMIA) {
            return "Il valore di glicemia non può essere superiore a " + MAX_GLICEMIA + " mg/dL";
        }

        return null; // Glicemia tecnicamente valida
    }

    /**
     * Sanitizza una stringa per l'uso nei CSV (rimuove caratteri problematici)
     * @param str la stringa da sanitizzare
     * @return stringa sanitizzata
     */
    public static String sanitizeForCSV(String str) {
        if (str == null) {
            return "";
        }

        // Rimuove caratteri di controllo e normalizza gli spazi
        return str.replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}