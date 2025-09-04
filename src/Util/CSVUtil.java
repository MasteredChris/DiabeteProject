package Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {
    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_QUOTE = "\"";

    /**
     * Legge un file CSV e restituisce una lista di array di stringhe
     * @param filename il path del file CSV
     * @return lista di righe, ogni riga è un array di stringhe
     */
    public static List<String[]> readCSV(String filename) {
        List<String[]> data = new ArrayList<>();

        try {
            File file = new File(filename);

            // Se il file non esiste, restituisce lista vuota
            if (!file.exists()) {
                return data;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // Salta le righe vuote
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] row = parseCSVLine(line);
                    data.add(row);
                }
            }

        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file CSV " + filename + ": " + e.getMessage());
        }

        return data;
    }

    /**
     * Scrive una lista di array di stringhe in un file CSV
     * @param filename il path del file CSV
     * @param data la lista di dati da scrivere
     */
    public static void writeCSV(String filename, List<String[]> data) {
        try {
            // Crea le directory se non esistono
            File file = new File(filename);
            file.getParentFile().mkdirs();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                for (String[] row : data) {
                    String csvLine = formatCSVLine(row);
                    writer.write(csvLine);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file CSV " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Aggiunge una riga alla fine di un file CSV esistente
     * @param filename il path del file CSV
     * @param row l'array di stringhe da aggiungere
     */
    public static void appendToCSV(String filename, String[] row) {
        try {
            // Crea le directory se non esistono
            File file = new File(filename);
            file.getParentFile().mkdirs();

            // Se il file non esiste, crea un nuovo file
            boolean fileExists = file.exists();

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

                String csvLine = formatCSVLine(row);
                writer.write(csvLine);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'aggiunta al file CSV " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Parsa una riga CSV gestendo virgole e virgolette
     * @param line la riga da parsare
     * @return array di stringhe
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Doppia virgoletta = virgoletta letterale
                    currentField.append('"');
                    i++; // Salta la prossima virgoletta
                } else {
                    // Inizio o fine campo quoted
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Separatore di campo (solo se non siamo dentro le virgolette)
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                // Carattere normale
                currentField.append(c);
            }
        }

        // Aggiungi l'ultimo campo
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    /**
     * Formatta un array di stringhe in una riga CSV
     * @param row l'array di stringhe
     * @return la riga CSV formattata
     */
    private static String formatCSVLine(String[] row) {
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                line.append(CSV_SEPARATOR);
            }

            String field = row[i] != null ? row[i] : "";

            // Se il campo contiene virgole, virgolette o newline, lo mettiamo tra virgolette
            if (field.contains(CSV_SEPARATOR) || field.contains(CSV_QUOTE) || field.contains("\n") || field.contains("\r")) {
                line.append(CSV_QUOTE);
                // Escape delle virgolette doppie
                line.append(field.replace(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE));
                line.append(CSV_QUOTE);
            } else {
                line.append(field);
            }
        }

        return line.toString();
    }

    /**
     * Conta il numero di righe in un file CSV (escludendo l'header)
     * @param filename il path del file CSV
     * @return numero di righe di dati (senza header)
     */
    public static int countDataRows(String filename) {
        List<String[]> data = readCSV(filename);
        return data.size() > 0 ? data.size() - 1 : 0; // -1 per escludere header
    }

    /**
     * Verifica se un file CSV esiste ed ha almeno l'header
     * @param filename il path del file CSV
     * @return true se il file esiste ed ha almeno una riga
     */
    public static boolean csvExists(String filename) {
        File file = new File(filename);
        return file.exists() && file.length() > 0;
    }

    /**
     * Crea un file CSV vuoto con solo l'header
     * @param filename il path del file CSV
     * @param header l'array di stringhe che rappresenta l'header
     */
    public static void createCSVWithHeader(String filename, String[] header) {
        List<String[]> data = new ArrayList<>();
        data.add(header);
        writeCSV(filename, data);
    }

    /**
     * Legge solo l'header di un file CSV
     * @param filename il path del file CSV
     * @return l'array di stringhe dell'header, o null se il file non esiste
     */
    public static String[] readCSVHeader(String filename) {
        List<String[]> data = readCSV(filename);
        return data.isEmpty() ? null : data.get(0);
    }

    /**
     * Verifica se un file CSV ha l'header corretto
     * @param filename il path del file CSV
     * @param expectedHeader l'header atteso
     * @return true se l'header corrisponde
     */
    public static boolean validateCSVHeader(String filename, String[] expectedHeader) {
        String[] actualHeader = readCSVHeader(filename);

        if (actualHeader == null || actualHeader.length != expectedHeader.length) {
            return false;
        }

        for (int i = 0; i < expectedHeader.length; i++) {
            if (!expectedHeader[i].equals(actualHeader[i])) {
                return false;
            }
        }

        return true;
    }
}