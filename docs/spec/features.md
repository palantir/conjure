# Features

## HTTP endpoints

- GET, PUT, POST, DELETE
- Query parameters
- Path parameters
- Headers
- Cookie auth

## Types

You can define APIs using the following types:

- Complex types:
  - `list<T>`
  - `map<K, V>`
  - `optional<T>`
  - `set<T>`
  - Enum
  - Union

- Primitives
  - `any`
  - `bearertoken`
  - `binary`
  - `boolean`
  - `datetime`
  - `double`
  - `integer`
  - `rid`
  - `safelong`
  - `string`
  - `uuid`

- Migration types:
  - ExternalReference

## Example

Here's an example Conjure YML file that uses many of the above features:

```yaml
services:
  ExampleService:
    name: Example Service
    package: com.palantir.example
    path: /example
    endpoints:

      # The simplest Conjure endpoints can be specified by choosing a name, e.g. 'helloWorld' and a http path
      helloWorld:
        http: GET /notify-something

      # In practise, many endpoints have a 'return' type.
      demoReturnTypes:
        http: POST /helloWorld
        returns: HelloReponse

      # You can also specify path parameters, just use curly braces and make sure to reference your path param
      # in the 'args' section
      demoPathParams:
        http: DELETE /recipes/{id}
        args:
          id: integer

      # Query parameters are also supported, just specify `param-type: query`.
      # This will result in calls like https://your-server/example/recipes/latest?limit=10
      demoQueryParam:
        http: GET /recipes/latest
        returns: LatestResponse
        args:
          limit:
            type: optional<integer>
            param-type: query

      # Requests may specify well typed body args, using  `param-type: body`.
      demoBody:
        http: POST /recipes
        returns: CreateRecipeResponse
        args:
          createRecipe:
            type: CreateRecipeRequest
            param-type: body

types:
  definitions:
    default-package: com.palantir.example
    objects:
      HelloResponse:
        fields:
          greeting: string

      LatestResponse:
        fields:
          id: integer

      CreateRecipeRequest:
        fields:
          foo: string

      CreateRecipeResponse:
        fields:
          id: safelong
```
