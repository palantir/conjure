# RFC: Contract for Conjure generators

11 Jun 2018

Conjure generators should conform to a well-defined contract so that a single build tool (e.g. gradle-conjure) can easily support new generators.

## Distribution

Conjure generators may be distributed as a tgz containing a platform independent executable. For example, the archive conjure-java-0.2.3.tgz unpacks to the following structure:

```
conjure-java-0.2.3
├── bin
│   ├── conjure-java
│   └── conjure-java.bat
└── lib
    ├── animal-sniffer-annotations-1.14.jar
    ├── auth-tokens-3.0.1.jar
    ├── commons-cli-1.2.jar
    ...
```

`conjure-<lang>-<version>/bin/conjure-<lang>` should be a script that works on Mac / Linux. There is currently no requirement to run on Windows, but this may change in the future.

## Primary command

Conjure generators should expect to be called with the command `generate` as the first argument. They read a single JSON file, specified by the positional argument `<input-json>` and write to the `<ouput-directory>` folder. They must exit 0 on success, or a non-zero code if they failed.  Errors must be written to stderr.

```
conjure-<lang> generate <input-json> <output-directory>
```

Generators can assume that this output directory already exists and is empty. (It is not the responsibility of conjure generators to do any diffing or up-to-date checking - this should be done by a higher level build tool, like [gradle-conjure](https://github.com/palantir/gradle-conjure).)

Generators should not produce `.gitignore` files or any files that would only make sense if checked in to a repository.

Generators are free to implement any other behaviour if the `generate` command is not invoked.

## Options

**Allow arbitrary `--key=value` options and `--foo` flags, for example:**

```
conjure-typescript --version="0.1.0" --goFastMode
```

Ideally, Conjure generators should be zero-config CLIs - they should work out of the box so that users don't have to trawl through documentation in order to generate usable code.  However, some limited configurability is sometimes necessary.  For example, conjure-typescript produces a package.json file that must contain author, license and version fields (otherwise it would be very inconvenient to publish), but these clearly need to be specified by the user.

Conjure generators may expect command line options of the form `--<key>=<value>` where the key is a camelCase string that matches the regex `[a-z][a-zA-Z0-9]*`. Values may contain spaces. Generators should reject duplicate `<key>`s. Unknown keys should be ignored.

Flags such as `--foo` may also be passed to switch on some functionality, or `--foo=false` to switch it off.

### Alternatives considered

1. **pass arbitrary JSON to the CLI.** - This would satisfy the requirements, but the ease of passing complex options (e.g. lists, maps etc) may actually encourage Conjure generator authors to stray from the 'zero-config' recommendation and build increasingly complicated CLIs.
2. **plain on/off feature flags: GO_FAST, PRESERVE_DINGBATS** - This is aesthetically very attractive, but doesn't support some essential requirements of conjure-typescript, e.g. license, version, author (this must be an arbitrary string, it's not a boolean on/off flag).
3. **configurability via environment variables** - This is less intuitive than the `--key=value` options.

### Example interface from gradle-conjure

```groovy
conjure {
  typescript {
    version = '0.1.0'
    author = 'dave'
    license = 'MIT'
    goFastMode = true
  }
  java {
    license = 'MIT'
    retrofitCompletableFutures = true
  }
}
```
