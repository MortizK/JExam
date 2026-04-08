package com.jexam.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumParsingTest {
    @Test
    void difficultyShouldConvertToAndFromXml() {
        assertEquals("easy", Difficulty.EASY.toXmlValue());
        assertEquals("medium", Difficulty.MEDIUM.toXmlValue());
        assertEquals("hard", Difficulty.HARD.toXmlValue());

        assertEquals(Difficulty.EASY, Difficulty.fromXmlValue("easy"));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromXmlValue("MEDIUM"));
        assertEquals(Difficulty.HARD, Difficulty.fromXmlValue("HaRd"));
    }

    @Test
    void scopeShouldConvertToAndFromXml() {
        assertEquals("exam", Scope.EXAM.toXmlValue());
        assertEquals("mock-exam", Scope.MOCK_EXAM.toXmlValue());

        assertEquals(Scope.EXAM, Scope.fromXmlValue("exam"));
        assertEquals(Scope.MOCK_EXAM, Scope.fromXmlValue("MOCK-EXAM"));
    }

    @Test
    void fromXmlShouldRejectUnknownValues() {
        IllegalArgumentException difficultyEx = assertThrows(
            IllegalArgumentException.class,
            () -> Difficulty.fromXmlValue("invalid")
        );
        assertEquals("Unknown difficulty: invalid", difficultyEx.getMessage());

        IllegalArgumentException scopeEx = assertThrows(
            IllegalArgumentException.class,
            () -> Scope.fromXmlValue("invalid")
        );
        assertEquals("Unknown scope: invalid", scopeEx.getMessage());
    }
}
