# RFC: Consistent test cases

5 Jul 2018

_Currently, conjure-generators all use slightly different test-cases, each covering different edge cases. This makes code reviews tricky as it isn't always self-evident whether a change will maintain wire-spec compatibility. This RFC proposes a single tool to exhaustively test that all clients and servers adhere to a common wire spec, defined in [wire.md](https://github.com/palantir/conjure/blob/develop/wire.md)._

## Goals

1. **Fearless code reviews** - an existing conjure-generator can be drastically refactored and still demonstrate that it will maintain wire compatibility with other conjure-generated clients/servers.
2. **Eliminate duplicated work** - maintainers no longer need to try to think of all possible edge cases when testing a conjure-generator, these should be exhaustively defined in one place.
3. **Easy to run the tests** - it should be easy for contributors to run the tests locally and on CI.
4. **Easy to update the tests** - the master test cases should have a compact, human-readable definition and should be published centrally so that consumers can easily upgrade.

## Types of compliance

A single generator may generate just clients or servers, so these will be tested separately:

1. clients
    - clients must understand spec-compliant server responses
    - clients must send spec-compliant requests
    - clients must reject non-compliant server responses
2. servers
    - servers must understand spec-compliant requests
    - servers must return spec-compliant responses
    - servers must reject non-compliant requests

_Note, there isn't a 'serialization' category here because client-side and server-side serialization and deserialization are subtly different. For example, clients must tolerate extra fields in a server response (otherwise server authors would never be able to add new functionality). Servers on the other hand may reject unknown fields in a JSON request body as this is likely a programmer error._

## Proposal

A single repository should publish the following versioned artifacts:

* `test-cases.yml` - a self-contained file containing exhaustive tests for both clients and servers
* `test-api.conjure.json` - clients must be generated from this conjure IR in order to interact with the test server. This conjure definition should also contain types sufficient for deserializing the test-cases.yml file.
* `compliance-server` - an executable that will send sample responses and make assertions about the generated client's requests
* `compliance-client` - an executable that will send sample requests and make assertions about the generated server's responses

## Running the tests

Contributors should be able to run the tests from within their IDE using a language-native test harness.  For example, for conjure-java, the tests should be runnable from JUnit inside IntelliJ.

It is recommended that setting up the tests for the first time requires minimal hand-written network requests - ideally, these should just be loaded from the master test-cases.yml file to facilitate easy updates.

## `test-cases.yml`

The test-cases.yml file should be well-typed and easy to deserialize. It should capture behaviour and edge cases from [wire.md](https://github.com/palantir/conjure/blob/develop/wire.md):

* primitives (string, boolean, datetime, safelong, etc)
* collection types (list, set, map, optional)
* complex types (object, union, enum)
* binary uploads and responses, binary fields and path params
* auth types (header, none, cookie)
* headers
* http methods (GET, POST, DELETE)
* path parameters
* coercing from absent fields / null fields
* invalid JSON
* set uniqueness, duplicate map keys

## Client verification using the compliance-server

To prove compliance, a conjure-generator should generate objects and client interfaces for a special IR file: `test-api.conjure.json`. These generated clients will be used to make network requests to the compliance-server.

The compliance-server has a few responsibilities:

* it should issue a variety of both valid and invalid responses to exercise the client deserialization code
* it should make detailed assertions about incoming requests from the generated client
* accumulate statistics about passed/failed tests

To verify a client's serialization and deserialization logic, the client under test should repeatedly make requests to a series of GET endpoints on the compliance-server. The compliance-server will return successive valid and then invalid JSON responses (in a well-defined order). The client should deserialize the response body for the positive tests, then re-serialize this and echo it back to the server as a POST request.  For the non-compliant server responses (negative cases), the client should error and the test harness should notify the compliance-server of the expected deserialization failure.

More intricate requests may need to be constructed manually.

## Server verification using the compliance-client

TODO

## Degrees of compliance

Ideally generators should be 100% compliant, but new generators might not comply with all the edge cases from `test-cases.yml`. The generator may still be usable in this state, but these caveats should be called out explicitly so that users can assess its completeness.

## Where should tests be run

Ideally, this test-suite should be run as part of CI for the `conjure-<lang>` generator, but also on the `conjure-<lang>-runtime` repo (suggested in RFC 003) so that regressions are also prevented in the supporting libraries.
