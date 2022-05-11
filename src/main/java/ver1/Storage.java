package ver1;

import org.jetbrains.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public abstract class Storage {
    /**
     * This MAP is used to store all the CREATE_DB query foreach Class extends Storage has at least one instance,
     * using the Class name as Key and query as Value
     */
    private static Map<String, String> create;
    /**
     * This MAP is used to store all the attributes not forbidden foreach Class extends Storage has at least one
     * instance, using the Class name as Key and a Map<String, TYPES> as Value, where there is the attribute name as
     * Key and TYPES as Value
     */
    private static Map<String, Map<String, TYPES>> types;
    /**
     * This MAP is used to store all the forbidden attribute name foreach Class extends Storage has at least one
     * instance, using the Class name as Key and ArrayList<String> as Value contains all the name forbidden
     */
    private static Map<String, ArrayList<String>> forbidden;
    /**
     * This MAP is used to store all the CREATE_DB query foreach Class extends Storage has at least one instance,
     * using the Class name as Key and ResulSet as Value
     */
    private static Map<String, ResultSet> ResultSets;

    /**
     * Call function 'super()' at start of all Constructors of Class that extends Storage
     *
     * @param except A variadic String[] that represent all the names of forbidden attributes, could be null (no
     * params passed to function)
     */
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

    /**
     * @param name Classname you want to get 'create DB query', not null required
     *
     * @return String representing query to create Table in DB, could be empty string in case there isn't a create
     * query saved in Storage before calling getCreate
     */
    public @NotNull String getCreate(@NotNull String name) {
        return create.getOrDefault(name, "");
    }

    /**
     * @param name Name of attribute you want to get from this object, not null required
     *
     * @return Object representing the value of the attribute, could be null if attribute name is not present in Object
     */
    public @Nullable Object getByName(@NotNull String name) {
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

    /**
     * This function save in private MAP forbidden all the names of attributes are not necessary to be mapped by
     * Storage for the class where name is specified in the first String passed to this function
     *
     * @param name Classname you want to set forbidden names as ArrayList inside the Entry<String,ArrayList<String>>
     * in private MAP forbidden, not null required
     * @param except A variadic String[] that represent all the names of forbidden attributes, could be null (no
     * params passed to function as except)
     */
    private void setForbidden(@NotNull String name, String... except) {
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

    /**
     * No parameters
     *
     * @return String[] representing all the names of attribute mapped in Storage for the class of Object call this
     * function, never null
     */
    protected @NotNull String[] Keys() {
        String name = TYPES.getClassName(this.getClass());
        Set<String> k = new LinkedHashSet<>(types.get(name).keySet());
        return k.toArray(new String[0]);
    }

    /**
     * No parameters
     *
     * @return TYPES[] representing all the TYPES of attribute mapped in Storage for the class of Object call this
     * function, never null
     */
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

    /**
     * <STRONG>FOLLOW THIS STEPS TO IMPLEMENT CORRECTLY THIS FUNCTION</STRONG>
     * In case you have a class like Person you need to implement this function:
     * <STRONG>return c.cast(new Person(this.getAttr1(), this.getAttr2(), ..., this.getAttrN()));</STRONG>
     *
     * @param c Is the class of the object you want to 'duplicate' (example you have class Person, you will write
     * Person.class), not null required
     *
     * @return A new Object of T is the copy of the object who called this function
     */
    public abstract <T extends Storage> @NotNull T duplicate(@NotNull Class<T> c);
    /**
     * FOLLOW THIS STEPS TO IMPLEMENT CORRECTLY THIS FUNCTION
     * In case you have a class like Person you need to implement this function:
     * return "CREATE TABLE Person(...);";
     *
     * @return String representing query to create Table in DB, never null
     */
    public abstract @NotNull String getCreateDB();
    // CSV

    /**
     * @param delimiter String used as delimiter between fields, not null required
     *
     * @return String representing all the field ad strings delimited by delimiter, never null
     */
    public @NotNull String toCSV(@NotNull String delimiter) {
        StringJoiner sj = new StringJoiner(delimiter);
        for (String n : this.Keys()) {
            Object value = getByName(n);
            if (value != null) {
                sj.add(value.toString());
            }
        }
        return sj.toString();
    }

    /**
     * This function returns a new instance of T that extends Storage using String as input and delimiter to get all
     * fields, needs template ("Default Constructor of T instance") to set all fields of this and then return
     * template modified using data between <STRONG>delimiter</STRONG> in String <STRONG>csv</STRONG>
     *
     * @param csv String representing the data of T delimited by delimiter passed as 3rd param, not null required
     * @param template Default Constructor of T instance (example you have class Person, for template you'll
     * use Person()), not null required
     * @param delimiter String representing delimiter between fields inside <STRONG>csv</STRONG> String, not null
     * required
     *
     * @return True if template correctly filled using csv data, if an Error occurs returns False
     */
    public <T extends Storage> boolean FromCSV(@NotNull String csv, @NotNull T template, @NotNull String delimiter) {
        String name = TYPES.getClassName(this.getClass());
        Field[] fs = template.getClass().getDeclaredFields();
        String[] values = csv.split(delimiter);
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
            return true;
        } catch (IllegalAccessException e) {
            System.out.printf("Error! FromCSV for (%s) failed...", name);
            return false;
        }
    }

    /**
     * This function save all ArrayList of T in file where name is specified as 2nd param
     *
     * @param lst ArrayList of T representing the elements you want to save in file.csv, not null required
     * @param filename Filename of the file you want to create/override and insert all csv data of lst, not null
     * required
     */
    public static <T extends Storage> void saveToCSV(@NotNull ArrayList<T> lst, @NotNull String filename) {
        if (lst.size() > 0) {
            String name = TYPES.getClassName(lst.get(0).getClass());
            File file = new File(filename);
            Path path = file.toPath();
            List<String> lines = new ArrayList<>();
            for (T data : lst)
                lines.add(data.toCSV(";"));
            try {
                Files.write(path, lines);
            } catch (IOException e) {
                System.out.printf("Error! Save to CSV for %s failed\n", name);
            }
        }
    }

    /**
     * This function read all data of csv file where name is specified as 1st param, needs T template (result of
     * Default constructor to call duplicate function to add one item inside ArrayList of T as result) and Class<T> c
     * (example you have class Person you will write Person.class)
     *
     * @param filename Filename of the file.csv you want to read to get ArrayList of T, not null required
     * @param template Default Constructor of T instance (example you have class Person, for template you'll use
     * Person()), not null required
     * @param c Class<T> used to call duplicate for template (template.duplicate(c)), not null required
     *
     * @return ArrayList<T> representing all data readed from the file.csv, never null but could be empty if Error
     * occurs
     */
    public static <T extends Storage> @NotNull ArrayList<T> loadFromCSV(@NotNull String filename, @NotNull T template, @NotNull Class<T> c) {
        String name = TYPES.getClassName(template.getClass());
        ArrayList<T> ris = new ArrayList<>();
        File file = new File(filename);
        if (file.exists()) {
            Path path = file.toPath();
            try {
                Scanner scanner = new Scanner(path);
                while (scanner.hasNextLine()) {
                    if (template.FromCSV(scanner.nextLine(), template, ";")) {
                        try {
                            ris.add(template.duplicate(c));
                        } catch (Exception ignored) {
                            System.out.println("Error loadCSV!");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.printf("Error! Load from CSV for %s failed\n", name);
            }
        }
        return ris;
    }

    // DB
    public static <T extends Storage> void saveToDB(@NotNull List<T> lst) {
        if (lst.size() > 0) {
            String name = TYPES.getClassName(lst.get(0).getClass());
            try {
                Statement statement = DBManager.getStatement();
                statement.executeUpdate(String.format("DROP TABLE IF EXISTS %s", name));
                Storage.create.put(name, lst.get(0).getCreateDB());
                statement.executeUpdate(Storage.create.get(name));
                Map<String, TYPES> m = Storage.types.get(name);
                for (T x : lst) {
                    StringJoiner sql = new StringJoiner(",", String.format("INSERT INTO %s VALUES (", name), ");");
                    for (String k : m.keySet()) {
                        Object value = x.getByName(k);
                        if (value != null) {
                            sql.add(TYPES.howToPrint(value, m.get(k)));
                        }
                    }
                    statement.executeUpdate(sql.toString());
                }
            } catch (SQLException e) {
                System.out.printf("Error! Save to DB for %s failed\n", name);
            }
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
