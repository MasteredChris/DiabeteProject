package model;

import java.util.ArrayList;
import java.util.List;

public class Diabetologo extends Utente {
    private List<Paziente> pazienti;

    public Diabetologo(int id, String nome, String cognome, String email, String password) {
        super(id, nome, cognome, email, password);
        this.pazienti = new ArrayList<>();
    }


    @Override
    public String getType() {
        return "Diabetologo";
    }

    public List<Paziente> getPazienti() { return pazienti; }
    public void addPaziente(Paziente p) { pazienti.add(p); }
}
