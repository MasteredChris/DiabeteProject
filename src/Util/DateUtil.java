package Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    // Formattatori standard
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Formatta una data/ora per la visualizzazione nell'interfaccia
     * @param date la data/ora da formattare
     * @return stringa formattata (dd/MM/yyyy HH:mm)
     */
    public static String formatDate(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_DATE_TIME_FORMATTER);
    }

    /**
     * Formatta una data per la visualizzazione nell'interfaccia
     * @param date la data da formattare
     * @return stringa formattata (dd/MM/yyyy)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Formatta una data/ora per il salvataggio su CSV
     * @param date la data/ora da formattare
     * @return stringa formattata (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatDateForCSV(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_TIME_FORMATTER);
    }

    /**
     * Formatta una data per il salvataggio su CSV
     * @param date la data da formattare
     * @return stringa formattata (yyyy-MM-dd)
     */
    public static String formatDateForCSV(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Parsa una stringa in LocalDateTime
     * @param dateString stringa da parsare (formato yyyy-MM-dd HH:mm:ss o dd/MM/yyyy HH:mm)
     * @return LocalDateTime parsato o null se errore
     */
    public static LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            // Prova prima il formato CSV
            return LocalDateTime.parse(dateString.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Prova il formato display
                return LocalDateTime.parse(dateString.trim(), DISPLAY_DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e2) {
                System.err.println("Impossibile parsare la data: " + dateString);
                return null;
            }
        }
    }

    /**
     * Parsa una stringa in LocalDate
     * @param dateString stringa da parsare (formato yyyy-MM-dd o dd/MM/yyyy)
     * @return LocalDate parsato o null se errore
     */
    public static LocalDate parseDateOnly(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            // Prova prima il formato CSV
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Prova il formato display
                return LocalDate.parse(dateString.trim(), DISPLAY_DATE_FORMATTER);
            } catch (DateTimeParseException e2) {
                System.err.println("Impossibile parsare la data: " + dateString);
                return null;
            }
        }
    }

    /**
     * Verifica se una data/ora è compresa in un intervallo
     * @param data la data/ora da verificare
     * @param inizio data di inizio (inclusive)
     * @param fine data di fine (inclusive)
     * @return true se la data è nell'intervallo
     */
    public static boolean isInRange(LocalDateTime data, LocalDate inizio, LocalDate fine) {
        if (data == null || inizio == null || fine == null) {
            return false;
        }

        LocalDate dataOnly = data.toLocalDate();
        return !dataOnly.isBefore(inizio) && !dataOnly.isAfter(fine);
    }

    /**
     * Verifica se una data è compresa in un intervallo
     * @param data la data da verificare
     * @param inizio data di inizio (inclusive)
     * @param fine data di fine (inclusive)
     * @return true se la data è nell'intervallo
     */
    public static boolean isInRange(LocalDate data, LocalDate inizio, LocalDate fine) {
        if (data == null || inizio == null || fine == null) {
            return false;
        }

        return !data.isBefore(inizio) && !data.isAfter(fine);
    }

    /**
     * Calcola i giorni tra due date
     * @param inizio data di inizio
     * @param fine data di fine
     * @return numero di giorni (può essere negativo se fine < inizio)
     */
    public static long daysBetween(LocalDate inizio, LocalDate fine) {
        if (inizio == null || fine == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(inizio, fine);
    }

    /**
     * Calcola le ore tra due date/ore
     * @param inizio data/ora di inizio
     * @param fine data/ora di fine
     * @return numero di ore (può essere negativo se fine < inizio)
     */
    public static long hoursBetween(LocalDateTime inizio, LocalDateTime fine) {
        if (inizio == null || fine == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(inizio, fine);
    }

    /**
     * Calcola i minuti tra due date/ore
     * @param inizio data/ora di inizio
     * @param fine data/ora di fine
     * @return numero di minuti (può essere negativo se fine < inizio)
     */
    public static long minutesBetween(LocalDateTime inizio, LocalDateTime fine) {
        if (inizio == null || fine == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(inizio, fine);
    }

    /**
     * Verifica se una data è oggi
     * @param data la data da verificare
     * @return true se la data è oggi
     */
    public static boolean isToday(LocalDate data) {
        if (data == null) {
            return false;
        }
        return data.equals(LocalDate.now());
    }

    /**
     * Verifica se una data/ora è oggi
     * @param data la data/ora da verificare
     * @return true se la data è oggi
     */
    public static boolean isToday(LocalDateTime data) {
        if (data == null) {
            return false;
        }
        return data.toLocalDate().equals(LocalDate.now());
    }

    /**
     * Verifica se una data è nel passato
     * @param data la data da verificare
     * @return true se la data è precedente a oggi
     */
    public static boolean isPast(LocalDate data) {
        if (data == null) {
            return false;
        }
        return data.isBefore(LocalDate.now());
    }

    /**
     * Verifica se una data/ora è nel passato
     * @param data la data/ora da verificare
     * @return true se la data/ora è precedente a adesso
     */
    public static boolean isPast(LocalDateTime data) {
        if (data == null) {
            return false;
        }
        return data.isBefore(LocalDateTime.now());
    }

    /**
     * Verifica se una data è nel futuro
     * @param data la data da verificare
     * @return true se la data è successiva a oggi
     */
    public static boolean isFuture(LocalDate data) {
        if (data == null) {
            return false;
        }
        return data.isAfter(LocalDate.now());
    }

    /**
     * Verifica se una data/ora è nel futuro
     * @param data la data/ora da verificare
     * @return true se la data/ora è successiva a adesso
     */
    public static boolean isFuture(LocalDateTime data) {
        if (data == null) {
            return false;
        }
        return data.isAfter(LocalDateTime.now());
    }

    /**
     * Ottieni l'inizio del giorno per una data
     * @param data la data
     * @return LocalDateTime con ora 00:00:00
     */
    public static LocalDateTime startOfDay(LocalDate data) {
        if (data == null) {
            return null;
        }
        return data.atStartOfDay();
    }

    /**
     * Ottieni la fine del giorno per una data
     * @param data la data
     * @return LocalDateTime con ora 23:59:59
     */
    public static LocalDateTime endOfDay(LocalDate data) {
        if (data == null) {
            return null;
        }
        return data.atTime(23, 59, 59);
    }

    /**
     * Formatta un orario
     * @param time l'orario da formattare
     * @return stringa formattata (HH:mm)
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * Parsa una stringa in LocalTime
     * @param timeString stringa da parsare (formato HH:mm)
     * @return LocalTime parsato o null se errore
     */
    public static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeString.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Impossibile parsare l'orario: " + timeString);
            return null;
        }
    }

    /**
     * Calcola l'età in anni da una data di nascita
     * @param dataNascita la data di nascita
     * @return età in anni
     */
    public static int calculateAge(LocalDate dataNascita) {
        if (dataNascita == null) {
            return 0;
        }
        return (int) ChronoUnit.YEARS.between(dataNascita, LocalDate.now());
    }

    /**
     * Ottieni una descrizione relativa del tempo (es. "2 ore fa", "domani")
     * @param data la data/ora da descrivere
     * @return descrizione relativa
     */
    public static String getRelativeTimeDescription(LocalDateTime data) {
        if (data == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(data, now);

        if (minutes < 1) {
            return "Ora";
        } else if (minutes < 60) {
            return minutes + " minuti fa";
        } else if (minutes < 1440) { // 24 ore
            long hours = minutes / 60;
            return hours == 1 ? "1 ora fa" : hours + " ore fa";
        } else {
            long days = minutes / 1440;
            if (days == 1) {
                return "Ieri";
            } else if (days < 7) {
                return days + " giorni fa";
            } else if (days < 30) {
                long weeks = days / 7;
                return weeks == 1 ? "1 settimana fa" : weeks + " settimane fa";
            } else {
                return formatDate(data);
            }
        }
    }
}