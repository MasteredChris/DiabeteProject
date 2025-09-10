package model;

import java.time.LocalDate;

public class Rilevazione {

    private LocalDate data;
    private String tipoPasto; // "Prima colazione", "Dopo pranzo", ecc.
    private int valore;       // mg/dL
    private boolean fuoriRange;

    public Rilevazione(LocalDate data, String tipoPasto, int valore) {
        this.data = data;
        this.tipoPasto = tipoPasto;
        this.valore = valore;
        this.fuoriRange = calcolaFuoriRange();
    }

    // --- Metodi di accesso ---
    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getTipoPasto() {
        return tipoPasto;
    }

    public void setTipoPasto(String tipoPasto) {
        this.tipoPasto = tipoPasto;
        this.fuoriRange = calcolaFuoriRange();
    }

    public int getValore() {
        return valore;
    }

    public void setValore(int valore) {
        this.valore = valore;
        this.fuoriRange = calcolaFuoriRange();
    }

    public boolean isFuoriRange() {
        return fuoriRange;
    }

    // --- Logica per determinare se il valore è fuori range ---
    private boolean calcolaFuoriRange() {
        if (tipoPasto.toLowerCase().contains("prima")) {
            return valore < 80 || valore > 130;
        } else if (tipoPasto.toLowerCase().contains("dopo")) {
            return valore > 130; // invece di 180
        }
        return false;
    }


    @Override
    public String toString() {
        return data + " - " + tipoPasto + ": " + valore + " mg/dL" + (fuoriRange ? " ⚠" : "");
    }
}
