package ver1;

import org.jetbrains.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public abstract class Storage implements CSVserializable {
    private static Map<String, String> create;
    private static Map<String, Map<String, TYPES>> types;
    private static Map<String, ArrayList<String>> forbidden;
    private static Map<String, ResultSet> ResultSets;

    protected Storage(String... except) {
        String name = TYPES.getClassName(this.getClass());
        setForbidden(name, except);
        if (create == null) {
            create = new LinkedHashMap<>();
        }
        if (types == null) {
            types = new LinkedHashMap<>();
        }
        if (ResultSets == null) {
            ResultSets = new LinkedHashMap<>();
        }
        if (!create.containsKey(name)) {
            create.put(name, this.getCreateDB());
        }
        if (!types.containsKey(name)) {
            Map<String, TYPES> m = new LinkedHashMap<>();
            Field[] fs = this.getClass().getDeclaredFields();
            for (Field f : fs) {
                if (!forbidden.get(name).contains(f.getName())) {
                    m.put(f.getName(), TYPES.getTYPESByType(f.getType()));
                }
            }
            types.put(name, m);
        }
        if (!ResultSets.containsKey(name)) {
            try {
                Statement s = DBManager.getStatement();
                ResultSet rs = s.executeQuery(String.format("SELECT * FROM %s", name));
                rs.first();
                ResultSets.put(name, rs);
            } catch (SQLException e) {
                System.out.printf("Error! Adding (%s) to ResultSets FAILED", name);
            }
        }
    }

    public @NotNull String getCreate(String name) {
        return create.getOrDefault(name, "");
    }

    public Object getByName(@NotNull String name) {
        try {
            Field[] fs = this.getClass().getDeclaredFields();
            Object ris = null;
            for (Field f : fs) {
                if (f.getName().equalsIgnoreCase(name)) {
                    f.setAccessible(true);
                    ris = f.get(this);
                }
            }
            return ris;
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private void setForbidden(String name, String... except) {
        if (forbidden == null) {
            forbidden = new LinkedHashMap<>();
        }
        if (!forbidden.containsKey(name)) {
            ArrayList<String> f = new ArrayList<>();
            if (except != null) {
                f.addAll(Arrays.asList(except));
            }
            forbidden.put(name, f);
        }
    }

    protected @NotNull String[] Keys(String name) {
        Set<String> k = new LinkedHashSet<>(types.get(name).keySet());
        return k.toArray(new String[0]);
    }

    protected @NotNull TYPES[] Values(String name) {
        ArrayList<TYPES> v = new ArrayList<>(types.get(name).values());
        return v.toArray(new TYPES[0]);
    }

    @Override
    public String toString() {
        String name = TYPES.getClassName(this.getClass());
        StringBuilder ris = new StringBuilder();
        String[] k = Keys(name);
        TYPES[] v = Values(name);
        ris.append(name).append("\n{\n");
        for (int i = 0; i < k.length; i++) {
            ris.append("\t").append(v[i].toString()).append(" ").append(k[i]).append(";\n");
        }
        ris.append("}");
        return ris.toString();
    }

    // ABSTRACT METHODS
    public abstract <T extends Storage> @NotNull T duplicate(@NotNull Class<T> c);
    public abstract @NotNull String getCreateDB();

    // CSV
    @Override
    public @NotNull String toCSV() {
        String name = TYPES.getClassName(this.getClass());
        StringJoiner sj = new StringJoiner(";");
        for (String n : Keys(name)) {
            sj.add(getByName(n).toString());
        }
        return sj.toString();
    }

    @Override
    public <T extends Storage> T FromCSV(@NotNull String csv, @NotNull T template) {
        String name = TYPES.getClassName(this.getClass());
        Field[] fs = template.getClass().getDeclaredFields();
        String[] values = csv.split(";");
        int i = 0;
        try {
            for (Field f : fs) {
                f.setAccessible(true);
                for (String n : Keys(name)) {
                    if (f.getName().equalsIgnoreCase(n)) {
                        Object o = TYPES.castThis(values[i], types.get(name).get(n));
                        if (o != null) {
                            f.set(template, o);
                        }
                        i++;
                        break;
                    }
                }
            }
            return template;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static <T extends Storage> void saveToCSV(@NotNull ArrayList<T> lst, @NotNull String filename) throws IOException {
        File file = new File(filename);
        Path path = file.toPath();
        List<String> lines = new ArrayList<>();
        for (T data : lst)
            lines.add(data.toCSV());
        Files.write(path, lines);
    }

    public static <T extends Storage> @NotNull ArrayList<T> loadFromCSV(@NotNull String filename, @NotNull T template, @NotNull Class<T> c) throws IOException {
        ArrayList<T> ris = new ArrayList<>();
        File file = new File(filename);
        if (file.exists()) {
            ris = new ArrayList<>();
            Path path = file.toPath();
            Scanner scanner = new Scanner(path);
            while (scanner.hasNextLine()) {
                // modifico i valori del template per poi duplicarlo e aggiungerlo alla lista lst
                template = template.FromCSV(scanner.nextLine(), template);
                try {
                    ris.add(template.duplicate(c));
                } catch (Exception ignored) {
                    System.out.println("Error loadCSV!");
                }
            }
        }
        return ris;
    }

    // DB
    public static <T extends Storage> void saveToDB(@NotNull List<T> lst, @NotNull Statement statement, @NotNull T template) throws SQLException {
        String name = TYPES.getClassName(template.getClass());
        statement.executeUpdate(String.format("DROP TABLE IF EXISTS %s", name));
        statement.executeUpdate(template.getCreate(name));
        String[] k = template.Keys(name);
        TYPES[] v = template.Values(name);
        for (T x : lst) {
            StringJoiner sql = new StringJoiner(",", String.format("INSERT INTO %s VALUES (", name), ");");
            for (int i = 0; i < types.get(name).size(); i++) {
                sql.add(TYPES.howToPrint(x.getByName(k[i]), v[i]));
            }
            statement.executeUpdate(sql.toString());
        }
    }

    public static <T extends Storage> @NotNull ArrayList<T> loadFromDB(@NotNull Statement statement, @NotNull T template, @NotNull Class<T> c) throws SQLException {
        String name = TYPES.getClassName(template.getClass());
        ArrayList<T> lst = new ArrayList<>();
        ResultSet rs = getAll(name);
        rs.beforeFirst();
        while (rs.next()) {
            T selected = getSelected(name, template, c);
            if (selected != null) {
                lst.add(selected);
            }
        }
        return lst;
    }

    // ResulSet
    public static @NotNull ResultSet getAll(@NotNull String name) throws SQLException {
        if (!ResultSets.containsKey(name)) {
            ResultSet rs = DBManager.getStatement().executeQuery(String.format("SELECT * FROM %s", name));
            ResultSets.put(name, rs);
        }
        return ResultSets.get(name);
    }

    public static <T extends Storage> T getSelected(@NotNull String name, @NotNull T template, @NotNull Class<T> c) throws SQLException {
        try {
            int i = 1;
            ResultSet rs = ResultSets.get(name);
            Map<String, TYPES> map = Storage.types.get(name);
            Field[] fs = template.getClass().getDeclaredFields();
            for (Field f : fs) {
                if (map.containsKey(f.getName())) {
                    f.setAccessible(true);
                    Object value = null;
                    Method m = TYPES.howToGet(map.get(f.getName()).getType());
                    if (map.get(f.getName()).getType() == String.class) {
                        value = rs.getString(i++);
                    } else if (map.get(f.getName()).getType() == LocalDate.class) {
                        value = rs.getDate(i++).toLocalDate();
                    }
                    if (m != null) {
                        value = m.invoke(rs, i++);
                    }
                    f.set(template, value);
                }
            }
            return template.duplicate(c);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.printf("Error! GetSelected from (%s) failed...", name);
        }
        return null;
    }
}
