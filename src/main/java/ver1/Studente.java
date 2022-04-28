package ver1;

import java.util.ArrayList;
import java.util.List;

public class Studente extends Storage {
    private int idStudente;
    private String nome;
    private String cognome;
    private static int id = 1;

    public Studente(int idStudente, String nome, String cognome) {
        super("CREATE TABLE IF NOT EXISTS Studente(IdStudente INT AUTO_INCREMENT, Nome VARCHAR(20) NOT NULL,Cognome VARCHAR(20) NOT NULL,PRIMARY KEY(IdStudente));");
        setForbidden(new ArrayList<>(List.of("id")).toArray(new String[0]));
        setMap(this);
        setIdStudente(idStudente);
        setNome(nome);
        setCognome(cognome);
    }

    public Studente(String nome, String cognome) {
        super("CREATE TABLE IF NOT EXISTS Studente(IdStudente INT AUTO_INCREMENT, Nome VARCHAR(20) NOT NULL,Cognome VARCHAR(20) NOT NULL,PRIMARY KEY(IdStudente));");
        setForbidden(new ArrayList<>(List.of("id")).toArray(new String[0]));
        setMap(this);
        setIdStudente(id++);
        setNome(nome);
        setCognome(cognome);
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
}