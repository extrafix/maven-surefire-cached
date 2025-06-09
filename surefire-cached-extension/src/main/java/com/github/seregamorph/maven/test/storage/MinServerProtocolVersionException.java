package com.github.seregamorph.maven.test.storage;

import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public class MinServerProtocolVersionException extends RuntimeException {

    public MinServerProtocolVersionException(
        String message,
        @Nullable String serverProtocolVersionStr,
        int minServerProtocolVersion
    ) {
        super(message + ", serverProtocolVersion: "
            + (serverProtocolVersionStr == null ? "<empty>" : serverProtocolVersionStr) + ", "
            + "minServerProtocolVersion: " + minServerProtocolVersion);
    }
}
