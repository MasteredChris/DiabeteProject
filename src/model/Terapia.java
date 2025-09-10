package model;

import java.time.LocalDate;

public class Terapia {

    private String farmaco;
    private int assunzioniGiornaliere;
    private double quantitaPerAssunzione; // ad esempio mg o unità
    private String indicazioni; // ad esempio "dopo i pasti", "prima dei pasti"
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private int medicoId;


    public enum Stato { ATTIVA, IN_PAUSA, TERMINATA }
    private Stato stato;


    public Terapia(String farmaco, int assunzioniGiornaliere, double quantitaPerAssunzione,
                   String indicazioni, LocalDate dataInizio, LocalDate dataFine,  Stato stato, int medicoId) {
        this.farmaco = farmaco;
        this.assunzioniGiornaliere = assunzioniGiornaliere;
        this.quantitaPerAssunzione = quantitaPerAssunzione;
        this.indicazioni = indicazioni;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.stato = stato;
        aggiornaStatoAutomatico();
        this.medicoId = medicoId;
    }

    public String getFarmaco() {
        return farmaco;
    }

    public int getAssunzioniGiornaliere() {
        return assunzioniGiornaliere;
    }

    public double getQuantitaPerAssunzione() {
        return quantitaPerAssunzione;
    }

    public String getIndicazioni() {
        return indicazioni;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }
    public LocalDate getDataFine() {
        return dataFine;
    }
    public int getMedicoId() { return medicoId; }
    public Stato getStato() {
        return stato;
    }

    public void setStato(Stato stato) {
        LocalDate oggi = LocalDate.now();
        if (!oggi.isBefore(dataInizio) && !oggi.isAfter(dataFine)) {
            this.stato = stato;
        }
    }


    public void aggiornaStatoAutomatico() {
        LocalDate oggi = LocalDate.now();
        if (oggi.isAfter(dataFine)) {
            this.stato = Stato.TERMINATA;
        }
    }

    public boolean isAttiva() {
        aggiornaStatoAutomatico();
        return stato == Stato.ATTIVA;
    }


    @Override
    public String toString() {
        return farmaco + " - " + quantitaPerAssunzione + " per " + assunzioniGiornaliere + " volte al giorno (" + indicazioni + ") da " + dataInizio+" a "+dataFine+" - Stato: " + stato;
    }


}
