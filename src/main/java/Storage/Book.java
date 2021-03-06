package Storage;

import org.jetbrains.annotations.*;

public class Book extends Storage {
    private int id;
    private String title;
    private String author;
    private int pages;

    public Book() {
        super();
        setId(0);
        setTitle("");
        setAuthor("");
        setPages(0);
    }

    public Book(int id, String title, String author, int pages) {
        super();
        setId(id);
        setTitle(title);
        setAuthor(author);
        setPages(pages);
    }

    public int getId() {
        return id;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public @NotNull String getAuthor() {
        return author;
    }

    public int getPages() {
        return pages;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public void setAuthor(@NotNull String author) {
        this.author = author;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return String.format("B { %d, title='%s', author='%s', pages=%d }", id, title, author, pages);
    }

    @Override
    public <T extends Storage> @NotNull T duplicate(@NotNull Class<T> c) {
        return c.cast(new Book(this.getId(), this.getTitle(), this.getAuthor(), this.getPages()));
    }

    @Override
    public @NotNull String getCreateDB() {
        return "CREATE TABLE book (id INTEGER PRIMARY KEY, title VARCHAR(30), author VARCHAR(30), pages INTEGER)";
    }
}
