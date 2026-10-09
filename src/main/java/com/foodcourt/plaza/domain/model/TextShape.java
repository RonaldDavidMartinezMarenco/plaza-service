package com.foodcourt.plaza.domain.model;

import java.util.regex.Pattern;

/**
 * Stored text: surrounding spaces removed, repeated spaces collapsed to one.
 * The minimum length is measured after that. A newline is not a space, so it stays
 * and fails the character set.
 */
final class TextShape {

    static final int MIN = 2;
    static final Pattern NAME = Pattern.compile("^[\\p{L}\\p{N} &'.,-]+$");
    static final Pattern ADDRESS = Pattern.compile("^[\\p{L}\\p{N} #&'.,/-]+$");
    static final Pattern DESCRIPTION = Pattern.compile("^[\\p{L}\\p{N} .,;:!?¿¡'\"()\\-/&]+$");

    private static final Pattern LETTER = Pattern.compile("\\p{L}");

    private TextShape() {
    }

    static String collapse(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll(" {2,}", " ");
    }

    static boolean matches(String normalized, int max, Pattern allowed) {
        return normalized.length() >= MIN
                && normalized.length() <= max
                && LETTER.matcher(normalized).find()
                && allowed.matcher(normalized).matches();
    }
}
