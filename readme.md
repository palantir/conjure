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

Definition
----------
Presently, Conjure depends on a simple intermediate format, split into two
main sections, _types_ and _services_.

    types:
      # References to external types. External types must be renderable
      # to a primitive type (String, Integer or Double). External types will not
      # be rendered as part of code generation.
      #
      # imports is a map from type alias (used in service definitions and
      # other types) to an external type definition.
      imports:
        ResourceIdentifier:
          # One of String, Integer or Double
          base-type: String
          # A map of language name to a more refined type name (used as a hint)
          # in that language. Renderers may choose to ignore the hint even if
          # it is provided. (By default, Java renderers respect `java` keyed
          # hints.)
          external:
            java: com.palantir.ri.ResourceIdentifier

        AuthHeader:
          base-type: String
          external:
            java: com.palantir.tokens.AuthHeader

      # Definitions for object types to render as part of code generation.
      definitions:
        # The defualt package in which to store types (when a local package)
        # is not defined.
        default-package: com.palantir.foundry.catalog.api

        # objects is a map from type alias (used in service definitions and
        # other types) to an object type definition.
        #
        # Type names are typically Pascal Case.
        objects:      
          BackingFileSystem:
            # Override the default package specified at the definitions level.
            package: com.palantir.foundry.catalog.api.datasets

            # A map of field name to a type definition. Simple definitions that
            # omit documentation may simply encode:
            #
            #     [field]: [type]
            #
            # Docs may be included on this type by using the long form:
            #
            #     [field]:
            #       type: [type]
            #       docs: [docs]
            #
            # Where docs is a standard string and generally treated
            # throughout rendering as Markdown. Use YAML multiline-strings
            # to help with formatting.
            #
            # Non-primitive types may include other defined type aliases
            # (aliases may be used before definition) or leverage native
            # Map, List, Set or Optional types, which take type arguments.
            #
            # Field names are typically Camel Case.
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

    # services is a map from service name to service definition. Service names
    # are typically Pascal Case.
    services:
      TestService:
        # A human readable name for this service.
        name: Test Service

        # The package for this service.
        package: com.palantir.foundry.catalog.api

        # The base HTTP path for this service.
        base-path: /catalog

        # Optional docs for this service. Generally treated throughout rendering
        # as Markdown. Use YAML multiline-strings to help with formatting.
        docs: |
          A Markdown description of the service.

        # endpoints is a map of methods invokable on this service to endpoint
        # definitions. Endpoint names are typical Camel Case.
        endpoints:

          getFileSystems:
            # An HTTP request line with optional path parameters. Lines are of
            # the form:
            #
            #     METHOD /path/{var1}/more/{var2}
            #
            # Where `var1` and `var2` must correspond to arguments specified
            # below.
            http: GET /fileSystems

            # Optional authorization approach to use. Allowed types: header.
            # Omit the field to specify no authorization.
            authorization: header

            # Return type.
            returns: Map<String, BackingFileSystem>

            # Optional docs.
            docs: |
              Returns a mapping from file system id to backing file system configuration.

          createDataset:
            http: POST /datasets
            authorization: header
            args:
              request: CreateDatasetRequest
            returns: Dataset

          getDataset:
            http: GET /datasets/{datasetRid}
            authorization: header
            args:
              datasetRid: ResourceIdentifier
            returns: Optional<Dataset>

          getBranches:
            http: GET /datasets/{datasetRid}/branches
            authorization: header
            # A map of arg name to an arg definition. Simple definitions that
            # omit documentation may simply encode:
            #
            #     [arg]: [type]
            #
            # Docs may be included on this type by using the long form:
            #
            #     [arg]:
            #       type: [type]
            #       docs: [docs]
            #
            # Where docs is a standard string and generally treated
            # throughout rendering as Markdown. Use YAML multiline-strings
            # to help with formatting.
            #
            # Non-primitive types may include other defined type aliases
            # (aliases may be used before definition) or leverage native
            # Map, List, Set or Optional types, which take type arguments.
            #
            # Argument names are typically Camel Case. If path parameters are
            # included, there should be at least as many same-named arguments
            # in this map.
            #
            # By default arguments are treated as either path parameters or
            # as a body parameter based on whether the argument key matches
            # a path parameter.
            #
            # Both header and query parameters may also be encoded by specifying
            # `param-type` and for headers, in particular, optionally specify a
            # different parameter identifier (for use as the header) by
            # specifying `param-id`:
            #
            #     [arg]:
            #       type: [type]
            #       docs: [docs]
            #       param-type: [auto|path|body|header|query]
            #       param-id: [identifier to match path or specify header]
            #
            args:
              datasetRid:
                type: ResourceIdentifier
                docs: |
                  A valid dataset resource identifier.
            returns: Set<String>
