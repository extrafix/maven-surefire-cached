
[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.seregamorph/maven-surefire-cached?style=flat-square)](https://central.sonatype.com/artifact/com.github.seregamorph/maven-surefire-cached/overview)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

# Maven Surefire Cached
This extension wraps standard Maven `surefire` and `failsafe` plugins to support local and remote build caching.

## Comparison with Apache Maven Build Cache Extension
The [Apache Maven Build Cache Extension](https://maven.apache.org/extensions/maven-build-cache-extension/) is an open-source
project adding support of artifact caching to maven, also allowing to skip goal executions via cache.
It can cover a wide range of typical scenarios, however, it's not a good choice for pipelines separating compile and 
test phases. It does not properly handle test reports, does not support flexible test filtering for test distribution 
(caching them separately depending on filtered test subset) for multi-job execution.
Also it does not cache so called CLI executions like `mvn surefire:test`, only lifecycle executions
like `mvn clean test`, which is also not always convenient.

## Adoption
Add to the `.mvn/extensions.xml` of your project:
```xml
<extensions>
    <extension>
        <groupId>com.github.seregamorph</groupId>
        <artifactId>surefire-cached-extension</artifactId>
        <version>0.12</version>
    </extension>
</extensions>
```
This extension will print the cache statistics after the build.

## Configuration
It's mandatory to define `surefire-cached.json` file for the module. If the file is not found,
the extension will go thru the parent modules (till root) and try to find it there. Sample configuration file:
```json
{
  "common": {
    "inputIgnoredProperties": [
      "java.version",
      "os.arch",
      "os.name",
      "env.CI",
      "env.GITHUB_BASE_REF",
      "env.GITHUB_REF",
      "env.GITHUB_RUN_ID",
      "env.GITHUB_JOB",
      "env.GITHUB_SHA"
    ],
    "cacheExcludes": ["com.acme:core"]
  },
  "surefire": {
    "artifacts": {
      "surefire-reports": {
        "includes": ["surefire-reports/TEST-*.xml"]
      },
      "jacoco": {
        "includes": ["jacoco-surefire-*.exec"]
      }
    }
  },
  "failsafe": {
    "artifacts": {
      "failsafe-reports": {
        "includes": ["failsafe-reports/TEST-*.xml"]
      },
      "jacoco": {
        "includes": ["jacoco-failsafe-*.exec"]
      }
    }
  }
}
```

Sample adoption:
* https://github.com/seregamorph/spring-test-smart-context/pull/6
* https://github.com/seregamorph/rest-api-framework/pull/2

## Running with the local cache
Build your project with the extension, the caching will use default file storage `$HOME/.m2/test-cache`
```shell
mvn clean install
```

Or compile separately and run unit tests
```shell
mvn clean install -DskipTests=true
mvn surefire:test
```

Or via phase
```shell
mvn test
```

Then run integration tests
```shell
mvn clean install -DskipTests=true
mvn failsafe:integration-test -Dit.test=SampleIT
```
or via phase
```shell
mvn verify
```

## Running with the remote cache
Run server from this repo
```shell
./mvnw clean install
docker compose up
```

Build your project with the extension using the remote cache
```shell
mvn clean install -DcacheStorageUrl=http://localhost:8080/cache
```

Actuator endpoints are available at http://localhost:8080/actuator, see http://localhost:8080/actuator/prometheus
for metrics.

TODO grafana dashboard WiP

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
