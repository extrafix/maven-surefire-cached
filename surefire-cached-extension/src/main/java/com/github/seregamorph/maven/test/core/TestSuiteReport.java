package com.github.seregamorph.maven.test.core;

import com.github.seregamorph.maven.test.util.XmlUtils;
import java.io.File;
import java.math.BigDecimal;
import org.w3c.dom.Element;

/**
 * surefire/failsafe report files TEST-*.xml ("testsuite" tag)
 *
 * @author Sergey Chernov
 */
public record TestSuiteReport(String name, BigDecimal timeSeconds, int tests, int errors, int failures) {

    public static TestSuiteReport fromFile(File file) {
        var root = XmlUtils.parseXml(file);
        Element rootElement = root.getDocumentElement();
        String tagName = rootElement.getTagName();
        if (!"testsuite".equals(tagName)) {
            throw new IllegalArgumentException("Not a test suite report file: "
                + file + " with root element '" + tagName + "'");
        }

        // test class name
        var name = rootElement.getAttribute("name");
        var timeSeconds = new BigDecimal(rootElement.getAttribute("time"));
        var tests = Integer.parseInt(rootElement.getAttribute("tests"));
        var errors = Integer.parseInt(rootElement.getAttribute("errors"));
        var failures = Integer.parseInt(rootElement.getAttribute("failures"));

        return new TestSuiteReport(name, timeSeconds, tests, errors, failures);
    }
}
