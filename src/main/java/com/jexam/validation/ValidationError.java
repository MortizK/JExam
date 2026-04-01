package com.jexam.validation;

import java.util.Objects;

/**
 * Immutable validation error entry.
 */
public final class ValidationError {
    /**
     * Path to the offending field in the exam graph.
     */
    private final String path;

    /**
     * Human-readable validation message.
     */
    private final String message;

    /**
     * Creates a new validation error.
     *
     * @param errorPath field path
     * @param errorMessage validation message
     */
    public ValidationError(final String errorPath, final String errorMessage) {
        this.path = errorPath;
        this.message = errorMessage;
    }

    /**
     * Returns the failing field path.
     *
     * @return path in the exam model
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the validation message.
     *
     * @return message text
     */
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationError that)) {
            return false;
        }
        return Objects.equals(path, that.path) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, message);
    }

    @Override
    public String toString() {
        return path + ": " + message;
    }
}
