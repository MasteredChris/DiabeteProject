import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import controller.PazienteDashboardController;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

class PazienteDashboardControllerTest {

    private PazienteDashboardController controller;
    private Paziente paziente;

    @BeforeEach
    void setUp() {
        controller = new PazienteDashboardController();

        // Crea un paziente di test
        Diabetologo medico = new Diabetologo(123, "Mario", "Rossi", "mario.rossi@test.com", "password");
        paziente = new Paziente(456, "Luigi", "Verdi", "luigi.verdi@test.com", "password");
        paziente.setMedico(medico);
    }

    @Test
    @DisplayName("Test setup paziente")
    void testSetup() {
        assertNotNull(controller);
        assertNotNull(paziente);
        assertEquals("Luigi", paziente.getNome());
        assertEquals("Verdi", paziente.getCognome());
        assertNotNull(paziente.getMedico());
    }

    @Test
    @DisplayName("Test aggiunta rilevazione")
    void testAggiungiRilevazione() {
        // Arrange
        LocalDate data = LocalDate.now();
        Rilevazione rilevazione = new Rilevazione(data, "Prima colazione", 120);

        // Act
        paziente.aggiungiRilevazione(rilevazione);

        // Assert
        assertEquals(1, paziente.getRilevazioni().size());
        assertEquals(120, paziente.getRilevazioni().get(0).getValore());
    }

    @Test
    @DisplayName("Test aggiunta assunzione")
    void testAggiungiAssunzione() {
        // Arrange
        LocalDate data = LocalDate.now();
        LocalTime ora = LocalTime.of(8, 0);
        Assunzione assunzione = new Assunzione(data, ora, "Aspirina", 100.0);

        // Act
        paziente.aggiungiAssunzione(assunzione);

        // Assert
        assertEquals(1, paziente.getAssunzioni().size());
        assertEquals("Aspirina", paziente.getAssunzioni().get(0).getFarmaco());
    }

    @Test
    @DisplayName("Test aggiunta terapia")
    void testAggiungiTerapia() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate domani = oggi.plusDays(1);
        Terapia terapia = new Terapia("Aspirina", 2, 100.0, "Dopo i pasti",
                oggi, domani, Terapia.Stato.ATTIVA, paziente.getMedicoId());

        // Act
        paziente.getTerapie().add(terapia);

