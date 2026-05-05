package com.jexam.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UiTextCatalogTest {
    @Test
    void defaultCatalogShouldResolveLocalizedValuesAndFallbacks() {
        UiTextCatalog catalog = UiTextCatalog.loadDefault();

        assertEquals("New", catalog.text(UiLanguage.ENGLISH, "button.new"));
        assertEquals("Neu", catalog.text(UiLanguage.GERMAN, "button.new"));
        assertEquals("Validieren", catalog.text(UiLanguage.GERMAN, "button.validate"));
        assertEquals("missing.key", catalog.text(UiLanguage.GERMAN, "missing.key"));
    }

    @Test
    void loadFromResourceShouldRejectMissingCatalog() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> UiTextCatalog.loadFromResource("/i18n/does-not-exist.csv")
        );

        assertEquals("Missing UI text resource: /i18n/does-not-exist.csv", exception.getMessage());
    }
}