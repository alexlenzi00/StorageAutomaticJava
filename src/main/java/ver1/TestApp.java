package ver1;

import java.io.*;
import java.util.ArrayList;

public class TestApp {

    public static void main(String[] args) {
        // SAVE to csv
        ArrayList<Studente> init = new ArrayList<>();
        init.add(new Studente(1, "Alex", "Lenzi"));
        init.add(new Studente(2, "Nicola", "Bicocchi"));
        try {
            Studente.saveToCSV(init, new File("test.csv"));
        } catch (IOException e) {
            System.out.println("Error! Save to CVS failed");
        }
        // LOAD from CSV file
        ArrayList<Studente> students = new ArrayList<>();
        String name = "test.csv";
        try {
            File f = new File(name);
            if (f.exists()) {
                students = Storage.loadFromCSV(f, new Studente(0, "",""), Studente.class);
            }
        } catch(IOException e) {
            System.out.println("Error! Load to CSV failed...");
            System.out.println(e.getMessage());
        }
        System.out.println(students);
    }
}
