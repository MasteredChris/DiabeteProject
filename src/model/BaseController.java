package model;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Base controller class providing common functionality for all controllers.
 * Implements shared navigation, alert handling, and exception management.
 */
public abstract class BaseController {

    /**
     * Shows an alert dialog with the specified parameters.
     */
    protected void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert with the given title and message.
     */
    protected void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    /**
     * Navigates to the login view from any controller.
     */
    protected void navigateToLogin(Window currentWindow, String loginViewPath) {
        try {
            Stage stage = (Stage) currentWindow;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(loginViewPath));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login - Sistema Diabete");
        } catch (Exception e) {
            handleException("Errore durante il logout", e);
        }
    }

    /**
     * Handles exceptions by logging and showing error alerts.
     */
    protected void handleException(String userMessage, Exception e) {
        e.printStackTrace(); // Log to console
        showAlert("Errore", userMessage + ": " + e.getMessage());
    }

    /**
     * Validates that a string is not null or empty.
     */
    protected boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates that multiple strings are not null or empty.
     */
    protected boolean areNotEmpty(String... values) {
        for (String value : values) {
            if (!isNotEmpty(value)) {
                return false;
            }
        }
        return true;
    }
}
