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

Types
-----
Conjure's type system deals with two modes of types, external types, defined
outside of Conjure definition files but declared as explicit imports and given
'local' names for use in a Conjure file and internal types, which are fully
defined from other types within a Conjure file, primitives and built-ins.

### Primitives
Base types in the system are:
 * String
 * Integer (32-bit whole numbers)
 * Double (64-bit floating point numbers)

### Built-Ins
Conjure offers several built-ins to assist with mapping to existing language
constructs and to simplify API building. These types are genericized by other
defined Conjure types, which may also be built-ins (a Map of Maps is allowed):
 * `Map<K, V>`: a map of `K` to `V`; since Conjure generates JSON serializable
   objects, `K` is generally restricted to `String` (though this is not strictly
   enforced to allow for external types to be present as a key type).
 * `List<T>`: a list of `T`.
 * `Set<T>`: a set of `T`.
 * `Optional<T>`: an optional type to help prevent nullity. Optional types will
   serialize as null when absent and as a concrete non-null value when present.
   The empty String and the empty Map/Object are valid non-null values.

### External Types (Imports)
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
    base-type: String
    # A map of language name to a more refined type name (used as a hint)
    # in that language. Renderers may choose to ignore the hint even if
    # it is provided. (By default, Java renderers respect `java` keyed
    # hints.)
    external:
      java: com.palantir.ri.ResourceIdentifier
```

### Internal Types (Object Definitions)
Internal types, or object definitions, consist of objects completely defined
within the context of a Conjure file (possibly referencing other type aliases,
primitive types, and built-ins). Each object definition consists of a type
alias, an optional package, and a map of field names to field definitions.

Conjure renderers will use the type alias as the object name, and will emit
the object definition in the defined package or fall-back to the
definitions-wide default package.

Type aliases, as above, should use Pascal case.

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

Field names are typically Camel Case.

#### Object Definitions Example

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

      # A map of field name to a type definition.
      fields:
        # example of a field with docs:
        fileSystemId:
          type: String
          docs: The name by which this file system is identified.
        # example of a simple field:
        baseUri: String
        configuration: Map<String, String>
```

### Rendering

#### Java
Conjure's Java type renderer emits concrete final bean-pattern objects based on
provided definitions and will include as much Javadoc as provided. Objects are
emitted as public and defined one per file.

An example rendering of the `BackingFileSystem` type defined above:
```java
package com.palantir.foundry.catalog.api.datasets;

import java.lang.String;
import java.util.Map;

public final class BackingFileSystem {
    private final String fileSystemId;

    private final String baseUri;

    private final Map<String, String> configuration;

    public BackingFileSystem(String fileSystemId, String baseUri, Map<String, String> configuration) {
        this.fileSystemId = fileSystemId;
        this.baseUri = baseUri;
        this.configuration = configuration;
    }

    /**
     * The name by which this file system is identified.
     */
    public String getFileSystemId() {
        return this.fileSystemId;
    }

    public String getBaseUri() {
        return this.baseUri;
    }

    public Map<String, String> getConfiguration() {
        return this.configuration;
    }
}
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
 * `default-authz`: the default type of authorization to apply to this service
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
   implementations allow for `GET`, `POST`, and `DELETE`.
 * `authz`: an optional authorization requirement to override `default-authz`,
   and with identical options to `default-authz`. To override the default and
   remove authorization from an endpoint, use `none`.
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
   ```

   and for:
    * `type`: a valid Conjure type
    * (optional) `docs`: a standard string and generally treated throughout
      rendering as Markdown.
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

### Service Example

```yaml
services:
  TestService:
    name: Test Service
    package: com.palantir.foundry.catalog.api
    base-path: /catalog
    default-authz: header
    docs: |
      A Markdown description of the service.
    endpoints:
      getFileSystems:
        http: GET /fileSystems
        returns: Map<String, BackingFileSystem>
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
        returns: Optional<Dataset>
```

### Rendering

#### Java

```java
package com.palantir.foundry.catalog.api;

import com.palantir.foundry.catalog.api.datasets.BackingFileSystem;
import com.palantir.foundry.catalog.api.datasets.Dataset;
import com.palantir.ri.ResourceIdentifier;
import com.palantir.tokens.AuthHeader;
import java.lang.String;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * A Markdown description of the service.
 */
@Path("/catalog")
public interface TestService {
    /**
     * Returns a mapping from file system id to backing file system configuration.
     */
    @GET
    @Path("/fileSystems")
    Map<String, BackingFileSystem> getFileSystems(@HeaderParam("Authorization") AuthHeader authHeader);

    @POST
    @Path("/datasets")
    Dataset createDataset(@HeaderParam("Authorization") AuthHeader authHeader, CreateDatasetRequest request);

    @GET
    @Path("/datasets/{datasetRid}")
    Optional<Dataset> getDataset(@HeaderParam("Authorization") AuthHeader authHeader, @PathParam("datasetRid") ResourceIdentifier datasetRid);

    @GET
    @Path("/datasets/{datasetRid}/branches")
    Set<String> getBranches(@HeaderParam("Authorization") AuthHeader authHeader, @PathParam("datasetRid") ResourceIdentifier datasetRid);
}
```

Examples
--------
```yaml
types:
  imports:
    ResourceIdentifier:
      base-type: String
      external:
        java: com.palantir.ri.ResourceIdentifier

    AuthHeader:
      base-type: String
      external:
        java: com.palantir.tokens.AuthHeader

  definitions:
    default-package: com.palantir.foundry.catalog.api
    objects:      
      BackingFileSystem:
        package: com.palantir.foundry.catalog.api.datasets
        fields:
          fileSystemId:
            type: String
            docs: The name by which this file system is identified.
          baseUri: String
          configuration: Map<String, String>

      Dataset:
        package: com.palantir.foundry.catalog.api.datasets
        fields:
          fileSystemId: String
          rid:
            type: ResourceIdentifier
            docs: Uniquely identifies this dataset.

      CreateDatasetRequest:
        fields:
          fileSystemId: String
          path: String

services:
  TestService:
    name: Test Service
    package: com.palantir.foundry.catalog.api
    base-path: /catalog
    default-authz: header
    docs: |
      A Markdown description of the service.
    endpoints:
      getFileSystems:
        http: GET /fileSystems
        returns: Map<String, BackingFileSystem>
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
        returns: Optional<Dataset>
      getBranches:
        http: GET /datasets/{datasetRid}/branches
        args:
          datasetRid:
            type: ResourceIdentifier
            docs: |
              A valid dataset resource identifier.
        returns: Set<String>
```
