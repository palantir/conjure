# Getting started

_This guide assumes you want to add a Conjure-defined API to an existing gradle project._

## 1. Add the gradle plugin

Add some new gradle projects to your `settings.gradle`. Conjure YML files will live in `:your-project-api` and then generated code will be written to the `-objects`, `-jersey`, `-typescript` projects. Note, you can omit any of these projects if you don't need the generated code.  For example, if you only want to generate Java objects, you can just add the `-objects` project.

```diff
 rootProject.name = 'your-project'

 include 'your-project'
+include 'your-project-api'
+include 'your-project-api:your-project-api-objects'
+include 'your-project-api:your-project-api-jersey'
+include 'your-project-api:your-project-api-typescript'
```

In your top level `build.gradle` file, add a buildscript dependency on Conjure.  Then, apply the `com.palantir.conjure` plugin to your `-api` project.

```gradle
buildscript {
    repositories {
        maven {
            url 'https://dl.bintray.com/palantir/releases/'
        }
    }

    dependencies {
        classpath 'com.netflix.nebula:nebula-dependency-recommender:5.2.0'
        classpath 'com.palantir.gradle.conjure:gradle-conjure:4.0.0-rc3'
    }
}

// (optional) nebula-dependency-recommender makes it easy to specify versions of generators
subprojects {
    apply plugin: 'nebula.dependency-recommender'
    dependencyRecommendations {
        propertiesFile file: project.rootProject.file('versions.props')
    }
}
```

In `./your-project-api/build.gradle`, apply the plugin:

```gradle
apply plugin: 'com.palantir.conjure'
```

If you use nebula.dependency-recommender, specific some versions of the Conjure-generators you want to use.  These can be upgraded independently!

```
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
