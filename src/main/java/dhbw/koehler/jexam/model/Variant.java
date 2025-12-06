package dhbw.koehler.jexam.model;

public class Variant extends Item {

    private String question;
    private String answer;

    public Variant(String question, String answer) {
        super("Variant");
        this.question = question;
        this.answer = answer;
        this.setName(this.getName());
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

    // Name
    @Override
    public String getName() {
        if (this.question.length() > 20) {
            return this.question.substring(0, 20) + "...";
        }
        return this.question;
    }
}
