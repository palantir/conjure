# Conjure

_Conjure is a simple but opinionated toolchain for defining APIs once and magically generating client/server interfaces in many languages._

Conjure was developed to help scale Palantir's microservice architecture - it has been battle-tested across hundreds of repos and has allowed devs to be productive in many languages.

Define your API once and then Conjure will generate idiomatic clients for Java, TypeScript, Golang, Rust etc. The generated interfaces provide type-safe, clean abstractions so you can make network requests without worrying about the details.

For example in Java, Conjure interfaces allow you to build servers using existing Jersey compatible libraries like Dropwizard/Jetty.

## Features

- Enables teams to work together across many languages
- Eliminates an entire class of serialization bugs
- Ergonomic interfaces abstract away low-level details
- Expressive language to model your domain (enums, union types, maps, lists, sets)
- Helps devs preserve backwards compatibility (old clients can talk to new servers)
- Supports incremental switchover from existing JSON/HTTP servers
- Zero config (works out of the box)

## Getting started

See our [getting started](./docs/getting_started.md) guide to define your first Conjure API.

## Example

The following YAML file defines a simple Pet Store API. (See [full reference](./docs/specification.md))

```yaml
types:
  definitions:
    default-package: com.palantir.petstore
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
    package: com.palantir.petstore
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

The following generated Java interface can be used on the client and the server.

```java
package com.palantir.petstore;

...

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
@Generated("com.palantir.conjure.java.services.JerseyServiceGenerator")
public interface PetStoreService {
    /** Add a new pet to the store */
    @POST
    @Path("pet")
    void addPet(@HeaderParam("Authorization") AuthHeader authHeader, AddPetRequest addPetRequest);
}
```

Type-safe network calls to this API can made from TypeScript as follows:

```ts
function demo(): Promise<void> {
    const request: IAddPetRequest = {
        id: 1,
        name: "Rover",
        tags: [ "mytag" ],
        status: Status.AVAILABLE
    };
    return new PetStoreService(bridge).addPet(request);
}
```

## Ecosystem

Compiler
- [conjure](https://github.com/palantir/conjure)

Build tool

- [gradle-conjure](https://github.com/palantir/gradle-conjure)

Code generators

- [conjure-java](https://github.com/palantir/conjure-java)
- [conjure-typescript](https://github.com/palantir/conjure-typescript)
- conjure-python (coming)
- conjure-go (coming soon)
- conjure-rust (coming soon)

Client libraries

- [conjure-typescript-client](https://github.com/palantir/conjure-typescript-client)
- [conjure-python-client](https://github.com/palantir/conjure-python-client)
- [http-remoting](https://github.com/palantir/http-remoting)

Recommended server libraries

- [dropwizard](https://github.com/dropwizard/dropwizard)

## Contributing

See the [CONTRIBUTING.md](./CONTRIBUTING.md) document.
