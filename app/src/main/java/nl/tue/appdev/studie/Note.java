package nl.tue.appdev.studie;

import java.util.Vector;

public class Note {

    private String id;
    private String title;
    private String author;

    public Note(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
