package com.jexam.model.enums;

import java.util.Arrays;

/**
 * Difficulty levels for a task.
 */
public enum Difficulty {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    /**
     * XML representation value.
     */
    private final String xmlValue;

    Difficulty(final String xmlToken) {
        this.xmlValue = xmlToken;
    }

    /**
     * Returns the XML serialization token.
     *
     * @return XML value
     */
    public String toXmlValue() {
        return xmlValue;
    }

    /**
     * Parses a difficulty from its XML token.
     *
     * @param value XML value
     * @return matching difficulty
     */
    public static Difficulty fromXmlValue(final String value) {
        return Arrays.stream(values())
            .filter(v -> v.xmlValue.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Unknown difficulty: " + value
                )
            );
    }
}
