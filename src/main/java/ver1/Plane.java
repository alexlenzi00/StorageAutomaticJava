package ver1;

import org.jetbrains.annotations.*;
import java.time.LocalDate;
import java.util.UUID;

public class Plane extends Storage{
    private String uuid;
    private String name;
    private double length;
    private double wingspan;
    private LocalDate firstFlight;
    private String category;

    Plane() {
        super();
        init(this);
        setUuid("");
        setName("");
        setLength(0);
        setWingspan(0);
        setFirstFlight(LocalDate.now());
        setCategory("");
    }

    Plane(String uuid, String name, double length, double wingspan, LocalDate firstFlight, String category) {
        super();
        init(this);
        setUuid(uuid);
        setName(name);
        setLength(length);
        setWingspan(wingspan);
        setFirstFlight(firstFlight);
        setCategory(category);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public double getLength() {
        return length;
    }

    public double getWingspan() {
        return wingspan;
    }

    public LocalDate getFirstFlight() {
        return firstFlight;
    }

    public String getCategory() {
        return category;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public void setWingspan(double wingspan) {
        this.wingspan = wingspan;
    }

    public void setFirstFlight(LocalDate firstFlight) {
        this.firstFlight = firstFlight;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return String.format("P { %s, nome='%s', length=%.2f, wingspan=%.2f, firstflight=%s, category='%s' }", uuid, name, length, wingspan, firstFlight.toString(), category);
    }

    @Override
    public <T extends Storage> @NotNull T duplicate(@NotNull Class<T> c) {
        return c.cast(new Plane(this.getUuid(), this.getName(), this.getLength(), this.getWingspan(), this.getFirstFlight(), this.getCategory()));
    }

    @Override
    public @NotNull String getCreateDB() {
        return "CREATE TABLE planes (uuid VARCHAR(50) PRIMARY KEY, name VARCHAR(50), length REAL, wingspan REAL, firstFlight DATE, category VARCHAR(50))";
    }
}
