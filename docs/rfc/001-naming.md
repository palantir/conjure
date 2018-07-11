# RFC: Conjure naming

4 Jun 2018

Consistent names across the Conjure family of repos are important so that people can instinctively reach for the right thing and be productive across many different languages.

## Conjure generators

_Conjure generators_ are CLIs that take Conjure JSON (IR) and output some files in another language, _&lt;lang&gt;_. Conjure generator behaviour is specified in [002-contract-for-conjure-generators.md](002-contract-for-conjure-generators.md).
Names should comply with the following:

- **Executable**: `conjure-<lang>`
- **Maven coordinates of executable**: `com.palantir.conjure.<lang>:conjure-<lang>:<version>`
- **Git repo**: `palantir/conjure-<lang>`

## Build tools

_Build tools_ make it convenient to use Conjure CLIs within a particular build ecosystem. [`gradle-conjure`](https://github.com/palantir/gradle-conjure) is the Palantir supported Conjure build tool:

- **Maven coordinates**: `com.palantir.gradle.conjure:gradle-conjure:<version>`
- **Gradle plugin name**: `com.palantir.conjure`.  (Including 'gradle' here would be redundant)
- **Git repo**: `gradle-conjure`.

Build tools must be well differentiated from code generators, so shouldn't use the `conjure-<foo>` pattern.
