package ver1;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class TestApp {
    public static void main(String[] args) {
        Statement s;
        try {
            s = DBManager.getStatement();
        }
        catch (SQLException e ) {
            System.out.println("Error! Statement not available");
            System.out.println(e.getMessage());
            throw new IllegalArgumentException("R.I.P.");
        }

        // STUDENTE
        {
            // LISTA INIZIALE
            ArrayList<Studente> lst1 = new ArrayList<>();
            lst1.add(new Studente(1, "Alex", "Lenzi"));
            lst1.add(new Studente(2, "Nicola", "Bicocchi"));
            lst1.add(new Studente(3, "Francesca", "Caico"));
            System.out.println("INIT: " + lst1);
            // SAVE to CSV
            try {
                Studente.saveToCSV(lst1, "students.csv");
            } catch (IOException e) {
                System.out.println("Error! Save to CSV failed");
            }
            // LOAD from CSV
            try {
                ArrayList<Studente> students = Storage.loadFromCSV("students.csv", new Studente(), Studente.class);
                System.out.println("CSV: " + students);
            } catch (IOException e) {
                System.out.println("Error! Load from CSV failed");
            }
            // SAVE to DB
            try {
                Storage.saveToDB(lst1, s, new Studente());
            } catch (SQLException e) {
                System.out.println("Error! Save to DB failed");
            }
            // LOAD from DB
            try {
                ArrayList<Studente> students = Storage.loadFromDB(s, new Studente(), Studente.class);
                System.out.println("DB: " + students);
            } catch (SQLException e) {
                System.out.println("Error! Load from DB failed");
            }
        }

        // BOOK
        {
            // LISTA INIZIALE
            ArrayList<Book> lst2 = new ArrayList<>();
            lst2.add(new Book(1, "Title 1", "Alex Lenzi", 10));
            lst2.add(new Book(2, "Title 2", "Nicola Bicocchi", 10));
            lst2.add(new Book(3, "Title 3", "Francesca Caico", 10));
            System.out.println("INIT: " + lst2);
            // SAVE to CSV
            try {
                Studente.saveToCSV(lst2, "books.csv");
            } catch (IOException e) {
                System.out.println("Error! Save to CSV failed");
            }
            // LOAD from CSV
            try {
                ArrayList<Book> books = Storage.loadFromCSV("books.csv", new Book(), Book.class);
                System.out.println("CSV: " + books);
            } catch (IOException e) {
                System.out.println("Error! Load from CSV failed");
            }
            // SAVE to DB
            try {
                Storage.saveToDB(lst2, s, new Book());
            } catch (SQLException e) {
                System.out.println("Error! Save to DB failed");
            }
            // LOAD from DB
            try {
                ArrayList<Book> books = Storage.loadFromDB(s, new Book(), Book.class);
                System.out.println("DB: " + books);
            } catch (SQLException e) {
                System.out.println("Error! Load from DB failed");
            }
        }

        // PLANE
        {
            // LISTA OTTENUTA DA DB
            ArrayList<Plane> lst3 = new ArrayList<>();
            try {
                lst3 = Storage.loadFromDB(s, new Plane(), Plane.class);
                System.out.println("DB: " + lst3);
            } catch (SQLException e) {
                System.out.println("Error! Load from DB failed");
            }
            // SAVE to CSV
            try {
                Studente.saveToCSV(lst3, "planes.csv");
            } catch (IOException e) {
                System.out.println("Error! Save to CSV failed");
            }
            // LOAD from CSV
            try {
                ArrayList<Plane> planes = Storage.loadFromCSV("planes.csv", new Plane(), Plane.class);
                System.out.println("CSV: " + planes);
            } catch (IOException e) {
                System.out.println("Error! Load from CSV failed");
            }
        }

        // DB MODEL
        {
            try {
                ResultSet rs = Studente.getAll("Studente");
                rs.getDate(1);
                rs.first();
            } catch (SQLException e) {
                System.out.println("Error! GET ALL failed");
            }
        }
    }
}
