package model;

import view.DiabetologoDashboardController;

import java.util.ArrayList;
import java.util.List;

public class AppState {

    private static AppState instance;
    private DiabetologoDashboardController diabetologoDashboardController;

    private List<String> notificheAssunzioni = new ArrayList<>();
    private List<String> notificheGlicemia = new ArrayList<>();

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
    public void aggiungiNotificaAssunzione(String messaggio) {
        notificheAssunzioni.add(messaggio);
    }

    public List<String> prelevaNotificheAssunzioni() {
        List<String> copy = new ArrayList<>(notificheAssunzioni);
        notificheAssunzioni.clear();
        return copy;
    }

    // --- Notifiche glicemia fuori range ---
    public void aggiungiNotificaGlicemia(String messaggio) {
        notificheGlicemia.add(messaggio);
    }

    public List<String> prelevaNotificheGlicemia() {
        List<String> copy = new ArrayList<>(notificheGlicemia);
        notificheGlicemia.clear();
        return copy;
    }
}
