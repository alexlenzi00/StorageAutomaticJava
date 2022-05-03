package ver1;

import org.jetbrains.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public abstract class Storage implements CSVserializable {
    public Map<String, TYPES> map;
    public final String create;
    private ArrayList<String> forbidden;

    protected Storage(String create, String[] str) {
        this.create = create;
        this.forbidden = new ArrayList<>();
        setForbidden(str);
    }

    public String getCreate() {
        return create;
    }

    public Object getByName(String name) {
        try {
            Field[] fs = this.getClass().getDeclaredFields();
            Object ris = null;
            for (Field f : fs)
                if (f.getName().equalsIgnoreCase(name)) {
                    f.setAccessible(true);
                    ris = f.get(this);
                }
            return ris;
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    protected void setForbidden(String[] str) {
        this.forbidden = new ArrayList<>();
        forbidden.add("forbidden");
        forbidden.add("map");
        forbidden.add("create");
        if (str != null) {
            forbidden.addAll(Arrays.asList(str));
        }
    }

    protected <T extends Storage> void setMap(@NotNull T obj) {
        map = new LinkedHashMap<>();
        Field[] fs = obj.getClass().getDeclaredFields();
        for (Field f : fs) {
            if (!forbidden.contains(f.getName())) {
                map.put(f.getName(), TYPES.getTYPESByType(f.getType()));
            }
        }
    }

    protected String @NotNull [] Keys() {
        Set<String> a = new LinkedHashSet<>(map.keySet());
        String[] tmp = new String[a.size()];
        return a.toArray(tmp);
    }

    protected TYPES @NotNull [] Values() {
        ArrayList<TYPES> b = new ArrayList<>(map.values());
        TYPES[] tmp = new TYPES[b.size()];
        return b.toArray(tmp);
    }

    @Override
    public String toString() {
        StringBuilder ris = new StringBuilder();
        String[] k = Keys();
        TYPES[] v = Values();
        String name = TYPES.getClassName(this.getClass());
        ris.append(name).append("\n{\n");
        for (int i = 0; i < k.length; i++) {
            ris.append("\t").append(v[i].toString()).append(" ").append(k[i]).append(";\n");
        }
        ris.append("}");
        return ris.toString();
    }

    @Override
    public String toCSV() {
        StringJoiner sj = new StringJoiner(";");
        for (String name : Keys()) {
            sj.add(getByName(name).toString());
        }
        return sj.toString();
    }

    @Override
    public <T extends Storage> T FromCSV(@NotNull String csv, @NotNull T template) {
        Field[] fs = template.getClass().getDeclaredFields();
        String[] values = csv.split(";");
        int i = 0;
        try {
            for (Field f : fs) {
                f.setAccessible(true);
                for (String name : Keys()) {
                    if (f.getName().equalsIgnoreCase(name)) {
                        Object o = TYPES.castThis(values[i], map.get(name));
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

    public static <T extends Storage> void saveToDB(@NotNull List<T> lst, @NotNull Statement statement, T template) throws SQLException {
        String name = TYPES.getClassName(template.getClass());
        statement.executeUpdate(String.format("DROP TABLE IF EXISTS %s", name));
        statement.executeUpdate(template.getCreate());
        String[] k = template.Keys();
        TYPES[] v = template.Values();
        for (T s : lst) {
            StringJoiner sql = new StringJoiner(",", String.format("INSERT INTO %s VALUES (", name), ");");
            for (int i = 0; i < s.map.size(); i++) {
                sql.add(TYPES.howToPrint(s.getByName(k[i]), v[i]));
            }
            statement.executeUpdate(sql.toString());
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
                template.FromCSV(scanner.nextLine(), template);
                try {
                    ris.add(template.duplicate(c));
                } catch (Exception ignored) {
                    System.out.println("Error loadCSV!");
                }
            }
        }
        return ris;
    }

    public static <T extends Storage> ArrayList<T> loadFromDB(@NotNull Statement statement, @NotNull T template, @NotNull Class<T> c) throws SQLException {
        String name = TYPES.getClassName(template.getClass());
        ArrayList<T> lst = new ArrayList<>();
        ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s", name));
        try {
            while (rs.next()) {
                int i = 1;
                Field[] fs = template.getClass().getDeclaredFields();
                for (Field f : fs) {
                    f.setAccessible(true);
                    for (String k : template.Keys()) {
                        if (f.getName().equalsIgnoreCase(k)) {
                            Object value = null;
                            Method m = TYPES.howToGet(template.map.get(k).getType());
                            //System.out.printf("%d - M %sEXISTS!%n",i, m != null ? "" : "NOT ");
                            if (template.map.get(k).getType() == String.class) {
                                value = rs.getString(i++);
                            }
                            if (m != null) {
                                value = m.invoke(rs, i++);
                            }
                            //System.out.println("VALUE = " + value);
                            f.set(template, value);
                            break;
                        }
                    }
                }
                //System.out.println("TEMPLATE = " + template + "\n\n");
                lst.add(template.duplicate(c));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println("Error! LOAD from DB failed...");
        }
        return lst;
    }

    public abstract <T extends Storage> T duplicate(Class<T> c);
}
