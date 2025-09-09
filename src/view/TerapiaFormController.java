package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Terapia;

import java.time.LocalDate;

public class TerapiaFormController {

    @FXML private TextField farmacoField;
    @FXML private TextField assunzioniField;
    @FXML private TextField quantitaField;
    @FXML private TextField indicazioniField;
    @FXML private DatePicker dataInizioPicker;
    @FXML private DatePicker dataFinePicker;
    @FXML private ComboBox<Terapia.Stato> statoCombo;
    private int medicoId;

    private Terapia nuovaTerapia;

    public Terapia getNuovaTerapia() {
        return nuovaTerapia;
    }

    public void setMedicoId(int id) {
        this.medicoId = id;
    }

    @FXML
    private void handleSalva() {
        try {
            String farmaco = farmacoField.getText();
            int assunzioni = Integer.parseInt(assunzioniField.getText());
            double quantita = Double.parseDouble(quantitaField.getText());
            String indicazioni = indicazioniField.getText();
            LocalDate dataInizio = dataInizioPicker.getValue();
            LocalDate dataFine = dataFinePicker.getValue();
            Terapia.Stato stato = Terapia.Stato.ATTIVA;

            LocalDate oggi = LocalDate.now();

            // Controllo date
            if (dataInizio == null || dataFine == null) {
                showAlert("Errore", "Seleziona entrambe le date di inizio e fine!");
                return;
            }
            if (dataInizio.isBefore(oggi) || dataFine.isBefore(oggi)) {
                showAlert("Errore", "Le date non possono essere nel passato!");
                return;
            }
            if (dataFine.isBefore(dataInizio)) {
                showAlert("Errore", "La data di fine deve essere successiva a quella di inizio!");
                return;
            }

            // Imposta automaticamente lo stato a ATTIVA
            nuovaTerapia = new Terapia(
                    farmaco,
                    assunzioni,
                    quantita,
                    indicazioni,
                    dataInizio,
                    dataFine,
                    stato,
                    medicoId
            );

            Stage stage = (Stage) farmacoField.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Inserisci valori numerici corretti per assunzioni e quantità!");
            alert.showAndWait();
        }
    }


    @FXML
    private void handleAnnulla() {
        nuovaTerapia = null; // niente salvataggio
        Stage stage = (Stage) farmacoField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
