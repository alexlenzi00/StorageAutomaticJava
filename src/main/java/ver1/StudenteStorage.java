package ver1;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class StudenteStorage {
    public static List<Studente> loadFromDB(Statement statement) throws SQLException {
        List<Studente> students = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT * FROM Studente");
        while (rs.next())
            students.add(new Studente(rs.getInt("idStudente"), rs.getString("nome"), rs.getString("cognome")));
        return students;
    }

    public static void saveToDB(List<Studente> students, Statement statement) throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS Studente");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS Studente(IdStudente INT AUTO_INCREMENT,Nome VARCHAR(20) NOT NULL,Cognome VARCHAR(20) NOT NULL,PRIMARY KEY(IdStudente));");
        for (Studente s : students) {
            String sql = String.format(Locale.US, "INSERT INTO studente (nome, cognome) VALUES ('%s', '%s')", s.getNome(), s.getCognome());
            statement.executeUpdate(sql);
        }
    }

    public static List<Studente> loadFromFile(Path path) throws IOException {
        List<Studente> students = new ArrayList<>();
        Scanner scanner = new Scanner(path);
        while (scanner.hasNextLine()) {
            String[] fields = scanner.nextLine().split(";");
            students.add(new Studente(Integer.parseInt(fields[0]), fields[1], fields[2]));
        }
        scanner.close();
        return students;
    }


}
