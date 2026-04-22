package com.jexam.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates validation errors produced by validation routines.
 *
 * @author Moritz
 */
public final class ValidationResult {
    /**
     * Internal mutable storage for collected errors.
     */
    private final List<ValidationError> errors = new ArrayList<>();

    /**
     * Adds a single error entry.
     *
     * @param path path of invalid field
     * @param message validation message
     */
    public void addError(final String path, final String message) {
        errors.add(new ValidationError(path, message));
    }

    /**
     * Appends all errors from another result.
     *
     * @param other source result
     */
    public void addAll(final ValidationResult other) {
        errors.addAll(other.errors);
    }

    /**
     * Indicates whether the validation result has no errors.
     *
     * @return true when no errors exist
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Returns an immutable view of all collected errors.
     *
     * @return unmodifiable error list
     */
    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
