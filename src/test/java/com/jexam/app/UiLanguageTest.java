package com.jexam.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiLanguageTest {
    @Test
    void toStringShouldReturnUserFacingLabels() {
        assertEquals("English", UiLanguage.ENGLISH.toString());
        assertEquals("Deutsch", UiLanguage.GERMAN.toString());
    }

    @Test
    void valueOfShouldResolveDeclaredConstants() {
        assertEquals(UiLanguage.ENGLISH, UiLanguage.valueOf("ENGLISH"));
        assertEquals(UiLanguage.GERMAN, UiLanguage.valueOf("GERMAN"));
    }
}
