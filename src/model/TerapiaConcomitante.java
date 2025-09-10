package model;

public class TerapiaConcomitante {
    private String tipoTerapia;
    private String descrizione;

    public TerapiaConcomitante(String tipoTerapia, String descrizione) {
        this.tipoTerapia = tipoTerapia;
        this.descrizione = descrizione;
    }

    public String getTipoTerapia() {
        return tipoTerapia;
    }

    public void setTipoTerapia(String tipoTerapia) {
        this.tipoTerapia = tipoTerapia;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    @Override
    public String toString() {
        return tipoTerapia + (descrizione != null && !descrizione.isEmpty() ? " - " + descrizione : "");
    }
}
