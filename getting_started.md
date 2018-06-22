# Getting started

_This guide explains how to add a Conjure-defined API to an existing gradle project.  It is recommended to define your API in the same git repo that you implement your server._

## 1. Add the `com.palantir.conjure` gradle plugin

In your `settings.gradle` file, add some new projects to contain your API YML and generated code. Conjure YML files will live in `:your-project-api` and generated code will be written to the `-objects`, `-jersey`, and `-typescript` sub-projects.

```diff
 rootProject.name = 'your-project'

 include 'your-project'
+include 'your-project-api'
+include 'your-project-api:your-project-api-objects'
+include 'your-project-api:your-project-api-jersey'
+include 'your-project-api:your-project-api-typescript'
```

_Note, you can omit any of these projects if you don't need the generated code (gradle-conjure just looks at the project name suffix to figure out where to put generated code).  For example, if you only want to generate Java objects, you can just add the `your-project-api-objects` project and omit the others._

In your top level `build.gradle` file, add a buildscript dependency on Conjure.

```gradle
buildscript {
    repositories {
        maven {
            url 'https://dl.bintray.com/palantir/releases/'
        }
    }

    dependencies {
        classpath 'com.palantir.gradle.conjure:gradle-conjure:4.0.0-rc6'
    }
}

// conjure-generator versions must be specified explicitly so that it's clear they can be upgraded independently.

// (option 1):
subprojects {
    configurations.all {
        resolutionStrategy {
            force 'com.palantir.conjure.java:conjure-java:0.2.4'
            force 'com.palantir.conjure.java:conjure-java-lib:0.2.4'
            force 'com.palantir.conjure.typescript:conjure-typescript:0.6.1'
        }
    }
}

// (option 2): nebula.dependency-recommender (see below)
```

Then in `./your-project-api/build.gradle`, apply the plugin:

```gradle
apply plugin: 'com.palantir.conjure'
```

Running `./gradlew tasks` should now show a Conjure group with some associated tasks:

```
$ ./gradlew tasks

...

Conjure tasks
-------------
compileConjure - Generates code for your API definitions in src/main/conjure/**/*.yml
compileConjureObjects - Generates Java POJOs from your Conjure definitions.

...
```


### (Optional) Use `nebula.dependency-recommender`

If you already use [Nebula Dependency Recommender](https://github.com/nebula-plugins/nebula-dependency-recommender-plugin), you can omit the `resolutionStrategy` lines and use a properties file like `versions.props` instead:

```
com.palantir.conjure.java:* = <latest>
com.palantir.conjure.typescript:conjure-typescript = <latest>
```

_Check the GitHub releases page to find the latest version of [conjure-java](https://github.com/palantir/conjure-java/releases), [conjure-typescript](https://github.com/palantir/conjure-typescript/releases)._

## 2. Define your API in Conjure YML

Create your first YML file, e.g. `./your-project-api/src/main/conjure/your-project-api.yml`.  Run `./gradlew compileConjure` frequently to validate your syntax.

```yml
types:
  definitions:
    default-package: com.company.product
    objects:

      AddPetRequest:
        fields:
          id: integer
          name: string
          tags: set<Tag>
          status: Status

      Tag:
        alias: string

      Status:
        values:
        - AVAILABLE
        - PENDING
        - SOLD

services:
  PetStoreService:
    name: Pet Store Service
    package: com.company.product
    default-auth: header

    endpoints:
      addPet:
        docs: Add a new pet to the store
        http: POST /pet
        args:
          addPetRequest:
            type: AddPetRequest
            param-type: body
```

_Refer to the [Conjure specification](./specification.md) for an exhaustive list of allowed YML parameters._

After running `./gradlew compileConjure`, you should see a variety of files generated in your `-api-objects`, `-api-jersey` and `-api-typescript` projects.

## 3. Implement your server

In your main gradle project, you can now depend on the generated Jersey interfaces:

```gradle
// ./your-project/build.gradle

dependencies {
    compile project(':your-project-api:your-project-api-jersey')
    ...
}
```

You can now write a `PetStoreResource` class which `implements PetStoreService`.  Your implementation shouldn't need any `@Path` annotations, as these will all be inherited from the Jersey interface.

## 4. Publish artifacts

Jars can be published using your favourite Gradle publishing set-up, e.g. [Bintray](https://bintray.com/).

If you want to publish npm packages, you need to simulate the `npm login` command to ensure you have the necessary credentials to `npm publish`.  Add the following snippet to your `./your-project-api/build.gradle` to write the `$NPM_AUTH_TOKEN` environment variable to disk.  You should specify this as a secret variable on your CI server (e.g. CircleCI or TravisCI).

```diff
 apply plugin: 'com.palantir.conjure'

+project(':your-project-api:your-project-api-typescript') {
+    publishTypeScript.doFirst {
+        file('src/.npmrc') << "//registry.npmjs.org/:_authToken=${System.env.NPM_AUTH_TOKEN}"
+    }
+}
```

