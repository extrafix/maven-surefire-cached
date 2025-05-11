package com.github.seregamorph.maven.test.core;

import com.github.seregamorph.maven.test.util.HashUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergey Chernov
 */
public class FileHashCache {

    private static final Logger log = LoggerFactory.getLogger(FileHashCache.class);

    /**
     * "$canonicalAbsoluteFileName:$sensitivity" -> file hash
     */
    private final Cache<String, FileHashValue> cacheFiles;
    /**
     * "$canonicalAbsoluteDirName" -> directory hash
     */
    private final Cache<String, DirHashValue> cacheDirectories;

    public FileHashCache() {
        cacheFiles = CacheBuilder.newBuilder().build();
        cacheDirectories = CacheBuilder.newBuilder().build();
    }

    /**
     * Get hash sum of classpath element which may be a JAR file or a directory with classes. The META-INF/MANIFEST.MF
     * is ignored if present.
     * <p>
     * Note: this method will calculate the same hash sum for class directory and jar archive of the same directory.
     *
     * @param file class directory or JAR file
     * @return aggregated hash sum
     */
    public String getClasspathElementHash(File file) {
        try {
            var cacheKey = file.getCanonicalFile().getAbsolutePath();
            if (file.isDirectory()) {
                Callable<DirHashValue> loader = () -> {
                    var hash = plainHash(HashUtils.hashDirectory(file));
                    return new DirHashValue(hash, file.lastModified());
                };
                var fileHashValue = cacheDirectories.get(cacheKey, loader);
                if (fileHashValue.fileLastModified() != file.lastModified()) {
                    log.warn("Invalidating cache of: {}", cacheKey);
                    cacheDirectories.invalidate(cacheKey);
                    fileHashValue = cacheDirectories.get(cacheKey, loader);
                }
                return fileHashValue.hash();
            } else if (file.exists()) {
                Callable<FileHashValue> loader = () -> {
                    // calculate has hums of zip entries ignoring timestamps
                    var hash = plainHash(HashUtils.hashZipFile(file));
                    return new FileHashValue(hash, file.length(), file.lastModified());
                };
                var fileHashValue = cacheFiles.get(cacheKey, loader);
                if (file.length() != fileHashValue.fileLength()
                    || fileHashValue.fileLastModified() != file.lastModified()) {
                    // this should not happen in a regular mvnw
                    log.warn("Invalidating cache of file: {}", cacheKey);
                    cacheFiles.invalidate(cacheKey);
                    fileHashValue = cacheFiles.get(cacheKey, loader);
                }
                return fileHashValue.hash();
            } else {
                // this can be a non-existing classes directory for modules with empty sourceSet
                return HashUtils.HASH_EMPTY_FILE_COLLECTION;
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String plainHash(Map<String, String> mapHash) {
        var filteredMap = new TreeMap<>(mapHash);
        // remove MANIFEST.MF from the hash to unify hash calculation for jar files and classes directories
        filteredMap.remove("META-INF/MANIFEST.MF");

        if (filteredMap.isEmpty()) {
            return HashUtils.HASH_EMPTY_FILE_COLLECTION;
        }

        var sw = new StringBuilder();
        filteredMap.forEach((key, value) -> sw.append(key).append(":").append(value).append("\n"));
        return HashUtils.hashArray(sw.toString().getBytes(StandardCharsets.UTF_8));
    }

    private record DirHashValue(String hash, long fileLastModified) {
    }

    private record FileHashValue(String hash, long fileLength, long fileLastModified) {
    }
}
