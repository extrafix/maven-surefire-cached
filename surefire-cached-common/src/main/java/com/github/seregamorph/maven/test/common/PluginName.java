package com.github.seregamorph.maven.test.common;

import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;

/**
 * @author Sergey Chernov
 */
public final class PluginName implements Comparable<PluginName>{

    // implementation notice: the enum is not used because it does not allow auto-conversion in spring webmvc

    public static final PluginName SUREFIRE_CACHED = new PluginName("surefire-cached");
    public static final PluginName FAILSAFE_CACHED = new PluginName("failsafe-cached");

    private final String name;

    private PluginName(String name) {
        this.name = name;
    }

    @JsonValue
    public String name() {
        return name;
    }

    /**
     * To support auto-conversion e.g., as a spring webmvc Controller parameter without additional converter
     * configuration
     *
     * @param pluginName
     * @return
     */
    @Nonnull
    public static PluginName valueOf(String pluginName) {
        if (SUREFIRE_CACHED.name().equals(pluginName)) {
            return SUREFIRE_CACHED;
        } else if (FAILSAFE_CACHED.name().equals(pluginName)) {
            return FAILSAFE_CACHED;
        } else {
            throw new IllegalArgumentException("Unknown plugin name: " + pluginName);
        }
    }

    @Override
    public int compareTo(PluginName that) {
        // surefire-cached < failsafe-cached
        return that.name.compareTo(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
