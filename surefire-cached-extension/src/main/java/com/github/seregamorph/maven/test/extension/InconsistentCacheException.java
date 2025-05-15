package com.github.seregamorph.maven.test.extension;

/**
 * The cache entity is missing some of the files. This may happen if the cache storage evicts old entities.
 *
 * @author Sergey Chernov
 */
public class InconsistentCacheException extends Exception {

    public InconsistentCacheException(String message) {
        super(message);
    }
}
