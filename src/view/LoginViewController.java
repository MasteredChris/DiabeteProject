package view;

import controller.LoginController;
import model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginViewController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private LoginController loginController;

    @FXML
    private void initialize() {
        loginController = new LoginController();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        Utente utente = loginController.login(email, password);

        if (utente != null) {
            try {
                Stage stage = (Stage) emailField.getScene().getWindow();
                FXMLLoader loader;

                if (utente instanceof Paziente) {
                    loader = new FXMLLoader(getClass().getResource("PazienteDashboard.fxml"));
                } else {
                    loader = new FXMLLoader(getClass().getResource("DiabetologoDashboard.fxml"));
                }

                Scene scene = new Scene(loader.load());

                stage.setScene(scene);
                stage.setTitle("Dashboard " + utente.getType());

                // Massimizza la finestra basandosi sullo schermo
                javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());

                // Passo l'utente al controller giusto
                if (utente instanceof Paziente) {
                    PazienteDashboardController controller = loader.getController();
                    controller.setUtente((Paziente) utente);
                } else {
                    DiabetologoDashboardController controller = loader.getController();
                    controller.setUtente((Diabetologo) utente);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            messageLabel.setText("Email o password errati!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

}
