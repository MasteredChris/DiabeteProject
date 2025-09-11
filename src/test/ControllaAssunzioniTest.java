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
 * Versione semplificata senza Mockito.
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

        @Override
        public void showAlert(String title, String message) {
            this.alertShown = true;
            this.lastAlertTitle = title;
            this.lastAlertMessage = message;
        }

        public boolean wasAlertShown() {
            return alertShown;
        }

        public String getLastAlertTitle() {
            return lastAlertTitle;
        }

        public String getLastAlertMessage() {
            return lastAlertMessage;
        }
    }
}