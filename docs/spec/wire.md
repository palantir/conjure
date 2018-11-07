# Conjure Wire Specification

_This document defines how clients and servers should behave based on endpoints and types defined in a Conjure IR file._

For convenience, we define a _de-alias_ function which recursively collapses the Conjure _Alias_ type and is an identity function otherwise:

```
de-alias(Alias of T) -> de-alias(T)
de-alias(T) -> T
```

<!-- these are just markdown link definitions, they do not get rendered -->
[JSON format]: #json-format
[PLAIN format]: #plain-format
[Canonical JSON format]: #canonical-json-format

## HTTP requests
This section assumes familiarity with HTTP concepts as defined in [RFC2616 Hypertext Transfer Protocol -- HTTP/1.1](https://tools.ietf.org/html/rfc2616).

1. **SSL/TLS** - Conjure clients must support requests using Transport Layer Security (TLS) and may optionally support HTTP requests. This ensures that any Conjure client implementation will be able to interact with any Conjure server implementation.

1. **HTTP Methods** - Conjure clients must support the following HTTP methods: `GET`, `POST`, `PUT`, and `DELETE`.

1. **Path parameters** - For Conjure endpoints that have user-defined path parameters, clients must interpolate values for each of these path parameters. Values must be serialized using the [PLAIN format][] and must also be [URL encoded](https://tools.ietf.org/html/rfc3986#section-2.1) to ensure reserved characters are transmitted unambiguously.

    For example, the following Conjure endpoint contains several path parameters of different types:
    ```yaml
    demoEndpoint:
      http: GET /demo/{file}/rev/{revision}
      args:
        file: string
        revision: integer
    ```

    In this example, the `file` argument with value `var/conf/install.yml` is percent encoded:
    ```
    /demo/var%2Fconf%2Finstall.yml/rev/53
    ```

1. **Query parameters** - If an endpoint specifies one or more parameters of type `query`, clients must convert these (key,value) pairs into a [query string](https://tools.ietf.org/html/rfc3986#section-3.4) to be appended to the request URL. If a value of de-aliased type `optional<T>` is not present, then the key must be omitted from the query string.  Otherwise, the inner value must be serialized using the [PLAIN format][] and any reserved characters percent encoded.

    For example, the following Conjure endpoint contains two query parameters:
    ```yaml
    demoEndpoint:
      http: GET /recipes
      args:
        filter: optional<string>
        limit: optional<integer>
    ```

    These examples illustrate how an `optional<T>` value should be omitted if the value is not present
    ```
    /recipes?filter=Hello%20World&limit=10
    /recipes?filter=Hello%20World
    /recipes
    ```

1. **Body parameter** - If an endpoint defines an argument of type `body`, clients must serialize the user-provided value using the [JSON format][], unless:
    - the de-aliased argument is type `binary`: the clients must write the raw binary bytes directly to the request body
    - the de-aliased argument is type `optional<T>` and the value is not present: it is recommended to send an empty request body, although clients may alternatively send the JSON value `null`.
  It is recommended to add a `Content-Length` header for [compatibility](https://tools.ietf.org/html/rfc2616#section-4.4) with HTTP/1.0 servers.

    For example, the following Conjure endpoint defines a request body:
    ```yaml
    demoEndpoint:
      http: POST /names
      args:
        newName:
          type: optional<string>
          param-type: body
    ```

    In this case, if `newName` is not present, then the [JSON format][] allows clients to send a HTTP body containing `null` or send an empty body.  If `newName` is present, then the body will include JSON quotes, e.g. `"Joe blogs"`.

1. **Headers** - Conjure `header` parameters must be serialized in the [PLAIN format][] and transferred as [HTTP Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers). Header names are case insensitive. Parameters of Conjure type `optional<T>` must be omitted entirely if the value is not present, otherwise just serialized using the [PLAIN Format][].

    1. **Content-Type header** - For Conjure endpoints that define a `body` argument, a `Content-Type` header must be added.  If the body is of type `binary`, the content-type `application/octet-stream` must be used. Otherwise, clients must send `Content-Type: application/json`. Note that the default encoding for `application/json` content type is [`UTF-8`](http://www.ietf.org/rfc/rfc4627.txt).

    1. **Accept header** - Clients must send an `Accept: application/json` header for all requests unless the endpoint returns binary, in which case the client must send `Accept: application/octet-stream`. This ensures changes can be made to the wire format in a non-breaking way.

    1. **User-agent** - Requests must include a `User-Agent` header.

    1. **Header Authorization** - If an endpoint defines an `auth` field of type `header`, clients must send a header with name `Authorization` and case-sensitive value `Bearer {{string}}` where `{{string}}` is a user-provided string.

    1. **Cookie Authorization** - If an endpoint defines an `auth` field of type `cookie`, clients must send a cookie header with value `{{cookieName}}={{value}}`, where `{{cookieName}}` comes from the IR and `{{value}}` is a user-provided value.

    1. **Additional headers** - Clients may inject additional headers (e.g. for Zipkin tracing, or `Fetch-User-Agent`), as long as these do not clash with any headers already specified in the endpoint definition.

## HTTP responses
1. **Status codes** - Conjure servers must respond to successful requests with HTTP status [`200 OK`](https://tools.ietf.org/html/rfc2616#section-10.2.1) unless:

    - the de-aliased return type is `optional<T>` and the value is not present: servers must send [`204 No Content`](https://tools.ietf.org/html/rfc2616#section-10.2.5).
    - the de-aliased return type is a `map`, `list` or `set`: it is recommended to send `204` but servers may send `200` if the HTTP body is `[]` or `{}`.

    Using `204` in this way ensures that clients calling a Conjure endpoint with `optional<binary>` return type can differentiate between a non-present optional (`204`) and a present binary value containing zero bytes (`200`).

    Further non-successful status codes are defined in the Conjure errors section below.

1. **Response body** - Conjure servers must serialize return values using the [JSON format][] defined below, unless:

    - the de-aliased return type is `optional<T>` and the value is not present: servers must omit the HTTP body.
    - the de-aliased return type is `binary`: servers must write the raw bytes as an octet-stream.

1. **Content-Type header** - Conjure servers must send a `Content-Type` header according to the endpoint's return type:

    - if the de-aliased return type is `binary`, servers must send `Content-Type: application/octet-stream`,
    - otherwise, servers must send `Content-Type: application/json`.

1. **Conjure errors** - In order to send a Conjure error, servers must serialize the error using the [JSON format][]. In addition, servers must send a http status code corresponding to the error's code.

    Conjure Error code         | HTTP Status code |
    -------------------------- | ---------------- |
    PERMISSION_DENIED          | 403
    INVALID_ARGUMENT           | 400
    NOT_FOUND                  | 404
    CONFLICT                   | 409
    REQUEST_ENTITY_TOO_LARGE   | 413
    FAILED_PRECONDITION        | 500
    INTERNAL                   | 500
    TIMEOUT                    | 500
    CUSTOM_CLIENT              | 400
    CUSTOM_SERVER              | 500


## Behaviour
1. **Forward compatible clients** - Clients must tolerate extra headers, unknown fields in JSON objects and unknown variants of Conjure enums and unions. This ensures that old clients will continue to work, even if a newer version of a server includes extra fields in a JSON response.

1. **Client base URL** - Clients must allow users to specify a base URL for network requests because Conjure endpoint definitions only include domain-agnostic http path suffixes.

1. **Servers reject unknown fields** - Servers must request reject all unexpected JSON fields. This helps developers notice bugs and mistakes quickly, instead of allowing silent failures.

1. **Servers tolerate extra headers** - Servers must tolerate extra headers not defined in the endpoint definition. This is important because proxies frequently modify requests to include additional headers, e.g. `X-Forwarded-For`.

1. **Round-trip of unknown variants** - Clients should be able to round trip unknown variants of enums and unions.

1. **CORS and HTTP preflight requests** - In order to be compatible with browser [preflight requests](https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request), servers must support the HTTP `OPTIONS` method .

1. **HTTP/2** - The Conjure wire specification is compatible with HTTP/2, but it is not required.


## JSON format
This format describes how all Conjure types are serialized into and deserialized from JSON ([RFC 7159](https://tools.ietf.org/html/rfc7159)).

#### Built-in types

Conjure&nbsp;Type | JSON Type                                          | Comments |
----------------- | ---------------------------------------------------| -------- |
`bearertoken`     | String                                             | In accordance with [RFC 6750](https://tools.ietf.org/html/rfc6750#section-2.1).
`binary`          | String                                             | Represented as a Base64 encoded string in accordance with [RFC 4648](https://tools.ietf.org/html/rfc4648#section-4).
`boolean`         | Boolean                                            |
`datetime`        | String                                             | In accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).
`double`          | Number or `"NaN"` or `"Infinity"` or `"-Infinity"` | As defined by [IEEE 754 standard](http://ieeexplore.ieee.org/document/4610935/).
`integer`         | Number                                             | Signed 32 bits, value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
`rid`             | String                                             | In accordance with the [Resource Identifier](https://github.com/palantir/resource-identifier) definition.
`safelong`        | Number                                             | Integer with value ranging from -2<sup>53</sup> + 1 to 2<sup>53</sup> - 1.
`string`          | String                                             | UTF-8 string
`uuid`            | String                                             | In accordance with [RFC 4122](https://tools.ietf.org/html/rfc4122).
`any`             | N/A                                                | May be any of the above types or an `object` with any fields.

#### Container types

Conjure&nbsp;Type | JSON&nbsp;Type                | Comments |
----------------- | ----------------------------- | -------- |
`optional<T>`     | `JSON(T)`&nbsp;or&nbsp;`null` | If present, must be serialized as `JSON(e)`. If the value appears inside a JSON Object, then the corresponding key should be omitted. Alternatively, the field may be set to `null`. Inside JSON Array, a non-present Conjure optional value must be serialized as JSON `null`.
`list<T>`         | Array                         | Each element, e, of the list is serialized using `JSON(e)`. Order must be maintained.
`set<T>`          | Array                         | Each element, e, of the set is serialized using `JSON(e)`. Order is insignificant but it is recommended to preserve order where possible. The Array must not contain duplicate elements (as defined by the canonical format below).
`map<K, V>`       | Object                        | A key k is serialized as a string with contents `PLAIN(k)`. Values are serialized using `JSON(v)`. For any (key,value) pair where the value is of de-aliased type `optional<?>`, the key should be omitted from the JSON Object if the value is absent, however, the key may remain if the value is set to `null`. The Object must not contain duplicate keys (as defined by the canonical format below).

#### Named types

Conjure&nbsp;Type | JSON&nbsp;Type | Comments |
----------------- | ---------------| -------- |
_Object_          | Object         | Keys are obtained from the Conjure object's fields and values using `JSON(v)`. For any (key,value) pair where the value is of `optional<?>` type, the key must be omitted from the JSON Object if the value is absent.
_Enum_            | String         | String representation of the enum value
_Union_           | Object         | (See union JSON format below)
_Alias_(x)        | `JSON(x)`      | Aliases are serialized exactly the same way as their corresponding de-aliased Conjure types.

#### Union JSON format

Conjure Union types are serialized as JSON objects with exactly two keys:

1. `type` key - this determines the variant of the union, e.g. `foo`
1. `{{variant}}` key - this key must match the variant determined above, and the value is `JSON(v)`.
  Example union type definition:
  ```yaml
  types:
    definitions:
      objects:

        MyUnion:
          union:
            foo: boolean
            bar: list<string>
  ```
  Example union type in JSON representation:
  ```json
  // In this example the variant is `foo` and the inner type is a Conjure `boolean`.
  {
    "type": "foo",
    "foo": true
  }

  // In this example, the variant is `bar` and the inner type is a Conjure `list<string>`
  {
    "type": "bar",
    "bar": ["Hello", "world"]
  }
  ```

#### Conjure Errors
Conjure Errors are serialized as JSON objects with the following keys:
1. `errorCode` - this must match one of the Conjure error code below.
  ```
  PERMISSION_DENIED
  INVALID_ARGUMENT
  NOT_FOUND
  CONFLICT
  REQUEST_ENTITY_TOO_LARGE
  FAILED_PRECONDITION
  INTERNAL
  TIMEOUT
  CUSTOM_CLIENT
  CUSTOM_SERVER
  ```
1. `errorName` - this is a fixed name identifying the error, e.g. `Recipe:RecipeNotFound`.
1. `errorInstanceId` - this provides a unique identifier, `uuid` type, for this error instance.
1. `parameters` - this map provides additional information regarding the nature of the error.

  Example error type definition:
  ```yaml
  types:
    definitions:
      errors:
        RecipeNotFound:
          namespace: Recipe
          code: NOT_FOUND
          safe-args:
            name: RecipeName
  ```
  Example error type in JSON presentation:
  ```json
  {
      "errorCode": "NOT_FOUND",
      "errorName": "Recipe:RecipeNotFound",
      "errorInstanceId": "xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx",
      "parameters": {
          "name": "roasted broccoli with garlic"
      }
  }
  ```

#### Deserialization

- **Coercing JSON `null` / absent to Conjure types** - If a JSON key is absent or the value is `null`, two rules apply:

  - Conjure `optional`, `list`, `set`, `map` types must be initialized to their empty variants,
  - Attempting to coerce null/absent to any other Conjure type must cause an error, i.e. missing JSON keys must cause an error.

  _Note: this rule means that the Conjure type `optional<optional<T>>` would be ambiguously deserialized from `null`: it could be  `Optional.empty()` or `Optional.of(Optional.empty())`. To avoid this ambiguity, Conjure ensures definitions do not contain this type._

- **Dedupe `set` / `map` keys using [Canonical JSON format][]** - When deserializing Conjure `set` or `map` keys, equivalence of two items can be determined by converting the JSON value to the Canonical JSON format and then comparing byte equality.

- **No automatic casting** - Unexpected JSON types should not be automatically coerced to a different expected type. For example, if a Conjure definition specifies a field is `boolean`, the JSON strings `"true"` and `"false"` should not be accepted.


## PLAIN format

The PLAIN format allows representing conjure values in an unstructured way.
Only a subset of de-aliased Conjure types, listed below, have a PLAIN format representation.

Conjure&nbsp;Type | PLAIN&nbsp;Representation                     | Comments |
----------------- | ----------------------------------------------| -------- |
`bearertoken`     | unquoted String                               | In accordance with [RFC 6750](https://tools.ietf.org/html/rfc6750#section-2.1).
`binary`          | unquoted String                               | Represented as a Base64 encoded string in accordance with [RFC 4648](https://tools.ietf.org/html/rfc4648#section-4).
`boolean`         | `true` or `false`                             |
`datetime`        | unquoted String                               | In accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).
`double`          | Number or `NaN` or `Infinity` or `-Infinity`  | As defined by [IEEE 754 standard](http://ieeexplore.ieee.org/document/4610935/).
`integer`         | Number                                        | Signed 32 bits, value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
`rid`             | unquoted String                               | In accordance with the [Resource Identifier](https://github.com/palantir/resource-identifier) definition.                    
`safelong`        | Number                                        | Integer with value ranging from -2<sup>53</sup> + 1 to 2<sup>53</sup> - 1.           
`string`          | unquoted String                               | UTF-8 string                    
`uuid`            | unquoted String                               | In accordance with [RFC 4122](https://tools.ietf.org/html/rfc4122).                    
_Enum_            | unquoted variant name                         | UTF-8 string                          


## Canonical JSON Format

The Canonical JSON format is a constrained version of the [JSON format][] which disambiguates values for 
types which have multiple distinct representations that are conceptually equivalent. 
Implementations of Conjure clients/servers must convert types (even if implicitly) from their JSON/Plain format to 
their canonical form when determining equality.

Aside from the cases described below, the canonical representation is the same as the [JSON representation][JSON format].

#### Canonical double

The canonical JSON format of a double must conform to these constraints:

1. Non-scientific notation must be used
1. At least one decimal point must be used, even if it is `.0`
1. No superfluous trailing `0` decimals outside of the above case

**Examples**:

|     JSON representation     |  Canonical representation   |
| --------------------------- | --------------------------- |
| `-0`                        | `-0.0`                      |
| `0`                         | `0.0`                       |
| `1`                         | `1.0`                       |
| `1.00000`                   | `1.0`                       |
| `"1e1"`                     | `10.0`                      |
| `1.2345678`                 | `1.2345678`                 |
| `1.23456780`                | `1.2345678`                 |
| `"NaN"`                     | `"NaN"`                     |
| `"Infinity"`                | `"Infinity"`                |
| `"-Infinity"`               | `"-Infinity"`               |

#### Canonical datetime

The canonical JSON format of a datetime is a string formatted according to `YYYY-MM-DDTHH:mm:ss±hh:mm`, in accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).

**Examples**:

|     JSON representation       |  Canonical representation     |
| ----------------------------- | ----------------------------- |
| `"2018-07-19T08:11:21Z"`      | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T08:11:21+00:00"` | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T08:11:21-00:00"` | `"2018-07-19T08:11:21+00:00"` |
| `"20180719T081121Z"`          | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T05:11:21+03:00"` | `"2018-07-19T05:11:21+03:00"` |
