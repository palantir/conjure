Conjure Intermediate Representation
===================================

The format described in [readme.md](readme.md) is for a human-friendly format that allows specification of
defaults and shorthand definitions. However, compilers should be implemented against an intermediate representation
(IR).

This representation has been chosen to facilitate the implementation of compilers across different languages and has
four key differences from the human-friendly representation:
1. It consists of a single file (even if the original input consisted of multiple files and included cross-file
references).
2. All service and type references are fully qualified (the combination of the type/service package and the type/service
name).
3. No defaults are assumed. The definition for each type, service, etc is self-contained.
4. All Conjure types (for example, "map<string, integer>") have a more structured representation to eliminate the need
for parsing.

The IR is expected to be in JSON format. The following describes version 1 of the IR format.

## Version 1

Version 1 of the IR format has four top-level keys: "version", "types", "services", and "errors".

### Version

The "version" key has an integer value and indicates the IR format version of the document (1 for version 1).

### Types

The "types" section is a list of type definitions, which can be one of the following: an alias definition, an enum
definition, an object definition, or an union definition. Each type definition should include two keys: "type" and one
of "alias", "enum", "object", or "union", depending on whether the type is an alias, an enum, an object, or an union
definition. The "type" key has a string value that should be one of "alias", "enum", "object", or "union", matching the
other key that is present.

The contents of the "alias", "enum", "object", and "union" keys are described below.

#### Aliases

