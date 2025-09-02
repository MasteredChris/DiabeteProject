package Model;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class OrarioAssunzione {
    private LocalTime ora;
    private double quantita;
    private String descrizione;

    public OrarioAssunzione(LocalTime ora, double quantita, String descrizione) {
        this.ora = ora;
        this.quantita = quantita;
        this.descrizione = descrizione;
    }

    // Getter standard
    public LocalTime getOra() { return ora; }
    public double getQuantita() { return quantita; }
    public String getDescrizione() { return descrizione; }

    // Metodo utility
    public boolean isPassato() {
        return LocalTime.now().isAfter(this.ora);
    }
}