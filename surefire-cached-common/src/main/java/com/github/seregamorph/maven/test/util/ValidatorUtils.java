package com.github.seregamorph.maven.test.util;

/**
 * @author Sergey Chernov
 */
public final class ValidatorUtils {

    public static void validateFileName(String fileName) {
        if (fileName.contains("..")) {
            // prevent possible path traversal attacks
            throw new IllegalArgumentException("Invalid fileName: " + fileName);
        }
        if (!fileName.matches("^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*$")) {
            throw new IllegalArgumentException("Invalid fileName: " + fileName);
        }
    }

    private ValidatorUtils() {
    }
}
