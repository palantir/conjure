# Conjure
_Conjure is a simple but opinionated toolchain for defining APIs once and magically generating client/server interfaces in many languages._

Conjure was developed to help scale Palantir's microservice architecture - it has been battle-tested across hundreds of repos and has allowed devs to be productive in many languages.

Define your API once and then Conjure will generate idiomatic clients for Java, TypeScript, Golang, Rust etc. The generated interfaces provide type-safe, clean abstractions so you can make network requests without worrying about the details.

For example in Java, Conjure interfaces allow you to build servers using existing Jersey compatible libraries like Dropwizard/Jetty.

**See our [getting started](docs/getting_started.md) guide to define your first Conjure API.**

## Features
- Enables teams to work together across many languages
- Eliminates an entire class of serialization bugs
- Ergonomic interfaces abstract away low-level details
- Expressive language to model your domain (enums, union types, maps, lists, sets)
- Helps devs preserve backwards compatibility (old clients can talk to new servers)
- Supports incremental switchover from existing JSON/HTTP servers
- Zero config (works out of the box)


## Example
The following YAML file defines a simple Recipe Book API. (See [full reference](/docs/spec/conjure_definitions.md))

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
        docs: Adds a new recipe to the store.
        args:
          createRecipeRequest:
            param-type: body
            type: Recipe
```

The following generated Java interface can be used on the client and the server.

```java
package com.company.product;

...

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
@Generated("com.palantir.conjure.java.services.JerseyServiceGenerator")
public interface RecipeBookService {
    /** Add a new recipe to the store. */
    @POST
    @Path("recipes")
    void createRecipe(@HeaderParam("Authorization") AuthHeader authHeader, Recipe createRecipeRequest);
}
```

Type-safe network calls to this API can made from TypeScript as follows:

```ts
function demo(): Promise<void> {
    const request: IRecipe = {
        name: "Baked chicken with sun-dried tomatoes",
        steps: [
            RecipeStep.chop("Tomatoes"),
            RecipeStep.chop("Chicken"),
            RecipeStep.chop("Potatoes")
            RecipeStep.mix(["Herbs", "Tomatoes", "Chicken"]),
        ]
    };
    return new RecipeBookService(bridge).createRecipe(request);
}
```

## Ecosystem

##### Compiler

- [conjure](/docs/compiler_usage.md)

##### Build tool

- [gradle-conjure](https://github.com/palantir/gradle-conjure)

##### Code generators

- [conjure-java](https://github.com/palantir/conjure-java)
- [conjure-typescript](https://github.com/palantir/conjure-typescript)
- [conjure-python](https://github.com/palantir/conjure-python)
- conjure-go (coming soon)
- conjure-rust (coming soon)

##### Client/server libraries

- [conjure-java-runtime](https://github.com/palantir/conjure-java-runtime)
- [conjure-typescript-client](https://github.com/palantir/conjure-typescript-client)
- [conjure-python-client](https://github.com/palantir/conjure-python-client)

##### Recommended server libraries

- [dropwizard](https://github.com/dropwizard/dropwizard)

##### Miscellaneous tools

- [conjure-postman](https://github.com/palantir/conjure-postman)
- conjure-jsonschema (coming soon)
- conjure-backcompat (coming soon)

## Contributing

See the [CONTRIBUTING.md](/CONTRIBUTING.md) document.
