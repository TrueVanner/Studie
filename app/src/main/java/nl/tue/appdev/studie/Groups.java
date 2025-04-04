package nl.tue.appdev.studie;

import java.util.ArrayList;
import java.util.List;
//Public class used to add the group that's trying to be created using the GroupCreationActivity into the database
public class Groups {
    private String name;
    private String code;
    private boolean isPublic;
    private List<String> flashcards;
    private List<String> flashcardsets;
    private List<String> notes;

    public Groups(){

    }
    public Groups(String name, String code, boolean isPublic){
        this.name=name;
        this.code=code;
        this.isPublic=isPublic;
        this.flashcards = new ArrayList<>();
        this.flashcardsets = new ArrayList<>();
        this.notes = new ArrayList<>();
    }
    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getcode() {
        return code;
    }

    public void setcode(String code) {
        this.code = code;
    }

    public boolean getisPublic() {
        return isPublic;
    }

    public void setisPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<String> getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(List<String> flashcards) {
        this.flashcards = flashcards;
    }

    public List<String> getFlashcardsets() {
        return flashcardsets;
    }

    public void setFlashcardsets(List<String> flashcardsets) {
        this.flashcardsets = flashcardsets;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }
}
