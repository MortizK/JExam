package com.jexam.model.enums;

import java.util.Arrays;

public enum Difficulty {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    private final String xmlValue;

    Difficulty(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    public String toXmlValue() {
        return xmlValue;
    }

    public static Difficulty fromXmlValue(String value) {
        return Arrays.stream(values())
            .filter(v -> v.xmlValue.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown difficulty: " + value));
    }
}
