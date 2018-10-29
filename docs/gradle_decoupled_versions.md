# Gradle decoupled versions

We recommend specifying all version numbers in one place. Using a plugin like [`com.palantir.baseline-versions`](https://github.com/palantir/gradle-baseline#compalantirbaseline-versions) allows you to delete the following boilerplate and specify versions in a single `versions.props` file:

```diff
 apply plugin: 'com.palantir.conjure'

-dependencies {
-    conjureCompiler 'com.palantir.conjure:conjure:4.0.0'
-    conjureJava 'com.palantir.conjure.java:conjure-java:2.0.0'
-    conjureTypeScript 'com.palantir.conjure.typescript:conjure-typescript:3.3.0'
-}
-
-subprojects {
-    pluginManager.withPlugin 'java', {
-        dependencies {
-            compile 'com.palantir.conjure.java:conjure-lib:2.0.0'
-        }
-    }
-}
```

In versions.props:

```diff
+com.palantir.conjure:conjure = 4.0.0
+com.palantir.conjure.java:* = 2.0.0
+com.palantir.conjure.typescript:* = 3.2.0
```

This ensures you don't have different version numbers across subprojects and also makes upgrades convenient.
