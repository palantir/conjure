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

## Published artifacts

A single repository will publish the following versioned artifacts:

* `test-cases.json` - a self-contained file containing exhaustive tests for both clients and servers.
* `verification-api.conjure.json` - clients must be generated from this conjure IR file in order to interact with the test server. This conjure definition should also contain types sufficient for deserializing the test-cases.json file.
* `verification-server` - an executable that will produce sample responses and make assertions about the generated client's requests.

## Workflow

**Prerequisites**

- ensure `test-cases.json` is available locally
- ensure clients have been generated from the `verification-api.conjure.json` IR file.

In some languages it is acceptable to check-in these files and in other languages it is more appropriate to declare a dependency on the versioned artifact and download them on demand.  We recommend downloading them on demand if possible, as this reduces the risk of contributors modifying these files.

**Users must write some language-specific test harness** - this harness must:

1. spin up the `verification-server` as an external resource. For example in JUnit, this would probably be a `@BeforeClass` method.
1. read in `test-cases.json` and invoke their client to make a network call to the running `verification-server`.
1. make one final call to the `verification-server` to check no test-cases have been missed.  In JUnit this could be an `@AfterClass` method.
1. shut down the `verification-server`.

_Note, we recommend making the harness easy to run from within your IDE so that devs have familiar tools available if they want to step through a particular test._

## `test-cases.json`

The test-cases.json file is a well-structured file that will capture behaviour and edge cases from [wire.md](https://github.com/palantir/conjure/blob/develop/wire.md), e.g.:

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

`test-cases.json` file can be deserialized using the Conjure `TestCases` type, defined in `verification-api.conjure.json`. Note that this RFC doesn't specify the exact format or contents as we'll undoubtedly want to release improvements (see the 'Versioning' section below). Key topics that will be covered include:

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

## Stateful verification-server

The verification-server must be stateful so that it can keep track of endpoints calls (and thereby which tests have passed/failed/never been run).

In addition to all the test endpoints, it must expose a special "checkNoTestCasesHaveBeenMissed" endpoint which will return a HTTP 200 response to signify that the client has passed everything, otherwise, it must return a non 2XX response. Intentional `IgnoredTestCases` (see below) may be POSTed to this endpoint in which case the verification-server will return a successful 200 response if all non-ignored tests have passed.

The server's state can be reset by restarting the server.

## Partial compliance

Ideally generators should be 100% compliant, but new generators might not comply with all the edge cases from `test-cases.json`. Generators may still be usable in this state, but it is recommended to declare any ignored tests in a well-structured `ignored-test-cases.json` file so that potential users can easily evaluate degrees of completeness.

This file should be typed according to the `IgnoredTestCases` Conjure type from `verification-api.conjure.json`.

## Where should tests be run

Ideally, these tests should run as part of continuous integration for the `conjure-<lang>` generator, but also on the `conjure-<lang>-runtime` repo (suggested in RFC 003) so that regressions are also prevented in the supporting libraries.

## Versioning

Improvements to the `test-cases.json` or `verification-server` (e.g. additional inputs) will be released as SemVer minor versions. They will only exercise functionality described in version 1 of the [Intermediate Representation](../intermediate_representation.md).
