package com.jexam.model;

import java.util.Objects;

public class Variant {
    private String question;
    private String answer;

    public Variant(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Variant variant)) {
            return false;
        }
        return Objects.equals(question, variant.question) && Objects.equals(answer, variant.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, answer);
    }
}
