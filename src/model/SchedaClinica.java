package model;

public class SchedaClinica {
    private String fattoriRischio;
    private String pregressePatologie;
    private String comorbidita;

    public SchedaClinica() {
        this.fattoriRischio = "";
        this.pregressePatologie = "";
        this.comorbidita = "";
    }

    public SchedaClinica(String fattoriRischio, String pregressePatologie, String comorbidita) {
        this.fattoriRischio = fattoriRischio;
        this.pregressePatologie = pregressePatologie;
        this.comorbidita = comorbidita;
    }

    public String getFattoriRischio() {
        return fattoriRischio;
    }

    public void setFattoriRischio(String fattoriRischio) {
        this.fattoriRischio = fattoriRischio;
    }

    public String getPregressePatologie() {
        return pregressePatologie;
    }

    public void setPregressePatologie(String pregressePatologie) {
        this.pregressePatologie = pregressePatologie;
    }

    public String getComorbidita() {
        return comorbidita;
    }

    public void setComorbidita(String comorbidita) {
        this.comorbidita = comorbidita;
    }

    @Override
    public String toString() {
        return "Fattori di rischio: " + fattoriRischio +
                " | Pregresse patologie: " + pregressePatologie +
                " | Comorbidità: " + comorbidita;
    }
}
