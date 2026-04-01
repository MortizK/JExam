package com.jexam.app;

/**
 * Supported UI languages.
 */
public enum UiLanguage {
    ENGLISH("English"),
    GERMAN("Deutsch");

    private final String label;

    UiLanguage(final String displayLabel) {
        this.label = displayLabel;
    }

    @Override
    public String toString() {
        return label;
    }
}
