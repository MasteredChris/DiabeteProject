package view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.Paziente;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ContattaMedicoController {

    @FXML
    private TextArea messaggioArea;

    private Paziente paziente;

    public void setPaziente(Paziente paziente) {
        this.paziente = paziente;
    }

    @FXML
    private void handleInvia() {
        if (paziente == null || paziente.getMedico() == null || paziente.getMedico().getEmail().isBlank()) {
            showAlert("Errore", "Non hai un medico assegnato.");
            return;
        }

        String destinatario = paziente.getMedico().getEmail();
        String oggetto = "Richiesta dal paziente " + paziente.getNome() + " " + paziente.getCognome();
        String corpo = messaggioArea.getText();

        if (corpo.isBlank()) {
            showAlert("Errore", "Scrivi un messaggio prima di inviare.");
            return;
        }

        try {
            // Codifica URL per oggetto e corpo, sostituendo i + con %20
            String oggettoEnc = URLEncoder.encode(oggetto, StandardCharsets.UTF_8).replace("+", "%20");
            String corpoEnc = URLEncoder.encode(corpo, StandardCharsets.UTF_8).replace("+", "%20");

            String uriStr = String.format("mailto:%s?subject=%s&body=%s",
                    destinatario,
                    oggettoEnc,
                    corpoEnc);

            // Apre il client email predefinito
            java.awt.Desktop.getDesktop().mail(new URI(uriStr));

            showAlert("Successo", "Si è aperto il client email con il messaggio già pronto.");
            messaggioArea.clear();

            // Chiude la finestra
            Stage stage = (Stage) messaggioArea.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore", "Impossibile aprire il client email.");
        }
    }

    @FXML
    private void handleAnnulla() {
        // Chiude la finestra senza inviare
        Stage stage = (Stage) messaggioArea.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
