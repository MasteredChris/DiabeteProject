package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventoClinico {
    private int pazienteId;
    private String tipo;          // "Sintomo" o "Patologia"
    private String descrizione;
    private LocalDate data;       // solo data
    private LocalTime ora;        // solo ora
    private String note;

    public EventoClinico(String tipo, String descrizione, LocalDate data, LocalTime ora, String note) {
        this.tipo = tipo;
        this.descrizione = descrizione;
        this.data = data;
        this.ora = ora;
        this.note = note;
    }

    // getter e setter
    public String getTipo() { return tipo; }
    public String getDescrizione() { return descrizione; }
    public LocalDate getData() { return data; }
    public LocalTime getOra() { return ora; }
    public String getNote() { return note; }
}
