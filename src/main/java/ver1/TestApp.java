package ver1;

import java.io.*;
import java.util.ArrayList;

public class TestApp {

    public static void main(String[] args) {
        // LOAD from CSV file
        ArrayList<Studente> students = new ArrayList<>();
        String name = "test.csv";
        try {
            File f = new File(name);
            if (f.exists()) {
                Studente.loadFromCSV(f, new Studente(0, "",""));
            }
        } catch(IOException e) {
            System.out.println("Error! Load to CSV failed...");
            System.out.println(e.getMessage());
        }
        System.out.println(students);
    }
}
