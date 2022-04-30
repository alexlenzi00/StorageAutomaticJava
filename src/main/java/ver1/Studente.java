package ver1;

import java.util.*;

public class Studente extends Storage {
    private int idStudente;
    private String nome;
    private String cognome;
    private static int id = 1;
    private static final String create_db = "CREATE TABLE IF NOT EXISTS Studente(IdStudente INT AUTO_INCREMENT, Nome " + "VARCHAR (20) NOT NULL,Cognome VARCHAR(20) NOT NULL,PRIMARY KEY(IdStudente));";

    public Studente(int idStudente, String nome, String cognome) {
        super(create_db);
        setForbidden(new ArrayList<>(List.of("id", "create_db")).toArray(new String[0]));
        setMap(this);
        setIdStudente(idStudente);
        setNome(nome);
        setCognome(cognome);
    }

    public Studente(String nome, String cognome) {
        super(create_db);
        setForbidden(new ArrayList<>(List.of("id", "create_db")).toArray(new String[0]));
        setMap(this);
        setIdStudente(id++);
        setNome(nome);
        setCognome(cognome);
    }

    public Studente() {
        super(create_db);
        setForbidden(new ArrayList<>(List.of("id", "create_db")).toArray(new String[0]));
        setMap(this);
        setIdStudente(0);
        setNome("");
        setCognome("");
    }

    public int getIdStudente() {
        return idStudente;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    private void setIdStudente(int idStudente) {
        this.idStudente = idStudente;
    }

    private void setNome(String nome) {
        this.nome = nome;
    }

    private void setCognome(String cognome) {
        this.cognome = cognome;
    }

    @Override
    public String toString() {
        return "S { " + idStudente + ", nome='" + nome + "', cognome='" + cognome + "' }";
    }

    public <T extends Storage> T duplicate(Class<T> c) {
        return c.cast(new Studente(this.idStudente, this.nome, this.cognome));
    }
}