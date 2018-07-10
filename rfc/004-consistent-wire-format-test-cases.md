# RFC: Consistent wire-format test cases

5 Jul 2018

_Currently, conjure-generators all use slightly different test-cases, each covering different edge cases. This makes code reviews tricky as it isn't always self-evident whether a change will maintain wire compatibility. This RFC proposes a single tool to test that clients adhere to a common wire spec, defined in [wire.md](https://github.com/palantir/conjure/blob/develop/wire.md). Verifying server compliance will be tackled in a separate, dedicated RFC._

## Goals

1. **Fearless code reviews** - a conjure-generator can be drastically refactored and still demonstrate that it will maintain wire compatibility.
2. **Eliminate duplicated work** - maintainers no longer need to try to think of all possible edge cases when testing a conjure-generator or client, these should be exhaustively defined in one place.
3. **Easy to use** - it should be easy for contributors to verify their generator is compliant locally and on CI.
4. **Easy to update the tests** - the master test cases should have a compact, human-readable definition and should be published centrally so that consumers can easily upgrade.

## Types of compliance

To be fully compliant, a Conjure-generated client must:

  - understand spec-compliant server responses
  - always send spec-compliant requests
  - reject non-compliant server responses

_Note, there isn't a 'serialization' category here because client-side and server-side serialization and deserialization are subtly different. For example, clients must tolerate extra fields in a server response (otherwise server authors would never be able to add new functionality). Servers on the other hand may reject unknown fields in a JSON request body as this is likely a programmer error._

## Proposal

A single repository should publish the following versioned artifacts:

* `test-cases.json` - a self-contained file containing exhaustive tests for both clients and servers.
* `verification-api.conjure.json` - clients must be generated from this conjure IR file in order to interact with the test server. This conjure definition should also contain types sufficient for deserializing the test-cases.json file.
* `verification-server` - an executable that will produce sample responses and make assertions about the generated client's requests.

## Running the tests

Contributors should be able to run the tests from within their IDE using a language-native test harness.  For example, for conjure-java, the tests should be runnable from JUnit inside IntelliJ.

It is recommended that setting up the tests for the first time requires minimal hand-written network requests - ideally, these should just be loaded from the master `test-cases.json` file to facilitate easy updates.

## `test-cases.json`

The test-cases.json file should be well-structured (i.e. can be deserialized into a Conjure-defiend type). It should capture behaviour and edge cases from [wire.md](https://github.com/palantir/conjure/blob/develop/wire.md), e.g.:

* primitives (string, boolean, datetime, safelong, etc)
* collection types (list, set, map, optional)
* complex types (object, union, enum)
* binary uploads and responses, binary fields and path params
* auth types (header, none, cookie)
* headers
* http methods (GET, PUT, POST, DELETE)
* path and query parameters
* coercing from absent fields / null fields
* invalid JSON
* set uniqueness, duplicate map keys

This RFC doesn't specify the exact format or contents as we'll undoubtedly want to release improvements (see the 'Versioning' secion below).  Here is an illustrative example of some YAML test-cases, before conversion to JSON:

```yaml
client:
  autoDeserialize:
    ...
    receiveSafeLongExample:
      positive:
        - '{"value":-9007199254740991}'
        - '{"value":0}'
        - '{"value":9007199254740991}'
      negative:
        - '{"value":null}'
        - '{}'
        - '{"value":-9007199254740992}'
        - '{"value":9007199254740992}'
        - '{"value":1.23}'
        - '{"value":"12"}'
```

## Client verification using verification-server

To prove client-side compliance, a conjure-generator must generate objects and client interfaces from the master IR file: `verification-api.conjure.json`. These generated clients will be used to make network requests to the verification-server.

The verification-server has a few responsibilities:

* it must issue a variety of both valid and invalid responses to exercise the client deserialization code
* it must make detailed assertions about incoming requests from the generated client
* accumulate statistics about passed/failed tests

Some test cases can be automated easily, but some will need to be constructed manually:

1. For basic serialization cases, client under test should repeatedly make requests to a series of GET endpoints on the verification-server. The verification-server will return successive valid and then invalid JSON responses (in a well-defined order). The client should deserialize the response body for the positive tests, then re-serialize this and send it back to the server's 'confirm' endpoint as a POST request.  For the non-compliant server responses (negative cases), the client should error and the test harness should notify the verification-server of the expected deserialization failure.
2. For more complicated test cases, network calls will need to be constructed manually

## Degrees of compliance

Ideally generators should be 100% compliant, but new generators might not comply with all the edge cases from `test-cases.json`. The generator may still be usable in this state, but these caveats should be called out explicitly so that users can assess its completeness.

## Where should tests be run

Ideally, this test-suite should be run as part of CI for the `conjure-<lang>` generator, but also on the `conjure-<lang>-runtime` repo (suggested in RFC 003) so that regressions are also prevented in the supporting libraries.

## Versioning

Improvements to the `test-cases.json` (e.g. additional inputs) will be released as SemVer minor versions. They will only exercise functionality described in version 1 of the [Intermediate Representation](../intermediate_representation.md).

Changing the names of endpoints or services in `verification-api.conjure.json` will not be released as a major version.
