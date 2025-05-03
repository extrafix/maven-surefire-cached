# maven-surefire-cached
This extension wraps standard Maven surefire and failsafe plugins to support local and remote build caching.

## Comparison with Maven Build Cache Extension
The [Maven Build Cache Extension](https://maven.apache.org/extensions/maven-build-cache-extension/) is an open-source
project adding support of artifact caching to maven, also allowing to skip goal executions via cache.
It can cover a wide range of typical scenarios, however, it's not a good choice for pipelines separating build and 
test phases. It does not properly handle test reports, does not support flexible test filtering (caching them 
separately depending on filtered test subset) for parallel execution.
Also it does not cache so called CLI executions like `mvn surefire:test`, only lifecycle executions
like `mvn clean test`, which is also not always convenient.

## Adoption
Add to the or `.mvn/extensions.xml` of your project:
```xml
<extensions>
    <extension>
        <groupId>com.github.seregamorph</groupId>
        <artifactId>surefire-cached-extension</artifactId>
        <version>0.7</version>
    </extension>
</extensions>
```
This extension will print the cache statistics after the build.

Sample adoption:
* https://github.com/seregamorph/spring-test-smart-context/pull/6

First build without tests
```shell
mvn clean install -DskipTests=true
```

Then run unit tests
```shell
mvn surefire:test
```

Or via phase
```shell
mvn test
```

Then run integration tests
```shell
mvn failsafe:integration-test -Dit.test=SampleIT
```
or via phase
```shell
mvn verify
```

Using remote cache
```shell
mvn clean install -DcacheStorageUrl=http://localhost:8080/cache
```

## How it works
The extension wraps and replaces default Mojo factory
[DefaultMavenPluginManager](https://github.com/apache/maven/blob/maven-3.9.9/maven-core/src/main/java/org/apache/maven/plugin/internal/DefaultMavenPluginManager.java)
with own implementation [CachedMavenPluginManager](surefire-cached-extension/src/main/java/com/github/seregamorph/maven/test/extension/CachedMavenPluginManager.java).
All mojos are delegating to default behaviour except Surefire and Failsafe plugins. They are wrapped to caching logic,
which calculates task inputs (classpath elements hash codes) and reuses existing cached test result when available.

## Related projects
### Turbo reactor
The default Maven multi-module build does not do an efficient multi-core CPU utilization.
See [turbo-builder](https://github.com/seregamorph/maven-turbo-reactor) for more details.
