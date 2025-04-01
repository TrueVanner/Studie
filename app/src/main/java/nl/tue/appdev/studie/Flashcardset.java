package nl.tue.appdev.studie;

import java.util.ArrayList;
import java.util.Vector;

public class Flashcardset {
    private String id;
    private String title;
    private ArrayList<String> flashcardsIds = new ArrayList<>();
    private String author;

    public Flashcardset(String id,
                        String title,
                        ArrayList<String> flashcardsIds,
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

    public ArrayList<String> getFlashcardIds() {
        return flashcardsIds;
    }

    public String getAuthor() {
        return author;
    }

    public int getSize() {
        return flashcardsIds.size();
    }
}
