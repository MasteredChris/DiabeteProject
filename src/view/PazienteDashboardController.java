package view;

import controller.DataController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public class PazienteDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label medicoLabel;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<String> tipoPastoChoice;
    @FXML private TextField valoreField;
    @FXML private TableView<Rilevazione> rilevazioniTable;
    @FXML private TableColumn<Rilevazione, String> dataColumn;
    @FXML private TableColumn<Rilevazione, String> tipoPastoColumn;
    @FXML private TableColumn<Rilevazione, Integer> valoreColumn;

    @FXML private TableView<Terapia> terapieTable;
    @FXML private TableColumn<Terapia, String> farmacoColumn;
    @FXML private TableColumn<Terapia, Integer> assunzioniColumn;
    @FXML private TableColumn<Terapia, Double> quantitaColumn;
    @FXML private TableColumn<Terapia, String> indicazioniColumn;
    @FXML private TableColumn<Terapia, String> dataInizioColumn;
    @FXML private TableColumn<Terapia, String> dataFineColumn;
    @FXML private TableColumn<Terapia, Terapia.Stato> statoColumn;

    @FXML private DatePicker assunzioneDatePicker;
    @FXML private TextField oraField;
    @FXML private ComboBox<String> farmacoChoice;
    @FXML private TextField quantitaField;
    @FXML private TableView<Assunzione> assunzioniTable;
    @FXML private TableColumn<Assunzione, String> dataAssunzioneColumn;
    @FXML private TableColumn<Assunzione, String> oraAssunzioneColumn;
    @FXML private TableColumn<Assunzione, String> farmacoAssunzioneColumn;
    @FXML private TableColumn<Assunzione, Number> quantitaAssunzioneColumn;

    @FXML private TableView<EventoClinico> eventiTable;
    @FXML private TableColumn<EventoClinico, String> tipoEventoColumn;
    @FXML private TableColumn<EventoClinico, String> descrizioneEventoColumn;
    @FXML private TableColumn<EventoClinico, String> dataEventoColumn;
    @FXML private TableColumn<EventoClinico, String> oraEventoColumn;
    @FXML private TableColumn<EventoClinico, String> noteEventoColumn;

    @FXML private ChoiceBox<String> tipoEventoChoice;
    @FXML private TextField descrizioneEventoField;
    @FXML private TextField noteEventoField;

    @FXML private HBox pagina1;
    @FXML private HBox pagina2;


    private Paziente paziente;
    private DataController dataController = new DataController();
    private final String rilevazioniFile = "src/resources/rilevazioni.csv";
    private final String terapieFile = "src/resources/terapie.csv";
    private final String assunzioniFile = "src/resources/assunzioni.csv";
    private final String eventiCliniciFile = "src/resources/eventi_clinici.csv";
    private final String eventiFile = "src/resources/eventi_clinici.csv";

    @FXML
    public void initialize() {
        tipoPastoChoice.getItems().addAll(
                "Prima colazione", "Dopo colazione",
                "Prima pranzo", "Dopo pranzo",
                "Prima cena", "Dopo cena"
        );

        dataColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        tipoPastoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPasto()));
        valoreColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValore()));

        farmacoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmaco()));
        assunzioniColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getAssunzioniGiornaliere()));
        quantitaColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantitaPerAssunzione()));
        indicazioniColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIndicazioni()));
        dataInizioColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInizio().toString()));
        dataFineColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataFine().toString()));
        statoColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStato()));

        dataAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        oraAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOra().toString()));
        farmacoAssunzioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmaco()));
        quantitaAssunzioneColumn.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getQuantita()));

        // ChoiceBox tipo
        tipoEventoChoice.getItems().addAll("Sintomo", "Patologia");

        // Colonne tabella
        tipoEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        descrizioneEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescrizione()));
        dataEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        oraEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOra().toString()));
        noteEventoColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNote()));


        terapieTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Terapia item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    switch (item.getStato()) {
                        case ATTIVA -> setStyle("-fx-background-color: lightgreen;");
                        case IN_PAUSA -> setStyle("-fx-background-color: orange;");
                        case TERMINATA -> setStyle("-fx-background-color: tomato;");
                    }
                }
            }
        });
    }

    public void setUtente(Paziente paziente) {
        this.paziente = paziente;
        welcomeLabel.setText("Benvenuto, " + paziente.getNome() + " " + paziente.getCognome());
        if (paziente.getMedico() != null)
            medicoLabel.setText("Medico curante: Dr. " + paziente.getMedico().getNome() + " " + paziente.getMedico().getCognome());

        // Ricarica dati dal CSV usando DataController
        paziente.getRilevazioni().clear();
        dataController.caricaRilevazioni(rilevazioniFile, List.of(paziente));
        aggiornaListaRilevazioni();

        paziente.getTerapie().clear();
        dataController.caricaTerapie(terapieFile, List.of(paziente));
        aggiornaListaTerapie();

        paziente.getAssunzioni().clear();
        dataController.caricaAssunzioni(assunzioniFile, List.of(paziente));
        aggiornaListaAssunzioni();

        aggiornaFarmaciChoice();
        // Carica eventi clinici e aggiorna la tabella
        paziente.getEventiClinici().clear();
        dataController.caricaEventiClinici(eventiCliniciFile, List.of(paziente));
        aggiornaListaEventi();
    }

    @FXML
    private void handleSalvaRilevazione() {
        LocalDate data = datePicker.getValue();
        String tipoPasto = tipoPastoChoice.getValue();
        String valoreStr = valoreField.getText();

        if (data == null || tipoPasto == null || valoreStr.isEmpty()) {
            showAlert("Errore", "Compila tutti i campi");
            return;
        }

        try {
            int valore = Integer.parseInt(valoreStr);
            Rilevazione r = new Rilevazione(data, tipoPasto, valore);
            paziente.aggiungiRilevazione(r);

            dataController.salvaRilevazioni(rilevazioniFile, List.of(paziente));
            aggiornaListaRilevazioni();
            valoreField.clear();
        } catch (NumberFormatException e) {
            showAlert("Errore", "Inserisci un numero valido");
        } catch (IllegalArgumentException e) {
            showAlert("Errore", e.getMessage());
        }
    }

    @FXML
    private void handleEliminaRilevazione() {
        Rilevazione selezionata = rilevazioniTable.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            showAlert("Attenzione", "Seleziona una rilevazione da eliminare.");
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION,
                "Vuoi davvero eliminare questa rilevazione?",
                ButtonType.YES, ButtonType.NO);
        conferma.showAndWait();

        if (conferma.getResult() == ButtonType.YES) {
            paziente.getRilevazioni().remove(selezionata);
            dataController.salvaRilevazioni(rilevazioniFile, List.of(paziente));
            aggiornaListaRilevazioni();
        }
    }

    private void aggiornaListaRilevazioni() {
        ObservableList<Rilevazione> lista = FXCollections.observableArrayList(paziente.getRilevazioni());
        lista.sort(Comparator.comparing(Rilevazione::getData).reversed());
        rilevazioniTable.setItems(lista);
    }

    private void aggiornaListaTerapie() {
        LocalDate oggi = LocalDate.now();
        List<Terapia> terapieOrdinate = paziente.getTerapie().stream()
                .sorted(Comparator.comparing(Terapia::getStato)
                        .thenComparing(Terapia::getDataInizio))
                .toList();
        terapieTable.setItems(FXCollections.observableArrayList(terapieOrdinate));

        farmacoChoice.getItems().clear();
        for (Terapia t : terapieOrdinate) {
            if (t.getStato() == Terapia.Stato.ATTIVA &&
                    !oggi.isBefore(t.getDataInizio()) &&
                    !oggi.isAfter(t.getDataFine()) &&
                    !farmacoChoice.getItems().contains(t.getFarmaco())) {
                farmacoChoice.getItems().add(t.getFarmaco());
            }
        }
    }

    @FXML
    private void handleSalvaAssunzione() {
        LocalDate data = assunzioneDatePicker.getValue();
        String oraStr = oraField.getText();
        String farmaco = farmacoChoice.getValue();
        String qStr = quantitaField.getText();

        if (data == null || oraStr.isEmpty() || farmaco == null || qStr.isEmpty()) {
            showAlert("Errore", "Compila tutti i campi per l'assunzione.");
            return;
        }

        try {
            LocalTime ora = LocalTime.parse(oraStr);
            double quantita = Double.parseDouble(qStr);

            LocalDate oggi = LocalDate.now();
            boolean farmacoValido = paziente.getTerapie().stream().anyMatch(t ->
                    t.getFarmaco().equals(farmaco) &&
                            t.getStato() == Terapia.Stato.ATTIVA &&
                            (oggi.isEqual(t.getDataInizio()) || oggi.isAfter(t.getDataInizio())) &&
                            (oggi.isEqual(t.getDataFine()) || oggi.isBefore(t.getDataFine()))
            );

            if (!farmacoValido) {
                showAlert("Errore", "Non puoi assumere questo farmaco: terapia non attiva o fuori intervallo!");
                return;
            }

            Assunzione a = new Assunzione(data, ora, farmaco, quantita);
            paziente.aggiungiAssunzione(a);
            dataController.salvaAssunzioni(assunzioniFile, List.of(paziente));
            aggiornaListaAssunzioni();

            oraField.clear();
            quantitaField.clear();
        } catch (Exception e) {
            showAlert("Errore", "Formato non corretto per ora o quantità.");
        }
    }

    @FXML
    private void handleEliminaAssunzione() {
        Assunzione selezionata = assunzioniTable.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            showAlert("Attenzione", "Seleziona un'assunzione da eliminare.");
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.WARNING,
                "Vuoi davvero eliminare questa assunzione?",
                ButtonType.YES, ButtonType.NO);
        conferma.showAndWait();

        if (conferma.getResult() == ButtonType.YES) {
            // Rimuove l'assunzione dal paziente
            paziente.getAssunzioni().remove(selezionata);

            // Salva solo le assunzioni del paziente corrente
            dataController.salvaAssunzioni(assunzioniFile, List.of(paziente));

            // Aggiorna la TableView
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
        for (Terapia t : paziente.getTerapie()) {
            boolean valida = t.getStato() == Terapia.Stato.ATTIVA &&
                    !oggi.isBefore(t.getDataInizio()) &&
                    !oggi.isAfter(t.getDataFine());
            if (valida && !farmacoChoice.getItems().contains(t.getFarmaco())) {
                farmacoChoice.getItems().add(t.getFarmaco());
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.setTitle("Login - Sistema Diabete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    @FXML private void handleApriContatta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ContattaMedico.fxml"));
            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.setTitle("Contatta Medico");
            ContattaMedicoController controller = loader.getController();
            controller.setPaziente(paziente); // passa il paziente corrente
             stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore", "Impossibile aprire la finestra per contattare il medico.");
        }
    }


    @FXML
    private void handleAggiungiEvento() {
        String tipo = tipoEventoChoice.getValue();
        String descrizione = descrizioneEventoField.getText();
        String note = noteEventoField.getText();

        if (tipo == null || descrizione.isEmpty()) {
            showAlert("Errore", "Seleziona il tipo e scrivi la descrizione.");
            return;
        }

        LocalDate oggi = LocalDate.now();
        LocalTime ora = java.time.LocalTime.now();

        EventoClinico e = new EventoClinico(tipo, descrizione, oggi, ora, note);
        paziente.aggiungiEventoClinico(e); // metodo da aggiungere in Paziente

        dataController.salvaEventiClinici(eventiFile, List.of(paziente));
        aggiornaListaEventi();

        descrizioneEventoField.clear();
        noteEventoField.clear();
    }

    private void aggiornaListaEventi() {
        ObservableList<EventoClinico> lista = FXCollections.observableArrayList(paziente.getEventiClinici());
        lista.sort(Comparator.comparing(EventoClinico::getData).thenComparing(EventoClinico::getOra).reversed());
        eventiTable.setItems(lista);
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
