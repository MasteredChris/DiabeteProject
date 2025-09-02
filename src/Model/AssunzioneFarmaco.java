package Model;

import java.time.LocalDateTime;

public class AssunzioneFarmaco {
    private String id;
    private String idPaziente;
    private String idTerapia;
    private LocalDateTime dataOraAssunzione;
    private LocalDateTime dataOraPrevista;
    private String farmaco;
    private double quantitaAssunta;
    private boolean assunta;

    public AssunzioneFarmaco(String id, String idPaziente, String idTerapia, LocalDateTime dataOraAssunzione, LocalDateTime dataOraPrevista, String farmaco, double quantitaAssunta, boolean assunta) {
        this.id = id;
        this.idPaziente = idPaziente;
        this.idTerapia = idTerapia;
        this.dataOraAssunzione = dataOraAssunzione;
        this.dataOraPrevista = dataOraPrevista;
        this.farmaco = farmaco;
        this.quantitaAssunta = quantitaAssunta;
        this.assunta = assunta;
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
    public String getIdTerapia() {
        return idTerapia;
    }
    public void setIdTerapia(String idTerapia) {
        this.idTerapia = idTerapia;
    }
    public LocalDateTime getDataOraAssunzione() {
        return dataOraAssunzione;
    }
    public void setDataOraAssunzione(LocalDateTime dataOraAssunzione) {
        this.dataOraAssunzione = dataOraAssunzione;
    }
    public LocalDateTime getDataOraPrevista() {
        return dataOraPrevista;
    }
    public void setDataOraPrevista(LocalDateTime dataOraPrevista) {
        this.dataOraPrevista = dataOraPrevista;
    }
    public String getFarmaco() {
        return farmaco;
    }
    public void setFarmaco(String farmaco) {
        this.farmaco = farmaco;
    }
    public double getQuantitaAssunta() {
        return quantitaAssunta;
    }
    public void setQuantitaAssunta(double quantitaAssunta) {
        this.quantitaAssunta = quantitaAssunta;
    }

    public boolean isAssunta() {
        // Un farmaco è assunto se:
        // 1. Il flag assunta è true
        // 2. La data assunzione = data assunzione prevista

        //LocalDate oggi = LocalDate.now();

        if (!this.assunta) {
            return false;
        }

        if (!dataOraAssunzione.isEqual(dataOraPrevista)) {
            return false;
        }

        return true;
    }

    public boolean isInRitardo() {
        if (!this.assunta) {
            return false;
        }

        if (!dataOraAssunzione.isAfter(dataOraPrevista)) {
            return false;
        }

        return true;
    }

    public long getMinutiRitardo(){
        return dataOraAssunzione.getMinute()-dataOraPrevista.getMinute();
    }

}