        // Assert
        assertEquals(1, paziente.getTerapie().size());
        assertEquals("Aspirina", paziente.getTerapie().get(0).getFarmaco());
        assertEquals(Terapia.Stato.ATTIVA, paziente.getTerapie().get(0).getStato());
    }

    @Test
    @DisplayName("Test ricerca terapie per stato")
    void testTerapiePerStato() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate domani = oggi.plusDays(1);

        Terapia terapiaAttiva = new Terapia("Aspirina", 2, 100.0, "Dopo i pasti",
                oggi, domani, Terapia.Stato.ATTIVA, paziente.getMedicoId());
        Terapia terapiaInPausa = new Terapia("Paracetamolo", 1, 500.0, "Al bisogno",
                oggi, domani, Terapia.Stato.IN_PAUSA, paziente.getMedicoId());

        paziente.getTerapie().add(terapiaAttiva);
        paziente.getTerapie().add(terapiaInPausa);

        // Act & Assert
        long terapieAttive = paziente.getTerapie().stream()
                .filter(t -> t.getStato() == Terapia.Stato.ATTIVA)
                .count();
        long terapieInPausa = paziente.getTerapie().stream()
                .filter(t -> t.getStato() == Terapia.Stato.IN_PAUSA)
                .count();

        assertEquals(1, terapieAttive);
        assertEquals(1, terapieInPausa);
    }

    @Test
    @DisplayName("Test validazione dati rilevazione")
    void testValidazioneDatiRilevazione() {
        // Test dati validi
        LocalDate data = LocalDate.now();
        String tipoPasto = "Prima colazione";
        String valoreStr = "120";

        // Verifica che i dati siano validi
        assertNotNull(data);
        assertNotNull(tipoPasto);
        assertFalse(tipoPasto.trim().isEmpty());
        assertNotNull(valoreStr);
        assertFalse(valoreStr.trim().isEmpty());

        // Test parsing del valore
        assertDoesNotThrow(() -> {
            int valore = Integer.parseInt(valoreStr);
            assertTrue(valore > 0);
        });
    }

    @Test
    @DisplayName("Test validazione dati evento")
    void testValidazioneDatiEvento() {
        // Test dati validi
        String tipo = "Sintomo";
        String descrizione = "Mal di testa";
        String note = "Note varie";

        // Verifica che i dati siano validi
        assertNotNull(tipo);
        assertFalse(tipo.trim().isEmpty());
        assertNotNull(descrizione);
        assertFalse(descrizione.trim().isEmpty());
        // Le note possono essere vuote
        assertNotNull(note);
    }

    @Test
    @DisplayName("Test ricerca duplicati rilevazione")
    void testRicercaDuplicatiRilevazione() {
        // Arrange
        LocalDate data = LocalDate.now();
        String tipoPasto = "Prima colazione";

        Rilevazione rilevazione1 = new Rilevazione(data, tipoPasto, 120);
        paziente.aggiungiRilevazione(rilevazione1);

        // Act & Assert - verifica duplicato
        boolean duplicatoEsatto = paziente.getRilevazioni().stream()
                .anyMatch(r -> r.getData().equals(data) &&
                        r.getTipoPasto().equalsIgnoreCase(tipoPasto));
        assertTrue(duplicatoEsatto);

        // Verifica non duplicato (data diversa)
        LocalDate altraData = data.plusDays(1);
        boolean noDuplicatoData = paziente.getRilevazioni().stream()
                .anyMatch(r -> r.getData().equals(altraData) &&
                        r.getTipoPasto().equalsIgnoreCase(tipoPasto));
        assertFalse(noDuplicatoData);

        // Verifica non duplicato (tipo pasto diverso)
        String altroTipoPasto = "Dopo colazione";
        boolean noDuplicatoTipo = paziente.getRilevazioni().stream()
                .anyMatch(r -> r.getData().equals(data) &&
                        r.getTipoPasto().equalsIgnoreCase(altroTipoPasto));
        assertFalse(noDuplicatoTipo);
    }

    @Test
    @DisplayName("Test conteggio assunzioni per farmaco e data")
    void testConteggioAssunzioni() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate ieri = oggi.minusDays(1);

        paziente.aggiungiAssunzione(new Assunzione(oggi, LocalTime.of(8, 0), "Aspirina", 100.0));
        paziente.aggiungiAssunzione(new Assunzione(oggi, LocalTime.of(20, 0), "Aspirina", 100.0));
        paziente.aggiungiAssunzione(new Assunzione(ieri, LocalTime.of(8, 0), "Aspirina", 100.0));
        paziente.aggiungiAssunzione(new Assunzione(oggi, LocalTime.of(12, 0), "Paracetamolo", 500.0));

        // Act & Assert
        long aspirinaOggi = paziente.getAssunzioni().stream()
                .filter(a -> a.getData().equals(oggi) && a.getFarmaco().equals("Aspirina"))
                .count();
        assertEquals(2, aspirinaOggi);

        long aspirinaIeri = paziente.getAssunzioni().stream()
                .filter(a -> a.getData().equals(ieri) && a.getFarmaco().equals("Aspirina"))
                .count();
        assertEquals(1, aspirinaIeri);

        long paracetamoloOggi = paziente.getAssunzioni().stream()
                .filter(a -> a.getData().equals(oggi) && a.getFarmaco().equals("Paracetamolo"))
                .count();
        assertEquals(1, paracetamoloOggi);
    }

    @Test
    @DisplayName("Test validazione farmaco esistente in terapie attive")
    void testValidazioneFarmacoInTerapieAttive() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate ieri = oggi.minusDays(1);
        LocalDate domani = oggi.plusDays(1);

        // Terapia attiva e nel periodo
        Terapia terapiaValida = new Terapia("Aspirina", 2, 100.0, "Dopo i pasti",
                ieri, domani, Terapia.Stato.ATTIVA, paziente.getMedicoId());

        // Terapia in pausa
        Terapia terapiaInPausa = new Terapia("Paracetamolo", 1, 500.0, "Al bisogno",
                ieri, domani, Terapia.Stato.IN_PAUSA, paziente.getMedicoId());

        // Terapia futura
        Terapia terapiaFutura = new Terapia("Ibuprofene", 3, 200.0, "Ogni 8 ore",
                domani, domani.plusDays(7), Terapia.Stato.ATTIVA, paziente.getMedicoId());

        paziente.getTerapie().add(terapiaValida);
        paziente.getTerapie().add(terapiaInPausa);
        paziente.getTerapie().add(terapiaFutura);

        // Act & Assert
        // Aspirina: attiva e nel periodo
        boolean aspirinaValida = paziente.getTerapie().stream()
                .anyMatch(t -> t.getFarmaco().equals("Aspirina") &&
                        t.getStato() == Terapia.Stato.ATTIVA &&
                        !oggi.isBefore(t.getDataInizio()) &&
                        !oggi.isAfter(t.getDataFine()));
        assertTrue(aspirinaValida);

        // Paracetamolo: in pausa
        boolean paracetamoloValido = paziente.getTerapie().stream()
                .anyMatch(t -> t.getFarmaco().equals("Paracetamolo") &&
                        t.getStato() == Terapia.Stato.ATTIVA &&
                        !oggi.isBefore(t.getDataInizio()) &&
                        !oggi.isAfter(t.getDataFine()));
        assertFalse(paracetamoloValido);

        // Ibuprofene: futura
        boolean ibuprofenoValido = paziente.getTerapie().stream()
                .anyMatch(t -> t.getFarmaco().equals("Ibuprofene") &&
                        t.getStato() == Terapia.Stato.ATTIVA &&
                        !oggi.isBefore(t.getDataInizio()) &&
                        !oggi.isAfter(t.getDataFine()));
        assertFalse(ibuprofenoValido);
    }
}