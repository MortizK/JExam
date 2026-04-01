package com.jexam.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationResult {
    private final List<ValidationError> errors = new ArrayList<>();

    public void addError(String path, String message) {
        errors.add(new ValidationError(path, message));
    }

    public void addAll(ValidationResult other) {
        errors.addAll(other.errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
