package ver1;

import java.sql.*;

public class StudenteModel {
    ResultSet students;

    public StudenteModel(Statement statement) throws SQLException {
        students = statement.executeQuery("SELECT * from Studente");
        students.first();
    }

    public Studente getSelected() {
        Studente ris = null;
        try {
            if (students != null){
                ris = new Studente(students.getInt("idStudente"), students.getString("nome"), students.getString("cognome"));
            }
        }catch (SQLException ignored) {}
        return ris;
    }

    public void first() {
        try { students.first(); }
        catch (SQLException ignored) {}
    }

    public void last() {
        try { students.last(); }
        catch (SQLException ignored) {}
    }

    public void next() {
        try {
            if (!students.isLast())
                students.next();
            else
                first();
        } catch (SQLException ignored) {}
    }

    public void previous() {
        try {
            if (!students.isFirst())
                students.previous();
            else
                last();
        } catch (SQLException ignored) {}
    }

    public void insert(Studente student) {
        try {
            int n = students.getRow();
            students.moveToInsertRow();
            students.updateInt("idStudente", student.getIdStudente());
            students.updateString("nome", student.getNome());
            students.updateString("cognome", student.getCognome());
            students.insertRow();
            students.absolute(n);
        } catch (SQLException ignored) {}
    }

    public void remove() {
        try {
            students.deleteRow();
            next();
        } catch (SQLException ignored) {}
    }

    public void setNome(String nome) {
        try {
            students.updateString("nome", nome);
            students.updateRow();
        } catch (SQLException ignored) {}
    }

    public void setCognome(String cognome) {
        try {
            students.updateString("cognome", cognome);
            students.updateRow();
        } catch (SQLException ignored) {}
    }
}
