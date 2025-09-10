package view;

import controller.DataController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DiabetologoDashboardController extends BaseController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<Paziente> pazientiList;

    // ---------- Rilevazioni ----------
    @FXML private TableView<Rilevazione> rilevazioniTable;
    @FXML private TableColumn<Rilevazione, String> dataColumn;
    @FXML private TableColumn<Rilevazione, String> tipoPastoColumn;
    @FXML private TableColumn<Rilevazione, Integer> valoreColumn;

    // ---------- Terapie ----------
    @FXML private TableView<Terapia> terapieTable;
    @FXML private TableColumn<Terapia, String> farmacoColumn;
    @FXML private TableColumn<Terapia, Integer> assunzioniColumn;
    @FXML private TableColumn<Terapia, Double> quantitaColumn;
    @FXML private TableColumn<Terapia, String> indicazioniColumn;
    @FXML private TableColumn<Terapia, String> dataInizioColumn;
    @FXML private TableColumn<Terapia, String> dataFineColumn;
    @FXML private TableColumn<Terapia, Terapia.Stato> statoColumn;

    // ---------- Assunzioni ----------
    @FXML private TableView<Assunzione> assunzioniTable;
    @FXML private TableColumn<Assunzione, String> dataAssunzioneColumn;
    @FXML private TableColumn<Assunzione, String> oraAssunzioneColumn;
    @FXML private TableColumn<Assunzione, String> farmacoAssunzioneColumn;
    @FXML private TableColumn<Assunzione, Number> quantitaAssunzioneColumn;

    // ---------- Scheda clinica ----------
    @FXML private TextArea fattoriRischioArea;
    @FXML private TextArea patologieArea;
    @FXML private TextArea comorbiditaArea;
    @FXML private Button salvaSchedaBtn;

    // ---------- Eventi clinici ----------
    @FXML private TableView<EventoClinico> eventiTable;
    @FXML private TableColumn<EventoClinico, String> tipoEventoColumn;
    @FXML private TableColumn<EventoClinico, String> descrizioneEventoColumn;
    @FXML private TableColumn<EventoClinico, String> dataEventoColumn;
    @FXML private TableColumn<EventoClinico, String> oraEventoColumn;
    @FXML private TableColumn<EventoClinico, String> noteEventoColumn;

    @FXML private ListView<TerapiaConcomitante> terapieConcomitantiMedicoList;

    @FXML private VBox pagina1;
    @FXML private VBox pagina2;

    private static final Map<String, Integer> ORDINE_PASTI = Map.of(
            "Dopo cena", 1,
            "Prima cena", 2,
            "Dopo pranzo", 3,
            "Prima pranzo", 4,
            "Dopo colazione", 5,
            "Prima colazione", 6
    );

    private Diabetologo diabetologo;
    private final DataController dataController = new DataController();
    private final FilePathProvider filePathProvider = new FilePathProvider();

    @FXML
    public void initialize() {
        AppState.getInstance().setDiabetologoDashboardController(this);

        initializeTableColumns();
        initializeRowFactories();
        initializeEditableColumns();
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
        oraEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOra() != null ? c.getValue().getOra().toString() : ""
        ));
        noteEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNote()));
    }

    private void initializeRowFactories() {
        // Terapie row coloring
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

        // Rilevazioni row coloring for out-of-range values
        rilevazioniTable.setRowFactory(tv -> new TableRow<Rilevazione>() {
            @Override
            protected void updateItem(Rilevazione item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String colore = determinaColore(item.getTipoPasto(), item.getValore());
                    setStyle("-fx-background-color: " + colore + "; -fx-text-fill: black;");
                }
            }
        });
    }

    private void initializeEditableColumns() {
        statoColumn.setCellFactory(tc -> new ComboBoxTableCell<>(Terapia.Stato.values()));
    }

    public void setUtente(Diabetologo diabetologo) {
        this.diabetologo = diabetologo;
        initializeUserInterface();
        loadPatientsData();
        setupPatientSelectionListener();
        selectFirstPatient();
    }

    private void initializeUserInterface() {
        welcomeLabel.setText("Benvenuto Dr. " + diabetologo.getNome() + " " + diabetologo.getCognome());
        pazientiList.setItems(FXCollections.observableArrayList(diabetologo.getPazienti()));
        mostraNotifichePendenti();
    }

    private void loadPatientsData() {
        for (Paziente paziente : diabetologo.getPazienti()) {
            loadPatientData(paziente);
        }
    }

    private void loadPatientData(Paziente paziente) {
        paziente.getRilevazioni().clear();
        dataController.caricaRilevazioni(filePathProvider.getRilevazioniFile(), List.of(paziente));

        paziente.getTerapie().clear();
        dataController.caricaTerapie(filePathProvider.getTerapieFile(), List.of(paziente));

        paziente.getAssunzioni().clear();
        dataController.caricaAssunzioni(filePathProvider.getAssunzioniFile(), List.of(paziente));

        paziente.getEventiClinici().clear();
        dataController.caricaEventiClinici(filePathProvider.getEventiCliniciFile(), List.of(paziente));

        paziente.getTerapieConcomitanti().clear();
        dataController.caricaTerapieConcomitanti(filePathProvider.getTerapieConcomitantiFile(), List.of(paziente));
    }

    private void setupPatientSelectionListener() {
        pazientiList.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP != null) {
                updatePatientViews(newP);
            }
        });
    }

    private void updatePatientViews(Paziente paziente) {
        mostraRilevazioni(paziente);
        mostraTerapie(paziente);
        mostraAssunzioni(paziente);
        mostraSchedaClinica(paziente);
        mostraEventi(paziente);
        aggiornaTerapieConcomitanti(paziente);
    }

    private void selectFirstPatient() {
        if (!diabetologo.getPazienti().isEmpty()) {
            pazientiList.getSelectionModel().selectFirst();
        }
    }

    // ---------- Display Methods ----------

    private void mostraRilevazioni(Paziente paziente) {
        ObservableList<Rilevazione> lista = FXCollections.observableArrayList(paziente.getRilevazioni());
        lista.sort(Comparator
                .comparing(Rilevazione::getData).reversed()
                .thenComparing(r -> ORDINE_PASTI.getOrDefault(r.getTipoPasto(), Integer.MAX_VALUE))
        );
        rilevazioniTable.setItems(lista);
    }

    private String determinaColore(String tipoPasto, int valore) {
        if (tipoPasto.toLowerCase().contains("prima")) {
            return determineColorForPreMeal(valore);
        } else {
            return determineColorForPostMeal(valore);
        }
    }

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

    private void mostraTerapie(Paziente paziente) {
        updateTerapieStatus(paziente);
        ObservableList<Terapia> lista = FXCollections.observableArrayList(paziente.getTerapie());
        terapieTable.setItems(lista);
        terapieTable.refresh();
    }

    private void updateTerapieStatus(Paziente paziente) {
        LocalDate oggi = LocalDate.now();
        for (Terapia terapia : paziente.getTerapie()) {
            if (terapia.getStato() == Terapia.Stato.ATTIVA && oggi.isAfter(terapia.getDataFine())) {
                terapia.setStato(Terapia.Stato.TERMINATA);
            }
        }
    }

    private String getStyleForTerapiaStato(Terapia.Stato stato) {
        return switch (stato) {
            case ATTIVA -> "-fx-background-color: lightgreen;";
            case IN_PAUSA -> "-fx-background-color: orange;";
            case TERMINATA -> "-fx-background-color: tomato;";
        };
    }

    private void mostraAssunzioni(Paziente paziente) {
        ObservableList<Assunzione> lista = FXCollections.observableArrayList(paziente.getAssunzioni());
        lista.sort(Comparator.comparing(Assunzione::getData)
                .thenComparing(Assunzione::getOra).reversed());
        assunzioniTable.setItems(lista);
    }

    private void mostraSchedaClinica(Paziente paziente) {
        if (paziente.getSchedaClinica() != null) {
            populateSchedaClinicaFields(paziente.getSchedaClinica());
        } else {
            clearSchedaClinicaFields();
        }
    }

    private void populateSchedaClinicaFields(SchedaClinica scheda) {
        fattoriRischioArea.setText(scheda.getFattoriRischio());
        patologieArea.setText(scheda.getPregressePatologie());
        comorbiditaArea.setText(scheda.getComorbidita());
    }

    private void clearSchedaClinicaFields() {
        fattoriRischioArea.clear();
        patologieArea.clear();
        comorbiditaArea.clear();
    }

    private void mostraEventi(Paziente paziente) {
        ObservableList<EventoClinico> lista = FXCollections.observableArrayList(paziente.getEventiClinici());
        lista.sort(Comparator.comparing(EventoClinico::getData).reversed()
                .thenComparing(e -> e.getOra() != null ? e.getOra() : LocalTime.MIDNIGHT, Comparator.reverseOrder()));
        eventiTable.setItems(lista);
    }

    private void aggiornaTerapieConcomitanti(Paziente paziente) {
        terapieConcomitantiMedicoList.getItems().setAll(paziente.getTerapieConcomitanti());
    }

    // ---------- Event Handlers ----------

    @FXML
    private void handleLogout() {
        navigateToLogin(welcomeLabel.getScene().getWindow(), "LoginView.fxml");
    }

    @FXML
    private void handleAggiungiTerapia() {
        Paziente selectedPatient = pazientiList.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) return;

        try {
            showTerapiaDialog(selectedPatient);
        } catch (Exception e) {
            handleException("Errore nell'apertura del form terapia", e);
        }
    }

    private void showTerapiaDialog(Paziente paziente) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TerapiaForm.fxml"));
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Nuova Terapia");
        dialogStage.setScene(new Scene(loader.load()));

        TerapiaFormController controller = loader.getController();
        controller.setMedicoId(diabetologo.getId());

        dialogStage.showAndWait();

        Terapia nuovaTerapia = controller.getNuovaTerapia();
        if (nuovaTerapia != null) {
            paziente.aggiungiTerapia(nuovaTerapia);
            mostraTerapie(paziente);
            dataController.salvaTerapie(filePathProvider.getTerapieFile(), List.of(paziente));
        }
    }

    @FXML
    private void handleModificaStatoTerapia() {
        Terapia selectedTerapia = terapieTable.getSelectionModel().getSelectedItem();
        Paziente selectedPaziente = pazientiList.getSelectionModel().getSelectedItem();

        if (selectedTerapia == null || selectedPaziente == null) return;

        showStatoTerapiaDialog(selectedTerapia, selectedPaziente);
    }

    private void showStatoTerapiaDialog(Terapia terapia, Paziente paziente) {
        ChoiceDialog<Terapia.Stato> dialog = new ChoiceDialog<>(terapia.getStato(),
                Terapia.Stato.ATTIVA, Terapia.Stato.IN_PAUSA, Terapia.Stato.TERMINATA);
        dialog.setTitle("Cambia stato terapia");
        dialog.setHeaderText(null);
        dialog.setContentText("Seleziona nuovo stato:");

        dialog.showAndWait().ifPresent(nuovoStato -> {
            if (isValidStatusChange(terapia)) {
                updateTerapiaStatus(terapia, nuovoStato, paziente);
            } else {
                showAlert("Errore", "Non puoi modificare lo stato fuori dall'intervallo valido!", Alert.AlertType.ERROR);
            }
        });
    }

    private boolean isValidStatusChange(Terapia terapia) {
        LocalDate oggi = LocalDate.now();
        return !oggi.isBefore(terapia.getDataInizio()) && !oggi.isAfter(terapia.getDataFine());
    }

    private void updateTerapiaStatus(Terapia terapia, Terapia.Stato nuovoStato, Paziente paziente) {
        terapia.setStato(nuovoStato);
        dataController.salvaTerapie(filePathProvider.getTerapieFile(), List.of(paziente));
        mostraTerapie(paziente);
    }

    @FXML
    private void handleSalvaSchedaClinica() {
        Paziente selectedPaziente = pazientiList.getSelectionModel().getSelectedItem();
        if (selectedPaziente == null) {
            showAlert("Errore", "Seleziona un paziente prima di salvare la scheda.", Alert.AlertType.ERROR);
            return;
        }

        SchedaClinica scheda = createSchedaClinica();
        selectedPaziente.setSchedaClinica(scheda);

        dataController.salvaSchedeCliniche(filePathProvider.getSchedeFile(), List.of(selectedPaziente));
        showAlert("Successo", "Scheda clinica salvata correttamente.", Alert.AlertType.INFORMATION);
    }

    private SchedaClinica createSchedaClinica() {
        return new SchedaClinica(
                fattoriRischioArea.getText(),
                patologieArea.getText(),
                comorbiditaArea.getText()
        );
    }

    // ---------- Notification Methods ----------

    public void mostraNotificaAssunzioniMancanti(Paziente paziente, Terapia terapia) {
        String message = String.format(
                "Il paziente %s %s non ha registrato le assunzioni del farmaco \"%s\" per 3 giorni consecutivi.",
                paziente.getNome(), paziente.getCognome(), terapia.getFarmaco()
        );
        showNotificationAlert("Assunzioni non registrate", "Attenzione paziente non conforme", message, Alert.AlertType.WARNING);
    }

    public void mostraNotificaGlicemiaFuoriRange(Paziente paziente, Rilevazione rilevazione) {
        String message = String.format(
                "Il paziente %s %s ha registrato un valore di glicemia %d mg/dL (%s) il %s.",
                paziente.getNome(), paziente.getCognome(), rilevazione.getValore(),
                rilevazione.getTipoPasto(), rilevazione.getData()
        );
        showNotificationAlert("Glicemia fuori range", "Attenzione: glicemia anomala", message, Alert.AlertType.ERROR);
    }

    public void mostraNotifichePendenti() {
        showPendingAssunzioniNotifications();
        showPendingGlicemiaNotifications();
    }

    private void showPendingAssunzioniNotifications() {
        for (String msg : AppState.getInstance().prelevaNotificheAssunzioni()) {
            showNotificationAlert("Assunzioni non registrate", null, msg, Alert.AlertType.WARNING);
        }
    }

    private void showPendingGlicemiaNotifications() {
        for (String msg : AppState.getInstance().prelevaNotificheGlicemia()) {
            showNotificationAlert("Glicemia fuori range", null, msg, Alert.AlertType.ERROR);
        }
    }

    private void showNotificationAlert(String title, String header, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ---------- Page Navigation ----------

    @FXML
    private void mostraPagina1() {
        showPage(pagina1, pagina2);
    }

    @FXML
    private void mostraPagina2() {
        showPage(pagina2, pagina1);
    }

    private void showPage(VBox pageToShow, VBox pageToHide) {
        pageToShow.setVisible(true);
        pageToShow.setManaged(true);
        pageToHide.setVisible(false);
        pageToHide.setManaged(false);
    }
}