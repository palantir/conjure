## Compiler Usage

The recommended way to use conjure is via a build tool like [gradle-conjure](https://github.com/palantir/gradle-conjure). However, if you don't want to use gradle-conjure, there is also an executable published on [bintray](https://bintray.com/palantir/releases/conjure).

    Usage: conjure compile [-hV] <input> <output>
    Generate Conjure IR from Conjure YML definitions.
          <input>     Path to the input conjure YML definition file, or directory containing multiple such files.
          <output>    Path to the output IR file.
      -h, --help      Show this help message and exit.
      -V, --version   Print version information and exit.
