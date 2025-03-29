package nl.tue.appdev.studie;

public class Flashcard {

    private String question;
    private String answer;
    private String author;
    // TODO: implement attachments
    // private ? attachment;

    public Flashcard(String question,
                     String answer,
                     String author) {
        this.question = question;
        this.answer = answer;
        this.author = author;
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
