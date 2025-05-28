package com.github.seregamorph.maven.test.util;

import javax.annotation.Nullable;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

/**
 * @author Sergey Chernov
 */
public final class MavenPropertyUtils {

    @Nullable
    public static String getProperty(MavenSession session, MavenProject project, String propertyName) {
        String propertyValue = session.getSystemProperties().getProperty(propertyName);
        if (propertyValue == null) {
            propertyValue = project.getProperties().getProperty(propertyName);
        }
        return propertyValue;
    }

    public static boolean isEmptyOrTrue(String value) {
        return "".equals(value) || "true".equals(value);
    }

    private MavenPropertyUtils() {
    }
}
