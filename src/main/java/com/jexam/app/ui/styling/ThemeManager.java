package com.jexam.app.ui.styling;

import javafx.scene.Scene;

import java.net.URL;
import java.util.Objects;

/**
 * Applies and switches JavaFX themes using classpath CSS resources.
 */
public final class ThemeManager {
    private static final String BASE_STYLESHEET = "/styles/base.css";
    private static final String PROPERTY_ACTIVE_THEME = "jexam.theme.active";

    /**
     * Applies a concrete theme to the scene and records it as active.
     *
     * @param scene target scene
     * @param theme desired theme; null defaults to LIGHT
     */
    public void applyTheme(final Scene scene, final UiTheme theme) {
        Objects.requireNonNull(scene, "scene");
        UiTheme effectiveTheme = theme == null ? UiTheme.LIGHT : theme;

        String base = resolveStylesheet(BASE_STYLESHEET);
        String light = resolveStylesheet(UiTheme.LIGHT.stylesheetPath());
        String dark = resolveStylesheet(UiTheme.DARK.stylesheetPath());
        String target = resolveStylesheet(effectiveTheme.stylesheetPath());

        scene.getStylesheets().remove(light);
        scene.getStylesheets().remove(dark);
        if (!scene.getStylesheets().contains(base)) {
            scene.getStylesheets().add(base);
        }
        if (!scene.getStylesheets().contains(target)) {
            scene.getStylesheets().add(target);
        }
        scene.getProperties().put(PROPERTY_ACTIVE_THEME, effectiveTheme);
    }

    /**
     * Toggles between LIGHT and DARK theme and applies the result to the scene.
     *
     * @param scene target scene
     * @return newly active theme
     */
    public UiTheme toggleTheme(final Scene scene) {
        UiTheme current = activeTheme(scene);
        UiTheme next = current.toggled();
        applyTheme(scene, next);
        return next;
    }

    /**
     * Resolves the active theme from scene properties, defaulting to LIGHT.
     *
     * @param scene target scene
     * @return active theme
     */
    public UiTheme activeTheme(final Scene scene) {
        if (scene == null) {
            return UiTheme.LIGHT;
        }
        Object value = scene.getProperties().get(PROPERTY_ACTIVE_THEME);
        return value instanceof UiTheme theme ? theme : UiTheme.LIGHT;
    }

    private String resolveStylesheet(final String classpathPath) {
        URL resource = ThemeManager.class.getResource(classpathPath);
        if (resource == null) {
            throw new IllegalStateException("Missing stylesheet resource: " + classpathPath);
        }
        return resource.toExternalForm();
    }
}
