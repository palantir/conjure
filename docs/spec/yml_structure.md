# Conjure YML structure

A Conjure definition is made up of YML files.

## JSON schema

```json
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
