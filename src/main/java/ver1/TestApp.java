package ver1;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class TestApp {
    public static void main(String[] args) {
        // SAVE to CSV
        try {
            ArrayList<Studente> init = new ArrayList<>();
            init.add(new Studente(1, "Alex", "Lenzi"));
            init.add(new Studente(2, "Nicola", "Bicocchi"));
            Studente.saveToCSV(init, "save.csv");
        } catch (IOException e) {
            System.out.println("Error! Save to CSV failed");
        }
        // LOAD from CSV
        try {
            ArrayList<Studente> students = Storage.loadFromCSV("load.csv", new Studente(), Studente.class);
            System.out.println(students);
        } catch (IOException e) {
            System.out.println("Error! Load from CSV failed");
        }
        // SAVE to DB
        try {
            Connection c = DBManager.getConnection();
            Statement s = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ArrayList<Studente> lst = new ArrayList<>();
            lst.add(new Studente(1, "Alex", "Lenzi"));
            lst.add(new Studente(2, "Nicola", "Bicocchi"));
            lst.add(new Studente(3, "Francesca", "Caico"));
            Storage.saveToDB(lst, s, new Studente());
        } catch (SQLException e) {
            System.out.println("Error! Save to DB failed");
        }

        // LOAD from DB
        try {
            Connection c = DBManager.getConnection();
            Statement s = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ArrayList<Studente> lst = Storage.loadFromDB(s, new Studente(), Studente.class);
            System.out.println(lst);
        } catch (SQLException e) {
            System.out.println("Error! Save to DB failed");
        }

        // DB MODEL
        try{
            ResultSet rs = Studente.getAll("Studente");
            System.out.println(rs);
        }
        catch (SQLException e) {
            System.out.println("Error! GET ALL failed");
        }

    }
}
