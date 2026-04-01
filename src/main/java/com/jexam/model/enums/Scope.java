package com.jexam.model.enums;

import java.util.Arrays;

/**
 * Scope of a task in generated outputs.
 */
public enum Scope {
    /**
     * Regular exam scope.
     */
    EXAM("exam"),

    /**
     * Mock exam only scope.
     */
    MOCK_EXAM("mock-exam");

    /**
     * XML representation value.
     */
    private final String xmlValue;

    Scope(final String xmlToken) {
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
     * Parses a scope from its XML token.
     *
     * @param value XML value
     * @return matching scope
     */
    public static Scope fromXmlValue(final String value) {
        return Arrays.stream(values())
            .filter(v -> v.xmlValue.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Unknown scope: " + value)
            );
    }
}
