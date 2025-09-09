package model;

import java.time.LocalDate;

public class TerapiaConcomitante {
    private String farmaco;
    private LocalDate dataInizio;
    private LocalDate dataFine; // opzionale
    private String note;

    public TerapiaConcomitante(String farmaco, LocalDate dataInizio, LocalDate dataFine, String note) {
        this.farmaco = farmaco;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.note = note;
    }

    public String getFarmaco() {
        return farmaco;
    }

    public void setFarmaco(String farmaco) {
        this.farmaco = farmaco;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDataFine(LocalDate dataFine) {
        this.dataFine = dataFine;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return farmaco +
                " (" + dataInizio +
                (dataFine != null ? " - " + dataFine : "") + ")";
    }
}
