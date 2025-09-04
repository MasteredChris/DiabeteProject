import Controller.SistemaController;
import Util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static SistemaController sistemaController;

    // Dimensioni finestre
    private static final int LOGIN_WIDTH = 400;
    private static final int LOGIN_HEIGHT = 300;
    private static final int DASHBOARD_WIDTH = 1200;
    private static final int DASHBOARD_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApp.primaryStage = primaryStage;

        // Inizializza il sistema
        initializeSystem();

        // Configura la finestra principale
        primaryStage.setTitle("Sistema Telemedicina Diabetici");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(200);

        // Carica la schermata di login
        showLoginScreen();

        // Gestisce la chiusura dell'applicazione
        primaryStage.setOnCloseRequest(event -> {
            handleApplicationExit();
        });

        primaryStage.show();
    }

    /**
     * Inizializza il sistema controller e il database
     */
    private void initializeSystem() {
        try {
            System.out.println("Inizializzazione Sistema Telemedicina...");

            // Inizializza il database CSV se necessario
            if (!DatabaseUtil.isDatabaseInitialized()) {
                System.out.println("Database non trovato, inizializzazione...");
                DatabaseUtil.initializeDatabase();
            } else {
                System.out.println("Database trovato, verifica integrità...");
                if (!DatabaseUtil.validateDatabase()) {
                    System.out.println("Database corrotto, riparazione...");
                    DatabaseUtil.repairDatabase();
                }
            }

            // Crea il controller di sistema
            sistemaController = new SistemaController();

            // Mostra statistiche database
            DatabaseUtil.printDatabaseStats();

            System.out.println("Sistema inizializzato correttamente");

        } catch (Exception e) {
            System.err.println("Errore durante l'inizializzazione: " + e.getMessage());
            showErrorDialog("Errore di Inizializzazione",
                    "Impossibile inizializzare il sistema: " + e.getMessage());
        }
    }

    /**
     * Mostra la schermata di login
     */
    public static void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/View/login.fxml"));
            Parent root = loader.load();

            // Passa il sistema controller al controller della view
            LoginViewController loginController = loader.getController();
            loginController.setSistemaController(sistemaController);

            Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("/View/styles.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Login - Sistema Telemedicina");
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Errore caricamento schermata login: " + e.getMessage());
            showErrorDialog("Errore", "Impossibile caricare la schermata di login");
        }
    }

    /**
     * Mostra la dashboard del paziente
     */
    public static void showPazienteDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/View/paziente_dashboard.fxml"));
            Parent root = loader.load();

            // Passa il sistema controller al controller della view
            PazienteDashboardViewController dashboardController = loader.getController();
            dashboardController.setSistemaController(sistemaController);
            dashboardController.initialize();

            Scene scene = new Scene(root, DASHBOARD_WIDTH, DASHBOARD_HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("/View/styles.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Dashboard Paziente - Sistema Telemedicina");
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Errore caricamento dashboard paziente: " + e.getMessage());
            showErrorDialog("Errore", "Impossibile caricare la dashboard paziente");
        }
    }


    /**
     * Mostra la dashboard del medico
     */
    public static void showMedicoDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/View/medico-dashboard.fxml"));
            Parent root = loader.load();

            // Passa il sistema controller al controller della view
            MedicoDashboardViewController dashboardController = loader.getController();
            dashboardController.setSistemaController(sistemaController);
            dashboardController.initialize();

            Scene scene = new Scene(root, DASHBOARD_WIDTH, DASHBOARD_HEIGHT);
            scene.getStylesheets().add(MainApp.class.getResource("/View/styles.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Dashboard Medico - Sistema Telemedicina");
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Errore caricamento dashboard medico: " + e.getMessage());
            showErrorDialog("Errore", "Impossibile caricare la dashboard medico");
        }
    }

    /**
     * Mostra un dialog per aggiungere rilevazioni
     */
    public static void showRilevazioneDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/View/rilevazione-form.fxml"));
            Parent root = loader.load();

            RilevazioneViewController controller = loader.getController();
            controller.setSistemaController(sistemaController);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nuova Rilevazione");
            dialogStage.setScene(new Scene(root, 500, 400));
            dialogStage.setResizable(false);
            dialogStage.initOwner(primaryStage);
            dialogStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Errore caricamento dialog rilevazione: " + e.getMessage());
            showErrorDialog("Errore", "Impossibile aprire il form rilevazione");
        }
    }

    /**
     * Mostra un dialog per gestire terapie
     */
    public static void showTerapiaDialog(String idPaziente) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/View/terapia-form.fxml"));
            Parent root = loader.load();

            TerapiaViewController controller = loader.getController();
            controller.setSistemaController(sistemaController);
            controller.setIdPaziente(idPaziente);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Gestione Terapia");
            dialogStage.setScene(new Scene(root, 600, 500));
            dialogStage.setResizable(false);
            dialogStage.initOwner(primaryStage);
            dialogStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Errore caricamento dialog terapia: " + e.getMessage());
            showErrorDialog("Errore", "Impossibile aprire il form terapia");
        }
    }

    /**
     * Mostra un dialog di alert
     */
    public static void showAlertDialog(Model.Alert alert) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/View/alert-dialog.fxml"));
            Parent root = loader.load();

            AlertDialogViewController controller = loader.getController();
            controller.setAlert(alert);
            controller.setSistemaController(sistemaController);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Alert - " + alert.getTipo());
            dialogStage.setScene(new Scene(root, 400, 300));
            dialogStage.setResizable(false);
            dialogStage.initOwner(primaryStage);
            dialogStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Errore caricamento dialog alert: " + e.getMessage());
            showErrorDialog("Errore", "Impossibile aprire il dialog alert");
        }
    }

    /**
     * Esegue il logout e torna alla schermata di login
     */
    public static void logout() {
        if (sistemaController != null) {
            sistemaController.logout();
        }
        showLoginScreen();
    }

    /**
     * Mostra un dialog di errore
     */
    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * Mostra un dialog di informazione
     */
    public static void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * Mostra un dialog di conferma
     */
    public static boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);

        return alert.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK;
    }

    /**
     * Gestisce la chiusura dell'applicazione
     */
    private void handleApplicationExit() {
        try {
            System.out.println("Chiusura applicazione...");

            // Logout se necessario
            if (sistemaController != null && sistemaController.isLogged()) {
                sistemaController.logout();
            }

            // Statistiche finali
            System.out.println("Applicazione chiusa correttamente");

        } catch (Exception e) {
            System.err.println("Errore durante la chiusura: " + e.getMessage());
        }
    }

    /**
     * Getter per il sistema controller
     */
    public static SistemaController getSistemaController() {
        return sistemaController;
    }

    /**
     * Getter per il primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Aggiorna il titolo della finestra
     */
    public static void updateTitle(String title) {
        if (primaryStage != null) {
            primaryStage.setTitle(title + " - Sistema Telemedicina");
        }
    }

    public static void main(String[] args) {
        // Imposta proprietà sistema per JavaFX
        System.setProperty("javafx.preloader", "");

        // Avvia l'applicazione JavaFX
        launch(args);
    }
}