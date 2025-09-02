package Model;

public enum TipoAssunzione {
    PRIMA_PASTO("30 minuti prima del pasto"),
    DOPO_PASTO("2 ore dopo il pasto"),
    CON_PASTO("Durante il pasto"),
    A_STOMACO_VUOTO("A stomaco vuoto"),
    INDIPENDENTE_PASTI("Indipendente dai pasti"),
    PRIMA_DORMIRE("Prima di andare a dormire");

    private final String descrizione;

    TipoAssunzione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
