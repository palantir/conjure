# How to connect to a Conjure server from Java

Prerequisites

- API already defined using gradle-conjure (see [getting started](/docs/getting_started.md))
- Server implemented and working (e.g. using Dropwizard)

## 1. Depend on the Conjure-generated Jersey interfaces

```diff
 dependencies {
+    compile project('your-project-api:your-project-api-jersey')
 }
```

You should now be able to compile against generated interfaces and objects, e.g. `RecipeBookService`.

## 2. Depend on conjure-java-runtime

conjure-java just generates client/server interfaces, so to actually make network calls you need to provide a _client_. conjure-java-runtime uses reflection to read annotations from your generated interfaces and provides a client based on [Java 8 dynamic proxies](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Proxy.html).

```diff
 dependencies {
     compile project('your-project-api:your-project-api-jersey')
+    compile 'com.palantir.conjure.java.runtime:conjure-java-jaxrs-client:<latest>'
 }
```

*Check [palantir/conjure-java-runtime](https://github.com/palantir/conjure-java-runtime) for the latest release*

## 3. Construct a client

Pass your interface as the first argument to have a client created:

```java
RecipeBookService recipeBookService = JaxRsClient.create(
       RecipeBookService.class,
       UserAgent.of(Agent.of("your-server", "0.0.0")),
       NoOpHostEventsSink.INSTANCE,
       ClientConfigurations.of(ServiceConfiguration.builder()
               .addUris("http://localhost:8080/examples/api/")
               .security(SslConfiguration.of(Paths.get(TRUSTSTORE_PATH)))
               .build()));
```

## 4. Make a network call

```java
Recipe recipe = recipeBookService.getRecipe("Recipe name");
```
