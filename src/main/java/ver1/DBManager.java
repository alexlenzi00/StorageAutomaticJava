package ver1;

import java.sql.*;
import java.util.*;

public class DBManager {
    public static final String DB = "jdbc:mysql://localhost:3306";
    public static final String schema = "jdbc_schema";
    public static final String User = "nicola";
    public static final String Pass = "qwertyuio";
    public static final String Time = TimeZone.getDefault().getID();
    public static final String Driver = "com.mysql.cj.jdbc.Driver";
    public static final String URL = String.format(Locale.US, "%s/%s?user=%s&password=%s&serverTimezone=%s", DB, schema, User, Pass, Time);
    static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            try { Class.forName(Driver); }
            catch (ClassNotFoundException e) { System.out.println("Error! Failed to load the DB driver"); }
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    public static Statement getStatement () throws SQLException {
        return DBManager.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }

    public static void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
