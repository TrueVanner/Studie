package nl.tue.appdev.studie;

public class Note {

    private final String filename; // serves as ID
    private final String title;
    private final String authorId;
    private final String groupID;

    public Note(String filename, String title, String authorId, String groupID) {
        this.filename = filename;
        this.title = title;
        this.authorId = authorId;
        this.groupID = groupID;
    }

    public String getFilename() {
        return filename;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getGroupID() {
        return groupID;
    }
}
