# Conjure

<!-- tagline -->

interface and objects for RESTy APIS
interface definition language (IDL) for JSON over HTTP microservices
codegen clients for your JSON/HTTP servers
codegen RESTful clients and servers
polyglot / multi-language
magically bridging clients and servers across language divide


<!-- 1 paragraph what/why -->

simple and opinionated but based on stable, mature standards
enables devs to be productive  of microservices across many repos and languages
easily responsibly scale your microservices
type safe development
end to end framework
battle tested / proved in production
allows many microservices to coexist
easy & approachable

## Features

- Write once, generate anywhere (TM)
- Zero config (works out of the box)
- Supports incremental adoption from existing JSON/HTTP servers
- Devs don't have to think about low-level JSON wire format
- Think in domain language - Abstract away your low-level JSON wire format
- Expressive language to model your domain (enums, union types, maps, lists, sets)
- Ergonomic/idiomatic client and server interface
- Helps devs preserve backwards compatibility (old clients can talk to new servers)

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

example Conjure java
example Conjure typescript


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

