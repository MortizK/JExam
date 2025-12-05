package dhbw.koehler.jexaminer.model;

public class Variant {

    private String question;
    private String answer;

    public Variant(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    // Question
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    // Answer
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
