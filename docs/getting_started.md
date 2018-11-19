# Getting started

_This guide presents a minimal example of how to add a Conjure-defined API to an existing Gradle project._

To see a finished result, check out [conjure-java-example](https://github.com/palantir/conjure-java-example):

```
git clone https://github.com/palantir/conjure-java-example.git
```

## 1. Add the `com.palantir.conjure` gradle plugin

In your `settings.gradle` file, add some new projects to contain your API YML and generated code. Conjure YML files will live in `:your-project-api` and generated code will be written to the `-objects`, `-jersey`, and `-typescript` sub-projects. It is recommended to define your API in the same git repo that you implement your server.

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

```groovy
buildscript {
    repositories {
        maven { url 'https://dl.bintray.com/palantir/releases/' }
    }

    dependencies {
        classpath 'com.palantir.gradle.conjure:gradle-conjure:4.6.2'
    }
}

allprojects {
    repositories {
        maven { url 'https://dl.bintray.com/palantir/releases/' }
    }
}
```

Then in `./your-project-api/build.gradle`, apply the plugin and specify versions for each generator.

```groovy
apply plugin: 'com.palantir.conjure'

dependencies {
    conjureCompiler 'com.palantir.conjure:conjure:4.4.0'
    conjureJava 'com.palantir.conjure.java:conjure-java:2.5.0'
    conjureTypeScript 'com.palantir.conjure.typescript:conjure-typescript:3.4.0'
}

subprojects {
    pluginManager.withPlugin 'java', {
        dependencies {
            compile 'com.palantir.conjure.java:conjure-lib:2.5.0'
        }
    }
}
```

_Check the GitHub releases page to find the latest version of [conjure](https://github.com/palantir/conjure/releases), [conjure-java](https://github.com/palantir/conjure-java/releases), [conjure-typescript](https://github.com/palantir/conjure-typescript/releases)._

This boilerplate can be omitted if you supply version numbers for each dependency elsewhere. [See more](/docs/gradle_decoupled_versions.md).

Running `./gradlew tasks` should now show a Conjure group with some associated tasks:

```
$ ./gradlew tasks

...

Conjure tasks
-------------
compileConjure - Generates code for your API definitions in src/main/conjure/**/*.yml
compileConjureObjects - Generates Java POJOs from your Conjure definitions.
compileConjureTypeScript - Generates TypeScript files and a package.json from your Conjure definitions.
...
```

## 2. Define your API in Conjure YML

Create your first YML file, e.g. `./your-project-api/src/main/conjure/your-project-api.yml`.  Run `./gradlew compileConjure` frequently to validate your syntax.

```yaml
types:
  definitions:
    default-package: com.company.product
    objects:

      Recipe:
        fields:
          name: RecipeName
          steps: list<RecipeStep>

      RecipeStep:
        union:
          mix: set<Ingredient>
          chop: Ingredient

      RecipeName:
        alias: string

      Ingredient:
        alias: string

services:
  RecipeBookService:
    name: Recipe Book
    package: com.company.product
    base-path: /recipes
    endpoints:
      createRecipe:
        http: POST /
        args:
          createRecipeRequest:
            param-type: body
            type: Recipe
```

*Refer to the [Conjure specification](/docs/spec/conjure_definitions.md) for an exhaustive list of allowed YML parameters.*

After running `./gradlew compileConjure`, you should see a variety of files generated in your `-api-objects`, `-api-jersey` and `-api-typescript` projects.

## 3. Implement your server

In your main gradle project, you can now depend on the generated Jersey interfaces.  Assuming you're using a Jersey-compatible server like [Dropwizard](https://github.com/dropwizard/dropwizard), you'll also need to use [conjure-java-runtime](https://github.com/palantir/conjure-java-runtime) to set up serialization correctly:

```groovy
// ./your-project/build.gradle

dependencies {
    compile 'com.palantir.conjure.java.runtime:conjure-java-jersey-server:latest.release'
    compile project(':your-project-api:your-project-api-jersey')
    ...
}
```

You can now write a `RecipeBookResource` class which `implements RecipeBookService`.  Your implementation shouldn't need any `@Path` annotations, as these will all be inherited from the Jersey interface.

```diff
 public final class RecipeBookApplication extends Application<RecipeBookConfiguration> {

     @Override
     public void run(RecipeBookConfiguration configuration, Environment environment) {
+        environment.jersey().register(ConjureJerseyFeature.INSTANCE);
+        environment.jersey().register(new RecipeBookResource());
     }
 }
```

_See the [conjure-java-example](https://github.com/palantir/conjure-java-example) repo for a fully-working Dropwizard-based server._

## 4. Connect to the server

After starting your dropwizard server, you should be able to sanity-check your API using curl:

```
curl http://localhost:8080/your-context-path/api/recipes \
    -H 'Content-Type: application/json' \
    --data '{"name": "My recipe", "steps": []}'
```

## 5. Next steps

- [Connect to your server from Java](/docs/howto/connect_from_java.md)
- [Publish TypeScript to npm](/docs/howto/publish_typescript_to_npm.md)
- Check out [conjure-typescript-example](https://github.com/palantir/conjure-typescript-example) to see how these APIs can be used from a browser.
