# Conjure YML JSON schema

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
