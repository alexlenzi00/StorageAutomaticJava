package ver1;

public interface CSVserializable {
    String toCSV();
    <T extends Storage>  T FromCSV(String csv, T template);
}