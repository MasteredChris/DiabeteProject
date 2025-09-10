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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DiabetologoDashboardController {

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

    private String schedeFile = "src/resources/schede_cliniche.csv";
    private final String rilevazioniFile = "src/resources/rilevazioni.csv";
    private final String terapieFile = "src/resources/terapie.csv";
    private final String assunzioniFile = "src/resources/assunzioni.csv";
    private final String eventiCliniciFile = "src/resources/eventi_clinici.csv";
    private final String eventiFile = "src/resources/eventi_clinici.csv";
    private final String terapieConcomitantiFile= "src/resources/terapie_concomitanti.csv";

    private Diabetologo diabetologo;
    private DataController dataController = new DataController();

    @FXML
    public void initialize() {
        // ---------- Colonne Rilevazioni ----------
        dataColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        tipoPastoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPasto()));
        valoreColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValore()));

        // ---------- Colonne Terapie ----------
        farmacoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmaco()));
        assunzioniColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getAssunzioniGiornaliere()));
        quantitaColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantitaPerAssunzione()));
        indicazioniColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIndicazioni()));
        dataInizioColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInizio().toString()));
        dataFineColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataFine().toString()));
        statoColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStato()));

        // Colore righe Terapie
        terapieTable.setRowFactory(tv -> new TableRow<Terapia>() {
            @Override
            protected void updateItem(Terapia item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else {
                    switch (item.getStato()) {
                        case ATTIVA -> setStyle("-fx-background-color: lightgreen;");
                        case IN_PAUSA -> setStyle("-fx-background-color: orange;");
                        case TERMINATA -> setStyle("-fx-background-color: tomato;");
                    }
                }
            }
        });

        // Colore righe Rilevazioni fuori range
        rilevazioniTable.setRowFactory(tv -> new TableRow<Rilevazione>() {
            @Override
            protected void updateItem(Rilevazione item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.isFuoriRange()) setStyle("-fx-background-color: tomato;");
                else setStyle("");
            }
        });

        // ---------- Colonne Assunzioni ----------
        dataAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        oraAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOra().toString()));
        farmacoAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmaco()));
        quantitaAssunzioneColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getQuantita()));

        // Colonna stato modificabile
        statoColumn.setCellFactory(tc -> new ComboBoxTableCell<>(Terapia.Stato.values()));

        // ---------- Colonne Eventi Clinici ----------
        tipoEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        descrizioneEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescrizione()));
        dataEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        oraEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOra() != null ? c.getValue().getOra().toString() : ""
        ));
        noteEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNote()));
    }

    public void setUtente(Diabetologo diabetologo) {
        this.diabetologo = diabetologo;
        welcomeLabel.setText("Benvenuto Dr. " + diabetologo.getNome() + " " + diabetologo.getCognome());
        pazientiList.setItems(FXCollections.observableArrayList(diabetologo.getPazienti()));

        // Carica tutti i dati dei pazienti incluso le terapie concomitanti
        for (Paziente p : diabetologo.getPazienti()) {
            p.getRilevazioni().clear();
            dataController.caricaRilevazioni(rilevazioniFile, List.of(p));

            p.getTerapie().clear();
            dataController.caricaTerapie(terapieFile, List.of(p));

            p.getAssunzioni().clear();
            dataController.caricaAssunzioni(assunzioniFile, List.of(p));

            p.getEventiClinici().clear();
            dataController.caricaEventiClinici(eventiCliniciFile, List.of(p));

            // Carica TERAPIE CONCOMITANTI
            p.getTerapieConcomitanti().clear();
            dataController.caricaTerapieConcomitanti(terapieConcomitantiFile, List.of(p));
        }

        // Listener per selezione paziente
        pazientiList.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP != null) {
                mostraRilevazioni(newP);
                mostraTerapie(newP);
                mostraAssunzioni(newP);
                mostraSchedaClinica(newP);
                mostraEventi(newP);
                aggiornaTerapieConcomitanti(newP); // Aggiorna anche la lista delle terapie concomitanti
            }
        });

        // Seleziona automaticamente il primo paziente
        if (!diabetologo.getPazienti().isEmpty()) {
            pazientiList.getSelectionModel().selectFirst();
        }
    }


    // ---------- Metodi di visualizzazione ----------

    private void mostraRilevazioni(Paziente p) {
        ObservableList<Rilevazione> lista = FXCollections.observableArrayList(p.getRilevazioni());
        Map<String, Integer> ordinePasti = Map.of(
                "Dopo cena", 1,
                "Prima cena", 2,
                "Dopo pranzo", 3,
                "Prima pranzo", 4,
                "Dopo colazione", 5,
                "Prima colazione", 6
        );
        lista.sort(Comparator
                .comparing(Rilevazione::getData).reversed()
                .thenComparing(r -> ordinePasti.getOrDefault(r.getTipoPasto(), Integer.MAX_VALUE))
        );
        rilevazioniTable.setItems(lista);

        rilevazioniTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Rilevazione r, boolean empty) {
                super.updateItem(r, empty);
                if (r == null || empty) setStyle("");
                else {
                    String colore = determinaColore(r.getTipoPasto(), r.getValore());
                    setStyle("-fx-background-color: " + colore + "; -fx-text-fill: black;");
                }
            }
        });
    }

    private String determinaColore(String tipoPasto, int valore) {
        if (tipoPasto.toLowerCase().contains("prima")) {
            if (valore < 80) return "deepskyblue";
            else if (valore <= 130) return "lightgreen";
            else if (valore <= 180) return "khaki";
            else return "orange";
        } else {
            if (valore < 180) return "lightgreen";
            else if (valore <= 250) return "orange";
            else return "tomato";
        }
    }

    private void mostraTerapie(Paziente p) {
        LocalDate oggi = LocalDate.now();
        for (Terapia t : p.getTerapie()) {
            if (t.getStato() == Terapia.Stato.ATTIVA && oggi.isAfter(t.getDataFine())) {
                t.setStato(Terapia.Stato.TERMINATA);
            }
        }
        ObservableList<Terapia> lista = FXCollections.observableArrayList(p.getTerapie());
        terapieTable.setItems(lista);
        terapieTable.refresh();
    }

    private void mostraAssunzioni(Paziente p) {
        ObservableList<Assunzione> lista = FXCollections.observableArrayList(p.getAssunzioni());
        lista.sort(Comparator.comparing(Assunzione::getData)
                .thenComparing(Assunzione::getOra).reversed());
        assunzioniTable.setItems(lista);
    }

    private void mostraSchedaClinica(Paziente p) {
        if (p.getSchedaClinica() != null) {
            fattoriRischioArea.setText(p.getSchedaClinica().getFattoriRischio());
            patologieArea.setText(p.getSchedaClinica().getPregressePatologie());
            comorbiditaArea.setText(p.getSchedaClinica().getComorbidita());
        } else {
            fattoriRischioArea.clear();
            patologieArea.clear();
            comorbiditaArea.clear();
        }
    }

    private void mostraEventi(Paziente p) {
        ObservableList<EventoClinico> lista = FXCollections.observableArrayList(p.getEventiClinici());
        lista.sort(Comparator.comparing(EventoClinico::getData).reversed()
                .thenComparing(e -> e.getOra() != null ? e.getOra() : LocalTime.MIDNIGHT, Comparator.reverseOrder()));
        eventiTable.setItems(lista);
    }



    // ---------- Utility ----------

    private void showCustomAlert(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login - Sistema Diabete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- Metodi Terapie ----------
    @FXML
    private void handleAggiungiTerapia() {
        Paziente selected = pazientiList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TerapiaForm.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nuova Terapia");
            dialogStage.setScene(new Scene(loader.load()));

            TerapiaFormController controller = loader.getController();
            controller.setMedicoId(diabetologo.getId());

            dialogStage.showAndWait();

            Terapia nuova = controller.getNuovaTerapia();
            if (nuova != null) {
                selected.aggiungiTerapia(nuova);
                mostraTerapie(selected);
                dataController.salvaTerapie("src/resources/terapie.csv", List.of(selected));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModificaStatoTerapia() {
        Terapia selected = terapieTable.getSelectionModel().getSelectedItem();
        Paziente paziente = pazientiList.getSelectionModel().getSelectedItem();
        if (selected == null || paziente == null) return;

        ChoiceDialog<Terapia.Stato> dialog = new ChoiceDialog<>(selected.getStato(),
                Terapia.Stato.ATTIVA, Terapia.Stato.IN_PAUSA, Terapia.Stato.TERMINATA);
        dialog.setTitle("Cambia stato terapia");
        dialog.setHeaderText(null);
        dialog.setContentText("Seleziona nuovo stato:");
        dialog.showAndWait().ifPresent(nuovoStato -> {
            LocalDate oggi = LocalDate.now();
            if (oggi.isBefore(selected.getDataInizio()) || oggi.isAfter(selected.getDataFine())) {
                showCustomAlert("Errore", "Non puoi modificare lo stato fuori dall'intervallo valido!", Alert.AlertType.ERROR);
            } else {
                selected.setStato(nuovoStato);
                dataController.salvaTerapie("src/resources/terapie.csv", List.of(paziente));
                mostraTerapie(paziente);
            }
        });
    }

    @FXML
    private void handleSalvaSchedaClinica() {
        Paziente selected = pazientiList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showCustomAlert("Errore", "Seleziona un paziente prima di salvare la scheda.", Alert.AlertType.ERROR);
            return;
        }

        SchedaClinica scheda = new SchedaClinica(
                fattoriRischioArea.getText(),
                patologieArea.getText(),
                comorbiditaArea.getText()
        );
        selected.setSchedaClinica(scheda);

        dataController.salvaSchedeCliniche(schedeFile, List.of(selected));
        showCustomAlert("Successo", "Scheda clinica salvata correttamente.", Alert.AlertType.INFORMATION);
    }

    private void aggiornaTerapieConcomitanti(Paziente paziente) {
        terapieConcomitantiMedicoList.getItems().setAll(paziente.getTerapieConcomitanti());
    }

    @FXML
    private void mostraPagina1() {
        pagina1.setVisible(true);
        pagina1.setManaged(true);
        pagina2.setVisible(false);
        pagina2.setManaged(false);
    }

    @FXML
    private void mostraPagina2() {
        pagina2.setVisible(true);
        pagina2.setManaged(true);
        pagina1.setVisible(false);
        pagina1.setManaged(false);
    }
}
