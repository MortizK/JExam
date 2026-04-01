package com.jexam.model;

import java.util.Objects;

/**
 * Question-answer pair used inside a task.
 */
public final class Variant {
    /**
     * Variant question text.
     */
    private String question;

    /**
     * Variant answer text.
     */
    private String answer;

    /**
     * Creates a variant.
     *
     * @param questionText question value
     * @param answerText answer value
     */
    public Variant(final String questionText, final String answerText) {
        this.question = questionText;
        this.answer = answerText;
    }

    /**
     * Gets the question text.
     *
     * @return question value
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Sets the question text.
     *
     * @param questionText new question value
     */
    public void setQuestion(final String questionText) {
        this.question = questionText;
    }

    /**
     * Gets the answer text.
     *
     * @return answer value
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets the answer text.
     *
     * @param answerText new answer value
     */
    public void setAnswer(final String answerText) {
        this.answer = answerText;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Variant variant)) {
            return false;
        }
        return Objects.equals(question, variant.question)
            && Objects.equals(answer, variant.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, answer);
    }
}
