package com.github.seregamorph.maven.test.storage;

import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public class MinServerProtocolVersionException extends RuntimeException {

    public MinServerProtocolVersionException(
        String message,
        @Nullable Integer serverProtocolVersion,
        int minServerProtocolVersion
    ) {
        super(message + ", serverProtocolVersion: "
            + (serverProtocolVersion == null ? "<empty>" : serverProtocolVersion.toString()) + ", "
            + "minServerProtocolVersion: " + minServerProtocolVersion);
    }
}
