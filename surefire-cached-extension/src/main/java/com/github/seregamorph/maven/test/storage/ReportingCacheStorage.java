package com.github.seregamorph.maven.test.storage;

import javax.annotation.Nullable;

public interface ReportingCacheStorage extends CacheStorage {

    @Nullable
    String getReadFailureReport();

    @Nullable
    String getWriteFailureReport();
}
