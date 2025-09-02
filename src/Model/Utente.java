package Model;

public abstract class Utente {
    private String id;
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private String email;

    public Utente(String id, String username, String password, String nome, String cognome, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public boolean authenticate(String username, String password){
        return this.username.equals(username) && this.password.equals(password);
    }


    public abstract String getUserType();

    @Override
    public String toString() {
        return "Model.Utente{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", email='" + email + '\'' +
                ", userType='" + getUserType() + '\'' +
                '}';
    }
}
