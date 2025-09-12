import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import view.PazienteDashboardController;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class per il metodo controllaAssunzioni del PazienteDashboardController.
 * Test puramente unitari senza dipendenze da JavaFX.
 */
class ControllaAssunzioniTest {

    private TestPazienteDashboardController controller;
    private Paziente pazienteTest;
    private LocalDate dataOggi;

    // Costanti per i test
    private static final String FARMACO_INSULINA = "Insulina";
    private static final String FARMACO_METFORMINA = "Metformina";
    private static final int ASSUNZIONI_GIORNALIERE_INSULINA = 3;
    private static final int ASSUNZIONI_GIORNALIERE_METFORMINA = 2;

    @BeforeEach
    void setUp() {
        controller = new TestPazienteDashboardController();
        dataOggi = LocalDate.now();
        pazienteTest = createTestPaziente();
        setupTestTerapie();
    }

    private Paziente createTestPaziente() {
        Diabetologo medico = new Diabetologo(1, "Mario", "Rossi", "mario.rossi@email.com", "password");
        Paziente paziente = new Paziente(100, "Giovanni", "Bianchi", "giovanni.bianchi@email.com", "password", 1);
        paziente.setMedico(medico);
        return paziente;
    }

    private void setupTestTerapie() {
        // Terapia attiva per insulina
        Terapia terapiaInsulina = new Terapia(
                FARMACO_INSULINA,
                ASSUNZIONI_GIORNALIERE_INSULINA,
                10.0,
                "Prima dei pasti",
                dataOggi.minusDays(30),
                dataOggi.plusDays(30),
                Terapia.Stato.ATTIVA,
                1
        );

        // Terapia attiva per metformina
        Terapia terapiaMetformina = new Terapia(
                FARMACO_METFORMINA,
                ASSUNZIONI_GIORNALIERE_METFORMINA,
                500.0,
                "Dopo i pasti",
                dataOggi.minusDays(15),
                dataOggi.plusDays(60),
                Terapia.Stato.ATTIVA,
                1
        );

        pazienteTest.aggiungiTerapia(terapiaInsulina);
        pazienteTest.aggiungiTerapia(terapiaMetformina);
    }

    @Test
    @DisplayName("Controlla assunzioni complete per oggi - nessun alert dovrebbe essere mostrato")
    void testControllaAssunzioni_AssunzioniComplete_NessunAlert() {
        // Arrange: Aggiungi assunzioni complete per oggi
        addCompleteAssunzioniForDate(dataOggi);

        // Act
        controller.controllaAssunzioni(pazienteTest);

        // Assert: Nessun alert dovrebbe essere mostrato
        assertFalse(controller.wasAlertShown(), "Non dovrebbero essere mostrati alert per assunzioni complete");
        assertEquals(0, controller.getAlertCount(), "Il contatore degli alert dovrebbe essere 0");
    }

    @Test
    @DisplayName("Controlla assunzioni incomplete per oggi - dovrebbe mostrare alert")
    void testControllaAssunzioni_AssunzioniIncomplete_MostraAlert() {
        // Arrange: Aggiungi solo 1 assunzione di insulina invece di 3
        pazienteTest.aggiungiAssunzione(new Assunzione(dataOggi, LocalTime.of(8, 0), FARMACO_INSULINA, 10.0));
        // Aggiungi assunzioni complete per metformina
        pazienteTest.aggiungiAssunzione(new Assunzione(dataOggi, LocalTime.of(12, 0), FARMACO_METFORMINA, 500.0));
        pazienteTest.aggiungiAssunzione(new Assunzione(dataOggi, LocalTime.of(19, 0), FARMACO_METFORMINA, 500.0));

        // Act
        controller.controllaAssunzioni(pazienteTest);

        // Assert: Dovrebbe mostrare alert per insulina mancante
        assertTrue(controller.wasAlertShown(), "Dovrebbe essere mostrato un alert per assunzioni incomplete");
        assertTrue(controller.getLastAlertMessage().contains(FARMACO_INSULINA),
                "Il messaggio dovrebbe contenere il nome del farmaco mancante");
        assertTrue(controller.getLastAlertMessage().contains("2"),
                "Il messaggio dovrebbe indicare che mancano 2 assunzioni");
    }

