# How to invoke Conjure CLIs manually
The recommended way to use Conjure is via a build tool like [gradle-conjure](https://github.com/palantir/gradle-conjure). However, if you don't want to use gradle-conjure, all the Conjure ecosystems CLIs can be invoked manually - there are executables published on [maven central](https://search.maven.org/artifact/com.palantir.conjure/conjure).

## 1. Install the CLIs

Download the [`conjure`](https://search.maven.org/artifact/com.palantir.conjure/conjure) TGZ and any generators you want to use:

- [conjure-java](https://search.maven.org/artifact/com.palantir.conjure.java/conjure-java)
- [conjure-typescript](https://search.maven.org/artifact/com.palantir.conjure.typescript/conjure-typescript)
- [conjure-python](https://search.maven.org/artifact/com.palantir.conjure.python/conjure-python)
- [conjure-postman](https://search.maven.org/artifact/com.palantir.conjure.postman/conjure-postman)

Each of these TGZs contain an executable located at `<name>-<version>/bin/<name>`:

    $ tar -xvf conjure-4.4.0.tgz
    $ tree conjure-4.4.0/
    conjure-4.4.0
    ├── bin
    │   ├── conjure
    │   └── conjure.bat
    └── lib
        ├── animal-sniffer-annotations-1.14.jar
        ├── aopalliance-repackaged-2.5.0-b32.jar
        ├── auth-tokens-3.0.1.jar
        ├── checker-compat-qual-2.0.0.jar
        ...

The `conjure` executable can be invoked as follows:

    $ ./conjure-4.4.0/bin/conjure
    Usage: conjure [-hV] [COMMAND]
    CLI to generate Conjure IR from Conjure YML definitions.
    -h, --help      Show this help message and exit.
    -V, --version   Print version information and exit.
    Commands:
    compile  Generate Conjure IR from Conjure YML definitions.

## 2. Write a YML file

Create a YML file, e.g. `demo.yml` and write a valid Conjure Definition, e.g.:

```yaml
types:
  definitions:
    default-package: com.yourname.hello
    objects:

      Greeting:
        fields:
          from: string
          to: string
```

*Refer to the [Conjure Definitions](/docs/spec/conjure_definitions.md) specification for all possible options.*

## 3. Compile IR

Invoke the `conjure` CLI to compile your YML file. `conjure` will output an [intermediate representation](/docs/spec/intermediate_representation.md) JSON file which can then be passed into [generators](/readme.md#ecosystem) to create language bindings:

    $ ./conjure-4.4.0/bin/conjure compile demo.yml demo.conjure.json
    $ cat demo.conjure.json
    {
      "version" : 1,
      "errors" : [ ],
      "types" : [ {
        "type" : "object",
        "object" : {
        "typeName" : {
          "name" : "Greeting",
          "package" : "com.yourname.hello"
        },
    ...

## 4. Generate code

The `demo.conjure.json` IR JSON file can now be passed to generators, which will write files to a fresh directory:

    $ mkdir output
    $ conjure-typescript generate --rawSource demo.conjure.json output
    $ tree output
    output
    ├── hello
    │   ├── greeting.ts
    │   └── index.ts
    └── index.ts

    1 directory, 3 files
