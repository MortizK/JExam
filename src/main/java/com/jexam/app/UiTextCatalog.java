package com.jexam.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads localized UI text from a CSV resource.
 */
final class UiTextCatalog {
    private static final String DEFAULT_RESOURCE = "/i18n/ui-text.csv";

    private final Map<UiLanguage, Map<String, String>> textByLanguage;

    private UiTextCatalog(final Map<UiLanguage, Map<String, String>> textByLanguage) {
        this.textByLanguage = textByLanguage;
    }

    static UiTextCatalog loadDefault() {
        return loadFromResource(DEFAULT_RESOURCE);
    }

    static UiTextCatalog loadFromResource(final String resourcePath) {
        try (InputStream stream = UiTextCatalog.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Missing UI text resource: " + resourcePath);
            }
            return load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load UI text resource: " + resourcePath, e);
        }
    }

    String text(final UiLanguage language, final String key) {
        Map<String, String> english = textByLanguage.getOrDefault(UiLanguage.ENGLISH, Map.of());
        Map<String, String> localized = textByLanguage.getOrDefault(language, english);
        String value = localized.get(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        value = english.get(key);
        return value == null || value.isBlank() ? key : value;
    }

    private static UiTextCatalog load(final InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalStateException("UI text catalog is empty.");
            }

            List<String> headers = parseCsvLine(headerLine);
            if (headers.isEmpty() || !"key".equalsIgnoreCase(headers.get(0).replace("\uFEFF", ""))) {
                throw new IllegalStateException("UI text catalog must start with a key column.");
            }

            Map<UiLanguage, Map<String, String>> textByLanguage = new EnumMap<>(UiLanguage.class);
            for (UiLanguage language : UiLanguage.values()) {
                textByLanguage.put(language, new HashMap<>());
            }

            List<UiLanguage> languagesByColumn = new ArrayList<>();
            languagesByColumn.add(null);
            for (int columnIndex = 1; columnIndex < headers.size(); columnIndex++) {
                String header = headers.get(columnIndex).trim();
                if (header.isEmpty()) {
                    languagesByColumn.add(null);
                    continue;
                }
                try {
                    languagesByColumn.add(UiLanguage.valueOf(header.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Unsupported UI text language column: " + header, e);
                }
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                List<String> cells = parseCsvLine(line);
                if (cells.isEmpty()) {
                    continue;
                }

                String key = cells.get(0).trim();
                if (key.isEmpty()) {
                    continue;
                }

                for (int columnIndex = 1; columnIndex < cells.size() && columnIndex < languagesByColumn.size(); columnIndex++) {
                    UiLanguage language = languagesByColumn.get(columnIndex);
                    if (language == null) {
                        continue;
                    }

                    String value = cells.get(columnIndex);
                    if (value != null && !value.isBlank()) {
                        textByLanguage.get(language).put(key, value);
                    }
                }

                if (!textByLanguage.get(UiLanguage.ENGLISH).containsKey(key)) {
                    throw new IllegalStateException("Missing English text for key '" + key + "' at line " + lineNumber + ".");
                }
            }

            return new UiTextCatalog(unmodifiableCopy(textByLanguage));
        }
    }

    private static Map<UiLanguage, Map<String, String>> unmodifiableCopy(
        final Map<UiLanguage, Map<String, String>> source
    ) {
        Map<UiLanguage, Map<String, String>> copy = new EnumMap<>(UiLanguage.class);
        for (Map.Entry<UiLanguage, Map<String, String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static List<String> parseCsvLine(final String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (inQuotes) {
                if (character == '"') {
                    if (index + 1 < line.length() && line.charAt(index + 1) == '"') {
                        current.append('"');
                        index++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(character);
                }
            } else if (character == ',') {
                cells.add(current.toString());
                current.setLength(0);
            } else if (character == '"') {
                inQuotes = true;
            } else {
                current.append(character);
            }
        }

        cells.add(current.toString());
        return cells;
    }
}