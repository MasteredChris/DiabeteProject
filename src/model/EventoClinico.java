package model;

import java.time.LocalDate;

public class EventoClinico {
    public enum Tipo {
        SINTOMO,
        PATOLOGIA
    }

    private Tipo tipo;
    private String descrizione;
    private LocalDate dataInizio;
    private LocalDate dataFine; // opzionale
    private String note;

    public EventoClinico(Tipo tipo, String descrizione, LocalDate dataInizio, LocalDate dataFine, String note) {
        this.tipo = tipo;
        this.descrizione = descrizione;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.note = note;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
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
        return tipo + ": " + descrizione +
                " (" + dataInizio +
                (dataFine != null ? " - " + dataFine : "") + ")";
    }
}

