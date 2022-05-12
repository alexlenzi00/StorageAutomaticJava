package Storage;

import org.jetbrains.annotations.*;

public class Studente extends Storage {
    private int idStudente;
    private String nome;
    private String cognome;
    private static int id = 1;

    public Studente() {
        super("id");
        setIdStudente(0);
        setNome("");
        setCognome("");
    }

    public Studente(String nome, String cognome) {
        super("id");
        setIdStudente(id++);
        setNome(nome);
        setCognome(cognome);
    }

    public Studente(int idStudente, String nome, String cognome) {
        super("id");
        setIdStudente(idStudente);
        setNome(nome);
        setCognome(cognome);
    }

    public int getIdStudente() {
        return idStudente;
    }

    public @NotNull String getNome() {
        return nome;
    }

    public @NotNull String getCognome() {
        return cognome;
    }

    private void setIdStudente(int idStudente) {
        this.idStudente = idStudente;
    }

    private void setNome(@NotNull String nome) {
        this.nome = nome;
    }

    private void setCognome(@NotNull String cognome) {
        this.cognome = cognome;
    }

    @Override
    public String toString() {
        return String.format("S { %d, nome='%s', cognome='%s' }", idStudente, nome, cognome);
    }

    public <T extends Storage> @NotNull T duplicate(@NotNull Class<T> c) {
        return c.cast(new Studente(this.getIdStudente(), this.getNome(), this.getCognome()));
    }

    @Override
    public @NotNull String getCreateDB() {
        return "CREATE TABLE IF NOT EXISTS Studente(IdStudente INT AUTO_INCREMENT, Nome VARCHAR (20) NOT NULL, Cognome VARCHAR(20) NOT NULL,PRIMARY KEY(IdStudente));";
    }
}