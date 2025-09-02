package Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Terapia {
    private String id;
    private String idPaziente;
    private String idMedico;
    private String farmaco;
    private int assunzioniGiornaliere;
    private double quantitaPerAssunzione;
    private String indicazioni; //(dopo i pasti, lontano dai pasti, ecc.)
    private LocalDate dataInizio;
    private LocalDate dataFine; //(nullable)
    boolean attiva;

    public Terapia(String id, String idPaziente, String idMedico, String farmaco, int assunzioniGiornaliere, double quantitaPerAssunzione, String indicazioni, LocalDate dataInizio, LocalDate dataFine, boolean attiva) {
        this.id = id;
        this.idPaziente = idPaziente;
        this.idMedico = idMedico;
        this.farmaco = farmaco;
        this.assunzioniGiornaliere = assunzioniGiornaliere;
        this.quantitaPerAssunzione = quantitaPerAssunzione;
        this.indicazioni = indicazioni;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.attiva = attiva;
    }
    //Costruttore a tempo indeterminato
    public Terapia(String id, String idPaziente, String idMedico, String farmaco,int assunzioniGiornaliere, double quantitaPerAssunzione, String indicazioni) {
        this(id, idPaziente, idMedico, farmaco, assunzioniGiornaliere,quantitaPerAssunzione, indicazioni, LocalDate.now(), null, true);
    }

    public String getId() {return id;}
    public void setId(String id) {
        this.id = id;
    }
    public String getIdPaziente() {return idPaziente;}
    public void setIdPaziente(String idPaziente) {
        this.idPaziente = idPaziente;
    }
    public String getIdMedico() {
        return idMedico;
    }
    public void setIdMedico(String idMedico) {
        this.idMedico = idMedico;
    }
    public String getFarmaco() {
        return farmaco;
    }
    public void setFarmaco(String farmaco) {
        this.farmaco = farmaco;
    }
    public int getAssunzioniGiornaliere() {return assunzioniGiornaliere;}
    public void setAssunzioniGiornaliere(int assunzioniGiornaliere) {this.assunzioniGiornaliere = assunzioniGiornaliere;}
    public double getQuantitaPerAssunzione() {
        return quantitaPerAssunzione;
    }
    public void setQuantitaPerAssunzione(double quantitaPerAssunzione) {this.quantitaPerAssunzione = quantitaPerAssunzione;}
    public String getIndicazioni() {
        return indicazioni;
    }
    public void setIndicazioni(String indicazioni) {
        this.indicazioni = indicazioni;
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

    public boolean isAttiva() {
        // Una terapia è attiva se:
        // 1. Il flag attiva è true
        // 2. La data corrente è >= dataInizio
        // 3. La data corrente è <= dataFine (se specificata)

        LocalDate oggi = LocalDate.now();

        if (!this.attiva) {
            return false;
        }

        if (oggi.isBefore(this.dataInizio)) {
            return false; // Non ancora iniziata
        }

        if (this.dataFine != null && oggi.isAfter(this.dataFine)) {
            return false; // Già terminata
        }

        return true;
    }

    public List<OrarioAssunzione> calcolaOrariAssunzione() {
        List<OrarioAssunzione> orari = new ArrayList<>();

        if (!isAttiva()) {
            return orari; // Lista vuota se non è attiva
        }

        switch (assunzioniGiornaliere) {
            case 1:
                // Una volta al giorno alle 8:00
                orari.add(new OrarioAssunzione(LocalTime.of(8, 0), quantitaPerAssunzione, indicazioni));
                break;

            case 2:
                // Due volte: mattina e sera
                orari.add(new OrarioAssunzione(LocalTime.of(8, 0), quantitaPerAssunzione, "Mattina - " + indicazioni));
                orari.add(new OrarioAssunzione(LocalTime.of(20, 0), quantitaPerAssunzione, "Sera - " + indicazioni));
                break;

            case 3:
                // Tre volte: colazione, pranzo, cena
                orari.add(new OrarioAssunzione(LocalTime.of(8, 0), quantitaPerAssunzione, "Colazione - " + indicazioni));
                orari.add(new OrarioAssunzione(LocalTime.of(13, 0), quantitaPerAssunzione, "Pranzo - " + indicazioni));
                orari.add(new OrarioAssunzione(LocalTime.of(20, 0), quantitaPerAssunzione, "Cena - " + indicazioni));
                break;

            default:
                // Per altri casi, distribuisci equamente
                orari = distribuisciOrari(assunzioniGiornaliere);
        }

        return orari;
    }

    // Metodo privato per distribuzione equa
    private List<OrarioAssunzione> distribuisciOrari(int numAssunzioni) {
        List<OrarioAssunzione> orari = new ArrayList<>();
        int intervalloOre = 24 / numAssunzioni;

        for (int i = 0; i < numAssunzioni; i++) {
            LocalTime ora = LocalTime.of(8, 0).plusHours(intervalloOre * i);
            orari.add(new OrarioAssunzione(ora, quantitaPerAssunzione, "Assunzione " + (i+1)));
        }

        return orari;
    }

    public boolean isValidTerapia() {
        // Il farmaco deve essere specificato
        if (farmaco == null || farmaco.trim().isEmpty()) {
            return false;
        }

        // Numero di assunzioni ragionevole
        if (assunzioniGiornaliere < 1 || assunzioniGiornaliere > 6) {
            return false;
        }

        // Quantità positiva
        if (quantitaPerAssunzione <= 0) {
            return false;
        }

        // Data inizio obbligatoria
        if (dataInizio == null) {
            return false;
        }

        // Se c'è data fine, deve essere dopo l'inizio
        if (dataFine != null && dataFine.isBefore(dataInizio)) {
            return false;
        }

        return true;
    }

    public double calcolaDosaggioGiornaliero() {
        return quantitaPerAssunzione * assunzioniGiornaliere;
    }

    public long getDurataGiorni() {
        if (dataFine == null) {
            return -1; // Terapia a tempo indeterminato
        }

        return ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;
    }

    public long getGiorniRimanenti() {
        if (dataFine == null) {
            return -1; // Terapia a tempo indeterminato
        }

        LocalDate oggi = LocalDate.now();
        if (oggi.isAfter(dataFine)) {
            return 0; // Già terminata
        }

        return ChronoUnit.DAYS.between(oggi, dataFine) + 1;
    }

    // Modifica dosaggio
    public void modificaDosaggio(double nuovaQuantita, String motivazione) {
        double vecchiaQuantita = this.quantitaPerAssunzione;
        this.quantitaPerAssunzione = nuovaQuantita;

        // Log semplice (poi lo implementeremo meglio)
        System.out.println("MODIFICA DOSAGGIO - Terapia: " + this.id +
                " - Da: " + vecchiaQuantita + " A: " + nuovaQuantita +
                " - Motivo: " + motivazione);
    }

    // Modifica frequenza
    public void modificaFrequenza(int nuoveAssunzioni, String motivazione) {
        int vecchiaFrequenza = this.assunzioniGiornaliere;
        this.assunzioniGiornaliere = nuoveAssunzioni;

        System.out.println("MODIFICA FREQUENZA - Terapia: " + this.id +
                " - Da: " + vecchiaFrequenza + " A: " + nuoveAssunzioni +
                " - Motivo: " + motivazione);
    }

    // Sospendi terapia
    public void sospendiTerapia(String motivazione) {
        this.attiva = false;
        this.dataFine = LocalDate.now();

        System.out.println("SOSPENSIONE TERAPIA - Terapia: " + this.id +
                " - Motivo: " + motivazione);
    }


}
