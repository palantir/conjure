[![Circle CI](https://circle.palantir.build/gh/foundry/conjure.svg?style=shield)](https://circle.palantir.build/gh/elements/conjure)
[![Release](https://shields.palantir.build/artifactory/internal-jar-release/release/com.palantir.conjure/conjure-core/svg)](https://artifactory.palantir.build/artifactory/webapp/#/artifacts/browse/tree/General/internal-jar-release/com/palantir/conjure)
[![Snapshot](https://shields.palantir.build/artifactory/internal-jar-snapshot/snapshot/com.palantir.conjure/conjure-core/svg)](https://artifactory.palantir.build/artifactory/webapp/#/artifacts/browse/tree/General/internal-jar-snapshot/com/palantir/conjure)

Conjure
=======
An interface and object definition language and code generator for RESTy APIs.

Conjure helps define API contracts for HTTP services, and generates clean
interfaces and code for both servers and clients in a number of languages. Its
primary goal is to provide mechanisms for decoupling server implementation
details from client definitions.

Despite acting as a client generator, Conjure does not attempt to provide more
than renderable interfaces for consumer services to use, its goal is to remain
agnostic to client implementations while providing general language bindings for
use in client creation.

[Spellbook](http://spellbook.il.palantir.global) hosts Conjure files in a
readable, searchable format. Register your own Conjure files on Spellbook
by following the [submission instructions](https://github.palantir.build/foundry/spellbook).

Contents:

 - [Rationale](#why)
 - [Types](#types)
   - [Primitives](#primitives)
   - [Built-ins](#built-ins)
   - [Imported Types](#imported-types)
   - [(Experimental) Imported Conjure Types](#experimental-imported-conjure-types)
   - [Packages](#packages)
   - [Defined Types](#defined-types)
 - [Services](#services)
   - [Endpoints](#endpoints)
 - [Gradle Plugin](#gradle-plugin)
   - [Compilation](#conjure-compilation)
   - [TypeScript Publishing](#typescript-publishing)

Why?
----
The rationale for maintaining Conjure rather than picking an existing IDL (e.g., Swagger) for Elements (and other)
products is as follows. For the first handful of Elements projects (in particular, Gatekeeper, Compass, Foundry), we
picked JAX-RS as the API contract between servers (Jersey) and clients (Feign) since it offered the best compromise
between usability (simple Java proxies, straight-forward server implementations), dependency management (separation
between API definitions and their implementations), and interoperability (JSON as the lingua franca for both
backend-to-backend and frontend-to-backend communication, and the ability to interrogate with services via different
languages or even cURL). The idiosyncratic specifics of our interpretation of JAX-RS contracts are encoded in the
`http-remoting` libraries.

Given what we have since learned about the disadvantages of JAX-RS and the advantages of alternative RPC mechanisms
(e.g., gRPC), one may be inclined to consider this a historic mistake, at least for backend RPC; on the other hand, it
has allowed us to make swift progress on frontends and it has opened the door for simple service composition, in
particular for non-Elements and non-Java projects such as Foundry Mobile. Either way, it is the status quo. Since major
API breaks are currently a deal-breaker, we are effectively presented with two choices: either continue to specify APIs
in a Java-centric way via JAX-RS, or retrofit an IDL to our particular interpretation of the JAX-RS wire format. Conjure
is an experiment towards the latter choice and has the following advantages:

- Conjure simplifies non-Java implementations of API servers and clients. The Typescript bindings are a great example.
- Conjure allows us to specify the wire format independently of existing client/server libraries (e.g., Jersey).
- Conjure reduces the binary coupling to particular Java features. For example, a Conjure API can be compiled into
  Java7-compatible client code with Guava Optional types, or into Java8 code with Java8 Optionals.
- Value types are very simple to specify.
- Conjure can act as a bridge format to some other IDL we may eventually want to use. For example, Conjure can likely
  be compiled into Protobuf if we ever wanted to move our service implementations to gRPC.

Of course there are downsides. To mention just two, you cannot Google for Conjure problems, and someone has to maintain
Conjure and keep up with feature requests and bug fixes.

Types
-----
Conjure's type system deals with two modes of types: *imported types*, which are
defined outside of Conjure definition files but declared as explicit imports
and given 'local' names for use in a Conjure file, and *defined types*, which are
fully specified by primitives, built-ins and types defined in a Conjure file.

By convention, all primitive and built-in types use camel case, while all
user-defined types (e.g. imports and object definitions) use pascal case.

### Rules
* Type names must be unique with case-insensitive comparison. For example,
  it is not legal for an imported type and an enum type to have the same type
  name, even though the Conjure JSON format itself would not prevent this.
* Types cannot have the same name as any of the Conjure primitives or built-ins.
* The casing of a type may be changed in generated files to conform with the
  language specification of the generated code. The requirement that all type
  names be unique in a case-insensitive comparison guarantees that changing the
  case will not introduce collisions.
* Type names must contain only alphanumeric characters and underscores and must
  start with a letter ([A-Za-z]).

### Primitives
Base types in the system are:
 * `string`
 * `integer` (32-bit signed whole numbers)
 * `double` (64-bit floating point numbers)
 * `boolean` `(true|false)`

### Built-ins
Conjure offers several built-ins to assist with mapping to existing language
constructs and to simplify API building. These types are genericized by other
defined Conjure types, which may also be built-ins (a Map of Maps is allowed):
 * `any` (placeholder for unknown type)
 * `map<K, V>`: a map of `K` to `V`; since Conjure generates JSON serializable
   objects, `K` is generally restricted to `string` (though this is not strictly
   enforced to allow for external types to be present as a key type).
 * `list<T>`: a list of `T`.
 * `set<T>`: a set of `T`.
 * `optional<T>`: an optional type to help prevent nullity. Optional types will
   serialize as null when absent and as a concrete non-null value when present.
   The empty string and the empty map/object are valid non-null values.
 * `binary`: a sequence of bytes. When present as a return value for a service
   method, encodes an output stream.
 * `safelong`: a wrapper around a long type which enforces the value is safely
   representable as an integer in javascript (in javascript, between
   `Number.MIN_SAFE_INTEGER and Number.MAX_SAFE_INTEGER`).
 * `datetime`: a date/time capable of precisely representing an instant in
   human readable time, encoded as a string using ISO-8601 with required offset
   and optional zone id. Second-precision is required, formats may include up to
   nanosecond precision. Implementations must support handling the received
   string or precisely modeling the received human readable time with
   nanosecond precision. e.g. all of these are equivalent:
    * `2017-01-02T03:04:05Z`
    * `2017-01-02T03:04:05.000Z`
    * `2017-01-02T03:04:05.000000Z`
    * `2017-01-02T03:04:05.000000000Z`
    * `2017-01-02T04:04:05.000000000+01:00`
    * `2017-01-02T05:04:05.000000000+02:00`
    * `2017-01-02T04:04:05.000000000+01:00[Europe/Berlin]`

### Imported Types
External types, or imports, consist of references to types defined outside
the context of a Conjure file (possibly in other Conjure files, possibly
in external systems). A `base-type` is provided as a hint to generators for
how to handle this type when no external type reference is provided. Note
that the `base-type` fallback mechanism will only work if the referenced type
is serialized as a primitive -- if the imported type is a non-primitive JSON
object, then even if a generator falls back on the `base-type` the resulting
APIs will not handle serialization correctly.

References to external types are encoded in a language-specific map and must
refer to the fully qualified type name.

Type aliases should use Pascal case.

#### Imports Example

```yaml
# imports is a map from local type alias to an external type definition.
imports:
  ResourceIdentifier:
    # A primitive type.
    base-type: string
    # A map of language name to a more refined type name (used as a hint)
    # in that language. Renderers may choose to ignore the hint even if
    # it is provided. (By default, Java renderers respect `java` keyed
    # hints.)
    external:
      java: com.palantir.ri.ResourceIdentifier
```

### (Experimental) Imported Conjure Types
conjure-java has experimental support for importing Conjure types (not services) from other Conjure files. Note that the
syntax and semantics may be subject to change in future releases.

At the compiler level, an import is defined via a namespace and a relative or absolute path to the imported file, for
example:

```yaml
# Importing definition: my-service.yml
types:
  conjure-imports:
    importedTypes: path/to/imported-conjure-definition.yml
    # ... other imports
  definitions:
    default-package: test.a.api
    objects:

services:
  TestServiceA:
    name: Test Service A
    package: test.a
    endpoints:
      get:
        http: GET /get
        args:
          object: importedTypes.SimpleObject
```

```yaml
# Imported definition: path/to/imported-conjure-definition.yml
types:
  definitions:
    default-package: test.b.api
    objects:
      SimpleObject:
        fields:
          stringField: string
```

Note that the imported object is referenced as `<namespace>.<type-name>`.

The Conjure Gradle plugin facilitates procurement of imported files as follows:

```yaml
conjure {
    # The conjureImports field accepts arbitrary Gradle `FileCollection`s, for example,
    # `project.files(...)`, `project(':other-project').files(...)`, etc
    conjureImports files('external-import.yml')
}
```

For the compilation step, the files specified in `conjureImports` are made available in the `external-imports/`
directory relative to each Conjure definition in `src/main/conjure`. In this example, the file `external-import.yml` can
be imported by any file in `src/main/conjure` as:

```yaml
types:
  conjure-imports:
    foo: external-imports/external-import.yml
```

Furthermore, every file in the `src/main/conjure` source set is made available in the same directory and can be imported
as:
```yaml
types:
  conjure-imports:
    bar: other-def.yml   # made available from `src/main/conjure`
```

### Packages
Package names are used by generator implementations to determine the output
location and language-specific namespacing. Package names should follow the
Java style naming convention: `com.example.name`.

This manifests in the generated languages as follows:

* Java - the Conjure package name is the Java package name
* TypeScript - the folder containing the object is the Conjure package
name with the first two segments removed (this is done for brevity).
For example: `com.example.folder1.folder2 -> folder1/folder2`

Packages are not used for namespacing purposes within a Conjure file -- all
type names must be unique within a Conjure file, even across different types
(e.g. an object definition and an enum definition).

### Defined Types
Defined types consist of objects completely specified within the context of a
Conjure file (possibly referencing other type aliases, primitive types,
and built-ins). These types are *Object Definitions*, *Enum Definitions* or
*Alias Definitions*. These types of objects consist of, minimally, a type
alias, an optional package name and optional documentation.

Conjure renderers will use the type alias as the object name, and will emit
the object definition in the defined package or fall-back to the
definitions-wide default package.

Type names, as above, should use Pascal case.

#### Object Definitions
Each object definition consists of a type alias, an optional package, and a
map of field names to field definitions.

Each object includes a fields definition block, which is a map of field name to
a type definition. Simple definitions that omit documentation may simply set
the value to the type:

```yaml
[field]: [type]
```

Docs may be included on this type by using the long form:

```yaml
[field]:
  type: [type]
  docs: [docs]
```

Where docs is a standard string and generally treated throughout rendering as
Markdown. Use YAML multiline-strings to help with formatting.

The fields in a object definition can be primitives, built-ins, imports, type
aliases or other objects. User-defined types can be used in fields before they
are defined. If the type for a field is an object type, then the field is assumed
to be non-`null`/`nil` -- if it is possible for a field to be absent, this should
be encoded explicitly by specifying it within an `optional<>` built-in. This means
that an object type definition can only be recursive if the type reference to
itself is part of an `optional`, `list`, `set` or `map`.

Field names must appear in either camel case or kebab case. Type renderers
will respect casing for wire format, but may convert case formats to conform
with language restrictions. As a result, field names must be unique independent
of case format (e.g. an object may not define both `caseFormat` and
`case-format` as fields).

Technically, any valid JSON string can be used as a JSON key, so the restriction
that all field names must be camel case or kebab case means that there are
specifications for valid JSON objects that cannot be generated by Conjure. This
has not yet been an issue, but please file an issue if this is something that you
encounter that causes pain. In the mean time, this can be worked around by
defining an imported type that imports a compatible type that is defined externally.

#### Enum Definitions
Each enum definition consists of a type alias, an optional package, and a list
of valid enumeration values. Values _must not_ include the special value `UNKNOWN`,
which is reserved to represent an enumeration value that was found but unexpected
in generated object code. Enumeration values are serialized as strings in JSON.

Enum values must be uppercase words separated by underscores.

Each enum includes a values definition block, which is a list of the valid
values:

```yaml
values:
 - [value]
 - [another value]
```

The values specified in the values list must be unique.

#### Alias Definitions
As a convenience, Conjure offers the ability to alias primitive types so that
stronger types may be carried throughout generated code, though not all generators
support this feature, and will fallback to replacing the aliased type with its
concrete type.

Each alias type appears in the definitions block and may include an optional
docs block and a mandatory alias type:

```yaml
alias: <primitive type>
docs: optional docs
```

#### Examples
See also:
 * [Example Type Definitions](conjure-java/src/test/resources/example-types.yml)
 * [Example Generated Java Code](conjure-java/src/test/resources/test/api)
 * [Example Generated TypeScript Code](conjure-typescript/src/test/resources/types)

```yaml
# Definitions for object types to render as part of code generation.
definitions:
  # definitions-wide default package
  default-package: com.palantir.foundry.catalog.api

  # objects is a map from type alias (used in service definitions and
  # other types) to an object type definition.
  objects:
    BackingFileSystem:
      # Override the default package specified at the definitions level.
      package: com.palantir.foundry.catalog.api.datasets

      # Optional Docs
      docs: Optional Docs

      # A map of field name to a type definition.
      fields:
        # example of a field with docs:
        fileSystemId:
          type: string
          docs: The name by which this file system is identified.
        # example of a simple field:
        baseUri: string
        # parameterizing a built-in generic
        configuration: map<string, string>

    ExampleEnumeration:
      # enumerations have a values block instead of a fields block
      values:
        - A
        - B

    ExampleAlias:
      # alias types have an alias block instead of values or fields
      alias: string
```

Services
--------
Services are collections of endpoints that leverage defined types. Conjure
currently provides options for tuning the HTTP request line and modifying
arguments to be path, body, header or query; these options should be used
sparingly and where possible authors should limit customization.

One Conjure file may define multiple services. Each service must be uniquely
named and contain a small amount of metadata to help with documentation
and rendering:
 * `name`: a human readable name for the service
 * `package`: the package under which to render this service
 * `base-path`: the base HTTP path to serve endpoints on this service (default
   `/`)
 * `default-auth`: the default type of auth to apply to this service
   (default `none`). Options are:
    * `none`: do not apply authorization requirements
    * `header`: apply an `Authorization` header argument/requirement to every
      endpoint.
    * `cookie:<cookie id>`: apply a cookie argument/requirement to every
      endpoint with cookie name `<cookie id>`.
 * `docs`: a standard string and generally treated throughout rendering as
   Markdown.

Service identifiers are typically Pascal case.

### Endpoints
Endpoint definitions describe a method, its arguments and return type for
this service.
 * `http`: the request line for a particular endpoint, either in shorthand
   form:

   ```yaml
   http: GET /some/path/{someArg}
   ```

   or in long-form:

   ```yaml
   http:
     method: GET
     path: /some/path/{someArg}
   ```

   where arguments are encapsulated with braces, and must match any path
   arguments found in the later `args` section.

   HTTP methods need to be supported by the server, presently the server
   implementations allow for `GET`, `POST`, `PUT` and `DELETE`.
 * `auth`: an optional auth requirement to override `default-auth`,
   and with identical options to `default-auth`. To override the default and
   remove auth from an endpoint, use `none`.
 * `returns`: a valid Conjure type. `returns` is an optional property -- if
   it is not specified, it indicates that the endpoint does not return a value
   (equivalent to a `void` function in Java).
 * `docs`: a standard string and generally treated throughout rendering as
   Markdown.
 * `args`: a map of argument names (typically in camel case) to argument
   definitions, where an argument may use the short-hand form:

   ```yaml
   [arg]: [type]
   ```

   or longer form:

   ```yaml
   [arg]:
     type: [type]
     docs: [docs]
     param-id: [identifier]
     param-type: (auto|path|body|header|query)
     markers:
      - [type]
   ```

   and for:
    * `type`: a valid Conjure type (`binary` not allowed)
    * (optional) `docs`: a standard string and generally treated throughout
      rendering as Markdown.
    * (optional) `deprecated`: a string that indicates that this endpoint
      is deprecated and describes why
    * (optional) `param-id`: an identifier to use as a parameter value (e.g.
      if this is a header parameter, `param-id` defines the header key); by
      default the argument name is used as the `param-id`.
    * (optional) `param-type`: the type of argument: (default `auto`)
       * `path`: defined as a path parameter; argument name or when defined
         `param-id` must appear in the request line.
       * `body`: defined as the singular body parameter.
       * `header`: defined as a header parameter.
       * `query`: defined as a querystring parameter.
       * (default) `auto`: argument is treated as a path parameter if it appears
         between braces in the request line and as a body argument otherwise.
    * (optional) `markers`: markers are typed labels added used to hint generators
      to add language-specific typed labels to generated methods. Only imported
      types are supported.

### Service Example
See also:
 * [Example Service Definition](conjure-java/src/test/resources/example-service.yml)
 * [Example Generated Jersey Service Code](conjure-java/src/test/resources/test/api/TestService.jersey)
 * [Example Generated Retrofit Client Code](conjure-java/src/test/resources/test/api/TestService.retrofit)
 * [Example Generated TypeScript Client Code](conjure-typescript/src/test/resources/services)

```yaml
services:
  TestService:
    name: Test Service
    package: com.palantir.foundry.catalog.api
    base-path: /catalog
    default-auth: header
    docs: |
      A Markdown description of the service.
    endpoints:
      getFileSystems:
        http: GET /fileSystems
        returns: map<string, BackingFileSystem>
        docs: |
          Returns a mapping from file system id to backing file system configuration.

      createDataset:
        http: POST /datasets
        args:
          request: CreateDatasetRequest
        returns: Dataset

      getDataset:
        http: GET /datasets/{datasetRid}
        args:
          datasetRid: ResourceIdentifier
        returns: optional<Dataset>
```

Gradle Plugin
-------------
As a code generator, Conjure emits language-specific source code as output. The easiest
and most supported way to run Conjure is through the gradle plugins shipped with the
project.

### Conjure Compilation
The Conjure compilation plugin generates code for pre-defined output types
and enables importing of remote Conjure types for inclusion in definitions.

To help manage its multiple output targets, the Conjure plugin generates subprojects
beneath the project on which the plugin is applied. These subprojects _must_ be
included in `settings.gradle`.

Apply the following buildscript configuration to your root project to make the plugin
available to subprojects:
```gradle
buildscript {
    repositories {
        maven { url 'https://artifactory.palantir.build/artifactory/all-jar' }
    }
    dependencies {
        classpath "com.palantir.conjure:conjure-gradle-plugin:${conjureVersion}"
    }
}
```

Apply the Conjure compilation plugin to the project where Conjure source will be written:
```gradle
apply plugin: 'com.palantir.conjure'
```

In the root directory `settings.gradle` file, ensure you define:
```gradle
include '<proj>' // project where com.palantir.conjure plugin is applied
include '<proj>:<proj>-objects'
include '<proj>:<proj>-jersey'
include '<proj>:<proj>-retrofit'
include '<proj>:<proj>-typescript'
include '<proj>:<proj>-python' // experimental, but still required to be defined
```

The plugin then emits tailored outputs to each of these subprojects:
 * `<proj>-objects` (Java)
   * all of the defined bean types
   * ignores unknown properties on objects during deserialization
   * safely encapsulates unknown enumeration values during deserialization
 * `<proj>-jersey` (Java client/service definitions using Jersey)
 * `<proj>-retrofit` (Java client definitions using Retrofit)
 * `<proj>-typescript` (TypeScript)
 * (Experimental) `<proj>-python` (Python)

In addition to generated code, compilation tasks will emit a non-optional `.gitignore`
file to assist in preventing source controlling generated code (prefer generating code
as a compilation task).

(Experimental) Optionally specify additional external imports using `conjureImports`, which
accepts a Gradle `FileCollection`.

As an example:
```gradle
conjure {
    /**
     * Experimental:
     * Reference this file in Conjure source as external-imports/external-import.yml
     */
    conjureImports files('external-import.yml')
}
```

To generate source, run `compileConjure`.

Each source set is generated by an additional task, which `compileConjure` automatically
invokes:

 * `compileConjureObjects`
 * `compileConjureJersey`
 * `compileConjureRetrofit`
 * `compileConjureTypeScript`
 * (Experimental) `compileConjurePython`

To disable source generation for a particular configuration, set the appropriate
task to be disabled:

    tasks.getByName('<task name>').enabled = false

Depend on a generated project using standard gradle syntax:
```gradle
// e.g. for conjure project `project-api`, in `project-server/build.gradle`:
dependencies {
    compile project(':project-api:project-api-jersey')
}
```

Consumers of this plugin are expected to provide concrete versions for the following
dependencies, which are included automatically in appropriate projects:

 * `com.palantir.conjure:conjure-java-lib`
 * `com.squareup.retrofit2:retrofit`
 * `javax.ws.rs:javax.ws.rs-api`

Common strategies for specifying versions include:

 * Use the [nebula dependency recommender plugin](https://github.com/nebula-plugins/nebula-dependency-recommender-plugin)
 * Specify an explicit resolution strategy in the Conjure project's gradle:

   ```gradle
   subprojects {
       configurations.compile {
           resolutionStrategy {
               force 'com.palantir.conjure:conjure-java-lib:0.26.0'
               force 'com.squareup.retrofit2:retrofit:2.1.0'
               force 'javax.ws.rs:javax.ws.rs-api:2.0.1'
           }
       }
   }
   ```

### TypeScript Publication
The TypeScript Publication plugin enables publishing typescript artifacts from projects using Conjure.

When run, an npm package is published to ``@elements/${projectName}-conjure`

To add the plugin, depend on it in your root project build.gradle:

```gradle
buildscript {
    dependencies {
        classpath "com.palantir.conjure:conjure-publish-gradle-plugin:${conjureVersion}"
    }
}
```

Apply the plugin on projects containing generated TypeScript:

```gradle
apply plugin: 'com.palantir.gradle-conjure-publish'
```

To publish, run the `publishConjure` task.
