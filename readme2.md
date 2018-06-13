Magically generate multi-language HTTP/JSON clients and servers.

# Conjure

_Conjure is a simple but opinionated toolchain for defining APIs once and magically generating client/server interfaces in many languages._

Conjure was developed to help scale Palantir's microservice architecture - it has been battle-tested in hundreds of microservices and has allowed devs to be productive in many languages across many repos.

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

## Example

```yml
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

```js
PetStoreService petStoreService = JaxRsClients.create(..., PetStoreService.class);
petStoreService.addPet(AddPetRequest.builder()
    .id(1)
    .name("Rover")
    .tags(Tag.of("mytag"))
    .status(Status.AVAILABLE)
    .build());
```


## Motivation/history




## Architecture / How it works?

- gradle-conjure
- What is IR
- conjure-java
- conjure-typescript
- conjure-python

Extensibility

## Contributing

- What is an appropriate github issue
- How to set up your dev environment, recommended IDE, forking, tests, CircleCI etc
- what to use stackoverflow for?

