package com.jexam.validation;

import java.util.Objects;

public class ValidationError {
    private final String path;
    private final String message;

    public ValidationError(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
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
