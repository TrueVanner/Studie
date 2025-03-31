package nl.tue.appdev.studie;

import java.util.Vector;

public class Flashcardset {

    private String id;
    private String title;
    private Vector<String> flashcardsIds = new Vector<>();
    private String author;

    public Flashcardset(String id,
                        String title,
                        Vector<String> flashcardsIds,
                        String author) {
        this.id = id;
        this.title = title;
        this.flashcardsIds.addAll(flashcardsIds);
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Vector<String> getFlashcardIds() {
        return flashcardsIds;
    }

    public String getAuthor() {
        return author;
    }

    public int getSize() {
        return flashcardsIds.size();
    }
}
