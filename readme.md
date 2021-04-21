<p align="right">
<a href="https://autorelease.general.dmz.palantir.tech/palantir/conjure"><img src="https://img.shields.io/badge/Perform%20an-Autorelease-success.svg" alt="Autorelease"></a>
</p>

<p align="center">
<img src="docs/media/conjure-flat.svg" alt="Conjure logo" height="200" width="200" id="readme-logo">

# Conjure
</p>

_Conjure is a simple but opinionated toolchain for defining APIs once and generating client/server interfaces in multiple languages._

Conjure was developed to help scale Palantir's microservice architecture - it has been battle-tested across hundreds of repos and has allowed devs to be productive in many languages.

Define your API once and then Conjure will generate idiomatic clients for Java, TypeScript, Python etc. The generated interfaces provide type-safe, clean abstractions so you can make network requests without worrying about the details.

For example in Java, Conjure interfaces allow you to build servers using existing Jersey compatible libraries like Dropwizard/Jetty.

**See an [example below](#example), or check out our [getting started](docs/getting_started.md) guide to define your first Conjure API.**


## Features
- Enables teams to work together across many languages
- Eliminates an entire class of serialization bugs
- Abstracts away low-level details behind ergonomic interfaces
- Expressive language to model your domain (enums, union types, maps, lists, sets)
- Helps preserve backwards compatibility (old clients can talk to new servers)
- Supports incremental switchover from existing JSON/HTTP servers
- Zero config (works out of the box)


## Ecosystem
The Conjure compiler reads API definitions written in the concise, [human-readable YML format](/docs/spec/conjure_definitions.md) and produces a JSON-based [intermediate representation](/docs/spec/intermediate_representation.md) (IR).

_Conjure generators_ read IR and produce code in the target language. The associated libraries provide client and server implementations. Each generator is distributed as a CLI that conforms to [RFC002](/docs/rfc/002-contract-for-conjure-generators.md):

| Language | Generator | Libraries | Examples |
|--------------------|-------------------------------|-|-|
| Java | [conjure-java](https://github.com/palantir/conjure-java) | [conjure-java-runtime](https://github.com/palantir/conjure-java-runtime) | [conjure-java-example](https://github.com/palantir/conjure-java-example) |
| TypeScript | [conjure-typescript](https://github.com/palantir/conjure-typescript) | [conjure-typescript-runtime](https://github.com/palantir/conjure-typescript-runtime) | [conjure-typescript-example](https://github.com/palantir/conjure-typescript-example) |
| Python | [conjure-python](https://github.com/palantir/conjure-python) | [conjure-python-client](https://github.com/palantir/conjure-python-client) | - |
| Rust | [conjure-rust](https://github.com/palantir/conjure-rust) | - | - |
| Go   | [conjure-go](https://github.com/palantir/conjure-go) | [conjure-go-runtime](https://github.com/palantir/conjure-go-runtime) | - |

The [gradle-conjure](https://github.com/palantir/gradle-conjure) _build tool_ is the recommended way of interacting with the Conjure ecosystem as it seamlessly orchestrates all the above tools. Alternatively, the compiler and generators may also be invoked [manually](/docs/howto/invoke_clis_manually.md#how-to-invoke-conjure-clis-manually) as they all behave in a consistent way (specified by [RFC002](/docs/rfc/002-contract-for-conjure-generators.md)).

The [conjure-verification](https://github.com/palantir/conjure-verification) tools allow Conjure generator authors to verify that their generators and libraries produce code that complies with the [wire spec](/docs/spec/wire.md).

The following tools also operate on IR:

- [conjure-postman](https://github.com/palantir/conjure-postman) - generates [Postman](https://www.learning.postman.com/) [Collections](https://learning.getpostman.com/docs/postman/collections/intro-to-collections/) for interacting with Conjure defined APIs.
- conjure-backcompat - an experimental type checker that compares two IR definitions to evaluate whether they are wire format compatible (not yet open-sourced).


## Example
The following YAML file defines a simple Flight Search API. (See [concepts](/docs/concepts.md))

```yaml
types:
  definitions:
    default-package: com.palantir.flightsearch
    objects:

      Airport:
        alias: string
      SearchRequest:
        fields:
          from: Airport
          to: Airport
          time: datetime
      SearchResult:
        alias: list<Connection>
      Connection:
        fields:
          from: Airport
          to: Airport
          number: string

services:
  FlightSearchService:
    name: Flight Search Service
    package: com.palantir.flightsearch
    base-path: /flights
    endpoints:

      search:
        docs: Returns the list of flight connections matching a given from/to/time request.
        http: POST /search
        args:
          request: SearchRequest
        returns: SearchResult

      list:
        docs: Returns flights departing from the given airport on the given day.
        http: GET /list/{airport}/{time}
        args:
          airport: Airport
          time: datetime
        returns: SearchResult
```

The following generated Java interface can be used on the client and the server.

```java
package com.palantir.flightsearch;

...

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
@Generated("com.palantir.conjure.java.services.JerseyServiceGenerator")
public interface FlightSearchService {
    /** Returns the list of flight connections matching a given from/to/time request. */
    @POST
    @Path("flights/search")
    SearchResult search(SearchRequest request);

    /** Returns flights departing from the given airport on the given day. */
    @GET
    @Path("flights/list/{airport}/{time}")
    SearchResult list(@PathParameter("airport") Airport airport, @PathParameter("time") OffsetDateTime time);
}
```

Type-safe network calls to this API can made from TypeScript as follows:

```ts
function demo(): Promise<SearchResult> {
    const request: ISearchRequest = {
        from: "LHR",
        to: "JFK",
        number: "BA117"
    };
    return new FlightSearchService(bridge).search(request);
}
```

## Contributing
See the [CONTRIBUTING.md](/CONTRIBUTING.md) document.

## License
This tooling is made available under the [Apache 2.0 License](https://github.com/palantir/conjure/blob/master/LICENSE).
<!-- intentionally not using a '/LICENSE' link because docsify always appends .md which results in a 404 -->
