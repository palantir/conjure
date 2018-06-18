# Getting started

_This guide assumes you want to add a Conjure-defined API to an existing gradle project._

## 1. Add the gradle plugin

In your `settings.gradle` file, add some new projects to contain your API YML and Conjure-generated code. Conjure YML files will live in `:your-project-api` and generated code will be written to the `-objects`, `-jersey`, `-typescript` projects. Note, you can omit any of these projects if you don't need the generated code.  For example, if you only want to generate Java objects, you can just add the `-objects` project.

```diff
 rootProject.name = 'your-project'

 include 'your-project'
+include 'your-project-api'
+include 'your-project-api:your-project-api-objects'
+include 'your-project-api:your-project-api-jersey'
+include 'your-project-api:your-project-api-typescript'
```

In your top level `build.gradle` file, add a buildscript dependency on Conjure.

```gradle
buildscript {
    repositories {
        maven {
            url 'https://dl.bintray.com/palantir/releases/'
        }
    }

    dependencies {
        classpath 'com.palantir.gradle.conjure:gradle-conjure:4.0.0-rc3'
    }
}
```

Then in `./your-project-api/build.gradle`, apply the plugin:

```gradle
apply plugin: 'com.palantir.conjure'

// alternatively, use nebula.dependency-recommender (see below)
configurations.all {
    resolutionStrategy {
        force 'com.palantir.conjure.java:conjure-java:0.2.1'
        force 'com.palantir.conjure.typescript:conjure-typescript:0.3.0'
    }
}
```

`gradle-conjure` requires you to explicitly specify versions of the conjure-generators so that users are encouraged to update them frequently.  These generators are released entirely independently from the gradle-conjure plugin and can be upgraded separately.


### (Optional) Use `nebula.dependency-recommender`

Rather than adding `force` lines, [Nebula Dependency Recommender](https://github.com/nebula-plugins/nebula-dependency-recommender-plugin) is currently the preferred way to specify these version numbers. It can source versions from a well-structured properties file (which is easy to automatically upgrade).

```gradle
// build.gradle

subprojects {
    apply plugin: 'nebula.dependency-recommender'
    dependencyRecommendations {
        propertiesFile file: project.rootProject.file('versions.props')
    }
}
```
```
# versions.props

com.palantir.conjure.java:* = 0.2.4
com.palantir.conjure.typescript:conjure-typescript = 0.3.0
```

## 2. Write conjure yml

TODO

## 3. Publish artifacts

If you want to publish npm packages, you can modify `./your-project-api/build.gradle`:

```diff
 apply plugin: 'com.palantir.conjure'

+project(':your-project-api:your-project-api-typescript') {
+    publishTypeScript.doFirst {
+        file('src/.npmrc') << "//registry.npmjs.org/:_authToken=${System.env.NPM_AUTH_TOKEN}"
+    }
+}
```
