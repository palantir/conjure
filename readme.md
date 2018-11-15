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

The [conjure compiler](/docs/compiler_usage.md) reads API definitions written in the concise, [human-readable YML format](/docs/spec/conjure_definitions.md) and produces a JSON-based [intermediate representation](/docs/spec/intermediate_representation.md) (IR).

_Conjure generators_ read IR and produce code in the target language. The associated libraries provide client and server implementations. Each generator is distributed as a CLI that conforms to [RFC002](/docs/rfc/002-contract-for-conjure-generators.md):

| Language | Generator | Libraries | Examples |
|--------------------|-------------------------------|-|-|
| Java | [conjure-java](https://github.com/palantir/conjure-java) | [conjure-java-runtime](https://github.com/palantir/conjure-java-runtime) | [conjure-java-example](https://github.com/palantir/conjure-java-example)
| TypeScript | [conjure-typescript](https://github.com/palantir/conjure-typescript) | [conjure-typescript-runtime](https://github.com/palantir/conjure-typescript-runtime) | [conjure-typescript-example](https://github.com/palantir/conjure-typescript-example)
| Python | [conjure-python](https://github.com/palantir/conjure-python) | [conjure-python-runtime](https://github.com/palantir/conjure-python-runtime) | -

[gradle-conjure](https://github.com/palantir/gradle-conjure) is a _build tool_ that orchestrates invoking the compiler and generators above.

The following tools also operate on IR:

- [conjure-postman](https://github.com/palantir/conjure-postman) - generates [Postman](https://www.getpostman.com/) [Collections](https://www.getpostman.com/docs/v6/postman/collections/intro_to_collections) for interacting with Conjure defined APIs.
- conjure-backcompat - an experimental type checker that compares two IR definitions to evaluate whether they are wire format compatible (not yet open-sourced).

## Contributing

See the [CONTRIBUTING.md](/CONTRIBUTING.md) document.
