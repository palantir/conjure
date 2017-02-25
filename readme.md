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
Conjure's type system deals with two modes of types, imported types, defined
outside of Conjure definition files but declared as explicit imports and given
'local' names for use in a Conjure file and defined types, which are fully
specified by other types within a Conjure file, primitives and built-ins.

By convention, all primitive and built-in types use camel case, while all user
defined types (e.g. imports and object definitions) use pascal case.

### Primitives
Base types in the system are:
 * `string`
 * `integer` (32-bit whole numbers)
 * `double` (64-bit floating point numbers)
 * `boolean` `(true|false)`

### Built-Ins
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
 * `binary` (byte[])

### Imported Types
External types, or imports, consist of references to types defined outside
the context of a Conjure file (possibly in other Conjure files, possibly
in external systems). A base-type is provided as a hint to generators how
to handle this type when no external type reference is provided.

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
Conjure supports packages, which are namespaces for objects. Package names
should follow the Java style naming convention: `com.example.name`

This manifests in the generated languages as follows:

* Java - the Conjure package name is the Java package name
* TypeScript - the folder containing the object is the Conjure pakcage
name with the first two segments removed (this is done for brevity).
For example: `com.example.folder1.folder2 -> folder1/folder2`

### Defined Types
Defined types consist of objects completely specified within the context of a
Conjure file (possibly referencing other type aliases, primitive types,
and built-ins). These types are either *Object Definitions* or *Enum Definitions*.
Both types of objects consist of, minimally, a type alias, an optional
package name and optional documentation.

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

Non-primitive types may include other defined type aliases (aliases may be used
before definition) or leverage primitives and built-ins.

Field names must appear in either camel case or kebab case. Type renderers
will respect casing for wire format, but may convert case formats to conform
with language restrictions. As a result, field names must be unique independent
of case format (e.g. an object may not define both `caseFormat` and
`case-format` as fields).

#### Enum Definitions
Each enum definition consists of a type alias, an optional package, and a list
of valid enumeration values. Values _must not_ include the special value `UNKNOWN`,
which is reserved to represent an enumeration value that was found but unexpected
in generated object code.

Enum values must be uppercase words separated by underscores.

Each enum includes a values definition block, which is a list of the valid
values:

```yaml
values:
 - [value]
 - [another value]
```

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
 * `returns`: a valid Conjure type (`binary` not allowed)
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
The Conjure compilation plugin generates code for configurable, pre-defined output types
and enables importing of remote Conjure types for inclusion in definitions.

While the plugin does not prescribe a particular layout or project configuration, it's
loosely designed to work with the following recommended layout:
```
project-api
 ├── definition
 ├── jersey
 ├── retrofit
 └── typescript
project-server-interface
project-server
```

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

Apply the Conjure compilation plugin _only_ on the _definition_ (or equivalent) project,
where Conjure source will be written:
```gradle
apply plugin: 'com.palantir.conjure'
```

Choose one or more output target:
 * `jerseyServer` (Java)
   * errors on objects with unknown properties during deserialization
   * errors on enumerations with unknown values during deserialization
   * annotates services with JAX-RS annotations
 * `jerseyClient` (Java)
   * ignores unknown properties on objects during deserialization
   * safely encapsulates unknown enumeration values during deserialization
   * annotates services with JAX-RS annotations
 * `retrofitClient` (Java)
   * ignores unknown properties on objects during deserialization
   * safely encapsulates unknown enumeration values during deserialization
   * annotates services with Retrofit2 annotations
 * `typescriptClient` (TypeScript)

In addition to generated code, compilation tasks will emit a non-optional `.gitignore`
file to assist in preventing source controlling generated code (prefer generating code
as a compilation task).

If using the recommended layout, emit to `src/main/java`, since the projects exist
primarily to provide packaging and dependency isolation, not to contain additional code.

(Experimental) Optionally specify additional external imports using `conjureImports`, which
accepts a Gradle `FileCollection`.

As an example:
```gradle
conjure {
    jerseyServer {
        # emit to the project-server-interface project + src/main/java directory
        output project(':project-server-interface').file('src/main/java')
    }
    jerseyClient { output project(':api:jersey').file('src/main/java') }
    retrofitClient { output project(':api:retrofit').file('src/main/java') }
    typescriptClient { output project(':api:typescript').file('src') }

    /**
     * Experimental:
     * Reference this file in Conjure source as external-imports/external-import.yml
     */
    conjureImports files('external-import.yml')
}
```

To generate source, run `compileConjure`.

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
