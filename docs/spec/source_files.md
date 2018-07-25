# Conjure YML source files

A Conjure definition is made up of one or more source [YAML](http://yaml.org/) files.

The files must end in `.yml`, but the actual name has no effect on generated code (it is only used for referencing types across files).  Here is a suggested structure:

```
your-project/src/main/conjure/foo.yml
your-project/src/main/conjure/bar.yml
your-project/src/main/conjure/baz.yml
```

Each YAML file may define multiple _types_, _services_ and _errors_. The file boundaries have no semantic value as the Conjure compiler will combine these into one single IR document.

The Conjure compiler expects these files to conform to the following JSON schema, but it also enforces some additional constraints (e.g. no complex types in path parameters).

## JSON schema

```js
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "types": {
      "$ref": "#/definitions/TypesDefinition"
    },
    "services": {
      "$ref": "#/definitions/ServicesDefinition"
    }
  },
  "definitions": {

    "TypesDefinition": {
      "type": "object",
      "properties": {
        "imports": {
          // TODO
        },
        "conjure-imports": {
          // TODO
        },
        "definitions": {
          // TODO
        },
      }
    },

    "ServicesDefinition": {
      "type": "object",
      "patternProperties": {
        "^.*$": {
          "$ref": "#/definitions/ServiceDefinition"
        }
      }
    },

    "ServiceDefinition": {
      "type": "object",
      "required": ["package", "base-path"],
      "properties": {
        "name": {
          "type": "string"
        },
        "package": {
          "type": "string"
        },
        "docs": {
          "type": "string"
        },
        "default-auth": {
          "$ref": "#/definitions/AuthDefinition"
        },
        "base-path": {
          "type": "string"
        },
        "endpoints": {
          "type": "object",
          "patternProperties": {
            "^.*$": {
              "$ref": "#/definitions/EndpointDefinition"
            }
          }
        }
      }
    },

    "AuthDefinition": {
       // TODO
    },

    "EndpointDefinition": {
      "type": "object",
      "required": ["http"],
      "properties": {
        "http": {
          "type": "string"
        },
        "auth": {
          "$ref": "#/definitions/AuthDefinition"
        },
        "args": {
          // TODO
        },
        "markers": {
          "type": "array",
          "itemType": {
            "type": "string"
          }
        },
        "returns": {
          "type": "string"
        },
        "docs": {
          "type": "string"
        },
        "deprecated": {
          "type": "string"
        },
      }
    }

  }
}
```
