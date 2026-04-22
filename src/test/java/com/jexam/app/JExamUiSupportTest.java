package com.jexam.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JExamUiSupportTest {
    @Test
    void shouldResolveLocalizedTextFromCsvCatalog() {
        JExamUiSupport ui = new JExamUiSupport();

        ui.setLanguage(UiLanguage.ENGLISH);
        assertEquals("Create New Exam", ui.text("button.create.exam"));

        ui.setLanguage(UiLanguage.GERMAN);
        assertEquals("Neues Examen erstellen", ui.text("button.create.exam"));
    }

    @Test
    void shouldFallBackToEnglishWhenTranslationIsMissing() {
        JExamUiSupport ui = new JExamUiSupport();

        ui.setLanguage(UiLanguage.GERMAN);
        assertEquals("Klausur-PDF Vorschau", ui.text("button.preview"));
    }
}