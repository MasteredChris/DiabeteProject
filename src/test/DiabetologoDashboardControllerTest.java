import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import controller.DiabetologoDashboardController;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

class DiabetologoDashboardControllerTest {

    private DiabetologoDashboardController controller;
    private Diabetologo diabetologo;
    private Paziente paziente1;
    private Paziente paziente2;

    @BeforeEach
    void setUp() {
        controller = new DiabetologoDashboardController();

        // Crea diabetologo di test
        diabetologo = new Diabetologo(1, "Mario", "Rossi", "mario.rossi@test.com", "password");

        // Crea pazienti di test
        paziente1 = new Paziente(101, "Luigi", "Verdi", "luigi.verdi@test.com", "password");
        paziente1.setMedico(diabetologo);

        paziente2 = new Paziente(102, "Anna", "Bianchi", "anna.bianchi@test.com", "password");
        paziente2.setMedico(diabetologo);

        // Aggiungi pazienti al diabetologo
        diabetologo.getPazienti().add(paziente1);
        diabetologo.getPazienti().add(paziente2);
    }

    @Test
    @DisplayName("Test setup iniziale")
    void testSetup() {
        assertNotNull(controller);
        assertNotNull(diabetologo);
        assertEquals(2, diabetologo.getPazienti().size());
        assertEquals("Mario", diabetologo.getNome());
        assertEquals("Rossi", diabetologo.getCognome());
    }

    @Test
    @DisplayName("Test ordinamento rilevazioni per data e tipo pasto")
    void testOrdinamentoRilevazioni() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate ieri = oggi.minusDays(1);

        paziente1.aggiungiRilevazione(new Rilevazione(oggi, "Prima colazione", 120));
        paziente1.aggiungiRilevazione(new Rilevazione(oggi, "Dopo colazione", 140));
        paziente1.aggiungiRilevazione(new Rilevazione(ieri, "Prima colazione", 110));
        paziente1.aggiungiRilevazione(new Rilevazione(oggi, "Prima pranzo", 130));

        // Act - Simula l'ordinamento che farebbe il controller
        List<Rilevazione> rilevazioni = new ArrayList<>(paziente1.getRilevazioni());
        rilevazioni.sort((r1, r2) -> {
            int dateCompare = r2.getData().compareTo(r1.getData()); // Più recenti prima
            if (dateCompare != 0) return dateCompare;

            // Ordina per tipo pasto (simulando ORDINE_PASTI)
            int ordine1 = getOrdinePasto(r1.getTipoPasto());
            int ordine2 = getOrdinePasto(r2.getTipoPasto());
            return Integer.compare(ordine2, ordine1);
        });

