package com.jexam.generation;

/**
 * <p>GenerationMode class.</p>
 *
 * @author Moritz
 */
public enum GenerationMode {
    /**
     * Generate the exam without embedded answers.
     */
    EXAM,

    /**
     * Generate the solution document with embedded answers.
     */
    SOLUTION,

    /**
     * Generate a mock exam including all task scopes.
     */
    MOCK_EXAM
}
