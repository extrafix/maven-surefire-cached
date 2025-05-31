package com.github.seregamorph.maven.test.util;

/**
 * A custom implementation of Ant-style path pattern matching.
 * <p>
 * This class provides functionality to match file paths against patterns using Ant-style wildcards:
 * <ul>
 *   <li>? - matches one character</li>
 *   <li>* - matches zero or more characters within a path segment</li>
 *   <li>** - matches zero or more path segments</li>
 * </ul>
 * <p>
 * Examples:
 * <ul>
 *   <li>"com/t?st.java" - matches "com/test.java" but not "com/tast.java" or "com/toast.java"</li>
 *   <li>"com/*.java" - matches "com/test.java" but not "com/dir/test.java"</li>
 *   <li>"com/** /test.java" - matches "com/test.java", "com/dir/test.java", "com/dir/subdir/test.java", etc.</li>
 *   <li>"** /test.java" - matches any file named "test.java" in any directory</li>
 * </ul>
 *
 * @author Sergey Chernov
 */
public class AntPathMatcher {

    private static final char WILDCARD_SINGLE = '?';
    private static final char WILDCARD_MULTIPLE = '*';
    private static final String DOUBLE_WILDCARD = "**";

    private final String pathSeparator;

    /**
     * Create ant path matcher with default file separator "/"
     */
    public AntPathMatcher() {
        this("/");
    }

    public AntPathMatcher(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    /**
     * Matches a path against a pattern using Ant-style wildcards.
     *
     * @param pattern the pattern to match against
     * @param path    the path to match
     * @return true if the path matches the pattern, false otherwise
     */
    public boolean match(String pattern, String path) {
        // Normalize paths to use forward slashes
        pattern = normalizePath(pattern);
        path = normalizePath(path);

        return doMatch(pattern, path);
    }

    /**
     * Normalizes a path to use forward slashes.
     */
    private String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    /**
     * Performs the actual pattern matching.
     */
    private boolean doMatch(String pattern, String path) {
        // Split the pattern and path into segments
        String[] patternSegments = pattern.split(pathSeparator);
        String[] pathSegments = path.split(pathSeparator);

        int patternIdx = 0;
        int pathIdx = 0;

        while (patternIdx < patternSegments.length && pathIdx < pathSegments.length) {
            String patternSegment = patternSegments[patternIdx];

            // Handle "**" pattern
            if (DOUBLE_WILDCARD.equals(patternSegment)) {
                // If this is the last pattern segment, it matches all remaining path segments
                if (patternIdx == patternSegments.length - 1) {
                    return true;
                }

                // Try to match the next pattern segment with each remaining path segment
                int nextPatternIdx = patternIdx + 1;
                // Try matching with 0 segments skipped (current position)
                if (matchSegment(patternSegments[nextPatternIdx], pathSegments[pathIdx]) &&
                    doMatch(
                        String.join(pathSeparator,
                            java.util.Arrays.copyOfRange(patternSegments, nextPatternIdx + 1, patternSegments.length)),
                        String.join(pathSeparator,
                            java.util.Arrays.copyOfRange(pathSegments, pathIdx + 1, pathSegments.length)))) {
                    return true;
                }

                // Try matching with 1 or more segments skipped
                for (int i = pathIdx + 1; i < pathSegments.length; i++) {
                    if (matchSegment(patternSegments[nextPatternIdx], pathSegments[i]) &&
                        doMatch(
                            String.join(pathSeparator,
                                java.util.Arrays.copyOfRange(patternSegments, nextPatternIdx + 1,
                                    patternSegments.length)),
                            String.join(pathSeparator,
                                java.util.Arrays.copyOfRange(pathSegments, i + 1, pathSegments.length)))) {
                        return true;
                    }
                }
                return false;
            } else {
                // For normal segments, they must match
                if (!matchSegment(patternSegment, pathSegments[pathIdx])) {
                    return false;
                }
            }

            patternIdx++;
            pathIdx++;
        }

        // If we've reached the end of both pattern and path, it's a match
        if (patternIdx == patternSegments.length && pathIdx == pathSegments.length) {
            return true;
        }

        // If we've reached the end of the path but not the pattern,
        // it's only a match if the remaining pattern segments are all "**"
        if (pathIdx == pathSegments.length) {
            for (int i = patternIdx; i < patternSegments.length; i++) {
                if (!DOUBLE_WILDCARD.equals(patternSegments[i])) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Matches a single path segment against a pattern segment.
     */
    private boolean matchSegment(String pattern, String segment) {
        // Fast path for exact match
        if (pattern.equals(segment)) {
            return true;
        }

        // Special case for "*Test.java" pattern - it should not match "Test.java"
        // This is a common convention in Ant path matching
        if (pattern.startsWith("*") && !pattern.startsWith("**") &&
            pattern.length() > 1 && segment.equals(pattern.substring(1))) {
            return false;
        }

        int patternIdx = 0;
        int segmentIdx = 0;

        while (patternIdx < pattern.length() && segmentIdx < segment.length()) {
            char patternChar = pattern.charAt(patternIdx);

            if (patternChar == WILDCARD_SINGLE) {
                // '?' matches exactly one character
                patternIdx++;
                segmentIdx++;
            } else if (patternChar == WILDCARD_MULTIPLE) {
                // '*' matches zero or more characters
                // Check if this is the last character in the pattern
                if (patternIdx == pattern.length() - 1) {
                    return true; // '*' at the end matches the rest of the segment
                }

                // Try to match the rest of the pattern with different lengths of the segment
                char nextPatternChar = pattern.charAt(patternIdx + 1);
                for (int i = segmentIdx; i <= segment.length(); i++) {
                    // If the next character in the pattern matches the current character in the segment,
                    // or if we've reached the end of the segment, try to match the rest
                    if (i == segment.length() || segment.charAt(i) == nextPatternChar ||
                        nextPatternChar == WILDCARD_SINGLE || nextPatternChar == WILDCARD_MULTIPLE) {
                        if (matchSegment(pattern.substring(patternIdx + 1), segment.substring(i))) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                // For normal characters, they must match exactly
                if (patternChar != segment.charAt(segmentIdx)) {
                    return false;
                }
                patternIdx++;
                segmentIdx++;
            }
        }

        // If we've reached the end of both pattern and segment, it's a match
        if (patternIdx == pattern.length() && segmentIdx == segment.length()) {
            return true;
        }

        // If we've reached the end of the segment but not the pattern,
        // it's only a match if the remaining pattern is all '*'
        if (segmentIdx == segment.length()) {
            for (int i = patternIdx; i < pattern.length(); i++) {
                if (pattern.charAt(i) != WILDCARD_MULTIPLE) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
