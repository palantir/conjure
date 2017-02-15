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

- Conjure simplifies non-Java implementations of API servers and clients. The TypeScript bindings are a great example.
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
 * `any` (unknown placeholder type)
 * `binary` (byte[])

### Built-Ins
Conjure offers several built-ins to assist with mapping to existing language
constructs and to simplify API building. These types are genericized by other
defined Conjure types, which may also be built-ins (a Map of Maps is allowed):
 * `map<K, V>`: a map of `K` to `V`; since Conjure generates JSON serializable
   objects, `K` is generally restricted to `string` (though this is not strictly
   enforced to allow for external types to be present as a key type).
 * `list<T>`: a list of `T`.
 * `set<T>`: a set of `T`.
 * `optional<T>`: an optional type to help prevent nullity. Optional types will
   serialize as null when absent and as a concrete non-null value when present.
   The empty string and the empty map/object are valid non-null values.

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

Full Example
------------
A complete definition might look something like this excerpt from the Foundry
Catalog API.

```yaml
types:
  imports:
    ResourceIdentifier:
      base-type: string
      external:
        java: com.palantir.ri.ResourceIdentifier

    AuthHeader:
      base-type: string
      external:
        java: com.palantir.tokens.auth.AuthHeader

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

      Dataset:
        package: com.palantir.foundry.catalog.api.datasets
        fields:
          fileSystemId: string
          rid:
            type: ResourceIdentifier
            docs: Uniquely identifies this dataset.

      CreateDatasetRequest:
        fields:
          fileSystemId: string
          path: string

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
      getBranches:
        http: GET /datasets/{datasetRid}/branches
        args:
          datasetRid:
            type: ResourceIdentifier
            docs: |
              A valid dataset resource identifier.
        returns: set<string>
```

Gradle Plugins Usage
-------------------
Conjure supports two gradle plugins. To use either, ensure you have the following added to your root project `build.gradle`:
```gradle
buildscript {
    repositories {
        maven { url 'https://artifactory.palantir.build/artifactory/all-jar' }
    }
}
```

### Conjure Java Gradle Plugin

Currently, the best way to use Conjure for java projects is to apply the Conjure Gradle plugin. 

To add the plugin, depend on it in your root project `build.gradle`:
```gradle
buildscript {
    dependencies {
        classpath "com.palantir.conjure:conjure-java-gradle-plugin:${conjureVersion}"
    }
}
```

... and apply the plugin on any projects that will have Conjure files:
```gradle
apply plugin: 'com.palantir.gradle-conjure-java'
```

Then, add a `src/main/conjure` folder and author your Conjure files (typically with a `.yml` extension).

To generate code, run the `compileConjureJavaServer` task. Generated code -- today, the Gradle plugin
only generates Java -- will be placed in `src/generated/java`. Note that generated code currently biases
towards server-styled defaults.

By default, the plugin renders Conjure `optional<>` fields as Guava `Optional` types. This behavior can be toggled
via the `optionalType` configuration point:
```gradle
conjureJavaServer {
    optionalType = 'JAVA8'  // default: 'GUAVA'
}
```

### Conjure Publish Gradle Plugin

This plugin allows publishing artifacts from projects using conjure. The plugin publishes artifacts intended for languages supported by conjure. Currently the only language for which artifacts are published is Typescript.

* Tyepscript publishing
    * An npm package is published at the location `@elements/${projectName}-conjure`

To add the plugin, depend on it in your root project `build.gradle`:
```gradle
buildscript {
    dependencies {
        classpath "com.palantir.conjure:conjure-publish-gradle-plugin:${conjureVersion}"
    }
}
```

...and apply the plugin on any projects that have Conjure files:
```gradle
apply plugin: 'com.palantir.gradle-conjure-publish'
```

To publish, run the `publishConjure` task.
