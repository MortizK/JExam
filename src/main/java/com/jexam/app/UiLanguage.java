package com.jexam.app;

/**
 * Supported UI languages.
 *
 * @author Moritz
 */
public enum UiLanguage {
    ENGLISH("English"),
    GERMAN("Deutsch"),
    SPANISH("Español");

    private final String label;

    UiLanguage(final String displayLabel) {
        this.label = displayLabel;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return label;
    }
}
