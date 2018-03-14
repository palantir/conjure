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

[Spellbook](https://spellbook.palantir.build) hosts Conjure files in a
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
   - [TypeScript Publication](#typescript-publication)
 - [Feature Requests](#feature-requests)

Why?
----
The rationale for maintaining Conjure rather than picking an existing IDL (e.g., Swagger) for Foundry (and other)
products is as follows. For the first handful of Foundry projects (in particular, Gatekeeper, Compass, Foundry), we
picked JAX-RS as the API contract between servers (Jersey) and clients (Feign) since it offered the best compromise
between usability (simple Java proxies, straight-forward server implementations), dependency management (separation
between API definitions and their implementations), and interoperability (JSON as the lingua franca for both
backend-to-backend and frontend-to-backend communication, and the ability to interrogate with services via different
languages or even cURL). The idiosyncratic specifics of our interpretation of JAX-RS contracts are encoded in the
`http-remoting` libraries.

Given what we have since learned about the disadvantages of JAX-RS and the advantages of alternative RPC mechanisms
(e.g., gRPC), one may be inclined to consider this a historic mistake, at least for backend RPC; on the other hand, it
has allowed us to make swift progress on frontends and it has opened the door for simple service composition, in
particular for non-Foundry and non-Java projects such as Foundry Mobile. Either way, it is the status quo. Since major
API breaks are currently a deal-breaker, we are effectively presented with two choices: either continue to specify APIs
in a Java-centric way via JAX-RS, or retrofit an IDL to our particular interpretation of the JAX-RS wire format. Conjure
is an experiment towards the latter choice and has the following advantages:

- Conjure simplifies non-Java implementations of API servers and clients. The Typescript bindings are a great example.
- Conjure allows us to specify the wire format independently of existing client/server libraries (e.g., Jersey).
- Value types are very simple to specify.
- Conjure can act as a bridge format to some other IDL we may eventually want to use. For example, Conjure can likely
  be compiled into Protobuf if we ever wanted to move our service implementations to gRPC.

Of course there are downsides. To mention just two, you cannot Google for Conjure problems, and someone has to maintain
Conjure and keep up with feature requests and bug fixes.

#### Scope

The primary purpose of Conjure is to define cross-service APIs and its design decisions and feature development follow
this goal. In API design, stability and simplicity often trump feature-richness and expressive power; this informs the
limited scope and feature set of Conjure.  It is perfectly acceptable to use Conjure for other purposes (including
internal data structures, serializing to disk or DB, etc.), but those uses do not drive design decisions and feature
development.

Types
-----
Conjure's type system deals with two modes of types: *imported types*, which are
defined outside of Conjure definition files but declared as explicit imports
and given 'local' names for use in a Conjure file, and *defined types*, which are
fully specified by primitives, built-ins and types defined in a Conjure file.

By convention, all primitive and built-in types use camel case, while all
user-defined types (e.g. imports and object definitions) use pascal case.

### Rules
* Type names must be in `CamelCase` over `[a-z0-9]`.
* Type names must be unique with case-insensitive comparison. For example,
  it is not legal for an imported type and an enum type to have the same type
  name, even though the Conjure JSON format itself would not prevent this.
* Types cannot have the same name as any of the Conjure primitives or built-ins.
* The casing of a type may be changed in generated files to conform with the
  language specification of the generated code. The requirement that all type
  names be unique in a case-insensitive comparison guarantees that changing the
  case will not introduce collisions.

### Primitives
Base types in the system are:
 * `string`
 * `integer`: 32-bit signed whole numbers
 * `double`: 64-bit floating point numbers
 * `boolean`: `(true|false)`
 * `safelong`: a wrapper around a long type which enforces the value is safely representable as an integer in javascript,
   between `Number.MIN_SAFE_INTEGER` and `Number.MAX_SAFE_INTEGER`
 * `rid`: a [Resource Identifier](https://github.com/palantir/resource-identifier)
 * `bearertoken`: a [BearerToken](https://github.com/palantir/auth-tokens/#bearertoken)
 * `uuid`: a [Universally Unique Identifier](https://en.wikipedia.org/wiki/Universally_unique_identifier)

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

External types are provided as a temporary crutch for migrating existing APIs
to Conjure. New APIs should never use external types. Support for external
imports will be removed some time in 2018.

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

* Java - the Java package is the Conjure package name
* TypeScript - the TypeScript package is the Conjure package name with the first two segments removed (this is done for
  brevity). For example: `com.example.folder1.folder2 -> folder1-folder2`. The second part of the package is used as the
NPM publishing scope: `@example` in this case. See [Typescript Publication](#typescript-publication) for more details.
* Python - the Python package is the Conjure package name with the first two segments removed. For example:
  `com.example.folder1.folder2 -> folder1.folder2`.

Packages are not used for namespacing purposes within a Conjure file -- all
type names must be unique within a Conjure file, even across different types
(e.g. an object definition and an enum definition).

Package names should not be reused across projects. Reusing package names
can cause publishing errors because multiple generated packages will have
the same name.

### Defined Types
Defined types consist of objects completely specified within the context of a
Conjure file (possibly referencing other type aliases, primitive types,
and built-ins). These types are *Object Definitions*, *Enum Definitions*,
*Alias Definitions*, or *Error Definitions*. These types of objects consist of,
minimally, a type alias, an optional package name and optional documentation,
except for error types, which will be described in its own section below.

Conjure renderers will use the type alias as the object name, and will emit
the object definition in the defined package or fall-back to the
definitions-wide default package.

Type names, as above, must be in `UpperCamelCase`.

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

Field names must appear in either `lowerCamelCase`, or `kebab-case`, or `snake_case`.
Type renderers
will respect casing for wire format, but may convert case formats to conform
with language restrictions. As a result, field names must be unique independent
of case format (e.g. an object may not define both `caseFormat` and
`case-format` as fields).

Technically, any valid JSON string can be used as a JSON key, so the restriction
that all field names must be camel case or kebab case or snake case means that there are
specifications for valid JSON objects that cannot be generated by Conjure. This
has not yet been an issue, but please file an issue if this is something that you
encounter that causes pain. In the mean time, this can be worked around by
defining an imported type that imports a compatible type that is defined externally.

#### Union Definitions
Conjure supports unions, where a union contains exactly one of several possible types. Each union definition consists of
a type alias, an optional package, and a `union` block, which is a map of name (used for serialization of the union
type) to a member type definition. Member types may include any other Conjure type (built-ins, imports, etc.).

```yaml
union:
  [name]: [type]
  [another name]: [another type]
```

A concrete example where a union type might be used is in describing a payload object. For example, the payload can be
_either_ a service log _or_ a request log. Using the above format, this object could be defined in Conjure as follows:

```yaml
Payload:
  union:
    serviceLog: ServiceLog
    notAServiceLog: RequestLog
```

Docs may be included by using the long form:

```yaml
union:
  [name]:
    type: [type]
    docs: [docs]
```

The serialized format includes two fields: a `type` field with a value corresponding to the member name
 and a member name field with a value corresponding to the serialized, wrapped object.

For the above example, an instance of the `Payload` object containing a `ServiceLog` would be serialized as:

```javascript
{
  "type": "serviceLog",
  "serviceLog": {
    // fields of ServiceLog object
  }
}
```

An instance of the `Payload` object containing a `RequestLog` would be serialized as:

```javascript
{
  "type": "notAServiceLog",
  "notAServiceLog": {
    // fields of RequestLog object
  }
}
```

As with enums, there is the risk that union definitions are augmented over time with additional member types. Older
clients that are unaware of the new types should be able to deserialize and handle these objects gracefully.

In Java, a visitor interface is generated for union types, to facilitate consumption and customization of behavior
depending on the wrapped type. The interface includes a `visit()` method for each wrapped type, as well as a
`visitUnknown(String unknownType)` method which is executed when the wrapped object does not match any of the known
member types. Clients should implement the `visitUnknown` method by logging a warning, while servers should treat the
execution of this method as an exceptional state.

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

Docs may be included by using the long form:

```yaml
values:
  - value: [value]
    docs: [docs]
```

#### Alias Definitions
As a convenience, Conjure offers the ability to alias types so that
stronger types may be carried throughout generated code, though not all generators
support this feature, and will fallback to replacing the aliased type with its
concrete type.

Each alias type appears in the definitions block and may include an optional
docs block and a mandatory alias type:

```yaml
alias: <type>
docs: optional docs
```

#### Error Definitions
Conjure offers first-class support for HTTP Remoting 3 [ServiceException](https://github.com/palantir/http-remoting#error-propagation)
by providing the ability to define error types. An error definition consists of a type
alias, a namespace, an error code, which should be one of
[the codes defined in HTTP Remoting](https://github.com/palantir/http-remoting-api/blob/develop/errors/src/main/java/com/palantir/remoting/api/errors/ErrorType.java#L38),
optional documentation, and optional lists of safe and unsafe arguments. It lives in a different
block from other object definitions, called `errors`.

For each error definition, Conjure will generate an error type object and a factory method
for creating `ServiceException`s associated with that error type. They will both be put in
a class named `[YourNamespace]Errors`, along with the generated objects and factory methods
for other error definitions with the same namespace. Instead of having each error definition
define its own package, the default package name is used for all error definitions.

The type alias and namespace are required to be in `UpperCamelCase`, whereas error code is required to be
in `UPPER_UNDERSCORE_CASE`.

Error definition could optionally have two argument lists, `safe-args` and `unsafe-args`.
Each takes a list of fields, defined in the same syntax as fields in object definitions.
The difference between safe and unsafe arguments are explained in the docs of [HTTP Remoting](https://github.com/palantir/http-remoting#error-propagation).
Safe arguments come first in the generated factory methods.

#### Examples
See also:
 * [Example Type Definitions](conjure-java/src/test/resources/example-types.yml)
 * [Example Error Definitions](conjure-java/src/test/resources/example-errors.yml)
 * [Example Generated Java Code](conjure-java/src/integrationInput/java/test/api)
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

  errors:
    ExampleError:
      namespace: Conjure
      code: INTERNAL
      docs: Optional Docs
      safe-args:
        safeArgument: string
      unsafe-args:
        unsafeArgument: any
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
    * `cookie:<cookie name>`: apply a cookie argument/requirement to every
      endpoint with cookie name `<cookie name>`.
 * `docs`: a standard string and generally treated throughout rendering as
   Markdown.

Service identifiers are typically Pascal case.

### Endpoints
Endpoint definitions describe a method, its arguments and return type for
this service.
 * `http`: the request line for a particular endpoint, either in shorthand form:

   ```yaml
   http: GET /some/path/{someArg}
   ```

     * `method` must be one of  `GET`, `POST`, `PUT` or `DELETE`
     * `path` is a path segment that must begin with a slash (`/`) and must not end in a slash
     * A path segment can be specified to be a path parameter argument by encapsulating the segment in curly braces (`{}`)
     * A particular `method` + `path` combination can only be defined once per service. All path parameters are
       considered equivalent for the purposes of determining uniqueness -- for example, `GET /branch/{arg}/resolve` and
       `GET /branch/{id:.+}/resolve` are considered equivalent and thus cannot both be defined for a particular service.
     * Non-header parameter names must be lowerCamelCase. Header parameter names must be Upper-Kebab-Case (valid header
       names include `Cache-Control` or `Content-MD5`).
     * All path parameters that occur in the path must match a path argument specified in the `args` section
     * Path parameters must be unique: the same parameter cannot occur multiple times in the same path
     * Path parameter names are case-sensitive (`arg` and `aRg` are considered separate parameters)
     * The type for a path parameter must be a primitive type or an alias type that resolves to a primitive type
     * `:.+` can be appended to the variable name of a path parameter to specify that a non-greedy match should be
       performed for the parameter. Unlike other path parameters, specifying this will allow the parameter to match the
       slash (`/`) character.
       * For example, if the value of `path` is `/branch/{branchPath:.+}/resolve`, then the request paths
         `/branch/foo/resolve` and `/branch/foo/bar/resolve/resolve` would both match the path, and the value of the
         `branchPath` path parameter would be `foo` and `foo/bar/resolve`, respectively. A request of `/branch/resolve`
         would not match.
     * If the path parameter is the final segment of a path, then `:.*` can also be appended to the variable name to
       specify that the trailing parameter may be empty. For example, if the value of `path` is
       `/branch/{branchPath:.*}`, then `/branch/foo` and `branch/` would both match the path, and the value of the
       `branchPath` parameter would be `foo` and `` (empty), respectively.
     * `:.+` and `:.*` are the only regular expressions that are supported, and `:.*` is only supported for trailing
        path parameters (path parameters that are the final segments of a path)
     * For server implementations: if a request can be matched to multiple paths (due to the presence of two paths that
       share the same prefix segments and then differ in a path parameter segment versus a literal path segment), then
       the registered path that has the longest literal prefix match should be used to handle the request
       * For example, if `/path/dataset/{arg}` and `/path/{arg}/fetch` are both registered as paths and
         `/path/dataset/fetch` is issued as a request, the server should handle it as `/path/dataset/{arg}` with
         `{arg}` = "fetch" (rather than as `/path/{arg}/fetch` with `{arg}` = "dataset") because `/path/dataset` is the
         longest literal prefix match.
       * As a fallback, we stipulate that the behavior of path matching should match the behavior that would be
         exhibited by a JAX-RS server handling the equivalent path expressions and inputs.
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
    * `type`: a valid Conjure type (`binary` only allowed when `param-type` is explicitly set to `body`)
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
 * [Example Generated Jersey Service Code](conjure-java/src/test/resources/test/api/TestService.java.jersey)
 * [Example Generated Retrofit Client Code](conjure-java/src/test/resources/test/api/TestService.java.retrofit)
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

To help manage multiple output targets, the plugin will detect and configure
subprojects with standard names defined beneath the project where the plugin is
applied. Omitting a subproject will omit the output target. Example of a
`settings.gradle` file with all Conjure output targets enabled:

```gradle
// project where com.palantir.conjure plugin is applied
include '<proj>'

// include expected output targets as desired
include '<proj>:<proj>-objects'
include '<proj>:<proj>-jersey'
include '<proj>:<proj>-retrofit'
include '<proj>:<proj>-typescript'
include '<proj>:<proj>-python' // experimental
```

The plugin then emits tailored outputs to each of these subprojects:
 * `<proj>-objects` (Java)
   * all of the defined bean types
   * ignores unknown properties on objects during deserialization
   * safely encapsulates unknown enumeration values during deserialization
 * `<proj>-jersey` (Java client/service definitions using Jersey)
   * requires and automatically depends on `-objects` project
 * `<proj>-retrofit` (Java client definitions using Retrofit)
   * requires and automatically depends on `-objects` project
 * `<proj>-typescript` (TypeScript)
 * (Experimental) `<proj>-python` (Python)

In addition to generated code, compilation tasks will emit a non-optional `.gitignore`
file to assist in preventing source controlling generated code (prefer generating code
as a compilation task).

To generate source, run `compileConjure`.

Depend on a generated project using standard gradle syntax:
```gradle
// e.g. for conjure project `project-api`, in `project-server/build.gradle`:
dependencies {
    compile project(':project-api:project-api-jersey')
}
```

Java compilation will automatically depend on the code generation steps, so no
additional task dependency configuration should be required.

Consumers of this plugin are expected to provide concrete versions for the following
dependencies, which are included automatically in appropriate projects:

 * `com.palantir.conjure:conjure-java-lib`
 * `com.squareup.retrofit2:retrofit`
 * `javax.ws.rs:javax.ws.rs-api`
 * `com.palantir.remoting-api:errors`

Common strategies for specifying versions include:

 * Use the [nebula dependency recommender plugin](https://github.com/nebula-plugins/nebula-dependency-recommender-plugin)
 * Specify an explicit resolution strategy in the Conjure project's gradle:

   ```gradle
   subprojects {
       configurations.all {
           resolutionStrategy {
               force 'com.palantir.conjure:conjure-java-lib:0.31.0'
               force 'com.squareup.retrofit2:retrofit:2.2.0'
               force 'javax.ws.rs:javax.ws.rs-api:2.0.1'
               force 'com.palantir.remoting-api:errors:1.3.0'
           }
       }
   }
   ```

### TypeScript Publication
The TypeScript Publication plugin enables publishing typescript artifacts from projects using Conjure.

When run, a NPM package is published for each Conjure package defined in your project. The scope corresponds to the
second part of the Conjure package, with the later parts being joined to form the NPM package name. For example, the
following Conjure definition would produce `@palantir/foundry-catalog-api`, with generated code corresponding to
`ExampleEnumeration`, and `@palantir/foundry-catalog-api-datasets`, with generated code corresponding to
`BackingFileSystem`.

```yaml
definitions:
  default-package: com.palantir.foundry.catalog.api

  objects:
    BackingFileSystem:
      package: com.palantir.foundry.catalog.api.datasets
      fields:
        fileSystemId:
          type: string
          docs: The name by which this file system is identified.
        baseUri: string
        configuration: map<string, string>

    ExampleEnumeration:
      values:
        - A
        - B
```

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
apply plugin: 'com.palantir.typescript-publish'
```

To publish, run the `publishTypeScript` task.


Feature Requests
----------------

Conjure is a widely-used framework and as such has comparatively strict back-compatibility requirements. Further, since
Conjure supports multiple language, features can only be added if we have confidence in its implementation across
supported languages. This implies that the default position regarding new features is conservative: we prefer Conjure to
be as small as possible. If you would like to propose a feature, please reach out to
pd-foundry-infrastructure-dev@palantir.com or start a discussion on a Github issue.

Below is a list of features that we have considered but are unlikely to support:

### Support for TEXT_PLAIN endpoints
Conjure is an opinionated RPC library, not a generic HTTP IDL. As such, we prefer homogeneous APIs and today support
only APPLICATION_JSON endpoints. Service authors migrating existing APIs with TEXT_PLAIN endpoints to Conjure are
advised to split the API in two parts and implement the TEXT_PLAIN endpoints outside Conjure. For example, see
https://github.palantir.build/foundry/conjure/issues/418 .

### Support for polymorphic types
Polymorphic types are hard to implement, in particular across languages. Following prior art from Google's Protocol
Buffers, we do not support polymorphic types or interfaces. Union types offer a limited form of polymorphism that allows
library authors to encode alternative return values. For example, see
https://github.palantir.build/foundry/conjure/issues/201 .

### Imports from other Conjure APIs
Conjure does not support importing objects from other Conjure APIs. If you must reference objects from another service's
API, the recommendation is to redefine these types in your own service's Conjure definition.

Cross-API imports are undesirable for a few reasons:
1. API version coupling: breaks in the dependency's API result in breaks in the consuming API.
2. Transitive dependency complexity and the introduction of [diamond dependency](http://www.well-typed.com/blog/9/) risk
