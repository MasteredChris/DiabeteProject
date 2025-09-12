package controller;

import model.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller per la vista di login dell'applicazione.
 * Gestisce l'autenticazione degli utenti e la navigazione verso le dashboard appropriate.
 */
public class LoginViewController {

    private static final Logger LOGGER = Logger.getLogger(LoginViewController.class.getName());

    // Costanti per la configurazione
    private static final String ERROR_STYLE = "-fx-text-fill: red;";
    private static final String INFO_STYLE = "-fx-text-fill: blue;";
    private static final String PAZIENTE_DASHBOARD_FXML = "/view/PazienteDashboard.fxml";
    private static final String DIABETOLOGO_DASHBOARD_FXML = "/view/DiabetologoDashboard.fxml";
    private static final String DASHBOARD_TITLE_PREFIX = "Dashboard ";

    // Componenti FXML
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private LoginController loginController;

    /**
     * Inizializzazione del controller JavaFX.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Inizializzazione LoginViewController...");

        // Inizializzazione asincrona per non bloccare la UI
        initializeControllerAsync();

        // Setup event handlers per login con Enter
        setupEventHandlers();

        // Focus iniziale sul campo email
        Platform.runLater(() -> emailField.requestFocus());
    }

    /**
     * Inizializza il LoginController in modo asincrono.
     */
    private void initializeControllerAsync() {
        Task<LoginController> initTask = new Task<LoginController>() {
            @Override
            protected LoginController call() throws Exception {
                return new LoginController();
            }

            @Override
            protected void succeeded() {
                loginController = getValue();
                LOGGER.info("LoginController inizializzato correttamente");
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showMessage("Errore di inizializzazione del sistema", ERROR_STYLE);
                    LOGGER.log(Level.SEVERE, "Inizializzazione LoginController fallita", getException());
                });
            }
        };

        Thread initThread = new Thread(initTask);
        initThread.setDaemon(true);
        initThread.start();
    }

    /**
     * Configura gli event handler per i componenti.
     */
    private void setupEventHandlers() {
        // Permette il login premendo Enter in qualsiasi campo
        emailField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
    }

    /**
     * Gestisce il processo di login.
     */
    @FXML
    private void handleLogin() {
        if (loginController == null) {
            showMessage("Sistema non ancora pronto. Riprova tra qualche secondo.", INFO_STYLE);
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validazione input
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Per favore inserisci email e password.", ERROR_STYLE);
            return;
        }

        // Validazione formato email base
        if (!isValidEmail(email)) {
            showMessage("Formato email non valido.", ERROR_STYLE);
            return;
        }

        // Login asincrono per non bloccare la UI
        performLogin(email, password);
    }

    /**
     * Validazione formato email semplice.
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    /**
     * Esegue il login in modo asincrono.
     */
    private void performLogin(String email, String password) {
        // Disabilita il form durante il login
        setFormEnabled(false);
        showMessage("Accesso in corso...", INFO_STYLE);

        Task<Utente> loginTask = new Task<Utente>() {
            @Override
            protected Utente call() throws Exception {
                return loginController.login(email, password);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setFormEnabled(true);
                    handleLoginResult(getValue());
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setFormEnabled(true);
                    showMessage("Errore di sistema. Riprova più tardi.", ERROR_STYLE);
                    LOGGER.log(Level.SEVERE, "Errore durante il login", getException());
                });
            }
        };

        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    /**
     * Gestisce il risultato del login.
     */
    private void handleLoginResult(Utente utente) {
        if (utente != null) {
            LOGGER.log(Level.INFO, "Login successful per utente: {0}", utente.getEmail());
            navigateToDashboard(utente);
        } else {
            showMessage("Email o password errati!", ERROR_STYLE);
            passwordField.clear();
            emailField.requestFocus();
        }
    }

    /**
     * Naviga verso la dashboard appropriata.
     */
    private void navigateToDashboard(Utente utente) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            String fxmlFile = utente instanceof Paziente ?
                    PAZIENTE_DASHBOARD_FXML : DIABETOLOGO_DASHBOARD_FXML;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            // Configura la nuova scena
            stage.setScene(scene);
            stage.setTitle(DASHBOARD_TITLE_PREFIX + utente.getType());
            maximizeWindow(stage);

            // Inizializza il controller della dashboard
            initializeDashboardController(loader, utente);

            LOGGER.log(Level.INFO, "Navigazione alla dashboard {0} completata", utente.getType());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore caricando la dashboard", e);
            showMessage("Errore nel caricamento della dashboard.", ERROR_STYLE);
        }
    }

    /**
     * Inizializza il controller della dashboard con l'utente.
     */
    private void initializeDashboardController(FXMLLoader loader, Utente utente) {
        Object controller = loader.getController();

        if (utente instanceof Paziente && controller instanceof PazienteDashboardController) {
            ((PazienteDashboardController) controller).setUtente((Paziente) utente);
        } else if (utente instanceof Diabetologo && controller instanceof DiabetologoDashboardController) {
            ((DiabetologoDashboardController) controller).setUtente((Diabetologo) utente);
        }
    }

    /**
     * Massimizza la finestra.
     */
    private void maximizeWindow(Stage stage) {
        try {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Impossibile massimizzare la finestra", e);
            stage.setMaximized(true);
        }
    }

    /**
     * Abilita/disabilita il form.
     */
    private void setFormEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
    }

    /**
     * Mostra un messaggio all'utente.
     */
    private void showMessage(String message, String style) {
        messageLabel.setText(message);
        messageLabel.setStyle(style);
    }
}