An alias definition must have a "typeName" key describing the package and name of the type. It must also have an "alias"
key, with a value that is a representation of the aliased type (see
[Representation of Conjure Types](#representation-of-conjure-types)).

An alias definition may have a "docs" key containing string documentation for the alias type.

Example alias definition:
```json
{
  "types": [
    {
      "type": "alias",
      "alias": {
        "typeName": {
          "name": "ExampleAlias",
          "package": "com.palantir.foo"
        },
        "alias": {
          "type": "primitive",
          "primitive": "STRING"
        },
        "docs": "ExampleAlias is an alias of a string."
      }
    }
  ]
}
```

#### Enums

An enum definition must have a "typeName" key describing the package and name of the type. It must also have a "values"
key referring to a list of possible values for the enum. Each item in the list must have a "value" key
corresponding to the string representation of the value. Each item in the list may have a "docs" key containing
string documentation for the enum value.

An enum definition may have a "docs" key containing string documentation for the enum type.

Example enum definition:
```json
{
  "types": [
    {
      "type": "enum",
      "enum": {
        "typeName": {
          "name": "ExampleEnum",
          "package": "com.palantir.foo"
        },
        "values": [
          {
            "value": "FOO"
          },
          {
            "value": "BAR"
          }
        ],
        "docs": "Valid values for ExampleEnum include \"FOO\" and \"BAR\"."
      }
    }
  ]
}
```

#### Objects

An object definition must have a "typeName" key describing the package and name of the type. It must also have a
"fields" key referring to a list of field definitions. Each field definition must have a "fieldName" key with a string
value, and a "type" key with a value that is a representation of the field type (see
[Representation of Conjure Types](#representation-of-conjure-types)). Each field definition may have a "docs" key
containing string documentation for the field.

An object definition may have a "docs" key containing string documentation for the object.

Example object definition:
```json
{
  "types": [
    {
      "type": "object",
      "object": {
        "typeName": {
          "name": "ExampleObject",
          "package": "com.palantir.foo"
        },
        "fields": [
          {
            "fieldName": "description",
            "type": {
              "type": "primitive",
              "primitive": "STRING"
            }
          },
          {
            "fieldName": "exampleEnum",
            "type": {
              "type": "reference",
              "reference": {
                "name": "ExampleEnum",
                "package": "com.palantir.foo"
              }
            }
          }
        ],
        "docs": "ExampleObject has two fields, a string description and a reference to ExampleEnum."
      }
    }
  ]
}
```

#### Unions

An union definition must have a "typeName" key describing the package and name of the type. It must also have a "union"
key referring to list of field definitions. See the [objects section](#objects) for details on the field definition
format.

An union definition may have a "docs" key containing string documentation for the union type.

Example union definition:
```json
{
  "types": [
    {
      "type": "union",
      "union": {
        "typeName": {
          "name": "ExampleUnion",
          "package": "com.palantir.foo"
        },
        "union": [
          {
            "fieldName": "foo",
            "type": {
              "type": "primitive",
              "primitive": "INTEGER"
            }
          },
          {
            "fieldName": "bar",
            "type": {
              "type": "primitive",
              "primitive": "STRING"
            }
          }
        ],
        "docs": "ExampleUnion can either be an integer or a string."
      }
    }
  ]
}
```

#### Representation of Conjure Types

The following list enumerates valid Conjure types and provides examples of their representation.
1. primitive
1. optional
1. list
1. set
1. map
1. reference to a custom Conjure type
1. reference to an external, non-Conjure type

The representation of a primitive type must have a "type" key with a string value "primitive". It must also have a
"primitive" key with one of the following values:
1. STRING
1. DATETIME
1. INTEGER
1. DOUBLE
1. SAFELONG
1. BINARY
1. ANY
1. BOOLEAN
1. UUID
1. RID
1. BEARERTOKEN

String example:
```json
{
  "type": "primitive",
  "primitive": "STRING"
}
```

RID example:
```json
{
  "type": "primitive",
  "primitive": "RID"
}
```

The optional, list and set types share a similar representation, since each of these types encapsulates another type.
This representation must have a "type" key with a string value of "optional", "list", or "set". It must have a key
corresponding to this value, referring to an item description. The item description must have an "itemType" key that is
itself a reference to a Conjure type.

Example for a list of strings:
```json
{
  "type": "list",
  "list": {
    "itemType": {
      "type": "primitive",
      "primitive": "STRING"
    }
  }
}
```

Example for an optional string:
```json
{
  "type": "optional",
  "optional": {
    "itemType": {
      "type": "primitive",
      "primitive": "STRING"
    }
  }
}
```

The map type encapsulates two types. The representation must have a "type" key with a string value of "map". It must
also have a "map" key with two further keys, "keyType" and "valueType", that are themselves references to Conjure type.

Example for a map from string to integer:
```json
{
  "type": "map",
  "map": {
    "keyType": {
      "type": "primitive",
      "primitive": "STRING"
    },
    "valueType": {
      "type": "primitive",
      "primitive": "INTEGER"
    }
  }
}
```

Reference types refers to an existing Conjure type. The representation must have a "type" key with
a string value of "reference", and a "reference" key which describes the name and package of the referenced type. The
referenced type must be defined in the "types" section of the Conjure definition.

Example for a reference to `com.palantir.foo.FooRequest`:
```json
{
  "type": "reference",
  "reference": {
    "name": "FooRequest",
    "package": "com.palantir.foo"
  }
}
```

Example for a map from string to `com.palantir.foo.FooRequest`:
```json
{
  "type": "map",
  "map": {
    "keyType": {
      "type": "primitive",
      "primitive": "STRING"
    },
    "valueType": {
      "type": "reference",
      "reference": {
        "name": "FooRequest",
        "package": "com.palantir.foo"
      }
    }
  }
}
```

Finally, external references can be used to refer to non-Conjure types that are defined in
another language.  For example, when generating Java code, the type `com.palantir.foo.OldFooResponse` must
be present on the classpath in order to compile.  The "fallback" should be a
Conjure type definition which can be used to deserialize the JSON from an `com.palantir.foo.OldFooResponse`.
Note: these external references should be used sparingly because the serialization
behaviour of the external type cannot be guaranteed.

```json
{
  "type": "external",
  "external": {
    "externalReference": {
      "name": "OldFooResponse",
      "package": "com.palantir.foo"
    },
    "fallback": {
      "type": "primitive",
      "primitive": "STRING"
    }
  }
}
```

### Services

The "services" section is a list of service definitions. Each service definition must have a "serviceName" key
describing the package and name of the service. It must also have an "endpoints" key, which is a list of endpoint
definitions. Each endpoint definition must have the following keys:
- "endpointName": a string name for the endpoint
- "httpMethod": one of the strings "GET", "POST", "PUT", or "DELETE"
- "httpPath": a string describing the http path for the endpoint

Each endpoint definition may have the following keys:
- "auth": an object representing either header auth or cookie auth. The object must have a "type" key with value of
"header" or "cookie". If the value is "header", the object must also have a "header" key referring to an empty object.
If the value is "cookie", the object must also have a "cookie" key referring to a cookie auth definition. The cookie
auth definition must have a key "cookieName" with a string value that is the required cookie name.
- "args": a list of argument definitions. Each argument definition must have the keys "argName" (a string name for the
argument), "type" (a [representation](#representation-of-conjure-types) of the argument type) and "paramType", which
may be one of the types "PathParameterType", "QueryParameterType", "HeaderParameterType", or "BodyParameterType".
"HeaderParameterType" and "QueryParameterType" have a key "paramId" that specifies the corresponding header name or the query
parameter name. Each argument definition may have a "docs" key containing string documentation for the service.
Each argument definition may also have a "markers" key, consisting of a list of [types](#representation-of-conjure-types)
that serve as additional metadata for the argument.
- "returns": a [representation](#representation-of-conjure-types) of the return type of the endpoint
- "docs": string documentation for the endpoint
- "deprecated": a string explanation indicating the endpoint is deprecated and why

Each service definition may have a "docs" key containing string documentation for the service.

Example service definition:
```json
{
  "services": [
    {
      "serviceName": {
        "name": "WidgetService",
        "package": "com.palantir.widget"
      },
      "endpoints": [
        {
          "endpointName": "createWidget",
          "httpMethod": "POST",
          "httpPath": "/widgets",
          "auth": {
            "type": "header",
            "header": {}
          },
          "docs": "An endpoint for creating a widget. Requires an \"Authorization\" header."
        },
        {
          "endpointName": "getWidget",
          "httpMethod": "GET",
          "httpPath": "/widgets/{widgetRid}",
          "auth": {
            "type": "header",
            "header": {}
          },
          "args": [
            {
              "argName": "widgetRid",
              "type": {
                "type": "primitive",
                "primitive": "RID"
              },
              "paramType": "PATH"
            }
          ],
          "returns": {
            "type": "reference",
            "reference": {
              "name": "Widget",
              "package": "com.palantir.widget"
            }
          },
          "docs": "An endpoint for retrieving a widget. The RID of the desired widget is specified in the path of the request.\n"
        },
        {
          "endpointName": "getWidgets",
          "httpMethod": "GET",
          "httpPath": "/widgets",
          "auth": {
            "type": "header",
            "header": {}
          },
          "args": [
            {
              "argName": "createdAfter",
              "type": {
                "type": "primitive",
                "primitive": "DATETIME"
              },
              "paramType": {
                "type": "query",
                "query": {
                    "paramId": "createdAfter"
                }
              }
            }
          ],
          "returns": {
            "type": "list",
            "list": {
              "itemType": {
                "type": "reference",
                "reference": {
                  "name": "Widget",
                  "package": "com.palantir.widget"
                }
              }
            }
          },
          "docs": "An endpoint for retrieving all widgets, with optional filtering by the date of widget creation."
        }
      ],
      "docs": "API for creating and retrieving widgets."
    }
  ]
}
```

### Errors

The "errors" section is a list of error definitions. Each error definition must include the following keys:
- "code": a string in `UPPER_UNDERSCORE_CASE` and it must be one of the [ErrorType codes defined in
HTTP remoting](https://github.com/palantir/http-remoting-api/blob/develop/errors/src/main/java/com/palantir/remoting/api/errors/ErrorType.java#L38).
- "namespace": a string describing the namespace of the error and it must be in `UpperCamelCase`.
- "errorName": a `TypeName` definition describing the package and name of the error.

Each error definition may include the following keys:
- "docs": a string documentation of the error.
- "safeArgs": a list of safe arguments with a field definition as the list item type. See the [objects section](#objects) 
for details on the field definition format.
- "unsafeArgs": a list of unsafe arguments with a field definition as the list item type. See the [objects section](#objects) 
for details on the field definition format.

Note, the difference between safe and unsafe arguments are explained in the docs of [HTTP Remoting](https://github.com/palantir/http-remoting#error-propagation).

### Self-describing definition

See [ir.json](ir.json) for a definition of the IR format in the IR format. After a compiler has bootstrapped itself, it
may use this definition to generate code for deserializing the IR format.
