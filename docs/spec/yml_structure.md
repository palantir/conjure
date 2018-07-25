# Conjure YML structure

A Conjure definition is made up of one or more source [YAML](http://yaml.org/) files.

The files must end in `.yml`, but the actual name has no effect on generated code (it is only used for referencing types across files).  Here is a suggested structure:

```
your-project/src/main/conjure/foo.yml
your-project/src/main/conjure/bar.yml
your-project/src/main/conjure/baz.yml
```

Each YAML file may define multiple _types_, _services_ and _errors_.  The Conjure compiler expects these files to conform to the following JSON schema, but it also enforces some additional constraints (e.g. no complex types in path parameters).

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
        "imports": {},
        "conjure-imports": {},
        "definitions": {},
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
      "properties": {
        "name": {
          "type": "string"
        },
        "package": {},
        "docs": {},
        "default-auth": {},
        "base-path": {},
        "endpoints": {}
      }
    }

  }
}
```
