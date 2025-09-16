package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PazienteDashboardController extends BaseController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label medicoLabel;

    // ---------- Rilevazioni ----------
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<String> tipoPastoChoice;
    @FXML
    private TextField valoreField;
    @FXML
    private TableView<Rilevazione> rilevazioniTable;
    @FXML
    private TableColumn<Rilevazione, String> dataColumn;
    @FXML
    private TableColumn<Rilevazione, String> tipoPastoColumn;
    @FXML
    private TableColumn<Rilevazione, Integer> valoreColumn;

    // ---------- Terapie ----------
    @FXML
    private TableView<Terapia> terapieTable;
    @FXML
    private TableColumn<Terapia, String> farmacoColumn;
    @FXML
    private TableColumn<Terapia, Integer> assunzioniColumn;
    @FXML
    private TableColumn<Terapia, Double> quantitaColumn;
    @FXML
    private TableColumn<Terapia, String> indicazioniColumn;
    @FXML
    private TableColumn<Terapia, String> dataInizioColumn;
    @FXML
    private TableColumn<Terapia, String> dataFineColumn;
    @FXML
    private TableColumn<Terapia, Terapia.Stato> statoColumn;

    // ---------- Assunzioni ----------
    @FXML
    private DatePicker assunzioneDatePicker;
    @FXML
    private TextField oraField;
    @FXML
    private ComboBox<String> farmacoChoice;
    @FXML
    private TextField quantitaField;
    @FXML
    private TableView<Assunzione> assunzioniTable;
    @FXML
    private TableColumn<Assunzione, String> dataAssunzioneColumn;
    @FXML
    private TableColumn<Assunzione, String> oraAssunzioneColumn;
    @FXML
    private TableColumn<Assunzione, String> farmacoAssunzioneColumn;
    @FXML
    private TableColumn<Assunzione, Number> quantitaAssunzioneColumn;

    // ---------- Eventi clinici ----------
    @FXML
    private TableView<EventoClinico> eventiTable;
    @FXML
    private TableColumn<EventoClinico, String> tipoEventoColumn;
    @FXML
    private TableColumn<EventoClinico, String> descrizioneEventoColumn;
    @FXML
    private TableColumn<EventoClinico, String> dataEventoColumn;
    @FXML
    private TableColumn<EventoClinico, String> oraEventoColumn;
    @FXML
    private TableColumn<EventoClinico, String> noteEventoColumn;

    @FXML
    private ChoiceBox<String> tipoEventoChoice;
    @FXML
    private TextField descrizioneEventoField;
    @FXML
    private TextField noteEventoField;

    // ---------- Terapie concomitanti ----------
    @FXML
    private TextField tipoTerapiaField;
    @FXML
    private TextField descrizioneTerapiaField;
    @FXML
    private ListView<TerapiaConcomitante> terapieConcomitantiList;

    @FXML
    private HBox pagina1;
    @FXML
    private HBox pagina2;

    private static final List<String> TIPI_PASTO = Arrays.asList(
            "Prima colazione", "Dopo colazione",
            "Prima pranzo", "Dopo pranzo",
            "Prima cena", "Dopo cena"
    );

    private static final List<String> TIPI_EVENTO = Arrays.asList("Sintomo", "Patologia");

    private static final int GIORNI_CONSECUTIVI_MANCANTI = 3;

    private Paziente paziente;
    private final DataController dataController = new DataController();
    private final FilePathProvider filePathProvider = new FilePathProvider();

    @FXML
    public void initialize() {
        initializeChoiceBoxes();
        initializeTableColumns();
        initializeRowFactories();
    }

    private void initializeChoiceBoxes() {
        tipoPastoChoice.getItems().addAll(TIPI_PASTO);
        tipoEventoChoice.getItems().addAll(TIPI_EVENTO);
        tipoPastoChoice.setValue(TIPI_PASTO.get(0));
        tipoEventoChoice.setValue(TIPI_EVENTO.get(0));
    }

    private void initializeTableColumns() {
        // Rilevazioni columns
        dataColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        tipoPastoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPasto()));
        valoreColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValore()));

        // Terapie columns
        farmacoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmaco()));
        assunzioniColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getAssunzioniGiornaliere()));
        quantitaColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantitaPerAssunzione()));
        indicazioniColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIndicazioni()));
        dataInizioColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInizio().toString()));
        dataFineColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataFine().toString()));
        statoColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStato()));

        // Assunzioni columns
        dataAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        oraAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOra().toString()));
        farmacoAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmaco()));
        quantitaAssunzioneColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getQuantita()));

        // Eventi clinici columns
        tipoEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        descrizioneEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescrizione()));
        dataEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        oraEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOra().toString()));
        noteEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNote()));
    }

    private void initializeRowFactories() {
        terapieTable.setRowFactory(tv -> new TableRow<Terapia>() {
            @Override
            protected void updateItem(Terapia item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    setStyle(getStyleForTerapiaStato(item.getStato()));
                }
            }
        });
    }

    private String getStyleForTerapiaStato(Terapia.Stato stato) {
        return switch (stato) {
            case ATTIVA -> "-fx-background-color: lightgreen;";
            case IN_PAUSA -> "-fx-background-color: orange;";
            case TERMINATA -> "-fx-background-color: tomato;";
        };
    }

    public void setUtente(Paziente paziente) {
        this.paziente = paziente;
        initializeUserInterface();
        loadPatientData();
        performInitialChecks();
    }

    private void initializeUserInterface() {
        welcomeLabel.setText("Benvenuto, " + paziente.getNome() + " " + paziente.getCognome());
        if (paziente.getMedico() != null) {
            medicoLabel.setText("Medico curante: Dr. " + paziente.getMedico().getNome() + " " + paziente.getMedico().getCognome());
        }
    }

    private void loadPatientData() {
        loadRilevazioni();
        loadTerapie();
        loadAssunzioni();
        loadEventiClinici();
        loadTerapieConcomitanti();
    }

    private void loadRilevazioni() {
        paziente.getRilevazioni().clear();
        dataController.caricaRilevazioni(filePathProvider.getRilevazioniFile(), List.of(paziente));
        aggiornaListaRilevazioni();
    }

    private void loadTerapie() {
        paziente.getTerapie().clear();
        dataController.caricaTerapie(filePathProvider.getTerapieFile(), List.of(paziente));
        aggiornaListaTerapie();
    }

    private void loadAssunzioni() {
        paziente.getAssunzioni().clear();
        dataController.caricaAssunzioni(filePathProvider.getAssunzioniFile(), List.of(paziente));
        aggiornaListaAssunzioni();
    }

    private void loadEventiClinici() {
        paziente.getEventiClinici().clear();
        dataController.caricaEventiClinici(filePathProvider.getEventiCliniciFile(), List.of(paziente));
        aggiornaListaEventi();
    }

    private void loadTerapieConcomitanti() {
        paziente.getTerapieConcomitanti().clear();
        dataController.caricaTerapieConcomitanti(filePathProvider.getTerapieConcomitantiFile(), List.of(paziente));
        aggiornaListaTerapieConcomitanti();
    }

    private void performInitialChecks() {
        aggiornaFarmaciChoice();
        controllaAssunzioni(paziente);
    }

    // ---------- Rilevazioni Methods ----------

    @FXML
    private void handleSalvaRilevazione() {
        RilevazioneInput input = getRilevazioneInput();
        if (!isValidRilevazioneInput(input)) return;

        try {
            if (isDuplicateRilevazione(input)) {
                showAlert("Errore", "Esiste già una rilevazione per questa data e tipo pasto.");
                return;
            }

            Rilevazione rilevazione = createRilevazione(input);
            paziente.aggiungiRilevazione(rilevazione);

            saveRilevazioni();
            aggiornaListaRilevazioni();
            clearRilevazioneFields();

            checkGlicemiaRange(rilevazione);

        } catch (NumberFormatException e) {
            showAlert("Errore", "Inserisci un numero valido");
        } catch (IllegalArgumentException e) {
            showAlert("Errore", e.getMessage());
        }
    }

    private RilevazioneInput getRilevazioneInput() {
        return new RilevazioneInput(
                datePicker.getValue(),
                tipoPastoChoice.getValue(),
                valoreField.getText()
        );
    }

    private boolean isValidRilevazioneInput(RilevazioneInput input) {
        if (input.data() == null || input.tipoPasto() == null || input.valoreStr().isEmpty()) {
            showAlert("Errore", "Compila tutti i campi");
            return false;
        }
        return true;
    }

    private boolean isDuplicateRilevazione(RilevazioneInput input) {
        return paziente.getRilevazioni().stream()
                .anyMatch(r -> r.getData().equals(input.data()) &&
                        r.getTipoPasto().equalsIgnoreCase(input.tipoPasto()));
    }

    private Rilevazione createRilevazione(RilevazioneInput input) {
        int valore = Integer.parseInt(input.valoreStr());
        return new Rilevazione(input.data(), input.tipoPasto(), valore);
    }

    private void saveRilevazioni() {
        dataController.salvaRilevazioni(List.of(paziente));
    }

    private void clearRilevazioneFields() {
        valoreField.clear();
    }

    private void checkGlicemiaRange(Rilevazione rilevazione) {
        if (rilevazione.isFuoriRange()) {
            String msg = createGlicemiaNotificationMessage(rilevazione);
            AppState.getInstance().aggiungiNotificaGlicemia(Integer.toString(paziente.getMedicoId()),msg);
        }
    }

    private String createGlicemiaNotificationMessage(Rilevazione rilevazione) {
        return String.format(
                "Il paziente %s %s ha registrato un valore di glicemia %d mg/dL (%s) il %s.",
                paziente.getNome(), paziente.getCognome(), rilevazione.getValore(),
                rilevazione.getTipoPasto(), rilevazione.getData()
        );
    }

    @FXML
    private void handleEliminaRilevazione() {
        Rilevazione selezionata = rilevazioniTable.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            showAlert("Attenzione", "Seleziona una rilevazione da eliminare.");
            return;
        }

        if (confirmDeletion("Vuoi davvero eliminare questa rilevazione?")) {
            paziente.getRilevazioni().remove(selezionata);
            saveRilevazioni();
            aggiornaListaRilevazioni();
        }
    }

    private void aggiornaListaRilevazioni() {
        ObservableList<Rilevazione> lista = FXCollections.observableArrayList(paziente.getRilevazioni());
        lista.sort(Comparator.comparing(Rilevazione::getData).reversed());
        rilevazioniTable.setItems(lista);
    }

    // ---------- Terapie Methods ----------

    private void aggiornaListaTerapie() {
        LocalDate oggi = LocalDate.now();
        List<Terapia> terapieOrdinate = paziente.getTerapie().stream()
                .sorted(Comparator.comparing(Terapia::getStato)
                        .thenComparing(Terapia::getDataInizio))
                .toList();
        terapieTable.setItems(FXCollections.observableArrayList(terapieOrdinate));
        updateFarmaciChoice(oggi, terapieOrdinate);
    }

    private void updateFarmaciChoice(LocalDate oggi, List<Terapia> terapie) {
        farmacoChoice.getItems().clear();
        for (Terapia terapia : terapie) {
            if (isActiveTerapia(terapia, oggi) && !farmacoChoice.getItems().contains(terapia.getFarmaco())) {
                farmacoChoice.getItems().add(terapia.getFarmaco());
            }
        }
    }

    private boolean isActiveTerapia(Terapia terapia, LocalDate oggi) {
        return terapia.getStato() == Terapia.Stato.ATTIVA &&
                !oggi.isBefore(terapia.getDataInizio()) &&
                !oggi.isAfter(terapia.getDataFine());
    }

    // ---------- Assunzioni Methods ----------

    @FXML
    private void handleSalvaAssunzione() {
        AssunzioneInput input = getAssunzioneInput();
        if (!isValidAssunzioneInput(input)) return;

        try {
            LocalTime ora = LocalTime.parse(input.oraStr());
            double quantita = Double.parseDouble(input.quantitaStr());

            if (!isValidFarmaco(input.farmaco())) {
                showAlert("Errore", "Non puoi assumere questo farmaco: terapia non attiva o fuori intervallo!");
                return;
            }

            Assunzione assunzione = new Assunzione(input.data(), ora, input.farmaco(), quantita);
            paziente.aggiungiAssunzione(assunzione);

            saveAssunzioni();
            aggiornaListaAssunzioni();
            clearAssunzioneFields();
            controllaAssunzioni(paziente);

        } catch (DateTimeParseException e) {
            showAlert("Errore", "Formato ora non corretto (usa HH:MM).");
        } catch (NumberFormatException e) {
            showAlert("Errore", "Formato quantità non corretto.");
        }
    }

    private AssunzioneInput getAssunzioneInput() {
        return new AssunzioneInput(
                assunzioneDatePicker.getValue(),
                oraField.getText(),
                farmacoChoice.getValue(),
                quantitaField.getText()
        );
    }

    private boolean isValidAssunzioneInput(AssunzioneInput input) {
        if (input.data() == null || input.oraStr().isEmpty() ||
                input.farmaco() == null || input.quantitaStr().isEmpty()) {
            showAlert("Errore", "Compila tutti i campi per l'assunzione.");
            return false;
        }
        return true;
    }

    private boolean isValidFarmaco(String farmaco) {
        LocalDate oggi = LocalDate.now();
        return paziente.getTerapie().stream().anyMatch(terapia ->
                terapia.getFarmaco().equals(farmaco) &&
                        terapia.getStato() == Terapia.Stato.ATTIVA &&
                        (oggi.isEqual(terapia.getDataInizio()) || oggi.isAfter(terapia.getDataInizio())) &&
                        (oggi.isEqual(terapia.getDataFine()) || oggi.isBefore(terapia.getDataFine()))
        );
    }

    private void saveAssunzioni() {
        dataController.salvaAssunzioni(List.of(paziente));
    }

    private void clearAssunzioneFields() {
        oraField.clear();
        quantitaField.clear();
    }

    @FXML
    private void handleEliminaAssunzione() {
        Assunzione selezionata = assunzioniTable.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            showAlert("Attenzione", "Seleziona un'assunzione da eliminare.");
            return;
        }

        if (confirmDeletion("Vuoi davvero eliminare questa assunzione?")) {
            paziente.getAssunzioni().remove(selezionata);
            saveAssunzioni();
            aggiornaListaAssunzioni();
        }
    }

    private void aggiornaListaAssunzioni() {
        List<Assunzione> assunzioniOrdinate = paziente.getAssunzioni().stream()
                .sorted(Comparator.comparing(Assunzione::getData)
                        .thenComparing(Assunzione::getOra)
                        .reversed())
                .toList();
        assunzioniTable.setItems(FXCollections.observableArrayList(assunzioniOrdinate));
    }

    private void aggiornaFarmaciChoice() {
        farmacoChoice.getItems().clear();
        LocalDate oggi = LocalDate.now();
        for (Terapia terapia : paziente.getTerapie()) {
            if (isActiveTerapia(terapia, oggi) && !farmacoChoice.getItems().contains(terapia.getFarmaco())) {
                farmacoChoice.getItems().add(terapia.getFarmaco());
            }
        }
    }

    // ---------- Eventi Clinici Methods ----------

    @FXML
    private void handleAggiungiEvento() {
        EventoInput input = getEventoInput();
        if (!isValidEventoInput(input)) return;

        LocalDate oggi = LocalDate.now();
        LocalTime ora = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

        EventoClinico evento = new EventoClinico(input.tipo(), input.descrizione(), oggi, ora, input.note());
        paziente.aggiungiEventoClinico(evento);

        saveEventiClinici();
        aggiornaListaEventi();
        clearEventoFields();
    }

    private EventoInput getEventoInput() {
        return new EventoInput(
                tipoEventoChoice.getValue(),
                descrizioneEventoField.getText(),
                noteEventoField.getText()
        );
    }

    private boolean isValidEventoInput(EventoInput input) {
        if (input.tipo() == null || input.descrizione().isEmpty()) {
            showAlert("Errore", "Seleziona il tipo e scrivi la descrizione.");
            return false;
        }
        return true;
    }

    private void saveEventiClinici() {
        dataController.salvaEventiClinici(List.of(paziente));
    }

    private void clearEventoFields() {
        descrizioneEventoField.clear();
        noteEventoField.clear();
    }

    private void aggiornaListaEventi() {
        ObservableList<EventoClinico> lista = FXCollections.observableArrayList(paziente.getEventiClinici());
        lista.sort(Comparator.comparing(EventoClinico::getData)
                .thenComparing(EventoClinico::getOra)
                .reversed());
        eventiTable.setItems(lista);
    }

    // ---------- Terapie Concomitanti Methods ----------

    @FXML
    private void handleAggiungiTerapiaConcomitante() {
        String tipo = tipoTerapiaField.getText().trim();
        String descrizione = descrizioneTerapiaField.getText().trim();

        if (tipo.isEmpty()) {
            showAlert("Errore", "Inserisci il tipo di terapia.");
            return;
        }

        TerapiaConcomitante nuova = new TerapiaConcomitante(tipo, descrizione);
        paziente.aggiungiTerapiaConcomitante(nuova);

        saveTerapieConcomitanti();
        aggiornaListaTerapieConcomitanti();
        clearTerapiaConcomitanteFields();
    }

    @FXML
    private void handleRimuoviTerapiaConcomitante() {
        TerapiaConcomitante selezionata = terapieConcomitantiList.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            showAlert("Attenzione", "Seleziona una terapia concomitante da rimuovere.");
            return;
        }

        if (confirmDeletion("Vuoi davvero rimuovere questa terapia concomitante?")) {
            paziente.rimuoviTerapiaConcomitante(selezionata);
            saveTerapieConcomitanti();
            aggiornaListaTerapieConcomitanti();
        }
    }

    private void saveTerapieConcomitanti() {
        dataController.salvaTerapieConcomitanti(List.of(paziente));
    }

    private void clearTerapiaConcomitanteFields() {
        tipoTerapiaField.clear();
        descrizioneTerapiaField.clear();
    }

    private void aggiornaListaTerapieConcomitanti() {
        if (paziente != null) {
            terapieConcomitantiList.getItems().clear();
            terapieConcomitantiList.getItems().addAll(paziente.getTerapieConcomitanti());
        }
    }

    // ---------- Monitoring and Notifications ----------

    public void controllaAssunzioni(Paziente paziente) {
        LocalDate oggi = LocalDate.now();

        for (Terapia terapia : paziente.getTerapie()) {
            if (terapia.getStato() != Terapia.Stato.ATTIVA) continue;

            checkDailyAssunzioni(paziente, terapia, oggi);
            checkConsecutiveMissingAssunzioni(paziente, terapia, oggi);
        }
    }

    private void checkDailyAssunzioni(Paziente paziente, Terapia terapia, LocalDate oggi) {
        long assunzioniOggi = countAssunzioniForDate(paziente, terapia.getFarmaco(), oggi);
        if (assunzioniOggi < terapia.getAssunzioniGiornaliere()) {
            mostraAlertPaziente(terapia, assunzioniOggi);
        }
    }

    private void checkConsecutiveMissingAssunzioni(Paziente paziente, Terapia terapia, LocalDate oggi) {
        boolean treGiorniMancanti = true;
        LocalDate inizio = terapia.getDataInizio();
        LocalDate fine = terapia.getDataFine();

        for (int i = 1; i <= GIORNI_CONSECUTIVI_MANCANTI; i++) {
            LocalDate giorno = oggi.minusDays(i);

            if (giorno.isBefore(inizio) || giorno.isAfter(fine)) {
                treGiorniMancanti = false;
                break;
            }

            long assunzioniGiorno = countAssunzioniForDate(paziente, terapia.getFarmaco(), giorno);
            if (assunzioniGiorno >= terapia.getAssunzioniGiornaliere()) {
                treGiorniMancanti = false;
                break;
            }
        }

        if (treGiorniMancanti) {
            notificaMedico(paziente, terapia);
        }
    }

    private long countAssunzioniForDate(Paziente paziente, String farmaco, LocalDate data) {
        return paziente.getAssunzioni().stream()
                .filter(a -> a.getFarmaco().equals(farmaco))
                .filter(a -> a.getData().equals(data))
                .count();
    }

    private void mostraAlertPaziente(Terapia terapia, long assunzioniRegistrate) {
        int mancanti = terapia.getAssunzioniGiornaliere() - (int) assunzioniRegistrate;
        showAlert("Assunzioni incomplete",
                "Devi ancora registrare " + mancanti + " assunzione(i) per il farmaco: " + terapia.getFarmaco(), Alert.AlertType.WARNING);
    }

    private void notificaMedico(Paziente paziente, Terapia terapia) {
        String msg = String.format(
                "Il paziente %s %s non ha registrato le assunzioni del farmaco \"%s\" per %d giorni consecutivi.",
                paziente.getNome(), paziente.getCognome(), terapia.getFarmaco(), GIORNI_CONSECUTIVI_MANCANTI
        );
        AppState.getInstance().aggiungiNotificaAssunzione(Integer.toString(paziente.getMedicoId()),msg);
    }

    // ---------- Navigation and UI ----------

    @FXML
    private void handleLogout() {
        navigateToLogin(welcomeLabel.getScene().getWindow(), "/view/LoginView.fxml");
    }

    @FXML
    private void handleApriContatta() {
        try {
            showContactDoctorDialog();
        } catch (Exception e) {
            handleException("Impossibile aprire la finestra per contattare il medico", e);
        }
    }

    private void showContactDoctorDialog() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ContattaMedico.fxml"));
        Stage stage = new Stage();
        stage.setScene(new javafx.scene.Scene(loader.load()));
        stage.setTitle("Contatta Medico");

        ContattaMedicoController controller = loader.getController();
        controller.setPaziente(paziente);
        stage.show();
    }

    @FXML
    private void mostraPagina1() {
        showPage(pagina1, pagina2);
    }

    @FXML
    private void mostraPagina2() {
        showPage(pagina2, pagina1);
    }

    private void showPage(HBox pageToShow, HBox pageToHide) {
        pageToShow.setVisible(true);
        pageToShow.setManaged(true);
        pageToHide.setVisible(false);
        pageToHide.setManaged(false);
    }

    // ---------- Utility Methods ----------

    private boolean confirmDeletion(String message) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        conferma.showAndWait();
        return conferma.getResult() == ButtonType.YES;
    }

    // ---------- Input Record Classes ----------

    private record RilevazioneInput(LocalDate data, String tipoPasto, String valoreStr) {
    }

    private record AssunzioneInput(LocalDate data, String oraStr, String farmaco, String quantitaStr) {
    }

    private record EventoInput(String tipo, String descrizione, String note) {
    }
}