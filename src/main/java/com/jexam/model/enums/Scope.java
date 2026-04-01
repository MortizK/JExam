package com.jexam.model.enums;

import java.util.Arrays;

public enum Scope {
    EXAM("exam"),
    MOCK_EXAM("mock-exam");

    private final String xmlValue;

    Scope(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    public String toXmlValue() {
        return xmlValue;
    }

    public static Scope fromXmlValue(String value) {
        return Arrays.stream(values())
            .filter(v -> v.xmlValue.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown scope: " + value));
    }
}
