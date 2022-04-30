package ver1;

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
}
