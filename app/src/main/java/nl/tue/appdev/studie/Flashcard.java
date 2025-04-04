package nl.tue.appdev.studie;

public class Flashcard {

    private String id;
    private String question;
    private String answer;
    private String author;
    // TODO: implement attachments
    // private ? attachment;

    public Flashcard() {

    }

    public Flashcard(String id,
                     String question,
                     String answer,
                     String author) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAuthor() {
        return author;
    }

}
