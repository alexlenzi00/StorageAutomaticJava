package ver1;

import org.jetbrains.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public abstract class Storage {
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

    protected @NotNull String[] Keys() {
        String name = TYPES.getClassName(this.getClass());
        Set<String> k = new LinkedHashSet<>(types.get(name).keySet());
        return k.toArray(new String[0]);
    }

    protected @NotNull TYPES[] Values() {
        String name = TYPES.getClassName(this.getClass());
        ArrayList<TYPES> v = new ArrayList<>(types.get(name).values());
        return v.toArray(new TYPES[0]);
    }

    @Override
    public String toString() {
        String name = TYPES.getClassName(this.getClass());
        StringBuilder ris = new StringBuilder();
        String[] k = this.Keys();
        TYPES[] v = this.Values();
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
    public @NotNull String toCSV() {
        StringJoiner sj = new StringJoiner(";");
        for (String n : this.Keys()) {
            sj.add(getByName(n).toString());
        }
        return sj.toString();
    }

    public <T extends Storage> T FromCSV(@NotNull String csv, @NotNull T template) {
        String name = TYPES.getClassName(this.getClass());
        Field[] fs = template.getClass().getDeclaredFields();
        String[] values = csv.split(";");
        int i = 0;
        try {
            for (Field f : fs) {
                f.setAccessible(true);
                String n = f.getName();
                if (Storage.types.get(name).containsKey(n)) {
                    Object o = TYPES.castThis(values[i], types.get(name).get(n));
                    if (o != null) {
                        f.set(template, o);
                    }
                    i++;
                }
            }
            return template;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static <T extends Storage> void saveToCSV(@NotNull ArrayList<T> lst, T template, @NotNull String filename) {
        String name = TYPES.getClassName(template.getClass());
        File file = new File(filename);
        Path path = file.toPath();
        List<String> lines = new ArrayList<>();
        for (T data : lst)
            lines.add(data.toCSV());
        try {
            Files.write(path, lines);
        } catch (IOException e) {
            System.out.printf("Error! Save to CSV for %s failed\n", name);
        }
    }

    public static <T extends Storage> @NotNull ArrayList<T> loadFromCSV(@NotNull String filename, @NotNull T template, @NotNull Class<T> c) {
        String name = TYPES.getClassName(template.getClass());
        ArrayList<T> ris = new ArrayList<>();
        File file = new File(filename);
        if (file.exists()) {
            Path path = file.toPath();
            try {
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
            } catch (IOException e) {
                System.out.printf("Error! Load from CSV for %s failed\n", name);
            }
        }
        return ris;
    }

    // DB
    public static <T extends Storage> void saveToDB(@NotNull List<T> lst, @NotNull T template) {
        String name = TYPES.getClassName(template.getClass());
        try {
            Statement statement = DBManager.getStatement();
            statement.executeUpdate(String.format("DROP TABLE IF EXISTS %s", name));
            statement.executeUpdate(template.getCreate(name));
            String[] k = template.Keys();
            TYPES[] v = template.Values();
            for (T x : lst) {
                StringJoiner sql = new StringJoiner(",", String.format("INSERT INTO %s VALUES (", name), ");");
                for (int i = 0; i < types.get(name).size(); i++) {
                    sql.add(TYPES.howToPrint(x.getByName(k[i]), v[i]));
                }
                statement.executeUpdate(sql.toString());
            }
        } catch (SQLException e) {
            System.out.printf("Error! Save to DB for %s failed\n", name);
        }
    }

    public static <T extends Storage> ArrayList<T> loadFromDB(@NotNull T template, @NotNull Class<T> c) {
        String name = TYPES.getClassName(template.getClass());
        ArrayList<T> lst = new ArrayList<>();
        try {
            ResultSet rs = Storage.getAll(name);
            int index = rs.getRow();
            Storage.first(name);
            while (Storage.hasNext(name)) {
                T selected = getSelected(template, c);
                if (selected != null) {
                    lst.add(selected);
                }
                Storage.next(name);
            }
            rs.absolute(index);
        } catch (SQLException e) {
            System.out.printf("Error! Load from DB for %s failed\n", name);
        }
        return lst;
    }

    // ResulSet
    public static @NotNull ResultSet getAll(@NotNull String name) {
        try {
            ResultSet rs = ResultSets.get(name);
            if (rs == null) {
                rs = DBManager.getStatement().executeQuery(String.format("SELECT * FROM %s", name));
            }
            ResultSets.put(name, rs);
        } catch (SQLException e) {
            System.out.printf("Error! GetAll for (%s) failed\n", name);
        }
        return ResultSets.get(name);
    }

    public static <T extends Storage> T getSelected(@NotNull T template, @NotNull Class<T> c) {
        String name = TYPES.getClassName(c);
        try {
            int i = 1;
            ResultSet rs = Storage.getAll(name);
            Map<String, TYPES> map = Storage.types.get(name);
            Field[] fs = template.getClass().getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                String n = f.getName();
                if (map.containsKey(n)) {
                    Object value = null;
                    Method m = TYPES.howToGet(map.get(n));
                    if (map.get(n) == TYPES.STRING) {
                        value = rs.getString(i++);
                    } else if (map.get(n) == TYPES.DATE) {
                        value = rs.getDate(i++);
                    } else if (m != null) {
                        value = m.invoke(rs, i++);
                    }
                    f.set(template, value);
                }
            }
            return template.duplicate(c);
        } catch (IllegalAccessException | InvocationTargetException | SQLException e) {
            System.out.printf("Error! GetSelected for (%s) failed...\n", name);
            return null;
        }
    }

    public static <T extends Storage> void insert(@NotNull String name, @NotNull T obj) {
        try {
            ResultSet rs = Storage.getAll(name);
            Map<String, TYPES> map = Storage.types.get(name);
            Field[] fs = obj.getClass().getDeclaredFields();
            int index = rs.getRow();
            rs.moveToInsertRow();
            // ricerca dei vari campi "mappati" in map per il tipo T e utilizzo di updateSelected
            for (Field f : fs) {
                f.setAccessible(true);
                String n = f.getName();
                if (map.containsKey(n)) {
                    Storage.updateSelected(name, n, obj.getByName(n));
                }
            }
            rs.insertRow();
            rs.absolute(index);
        } catch (SQLException e) {
            System.out.printf("Error! Insert for (%s) failed...\n", name);
        }
    }

    public static void remove(String name) {
        try {
            ResultSet rs = Storage.getAll(name);
            rs.deleteRow();
            Storage.previous(name);
        } catch (SQLException e) {
            System.out.printf("Error! Remove for (%s) failed...\n", name);
        }
    }

    public static void previous(String name) {
        ResultSet rs = Storage.getAll(name);
        try {
            if (!rs.isFirst()) {
                Storage.absolute(name, rs.getRow() - 1);
            }
        } catch (SQLException e) {
            System.out.printf("Error! Previous for (%s) failed...\n", name);
        }
    }

    public static void next(String name) {
        ResultSet rs = Storage.getAll(name);
        try {
            if (!rs.isLast()) {
                Storage.absolute(name, rs.getRow() + 1);
            }
        } catch (SQLException e) {
            System.out.printf("Error! Next for (%s) failed...\n", name);
        }
    }

    public static void last(String name) {
        if (!absolute(name, getSizeOf(name))) {
            System.out.printf("Error! Last for (%s) failed...\n", name);
        }
    }

    public static void first(String name) {
        if (!absolute(name, 1)) {
            System.out.printf("Error! First for (%s) failed...\n", name);
        }
    }

    public static boolean hasNext(String name) {
        ResultSet rs = Storage.getAll(name);
        boolean ris = false;
        try {
            ris = rs.next();
            rs.previous();
        } catch (SQLException e) {
            System.out.printf("Error! hasNext for (%s) failed...\n", name);
        }
        return ris;
    }

    public static int getSizeOf(String name) {
        int ris = 0;
        ResultSet rs = Storage.getAll(name);
        try {
            int index = rs.getRow();
            if (rs.last()) {
                ris = rs.getRow();
                rs.absolute(index);
            }
        } catch (SQLException e) {
            System.out.printf("Error! GetSizeOf for (%s) failed...\n", name);
        }
        return ris - 1;
    }

    public static boolean absolute(String name, int index) {
        ResultSet rs = getAll(name);
        boolean ris = false;
        try {
            ris = rs.absolute(index);
            if (!ris) {
                System.out.printf("Index %d non valido per '%s'...\n", index, name);
            }
        } catch (SQLException e) {
            System.out.printf("Error! Absolute for (%s) failed...\n", name);
        }
        return ris;
    }

    public static void updateSelected(String name, String attribute, Object value) {
        try {
            ResultSet rs = Storage.getAll(name);
            Map<String, TYPES> map = Storage.types.get(name);
            // check if attribute is in map
            if (map != null && map.containsKey(attribute)) {
                // get attribute index in map
                int i = 1;
                for (String n : map.keySet()) {
                    if (n.equals(attribute)) {
                        break;
                    }
                    i++;
                }
                TYPES t = map.get(attribute);
                if (t == TYPES.STRING) {
                    rs.updateString(i, value.toString());
                } else {
                    Method m = TYPES.howToUpdate(t);
                    if (m != null) {
                        m.invoke(rs, i, TYPES.castThis(value.toString(), t));
                    }
                }
            } else {
                System.out.printf("La classe '%s' non ha nessun attributo con nome '%s'...\n", name, attribute);
            }
        } catch (SQLException | InvocationTargetException | IllegalAccessException e) {
            System.out.printf("Error! UpdateSelected for (%s) failed...\n", name);
        }
    }
}
