package ver1;

public interface Duplicable {
    <T extends Storage> Object copy(T src);
}
