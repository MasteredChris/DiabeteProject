package Model;

import java.time.LocalDateTime;
import java.util.List;

public class Rilevazione {
    private String id;
    private String idPaziente;
    private LocalDateTime dataOra;
    private int valoreGlicemia;
    private TipoRilevazione tipo;
    private String note;
    private List<String> sintomi;

    public Rilevazione(String id, String idPaziente, LocalDateTime dataOra, int valoreGlicemia, TipoRilevazione tipo, String note, List<String> sintomi) {
        this.id = id;
        this.idPaziente = idPaziente;
        this.dataOra = dataOra;
        this.valoreGlicemia = valoreGlicemia;
        this.tipo = tipo;
        this.note = note;
        this.sintomi = sintomi;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIdPaziente() {
        return idPaziente;
    }
    public void setIdPaziente(String idPaziente) {
        this.idPaziente = idPaziente;
    }
    public LocalDateTime getDataOra() {
        return dataOra;
    }
    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }
    public int getValoreGlicemia() {
        return valoreGlicemia;
    }
    public void setValoreGlicemia(int valoreGlicemia) {
        this.valoreGlicemia = valoreGlicemia;
    }

    public TipoRilevazione getTipo() {
        return tipo;
    }
    public void setTipo(TipoRilevazione tipo){
        this.tipo=tipo;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public List<String> getSintomi() {
        return sintomi;
    }
    public void setSintomi(List<String> sintomi) {
        this.sintomi = sintomi;
    }

    public boolean isNormale() {
        switch (this.tipo) {
            case PRIMA_PASTO:
                return valoreGlicemia >= 80 && valoreGlicemia <= 130;
            case DOPO_PASTO:
                return valoreGlicemia <= 180;
            default:
                return false;
        }
    }

    public GradoAllerta getGradoAllerta() {
        if (valoreGlicemia < 50) {
            return GradoAllerta.URGENTE; // Ipoglicemia severa
        } else if (valoreGlicemia > 300) {
            return GradoAllerta.URGENTE; // Iperglicemia critica
        } else if (tipo == TipoRilevazione.PRIMA_PASTO) {
            if (valoreGlicemia < 70 || valoreGlicemia > 180) {
                return GradoAllerta.ATTENZIONE;
            }
        } else if (tipo == TipoRilevazione.DOPO_PASTO) {
            if (valoreGlicemia > 250) {
                return GradoAllerta.ATTENZIONE;
            }
        }
        return GradoAllerta.NORMALE;
    }
}
