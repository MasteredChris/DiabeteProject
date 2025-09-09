package model;

public abstract class Utente {
    private int id;
    private String nome;
    private String cognome;
    private String email;
    private String password;

    public Utente(int id, String nome, String cognome, String email, String password) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public abstract String getType();
}
