package ver1;

import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.sql.Time;
import java.sql.Date;
import java.sql.ResultSet;

public enum TYPES {
    INT(int.class, Integer.class, "Int"),
    BYTE(byte.class, Byte.class, "Byte"),
    LONG(long.class, Long.class, "Long"),
    DATE(Date.class, Date.class, "Date"),
    TIME(Time.class, Time.class, "Time"),
    SHORT(short.class, Short.class, "Short"),
    FLOAT(float.class, Float.class, "Float"),
    BOOL(boolean.class, Boolean.class, "Boolean"),
    STRING(String.class, String.class, "String"),
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

    public @NotNull Class<?> getType() {
        return type;
    }

    public @NotNull Class<?> getWrap() {
        return wrap;
    }

    public @NotNull String getStr() {
        return str;
    }

    private void setMethods() {
        try {
            String g = String.format("get%s", this.getStr());
            String u = String.format("update%s", this.getStr());
            if (this.getType() != String.class) {
                this.castFun = this.wrap.getMethod("valueOf", String.class);
            }
            this.update = ResultSet.class.getMethod(u, int.class, this.getType());
            this.get = ResultSet.class.getMethod(g, int.class);
        } catch (NoSuchMethodException ignored) {
            System.out.printf("Error! setMethods for (%s) failed...\n", this.getStr());
        }
    }

    public static @NotNull String getClassName(@NotNull Class<?> c) {
        String tmp = c.toString();
        tmp = tmp.substring(tmp.lastIndexOf(' ') + 1);
        tmp = tmp.substring(tmp.lastIndexOf('.') + 1);
        return tmp;
    }

    public static @Nullable TYPES getTYPESByType(@NotNull Class<?> c) {
        TYPES[] ts = TYPES.class.getEnumConstants();
        for (TYPES t : ts) {
            if (t.getType() == c) {
                return t;
            }
        }
        return null;
    }

    public static @Nullable TYPES getTYPESByWrap(@NotNull Class<?> c) {
        TYPES[] ts = TYPES.class.getEnumConstants();
        for (TYPES t : ts) {
            if (t.getWrap() == c) {
                return t;
            }
        }
        return null;
    }

    public static @NotNull String howToPrint(@NotNull Object o, @NotNull TYPES t) {
        String s = t.getWrap().cast(o).toString();
        return (t == STRING || t == DATE) ? ("'" + s + "'") : s;
    }

    public static @Nullable Object castThis(@NotNull String str,@NotNull TYPES t) {
        try {
            if (t == STRING) {
                return str;
            } else if (t == DATE) {
                return Date.valueOf(str);
            }
            return t.castFun.invoke(str, str);
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable Method howToUpdate(@NotNull TYPES t) {
        return t.update;
    }

    public static Method howToGet(@NotNull TYPES t) {
        return t.get;
    }

    @Override
    public String toString() {
        return str;
    }
}
