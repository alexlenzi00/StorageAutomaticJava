package ver1;

import java.io.*;
import java.util.ArrayList;

public class TestApp {
    public static void main(String[] args) {
        // SAVE to csv
        try {
            ArrayList<Studente> init = new ArrayList<>();
            init.add(new Studente(1, "Alex", "Lenzi"));
            init.add(new Studente(2, "Nicola", "Bicocchi"));
            Studente.saveToCSV(init, "save.csv");
        } catch (IOException e) {
            System.out.println("Error! Save to CSV failed");
        }
        // LOAD from CSV file
        try {
            ArrayList<Studente> students = Storage.loadFromCSV("load.csv", new Studente(), Studente.class);
            System.out.println(students);
        } catch (IOException e) {
            System.out.println("Error! Load from CSV failed");
        }
    }
}
