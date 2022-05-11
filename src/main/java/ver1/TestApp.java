package ver1;

import java.sql.*;
import java.util.ArrayList;

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
            Studente.saveToCSV(lst1, "students.csv");

            // LOAD from CSV
            ArrayList<Studente> students = Storage.loadFromCSV("students.csv", new Studente(), Studente.class);
            System.out.println("CSV: " + students);

            // SAVE to DB
            Storage.saveToDB(lst1);

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
            Studente.saveToCSV(lst2, "books.csv");

            // LOAD from CSV
            ArrayList<Book> books = Storage.loadFromCSV("books.csv", new Book(), Book.class);
            System.out.println("CSV: " + books);

            // SAVE to DB
            Storage.saveToDB(lst2);

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
            Studente.saveToCSV(planes, "planes.csv");

            // SAVE to DB
            Storage.saveToDB(planes);

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

            // STUDENTE GET SELECTED & NEXT & FIRST
            System.out.printf("\nTable size for Studente: %d\n", Storage.getSizeOf("Studente"));
            Storage.first("Studente");
            Studente s1 = Storage.getSelected(new Studente(), Studente.class);
            Storage.next("Studente");
            Studente s2 = Storage.getSelected(new Studente(), Studente.class);
            Storage.next("Studente");
            Studente s3 = Storage.getSelected(new Studente(), Studente.class);
            Storage.next("Studente");
            Studente s4 = Storage.getSelected(new Studente(), Studente.class);

            System.out.println("STUDENTE: " + s1);
            System.out.println("STUDENTE: " + s2);
            System.out.println("STUDENTE: " + s3);
            System.out.println("STUDENTE: " + s4);

            // BOOK GET SELECTED & PREVIOUS & LAST
            System.out.printf("\nTable size for Book: %d\n", Storage.getSizeOf("Book"));
            Storage.last("Book");
            Book b1 = Storage.getSelected(new Book(), Book.class);
            Storage.previous("Book");
            Book b2 = Storage.getSelected(new Book(), Book.class);
            Storage.previous("Book");
            Book b3 = Storage.getSelected(new Book(), Book.class);
            Storage.previous("Book");
            Book b4 = Storage.getSelected(new Book(), Book.class);
            Storage.previous("Book");

            System.out.println("BOOK: " + b1);
            System.out.println("BOOK: " + b2);
            System.out.println("BOOK: " + b3);
            System.out.println("BOOK: " + b4);

            // PLANE GET SELECTED & ABSOLUTE
            System.out.printf("\nTable size for Plane: %d\n", Storage.getSizeOf("Plane"));
            if (Storage.absolute("Plane", 5)) {
                Plane p = Storage.getSelected(new Plane(), Plane.class);
                System.out.println("PLANE 05: " + p);
            }
            if (Storage.absolute("Plane", 50)) {
                Plane p = Storage.getSelected(new Plane(), Plane.class);
                System.out.println("PLANE 50: " + p);
            }
            if (Storage.absolute("Plane", 27)) {
                Plane p = Storage.getSelected(new Plane(), Plane.class);
                System.out.println("PLANE 27: " + p);
            }
        }
    }
}
