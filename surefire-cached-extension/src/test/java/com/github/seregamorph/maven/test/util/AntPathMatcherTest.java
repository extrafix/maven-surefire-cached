package com.github.seregamorph.maven.test.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AntPathMatcher}.
 */
public class AntPathMatcherTest {

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Test
    public void testDashMatch() {
        assertTrue(matcher.match("META-INF/maven/**/pom.properties",
            "META-INF/maven/com.acme/lib-gen-v1/pom.properties"));
        assertFalse(matcher.match("META-INF/maven/**/pom.properties",
            "META-INF/maven/com.acme/lib-gen-v1/pom.xml"));
    }

    @Test
    public void testWildcardColon() {
        var colonMatcher = new AntPathMatcher(":");
        assertTrue(colonMatcher.match("com.acme:*", "com.acme:lib"));
        assertTrue(colonMatcher.match("com.acme.*:*", "com.acme.module:lib"));
        assertFalse(colonMatcher.match("com.acme:*", "com.meac:lib"));
    }

    @Test
    public void testExactMatch() {
        assertTrue(matcher.match("com/example/Test.java", "com/example/Test.java"));
        assertFalse(matcher.match("com/example/Test.java", "com/example/test.java")); // case sensitive
    }

    @Test
    public void testSingleWildcard() {
        assertTrue(matcher.match("com/t?st.java", "com/test.java"));
        assertTrue(matcher.match("com/t?st.java", "com/tast.java"));
        assertFalse(matcher.match("com/t?st.java", "com/toast.java")); // ? matches exactly one character
    }

    @Test
    public void testMultipleWildcard() {
        assertTrue(matcher.match("com/*.java", "com/test.java"));
        assertTrue(matcher.match("com/*.java", "com/example.java"));
        assertFalse(matcher.match("com/*.java", "com/example/test.java")); // * doesn't match across path segments
    }

    @Test
    public void testDoubleWildcard() {
        assertTrue(matcher.match("com/**/test.java", "com/test.java"));
        assertTrue(matcher.match("com/**/test.java", "com/example/test.java"));
        assertTrue(matcher.match("com/**/test.java", "com/example/subdir/test.java"));
        assertFalse(matcher.match("com/**/test.java", "org/example/test.java")); // doesn't match different root
    }

    @Test
    public void testMixedWildcards() {
        assertTrue(matcher.match("com/**/*Test.java", "com/example/MyTest.java"));
        assertTrue(matcher.match("com/**/*Test.java", "com/example/subdir/AnotherTest.java"));
    }

    @Test
    public void testRootDoubleWildcard() {
        assertTrue(matcher.match("**/test.java", "test.java"));
        assertTrue(matcher.match("**/test.java", "com/test.java"));
        assertTrue(matcher.match("**/test.java", "com/example/test.java"));
    }

    @Test
    public void testNullOrEmpty() {
        assertTrue(matcher.match("", ""));
        assertFalse(matcher.match("", "test.java"));
    }

    @Test
    public void testWindowsPaths() {
        // Should normalize backslashes to forward slashes
        assertTrue(matcher.match("com\\example\\test.java", "com/example/test.java"));
        assertTrue(matcher.match("com/**/test.java", "com\\example\\test.java"));
    }

    @Test
    public void testComplexPatterns() {
        assertTrue(matcher.match("**/com/*/test/*_?ample.java", "org/com/example/test/my_sample.java"));
        assertFalse(matcher.match("**/com/*/test/*_?ample.java", "org/com/example/test/my_examples.java")); // extra 's'
    }
}
