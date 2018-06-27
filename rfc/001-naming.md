# RFC: Conjure naming

4 Jun 2018

Consistent names across the Conjure family of repos are important so that people can instinctively reach for the right thing and be productive across many different languages.

## Goals

All functionality in the Conjure ecosystem should be decomposed and named such that it is:

1. Easy for consumers to use (idiomatically in their chosen language).
2. Easy for consumers to file bugs or feature requests on the right repo.
3. Easy for maintainers to ensure backwards-compatibility (i.e. hard to cause unintentional compilation breaks).
4. Easy to version all artifacts in a SemVer compliant way.

A corollary of (1) is that only one repo should ever publish artifacts to one maven group (so that consumers can easily force one version).


<!--

## Example: `conjure-typescript`

Making API calls should be simple from a user's perspective:

```bash
npm install foo-api # where foo-api is a generated npm package
npm install conjure-client
```

The `foo-api` package would be produced by a _Conjure generator_:

- **Maven coordinates of executable**: `com.palantir.conjure.typescript:conjure-typescript:<version>`
- **Git repo**: [`palantir/conjure-typescript`](https://github.com/palantir/conjure-typescript)

Network requests are actually made by the `conjure-client` NPM package, which lives in a separate repo (so it can be released independently):

- **NPM package**: [`conjure-client`](https://www.npmjs.com/package/conjure-client)
- **Git repo**: [`palantir/conjure-typescript-client`](https://github.com/palantir/conjure-typescript-client)

_Note: the npm package is not called `conjure-typescript-client` because it actually works with any javascript ecosystem language - the `typescript` part would be at best redundant and at worst confusing._
 -->
## Conjure generators

_Conjure generators_ are CLIs that take Conjure JSON (IR) and output some files in another language, _&lt;lang&gt;_. Conjure generator behaviour is specified in [002-contract-for-conjure-generators.md](002-contract-for-conjure-generators.md).
Names should comply with the following:

- **Executable**: `conjure-<lang>`
- **Maven coordinates of executable**: `com.palantir.conjure.<lang>:conjure-<lang>:<version>`
- **Git repo**: `palantir/conjure-<lang>`

## Supporting libraries

It is recommended to factor out the logic to actually make and receive network calls into separate libraries so that users can upgrade these without requiring an upgrade of the actual Conjure-generated API artifact.

_Client libraries_ include code necessary to make outgoing network requests.

- **Artifact name**: `conjure-client`.  This should be idiomatic within the language but should not include the language name itself as this would be redundant. For example, `conjure_client` is acceptable if dashes are not allowed.
- **Maven coordinates** (if necessary): `com.palantir.conjure.<lang>:conjure-client:<version>`.
- **Git repo**: `conjure-<lang>-client`.

_Runtime libraries_ include functionality not specific to network requests which would otherwise be duplicated across all generated code (e.g. serialization types/utilities).

- **Artifact name**: `conjure-lib`
- **Git repo**: It is often acceptable to colocate this code with the Client library, but if a separate repo is warranted, it must include the name of the source language, e.g. `palantir/conjure-<lang>-lib`.

_Server libraries_ include code necessary to response to network requests.  No explicit guidance on naming here.

## Build tools

_Build tools_ make it convenient to use Conjure CLIs within a particular build ecosystem. [`gradle-conjure`](https://github.com/palantir/gradle-conjure) is the Palantir supported Conjure build tool:

- **Maven coordinates**: `com.palantir.gradle.conjure:gradle-conjure:<version>`
- **Gradle plugin name**: `com.palantir.conjure`.  (Including 'gradle' here would be redundant)
- **Git repo**: `gradle-conjure`.  Build tools must be well differentiated from code generators, so shouldn't use the `conjure-<foo>` pattern.

## Alternatives considered

Use a single pattern to derive repo and artifact name for generators, client libraries and runtime libraries: `conjure-<lang>(-(client|lib))?`.   This is sub-optimal for consumers, as published artifacts don't usually contain the source language.  For example, a Kotlin repo using API jars would have to depend on `conjure-java-lib`, or a pure javascript project might need to pull in `conjure-typescript-client` even if they don't want to use typescript.

