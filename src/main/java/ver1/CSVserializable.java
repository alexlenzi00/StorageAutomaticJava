package ver1;

import org.jetbrains.annotations.*;

public interface CSVserializable {
    @NotNull String toCSV();
    <T extends Storage> T FromCSV(@NotNull String csv, @NotNull T template);
}