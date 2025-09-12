package model;

import controller.DiabetologoDashboardController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppState {

    private static AppState instance;
    private DiabetologoDashboardController diabetologoDashboardController;

    // Notifiche associate per diabetologo ID
    private Map<String, List<String>> notificheAssunzioniPerDiabetologo = new HashMap<>();
    private Map<String, List<String>> notificheGlicemiaPerDiabetologo = new HashMap<>();

    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    // --- Dashboard del medico ---
    public void setDiabetologoDashboardController(DiabetologoDashboardController controller) {
        this.diabetologoDashboardController = controller;
    }

    public DiabetologoDashboardController getDiabetologoDashboardController() {
        return diabetologoDashboardController;
    }

    // --- Notifiche assunzioni mancanti ---
    public void aggiungiNotificaAssunzione(String diabetologoId, String messaggio) {
        notificheAssunzioniPerDiabetologo
                .computeIfAbsent(diabetologoId, k -> new ArrayList<>())
                .add(messaggio);
    }

    public List<String> prelevaNotificheAssunzioni(String diabetologoId) {
        List<String> notifiche = notificheAssunzioniPerDiabetologo.getOrDefault(diabetologoId, new ArrayList<>());
        List<String> copy = new ArrayList<>(notifiche);
        notifiche.clear();
        return copy;
    }

    // --- Notifiche glicemia fuori range ---
    public void aggiungiNotificaGlicemia(String diabetologoId, String messaggio) {
        notificheGlicemiaPerDiabetologo
                .computeIfAbsent(diabetologoId, k -> new ArrayList<>())
                .add(messaggio);
    }

    public List<String> prelevaNotificheGlicemia(String diabetologoId) {
        List<String> notifiche = notificheGlicemiaPerDiabetologo.getOrDefault(diabetologoId, new ArrayList<>());
        List<String> copy = new ArrayList<>(notifiche);
        notifiche.clear();
        return copy;
    }

    // --- Metodi di compatibilità (deprecated) ---
    @Deprecated
    public void aggiungiNotificaAssunzione(String messaggio) {
        // Mantieni compatibilità, ma non dovrebbe essere usato
        System.err.println("WARN: Usare aggiungiNotificaAssunzione(diabetologoId, messaggio)");
    }

    @Deprecated
    public List<String> prelevaNotificheAssunzioni() {
        // Mantieni compatibilità, ma non dovrebbe essere usato
        System.err.println("WARN: Usare prelevaNotificheAssunzioni(diabetologoId)");
        return new ArrayList<>();
    }

    @Deprecated
    public void aggiungiNotificaGlicemia(String messaggio) {
        // Mantieni compatibilità, ma non dovrebbe essere usato
        System.err.println("WARN: Usare aggiungiNotificaGlicemia(diabetologoId, messaggio)");
    }

    @Deprecated
    public List<String> prelevaNotificheGlicemia() {
        // Mantieni compatibilità, ma non dovrebbe essere usato
        System.err.println("WARN: Usare prelevaNotificheGlicemia(diabetologoId)");
        return new ArrayList<>();
    }
}