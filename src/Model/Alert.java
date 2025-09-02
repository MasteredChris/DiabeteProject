package Model;

import java.time.LocalDateTime;

public class Alert {
    private String id;
    private String idPaziente;
    private String idMedico;
    private TipoAlert tipo;
    private LocalDateTime dataCreazione;
    private String messaggio;
    private boolean visualizzato;
    private GradoAllerta urgenza;

    public Alert(String id, String idPaziente, String idMedico, TipoAlert tipo, LocalDateTime dataCreazione, String messaggio, boolean visualizzato, GradoAllerta urgenza) {
        this.id = id;
        this.idPaziente = idPaziente;
        this.idMedico = idMedico;
        this.tipo = tipo;
        this.dataCreazione = dataCreazione;
        this.messaggio = messaggio;
        this.visualizzato = visualizzato;
        this.urgenza = urgenza;
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
    public String getIdMedico() {
        return idMedico;
    }
    public void setIdMedico(String idMedico) {
        this.idMedico = idMedico;
    }
    public TipoAlert getTipo() {
        return tipo;
    }
    public void setTipo(TipoAlert tipo) {
        this.tipo = tipo;
    }
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
    public String getMessaggio() {
        return messaggio;
    }
    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }
    public boolean isVisualizzato() {
        return visualizzato;
    }
    public void setVisualizzato(boolean visualizzato) {
        this.visualizzato = visualizzato;
    }
    public GradoAllerta getUrgenza() {
        return urgenza;
    }
    public void setUrgenza(GradoAllerta urgenza) {
        this.urgenza = urgenza;
    }

    public void marcaVisualizzato(){
        this.visualizzato = true;
    }


}
