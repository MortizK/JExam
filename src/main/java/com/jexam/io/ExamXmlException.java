package com.jexam.io;

/**
 * Exception thrown when exam XML loading, validation, or writing fails.
 */
public class ExamXmlException extends Exception {
    /**
     * Creates an exception with message.
     *
     * @param message error message
     */
    public ExamXmlException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with message and cause.
     *
     * @param message error message
     * @param cause underlying cause
     */
    public ExamXmlException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
