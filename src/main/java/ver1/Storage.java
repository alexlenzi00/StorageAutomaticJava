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

    protected Storage(String create) {
        this.create = create;
        this.forbidden = new ArrayList<>();
        setForbidden(null);
    }

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

    private String @NotNull [] Keys() {
        Set<String> a = new LinkedHashSet<>(map.keySet());
        String[] tmp = new String[a.size()];
        return a.toArray(tmp);
    }

    private TYPES @NotNull [] Values() {
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

    public <T extends Storage> void saveToDB(@NotNull List<T> lst, @NotNull Statement statement) throws SQLException {
        String name = TYPES.getClassName(this.getClass());
        statement.executeUpdate(String.format("DROP TABLE IF EXISTS %s", name));
        statement.executeUpdate(getCreate());
        String[] k = Keys();
        TYPES[] v = Values();
        for (T s : lst) {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(name).append("VALUES (");
            for (int i = 0; i < map.size(); i++) {
                sql.append(TYPES.howToPrint(s.getByName(k[i]), v[i])).append(" ,");
            }
            sql.append(");");
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

    public static <T extends Storage> ArrayList<T> loadFromCSV(@NotNull String filename, T template, Class<T> c) throws IOException {
        ArrayList<T> ris = null;
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

    public abstract <T extends Storage> T duplicate(Class<T> c);
}