        // Assert
        assertEquals(4, rilevazioni.size());
        assertEquals(oggi, rilevazioni.get(0).getData()); // Prima oggi
        assertEquals("Prima colazione", rilevazioni.get(0).getTipoPasto()); // Prima colazione prima
        assertEquals("Dopo colazione", rilevazioni.get(1).getTipoPasto());
        assertEquals("Prima pranzo", rilevazioni.get(2).getTipoPasto());
        assertEquals(ieri, rilevazioni.get(3).getData()); // Ieri per ultimo
    }

    private int getOrdinePasto(String tipoPasto) {
        return switch (tipoPasto) {
            case "Prima colazione" -> 6;
            case "Dopo colazione" -> 5;
            case "Prima pranzo" -> 4;
            case "Dopo pranzo" -> 3;
            case "Prima cena" -> 2;
            case "Dopo cena" -> 1;
            default -> Integer.MAX_VALUE;
        };
    }

    @Test
    @DisplayName("Test determinazione colore per rilevazioni pre-pasto")
    void testColoreRilevazioniPrePasto() {
        // Test per valori prima del pasto (normale: 80-130 mg/dL)

        // Ipoglicemia
        String coloreIpo = determineColorForPreMeal(70);
        assertEquals("deepskyblue", coloreIpo);

        // Normale
        String coloreNormale = determineColorForPreMeal(100);
        assertEquals("lightgreen", coloreNormale);

        String coloreLimiteSuperiore = determineColorForPreMeal(130);
        assertEquals("lightgreen", coloreLimiteSuperiore);

        // Elevato moderato
        String coloreElevato = determineColorForPreMeal(150);
        assertEquals("khaki", coloreElevato);

        // Molto elevato
        String coloreMoltoElevato = determineColorForPreMeal(200);
        assertEquals("orange", coloreMoltoElevato);
    }

    @Test
    @DisplayName("Test determinazione colore per rilevazioni post-pasto")
    void testColoreRilevazioniPostPasto() {
        // Test per valori dopo il pasto (normale: <180 mg/dL)

        // Normale
        String coloreNormale = determineColorForPostMeal(150);
        assertEquals("lightgreen", coloreNormale);

        String coloreLimiteSuperiore = determineColorForPostMeal(179);
        assertEquals("lightgreen", coloreLimiteSuperiore);

        // Elevato moderato
        String coloreElevato = determineColorForPostMeal(200);
        assertEquals("orange", coloreElevato);

        String coloreLimiteElevato = determineColorForPostMeal(250);
        assertEquals("orange", coloreLimiteElevato);

        // Molto elevato
        String coloreMoltoElevato = determineColorForPostMeal(300);
        assertEquals("tomato", coloreMoltoElevato);
    }

    @Test
    @DisplayName("Test aggiornamento automatico stato terapie")
    void testAggiornamentoStatoTerapie() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate ieri = oggi.minusDays(1);
        LocalDate domani = oggi.plusDays(1);

        // Terapia che dovrebbe essere terminata automaticamente
        Terapia terapiaScaduta = new Terapia("Aspirina", 2, 100.0, "Dopo i pasti",
                ieri.minusDays(5), ieri, Terapia.Stato.ATTIVA, diabetologo.getId());

        // Terapia ancora valida
        Terapia terapiaValida = new Terapia("Paracetamolo", 1, 500.0, "Al bisogno",
                ieri, domani, Terapia.Stato.ATTIVA, diabetologo.getId());

        paziente1.getTerapie().add(terapiaScaduta);
        paziente1.getTerapie().add(terapiaValida);

        // Act - Simula updateTerapieStatus
        for (Terapia terapia : paziente1.getTerapie()) {
            if (terapia.getStato() == Terapia.Stato.ATTIVA && oggi.isAfter(terapia.getDataFine())) {
                terapia.setStato(Terapia.Stato.TERMINATA);
            }
        }

        // Assert
        assertEquals(Terapia.Stato.TERMINATA, terapiaScaduta.getStato());
        assertEquals(Terapia.Stato.ATTIVA, terapiaValida.getStato());
    }

    @Test
    @DisplayName("Test validazione cambio stato terapia")
    void testValidazioneCambioStatoTerapia() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate ieri = oggi.minusDays(1);
        LocalDate domani = oggi.plusDays(1);

        // Terapia nel periodo valido
        Terapia terapiaValida = new Terapia("Aspirina", 2, 100.0, "Dopo i pasti",
                ieri, domani, Terapia.Stato.ATTIVA, diabetologo.getId());

        // Terapia futura
        Terapia terapiaFutura = new Terapia("Paracetamolo", 1, 500.0, "Al bisogno",
                domani, domani.plusDays(7), Terapia.Stato.ATTIVA, diabetologo.getId());

        // Terapia passata
        Terapia terapiaPassata = new Terapia("Ibuprofene", 3, 200.0, "Ogni 8 ore",
                ieri.minusDays(10), ieri.minusDays(5), Terapia.Stato.TERMINATA, diabetologo.getId());

        // Act & Assert - Simula isValidStatusChange
        assertTrue(isValidStatusChange(terapiaValida, oggi));
        assertFalse(isValidStatusChange(terapiaFutura, oggi));
        assertFalse(isValidStatusChange(terapiaPassata, oggi));
    }

    private boolean isValidStatusChange(Terapia terapia, LocalDate oggi) {
        return !oggi.isBefore(terapia.getDataInizio()) && !oggi.isAfter(terapia.getDataFine());
    }

    @Test
    @DisplayName("Test creazione scheda clinica")
    void testCreazioneSchedaClinica() {
        // Arrange
        String fattoriRischio = "Diabete familiare, obesità";
        String patologie = "Ipertensione, dislipidemia";
        String comorbidita = "Neuropatia diabetica";

        // Act - Simula createSchedaClinica
        SchedaClinica scheda = new SchedaClinica(fattoriRischio, patologie, comorbidita);

        // Assert
        assertNotNull(scheda);
        assertEquals(fattoriRischio, scheda.getFattoriRischio());
        assertEquals(patologie, scheda.getPregressePatologie());
        assertEquals(comorbidita, scheda.getComorbidita());
    }

    @Test
    @DisplayName("Test ordinamento assunzioni per data e ora")
    void testOrdinamentoAssunzioni() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate ieri = oggi.minusDays(1);

        paziente1.aggiungiAssunzione(new Assunzione(oggi, LocalTime.of(8, 0), "Aspirina", 100.0));
        paziente1.aggiungiAssunzione(new Assunzione(oggi, LocalTime.of(20, 0), "Aspirina", 100.0));
        paziente1.aggiungiAssunzione(new Assunzione(ieri, LocalTime.of(12, 0), "Paracetamolo", 500.0));
        paziente1.aggiungiAssunzione(new Assunzione(oggi, LocalTime.of(12, 0), "Paracetamolo", 500.0));

        // Act - Simula l'ordinamento del controller
        List<Assunzione> assunzioni = new ArrayList<>(paziente1.getAssunzioni());
        assunzioni.sort((a1, a2) -> {
            int dateCompare = a2.getData().compareTo(a1.getData()); // Più recenti prima
            if (dateCompare != 0) return dateCompare;
            return a2.getOra().compareTo(a1.getOra()); // Ora più tarda prima
        });

        // Assert
        assertEquals(4, assunzioni.size());
        assertEquals(oggi, assunzioni.get(0).getData());
        assertEquals(LocalTime.of(20, 0), assunzioni.get(0).getOra()); // 20:00 prima
        assertEquals(LocalTime.of(12, 0), assunzioni.get(1).getOra()); // 12:00 seconda
        assertEquals(LocalTime.of(8, 0), assunzioni.get(2).getOra());  // 08:00 terza
        assertEquals(ieri, assunzioni.get(3).getData()); // Ieri per ultimo
    }

    @Test
    @DisplayName("Test ordinamento eventi clinici")
    void testOrdinamentoEventiClinici() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        LocalDate ieri = oggi.minusDays(1);

        EventoClinico evento1 = new EventoClinico("Sintomo", "Mal di testa", oggi, LocalTime.of(10, 0), "Note 1");
        EventoClinico evento2 = new EventoClinico("Sintomo", "Nausea", oggi, LocalTime.of(15, 0), "Note 2");
        EventoClinico evento3 = new EventoClinico("Controllo", "Visita", ieri, LocalTime.of(9, 0), "Note 3");
        EventoClinico evento4 = new EventoClinico("Sintomo", "Vertigini", oggi, null, "Note 4"); // Senza ora

        paziente1.getEventiClinici().add(evento1);
        paziente1.getEventiClinici().add(evento2);
        paziente1.getEventiClinici().add(evento3);
        paziente1.getEventiClinici().add(evento4);

        // Act - Simula l'ordinamento del controller
        List<EventoClinico> eventi = new ArrayList<>(paziente1.getEventiClinici());
        eventi.sort((e1, e2) -> {
            int dateCompare = e2.getData().compareTo(e1.getData()); // Più recenti prima
            if (dateCompare != 0) return dateCompare;

            LocalTime ora1 = e1.getOra() != null ? e1.getOra() : LocalTime.MIDNIGHT;
            LocalTime ora2 = e2.getOra() != null ? e2.getOra() : LocalTime.MIDNIGHT;
            return ora2.compareTo(ora1); // Ora più tarda prima
        });

        // Assert
        assertEquals(4, eventi.size());
        assertEquals(oggi, eventi.get(0).getData());
        assertEquals(LocalTime.of(15, 0), eventi.get(0).getOra()); // 15:00 prima
        assertEquals(LocalTime.of(10, 0), eventi.get(1).getOra()); // 10:00 seconda
        assertNull(eventi.get(2).getOra()); // Evento senza ora (trattato come MIDNIGHT)
        assertEquals(ieri, eventi.get(3).getData()); // Ieri per ultimo
    }

    @Test
    @DisplayName("Test logica notifiche assunzioni mancanti")
    void testLogicaNotificheAssunzioniMancanti() {
        // Arrange
        LocalDate oggi = LocalDate.now();
        Terapia terapia = new Terapia("Aspirina", 2, 100.0, "Dopo i pasti",
                oggi.minusDays(10), oggi.plusDays(10), Terapia.Stato.ATTIVA, diabetologo.getId());

        paziente1.getTerapie().add(terapia);

        // Simula 3 giorni senza assunzioni
        // Aggiungi assunzioni solo per alcuni giorni
        paziente1.aggiungiAssunzione(new Assunzione(oggi.minusDays(4), LocalTime.of(8, 0), "Aspirina", 100.0));
        // Giorni -3, -2, -1 senza assunzioni

        // Act - Logica per rilevare assunzioni mancanti
        int giorniConsecutiviSenzaAssunzioni = 0;
        for (int i = 1; i <= 3; i++) {
            LocalDate dataControllo = oggi.minusDays(i);
            boolean hasAssunzioniPerGiorno = paziente1.getAssunzioni().stream()
                    .anyMatch(a -> a.getData().equals(dataControllo) &&
                            a.getFarmaco().equals(terapia.getFarmaco()));

            if (!hasAssunzioniPerGiorno) {
                giorniConsecutiviSenzaAssunzioni++;
            } else {
                break; // Interrompi se trova un'assunzione
            }
        }

        // Assert
        assertEquals(3, giorniConsecutiviSenzaAssunzioni);
        assertTrue(giorniConsecutiviSenzaAssunzioni >= 3); // Condizione per notifica
    }

    @Test
    @DisplayName("Test logica notifiche glicemia fuori range")
    void testLogicaNotificheGlicemiaFuoriRange() {
        // Arrange - Valori fuori range
        Rilevazione glicemiaBassa = new Rilevazione(LocalDate.now(), "Prima colazione", 60);  // Troppo bassa
        Rilevazione glicemiaAlta = new Rilevazione(LocalDate.now(), "Prima colazione", 200);  // Troppo alta
        Rilevazione glicemiaNormale = new Rilevazione(LocalDate.now(), "Prima colazione", 100); // Normale
        Rilevazione postPastoAlta = new Rilevazione(LocalDate.now(), "Dopo pranzo", 300); // Post-pasto alta

        // Act & Assert - Test logica per determinare se è fuori range
        assertTrue(isGlicemiaFuoriRange(glicemiaBassa));
        assertTrue(isGlicemiaFuoriRange(glicemiaAlta));
        assertFalse(isGlicemiaFuoriRange(glicemiaNormale));
        assertTrue(isGlicemiaFuoriRange(postPastoAlta));
    }

    private boolean isGlicemiaFuoriRange(Rilevazione rilevazione) {
        boolean isPrePasto = rilevazione.getTipoPasto().toLowerCase().contains("prima");
        int valore = rilevazione.getValore();

        if (isPrePasto) {
            return valore < 80 || valore > 130; // Range normale pre-pasto: 80-130
        } else {
            return valore >= 180; // Valore problematico post-pasto: >=180
        }
    }

    @Test
    @DisplayName("Test popolamento campi scheda clinica")
    void testPopolamentoCampiSchedaClinica() {
        // Arrange
        SchedaClinica scheda = new SchedaClinica(
                "Diabete familiare",
                "Ipertensione",
                "Neuropatia"
        );

        paziente1.setSchedaClinica(scheda);

        // Act & Assert
        assertNotNull(paziente1.getSchedaClinica());
        assertEquals("Diabete familiare", paziente1.getSchedaClinica().getFattoriRischio());
        assertEquals("Ipertensione", paziente1.getSchedaClinica().getPregressePatologie());
        assertEquals("Neuropatia", paziente1.getSchedaClinica().getComorbidita());
    }

    // Metodi helper per simulare la logica del controller
    private String determineColorForPreMeal(int valore) {
        if (valore < 80) return "deepskyblue";
        else if (valore <= 130) return "lightgreen";
        else if (valore <= 180) return "khaki";
        else return "orange";
    }

    private String determineColorForPostMeal(int valore) {
        if (valore < 180) return "lightgreen";
        else if (valore <= 250) return "orange";
        else return "tomato";
    }
}