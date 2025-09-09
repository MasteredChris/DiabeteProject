package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Assunzione {
    private LocalDate data;
    private LocalTime ora;
    private String farmaco;
    private double quantita;

    public Assunzione(LocalDate data, LocalTime ora, String farmaco, double quantita) {
        this.data = data;
        this.ora = ora;
        this.farmaco = farmaco;
        this.quantita = quantita;
    }

    public LocalDate getData() { return data; }
    public LocalTime getOra() { return ora; }
    public String getFarmaco() { return farmaco; }
    public double getQuantita() { return quantita; }
}
