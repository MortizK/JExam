package com.jexam.app.ui.styling;

/**
 * Supported UI themes with stylesheet resource paths.
 *
 * @author Moritz
 */
public enum UiTheme {
    LIGHT("/styles/theme-light.css"),
    DARK("/styles/theme-dark.css");

    private final String stylesheetPath;

    UiTheme(final String stylesheetPath) {
        this.stylesheetPath = stylesheetPath;
    }

    /**
     * Returns the classpath stylesheet path for this theme.
     *
     * @return classpath stylesheet path
     */
    public String stylesheetPath() {
        return stylesheetPath;
    }

    /**
     * Returns the opposite theme for fast toggling.
     *
     * @return opposite theme
     */
    public UiTheme toggled() {
        return this == LIGHT ? DARK : LIGHT;
    }
}
