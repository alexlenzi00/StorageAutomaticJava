package ver1;

import org.jetbrains.annotations.*;
import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;

public enum TYPES {
    INT(int.class, Integer.class,"Int"),
    BYTE(byte.class,Byte.class, "Byte"),
    LONG(long.class, Long.class, "Long"),
    DATE(Date.class, Date.class, "Date"),
    TIME(Time.class, Time.class, "Time"),
    SHORT(short.class, Short.class, "Short"),
    FLOAT(float.class, Float.class, "Float"),
    BOOL(boolean.class, Boolean.class, "Boolean"),
    STRING(String.class, String.class, "String"),
    LOCALDATE(LocalDate.class, LocalDate.class, "Date"),
    DOUBLE(double.class, Double.class, "Double");
    private final Class<?> type;
    private final Class<?> wrap;
    private final String str;
    private Method castFun;
    private Method update;
    private Method get;
    TYPES(Class<?> type, Class<?> wrap, String str) {
        this.type = type;
        this.wrap = wrap;
        this.str = str;
        setMethods();
    }

    public Class<?> getType() {
        return type;
    }

    public Class<?> getWrap() {
        return wrap;
    }

    public String getStr() {
        return str;
    }

    private void setMethods () {
        try {
            String g = String.format("get%s", this.getStr());
            String u = String.format("update%s", this.getStr());
            this.castFun = this.wrap.getMethod("valueOf", String.class);
            this.update = ResultSet.class.getMethod(u, String.class, this.getType());
            this.get = ResultSet.class.getMethod(g, int.class);
        }
        catch (Exception ignored) {}
    }

    public static @NotNull String getClassName(@NotNull Class<?> c) {
        String tmp = c.toString();
        tmp = tmp.substring(tmp.lastIndexOf(' ') + 1);
        tmp = tmp.substring(tmp.lastIndexOf('.') + 1);
        return tmp;
    }

    public static TYPES getTYPESByType (Class<?> c) {
        TYPES[] ts = TYPES.class.getEnumConstants();
        for (TYPES t: ts){
            if (t.getType() == c){
                return t;
            }
        }
        return null;
    }

    public static TYPES getTYPESByWrap (Class<?> c) {
        TYPES[] ts = TYPES.class.getEnumConstants();
        for (TYPES t: ts){
            if (t.getWrap() == c){
                return t;
            }
        }
        return null;
    }

    public static String howToPrint(Object o, TYPES t) {
        String s = t.getWrap().cast(o).toString();
        return (t == STRING) ? ("'" + s + "'") : s;
    }

    public static Object castThis(String str, TYPES t) {
        try {
            if (t == STRING) {
                return str;
            }
           return t.castFun.invoke(str,str);
        } catch (Exception e) {
            return null;
        }
    }

    public static Method howToUpdate(Class<?> c) {
        TYPES t = getTYPESByType(c);
        if (t == null) {
            t = getTYPESByWrap(c);
        }
        return (t != null) ? t.update: null;
    }

    public static Method howToGet(Class<?> c) {
        TYPES t = getTYPESByType(c);
        if (t == null) {
            t = getTYPESByWrap(c);
        }
        return (t != null) ? t.get : null;
    }

    @Override
    public String toString() {
        return str;
    }
}