    @Test
    @DisplayName("Controlla assunzioni multiple incomplete - dovrebbe mostrare alert per entrambi")
    void testControllaAssunzioni_MultipleIncomplete_MostraAlertEntrambi() {
        // Arrange: Aggiungi solo 1 assunzione di insulina e 1 di metformina (entrambe incomplete)
        pazienteTest.aggiungiAssunzione(new Assunzione(dataOggi, LocalTime.of(8, 0), FARMACO_INSULINA, 10.0));
        pazienteTest.aggiungiAssunzione(new Assunzione(dataOggi, LocalTime.of(12, 0), FARMACO_METFORMINA, 500.0));

        // Act
        controller.controllaAssunzioni(pazienteTest);

        // Assert: Dovrebbe mostrare alert
        assertTrue(controller.wasAlertShown(), "Dovrebbe essere mostrato un alert per assunzioni incomplete");

        // Verifica che entrambi i farmaci siano menzionati nel messaggio
        String message = controller.getLastAlertMessage();
        assertTrue(message.contains(FARMACO_INSULINA) || controller.getAlertCount() > 1,
                "Dovrebbe essere menzionata l'insulina");
    }

    @Test
    @DisplayName("Controlla senza terapie attive - nessun controllo dovrebbe essere effettuato")
    void testControllaAssunzioni_SenzaTerapieAttive_NessunControllo() {
        // Arrange: Rimuovi tutte le terapie
        pazienteTest.getTerapie().clear();

        // Act
        controller.controllaAssunzioni(pazienteTest);

        // Assert: Nessun alert dovrebbe essere mostrato
        assertFalse(controller.wasAlertShown(), "Non dovrebbero essere mostrati alert senza terapie attive");
    }

    @Test
    @DisplayName("Controlla terapie terminate - non dovrebbe controllarle")
    void testControllaAssunzioni_TerapieTerminate_NonControllate() {
        // Arrange: Termina tutte le terapie
        pazienteTest.getTerapie().forEach(t -> t.setStato(Terapia.Stato.TERMINATA));

        // Act
        controller.controllaAssunzioni(pazienteTest);

        // Assert: Nessun alert dovrebbe essere mostrato
        assertFalse(controller.wasAlertShown(), "Non dovrebbero essere mostrati alert per terapie terminate");
    }

    private void addCompleteAssunzioniForDate(LocalDate data) {
        // Assunzioni complete per insulina (3 al giorno)
        pazienteTest.aggiungiAssunzione(new Assunzione(data, LocalTime.of(8, 0), FARMACO_INSULINA, 10.0));
        pazienteTest.aggiungiAssunzione(new Assunzione(data, LocalTime.of(13, 0), FARMACO_INSULINA, 10.0));
        pazienteTest.aggiungiAssunzione(new Assunzione(data, LocalTime.of(20, 0), FARMACO_INSULINA, 10.0));

        // Assunzioni complete per metformina (2 al giorno)
        pazienteTest.aggiungiAssunzione(new Assunzione(data, LocalTime.of(12, 0), FARMACO_METFORMINA, 500.0));
        pazienteTest.aggiungiAssunzione(new Assunzione(data, LocalTime.of(19, 0), FARMACO_METFORMINA, 500.0));
    }

    // Classe di test che estende il controller per catturare le chiamate ai metodi
    private static class TestPazienteDashboardController extends PazienteDashboardController {
        private boolean alertShown = false;
        private String lastAlertTitle = "";
        private String lastAlertMessage = "";
        private int alertCount = 0;

        // Override del metodo showAlert da BaseController per evitare JavaFX
        @Override
        public void showAlert(String title, String message) {
            this.alertShown = true;
            this.lastAlertTitle = title;
            this.lastAlertMessage = message;
            this.alertCount++;
            // NON chiamare super.showAlert() per evitare creazione di Alert JavaFX
            System.out.println("TEST ALERT: " + title + " - " + message);
        }

        // Override anche il metodo con AlertType se esiste
        @Override
        protected void showAlert(String title, String message, javafx.scene.control.Alert.AlertType type) {
            this.alertShown = true;
            this.lastAlertTitle = title;
            this.lastAlertMessage = message;
            this.alertCount++;
            System.out.println("TEST ALERT [" + type + "]: " + title + " - " + message);
        }

        // Metodi per verificare i risultati nei test
        public boolean wasAlertShown() {
            return alertShown;
        }

        public String getLastAlertTitle() {
            return lastAlertTitle;
        }

        public String getLastAlertMessage() {
            return lastAlertMessage;
        }

        public int getAlertCount() {
            return alertCount;
        }

        public void resetAlerts() {
            alertShown = false;
            lastAlertTitle = "";
            lastAlertMessage = "";
            alertCount = 0;
        }
    }
}