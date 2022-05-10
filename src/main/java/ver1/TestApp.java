package ver1;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestApp {
    public static void main(String[] args) {
        // STUDENTE
        {
            // LISTA INIZIALE
            ArrayList<Studente> lst1 = new ArrayList<>();
            lst1.add(new Studente(1, "Alex", "Lenzi"));
            lst1.add(new Studente(2, "Nicola", "Bicocchi"));
            lst1.add(new Studente(3, "Francesca", "Caico"));
            System.out.println("INIT: " + lst1);

            // SAVE to CSV
            Studente.saveToCSV(lst1, new Studente(), "students.csv");

            // LOAD from CSV
            ArrayList<Studente> students = Storage.loadFromCSV("students.csv", new Studente(), Studente.class);
            System.out.println("CSV: " + students);

            // SAVE to DB
            Storage.saveToDB(lst1, new Studente());

            // LOAD from DB
            students = Storage.loadFromDB(new Studente(), Studente.class);
            System.out.println("DB: " + students);
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
            Studente.saveToCSV(lst2, new Book(), "books.csv");

            // LOAD from CSV
            ArrayList<Book> books = Storage.loadFromCSV("books.csv", new Book(), Book.class);
            System.out.println("CSV: " + books);

            // SAVE to DB
            Storage.saveToDB(lst2, new Book());

            // LOAD from DB
            books = Storage.loadFromDB(new Book(), Book.class);
            System.out.println("DB: " + books);
        }

        // PLANE
        {
            // LOAD from CSV
            ArrayList<Plane> planes = Storage.loadFromCSV("planes.csv", new Plane(), Plane.class);
            System.out.println("CSV: " + planes);

            // SAVE to CSV
            Studente.saveToCSV(planes, new Plane(), "planes.csv");

            // SAVE to DB
            Storage.saveToDB(planes, new Plane());

            // LISTA OTTENUTA DA DB
            ArrayList<Plane> lst3 = Storage.loadFromDB(new Plane(), Plane.class);
            System.out.println("DB: " + lst3);
        }

        // AUTOMATIC MODEL
        {
            // INSERT IN DB
            Storage.insert("Studente", new Studente(10, "Test", "Insert"));
            Storage.insert("Book", new Book(10, "Title insert", "Storage", 5));
            Storage.insert("Plane", new Plane("aaaaaa", "Plane name", 1.0, 2.0, Date.valueOf("2020-09-12"), "Automatic"));

            // GET SELECTED IN DB
            Studente s1 = Storage.getSelected(new Studente(), Studente.class);
            Book b1 = Storage.getSelected(new Book(), Book.class);
            Plane p1 = Storage.getSelected(new Plane(), Plane.class);
            System.out.println("STUDENTE: " + s1);
            System.out.println("BOOK: " + b1);
            System.out.println("PLANE: " + p1);
        }
    }
}
