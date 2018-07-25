# Conjure
_Conjure is a simple but opinionated toolchain for defining APIs once and magically generating client/server interfaces in many languages._

Conjure was developed to help scale Palantir's microservice architecture - it has been battle-tested across hundreds of repos and has allowed devs to be productive in many languages.

Define your API once and then Conjure will generate idiomatic clients for Java, TypeScript, Golang, Rust etc. The generated interfaces provide type-safe, clean abstractions so you can make network requests without worrying about the details.

For example in Java, Conjure interfaces allow you to build servers using existing Jersey compatible libraries like Dropwizard/Jetty.

See our [getting started](./docs/getting_started.md) guide to define your first Conjure API.

## Features
- Enables teams to work together across many languages
- Eliminates an entire class of serialization bugs
- Ergonomic interfaces abstract away low-level details
- Expressive language to model your domain (enums, union types, maps, lists, sets)
- Helps devs preserve backwards compatibility (old clients can talk to new servers)
- Supports incremental switchover from existing JSON/HTTP servers
- Zero config (works out of the box)


## Concepts
_Conjure offers the following abstractions to use when defining your APIs. To see the JSON representation of these types, see the [Wire specification][]._
[Wire specification]: (/docs/spec/wire.md)


### HTTP endpoints
- `GET`, `PUT`, `POST`, `DELETE` - [HTTP methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods).
- _Query parameters_ - e.g. `https://example.com/api/something?foo=bar&baz=2`
- _Path parameters_ - Parsed sections of URLs e.g. `https://example.com/repo/{owner}/{repo}/pulls/{id}`
- _Headers_ - A non-case sensitive string name associated with a Conjure value (see [docs](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers)).
- _Cookie auth_ - A [HTTP Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies), often used for authentication.


### Named types
Users may define the following kinds of named types. These can be referenced by their name elsewhere in a Conjure definition.

  - _Object_ - a collection of named fields, each of which has their own Conjure type.
  - _Enum_ - a type consisting of named string variants, e.g. "RED", "GREEN", "BLUE".
  - _Alias_ - a named shorthand for another Conjure type, purely for readability.
  - _Union_ - a type representing different named variants, each of which can contain differen types. (Also known as 'algebraic data types' or 'tagged unions')

### Container types
  - `list<T>` - an ordered sequence of items of type `T`.
  - `map<K, V>` - values of type `V` each indexed by a unique key of type `K` (keys are unordered).
  - `optional<T>` - represents a value of type `T` which is either present or not present.
  - `set<T>` - a collection of distinct values of type `T`.

### Built-in types
  - `any` - a catch-all type which can represent arbitrary JSON including lists, maps, strings, numbers or booleans.
  - `bearertoken` - a string [Json Web Token (JWT)](https://jwt.io/)
  - `binary` - a sequence of binary.
  - `boolean` - `true` or `false`
  - `datetime` - an [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) value e.g. `2018-07-25T10:20:32+01:00`
  - `double` - a floating point number specified by [IEEE 754](https://ieeexplore.ieee.org/document/4610935/), which includes also be NaN, +/-Infinity and signed zero.
  - `integer` - a signed 32-bit integer value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
  - `rid` - a [Resource Identifier](https://github.com/palantir/resource-identifier), e.g. `ri.recipes.main.ingredient.1234`
  - `safelong` - a signed 53-bit integer that can be safely represented by browsers without loss of precision, value ranges from -2<sup>53</sup> to 2<sup>53</sup> - 1
  - `string` - a sequence of UTF-8 characters
  - `uuid` - a 128-bit number: [Universally Unique Identifier](https://en.wikipedia.org/wiki/Universally_unique_identifier#Versions) (aka guid)

### Opaque types
When migrating an existing API, it may be useful to use the following 'escape hatch' type.  These are not recommended for normal APIs because Conjure can't introspect these external types to figure out what their JSON structure will be, so they're effectively opaque.

  - _External Reference_ - a reference to a non-Conjure type, with an associated fallback Conjure type

### Errors
Conjure allows defining named, structured errors so that clients can expect specific pieces of information on failure or handle entire categories of problems in one fell swoop.

- _Structured errors_ - have the following properties:
  - _Name_ -  a user chosen description of the error e.g. `RecipeLocked`
  - _Namespace_ - a user chosen category of the error e.g. `RecipeErrors`
  - _Code_ - one of the following pre-defined categories
  - _Args_ - a map from string keys to Conjure types


## Example
The following YAML file defines a simple Pet Store API. (See [full reference](./docs/spec/source_files.md))

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
