package nl.tue.appdev.studie;

public class Note {

    private final String filename; // serves as ID
    private final String title;
    private final String author;
    private final String groupID;

    public Note(String filename, String title, String author, String groupID) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.groupID = groupID;
    }

    public String getFilename() {
        return filename;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGroupID() {
        return groupID;
    }
}
