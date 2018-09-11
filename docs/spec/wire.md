# Conjure Wire Specification

_This document defines how clients and servers should behave based on endpoints and types defined in a Conjure IR file._

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT
RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [BCP
14](https://tools.ietf.org/html/bcp14) [RFC2119](https://tools.ietf.org/html/rfc2119)
[RFC8174](https://tools.ietf.org/html/rfc8174) when, and only when, they appear in all capitals, as shown here.

For convenience, we define a _de-alias_ function which recursively collapses the Conjure _Alias_ type and is an identity function otherwise:

```
de-alias(Alias of T) -> de-alias(T)
de-alias(T) -> T
```

<!-- these are just markdown link definitions, they do not get rendered -->
[JSON format]: #json-format
[PLAIN format]: #plain-format
[CANONICAL format]: #canonical-format

## HTTP requests
This section assumes familiarity with HTTP concepts as defined in [RFC2616 Hypertext Transfer Protocol -- HTTP/1.1](https://tools.ietf.org/html/rfc2616).

1. **SSL/TLS** - Conjure clients MUST support requests using Transport Layer Security (TLS) and MAY optionally support HTTP requests. This ensures that any Conjure client implementation will be able to interact with any Conjure server implementation.

1. **HTTP Methods** - Conjure clients MUST support the following HTTP methods: `GET`, `POST`, `PUT`, and `DELETE`.

1. **Path parameters** - For Conjure endpoints that have user-defined path parameters, clients MUST interpolate values for each of these path parameters. Values MUST be serialized using the [PLAIN format][] and MUST also be [URL encoded](https://tools.ietf.org/html/rfc3986#section-2.1) to ensure reserved characters are transmitted unambiguously.

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

1. **Query parameters** - If an endpoint specifies one or more parameters of type `query`, clients MUST convert these (key,value) pairs into a [query string](https://tools.ietf.org/html/rfc3986#section-3.4) to be appended to the request URL. If a value of de-aliased type `optional<T>` is not present, then the key MUST be omitted from the query string.  Otherwise, the inner value MUST be serialized using the [PLAIN format][] and any reserved characters percent encoded.

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

1. **Body parameter** - If an endpoint defines an argument of type `body`, clients MUST serialize the user-provided value using the [JSON format][], UNLESS:
    - the de-aliased argument is type `binary`: the clients MUST write the raw binary bytes directly to the request body
    - the de-aliased argument is type `optional<T>` and the value is not present: it is RECOMMENDED to send an empty request body, although clients MAY alternatively send the JSON value `null`.
  It is RECOMMENDED to add a `Content-Length` header for [compatibility](https://tools.ietf.org/html/rfc2616#section-4.4) with HTTP/1.0 servers.

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

1. **Headers** - Conjure `header` parameters MUST be serialized in the [PLAIN format][] and transferred as [HTTP Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers). Header names are case insensitive. Parameters of Conjure type `optional<T>` MUST be omitted entirely if the value is not present, otherwise just serialized using the [PLAIN Format][].

    1. **Content-Type header** - For Conjure endpoints that define a `body` argument, a `Content-Type` header MUST be added.  If the body is of type `binary`, the content-type `application/octet-stream` MUST be used. Otherwise, clients MUST send `Content-Type: application/json`.

    1. **Accept header** - Clients MUST send an `Accept: application/json` header for all requests UNLESS the endpoint returns binary, in which case the client MUST send `Accept: application/octet-stream`. This ensures changes can be made to the wire format in a non-breaking way.

    1. **User-agent** - Requests MUST include a `User-Agent` header.

    1. **Header Authorization** - If an endpoint defines an `auth` field of type `header`, clients MUST send a header with name `Authorization` and case-sensitive value `Bearer {{string}}` where `{{string}}` is a user-provided string.

    1. **Cookie Authorization** - If an endpoint defines an `auth` field of type `cookie`, clients MUST send a cookie header with value `{{cookieName}}={{value}}`, where `{{cookieName}}` comes from the IR and `{{value}}` is a user-provided value.

    1. **Additional headers** - Clients MAY inject additional headers (e.g. for Zipkin tracing, or `Fetch-User-Agent`), as long as these do not clash with any headers already specified in the endpoint definition.

## HTTP responses
1. **Status codes** - Conjure servers MUST respond to successful requests with HTTP status [`200 OK`](https://tools.ietf.org/html/rfc2616#section-10.2.1) UNLESS:

    - the de-aliased return type is `optional<T>` and the value is not present: servers MUST send [`204 No Content`](https://tools.ietf.org/html/rfc2616#section-10.2.5).
    - the de-aliased return type is a `map`, `list` or `set`: it is RECOMMENDED to send `204` but servers MAY send `200` if the HTTP body is `[]` or `{}`.

    Using `204` in this way ensures that clients calling a Conjure endpoint with `optional<binary>` return type can differentiate between a non-present optional (`204`) and a present binary value containing zero bytes (`200`).

    Further non-successful status codes are defined in the Conjure errors section below.

1. **Response body** - Conjure servers MUST serialize return values using the [JSON format][] defined below, UNLESS:

    - the de-aliased return type is `optional<T>` and the value is not present: servers MUST omit the HTTP body.
    - the de-aliased return type is `binary`: servers MUST write the binary value using the [PLAIN format][].

1. **Content-Type header** - Conjure servers MUST send a `Content-Type` header according to the endpoint's return type:

    - if the de-aliased return type is `binary`, servers MUST send `Content-Type: application/octet-stream`,
    - otherwise, servers MUST send `Content-Type: application/json;charset=utf-8`.

1. **Conjure errors** - In order to send a Conjure error, servers MUST serialize the error using the [JSON format][]. In addition, servers MUST send a http status code corresponding to the error's code.

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
1. **Forward compatible clients** - Clients MUST tolerate extra headers, unknown fields in JSON objects and unknown variants of Conjure enums and unions. This ensures that old clients will continue to work, even if a newer version of a server includes extra fields in a JSON response.

1. **Client base URL** - Clients MUST allow users to specify a base URL for network requests because Conjure endpoint definitions only include domain-agnostic http path suffixes.

1. **Servers reject unknown fields** - Servers MUST request reject all unexpected JSON fields. This helps developers notice bugs and mistakes quickly, instead of allowing silent failures.

1. **Servers tolerate extra headers** - Servers MUST tolerate extra headers not defined in the endpoint definition. This is important because proxies frequently modify requests to include additional headers, e.g. `X-Forwarded-For`.

1. **Round-trip of unknown variants** - TODO ask Mark.

1. **CORS and HTTP preflight requests** - In order to be compatible with browser [preflight requests](https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request), servers MUST support the HTTP `OPTIONS` method .

1. **HTTP/2** - The Conjure wire specification is compatible with HTTP/2, but it is not required.


## JSON format
This format describes how all Conjure types are serialized into and deserialized from JSON ([RFC 7159](https://tools.ietf.org/html/rfc7159)).

**Built-in types:**

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

**Container types:**

Conjure&nbsp;Type | JSON&nbsp;Type                | Comments |
----------------- | ----------------------------- | -------- |
`optional<T>`     | `JSON(T)`&nbsp;or&nbsp;`null` | If present, MUST be serialized as `JSON(e)`. If the value appears inside a JSON Object, then the corresponding key SHOULD be omitted. Alternatively, the field MAY be set to `null`. Inside JSON Array, a non-present Conjure optional value MUST be serialized as JSON `null`.
`list<T>`         | Array                         | Each element, e, of the list is serialized using `JSON(e)`. Order must be maintained.
`set<T>`          | Array                         | Each element, e, of the set is serialized using `JSON(e)`. Order is insignificant but it is RECOMMENDED to preserve order where possible. The Array MUST NOT contain duplicate elements (as defined by the canonical format below).
`map<K, V>`       | Object                        | A key k is serialized as `PLAIN(k)`. Values are serialized using `JSON(v)`. For any (key,value) pair where the value is of de-aliased type `optional<?>`, the key SHOULD be omitted from the JSON Object if the value is absent, however, the key MAY remain if the value is set to `null`. The Object must not contain duplicate keys (as defined by the canonical format below).

**Named types:**

Conjure&nbsp;Type | JSON&nbsp;Type | Comments |
----------------- | ---------------| -------- |
_Object_          | Object         | Keys are obtained from the Conjure object's fields and values using `JSON(v)`. For any (key,value) pair where the value is of `optional<?>` type, the key MUST be omitted from the JSON Object if the value is absent.
_Enum_            | String         | String representation of the enum value
_Union_           | Object         | (See union JSON format below)
_Alias_(x)        | `JSON(x)`      | Aliases are serialized exactly the same way as their corresponding de-aliased Conjure types.

**Union JSON format:**

Conjure Union types are serialized as JSON objects with exactly two keys:

1. `type` key - this determines the variant of the union, e.g. `foo`
1. `{{variant}}` key - this key MUST match the variant determined above, and the value is `JSON(v)`.
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

**Conjure Errors:**
Conjure Errors are serialized as JSON objects with the following keys:
1. `errorCode` - this MUST match one of the Conjure error code below.
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

**Deserialization**

- **Disallow `null` for Conjure `any` type** - The JSON value `null` MUST NOT be deserialized into the Conjure type `any`.

- **Coercing JSON `null` / absent to Conjure types** - If a JSON key is absent or the value is `null`, two rules apply:

  - Conjure `optional`, `list`, `set`, `map` types MUST be initialized to their empty variants,
  - Attempting to coerce null/absent to any other Conjure type MUST cause an error, i.e. missing JSON keys SHOULD cause an error.

  _Note: this rule means that the Conjure type `optional<optional<T>>` would be ambiguously deserialized from `null`: it could be  `Optional.empty()` or `Optional.of(Optional.empty())`. To avoid this ambiguity, Conjure ensures definitions do not contain this type._

- **Dedupe `set` / `map` keys using CANONICAL format** - When deserializing Conjure `set` or `map` keys, equivalence of two items can be determined by converting the JSON value to the [CANONICAL format][] and then comparing byte equality.

- **No automatic casting** - Unexpected JSON types SHOULD NOT be automatically coerced to a different expected type. For example, if a Conjure definition specifies a field is `boolean`, the JSON strings `"true"` and `"false"` SHOULD NOT be accepted.


## PLAIN format
This format describes an unquoted representation of a _subset_ of de-aliased Conjure types.

Conjure&nbsp;Type | PLAIN&nbsp;Type                               |
----------------- | ----------------------------------------------|
`bearertoken`     | unquoted String
`binary`          | raw binary bytes
`boolean`         | Boolean
`datetime`        | unquoted String
`double`          | Number or `NaN` or `Infinity` or `-Infinity`
`integer`         | Number
`rid`             | unquoted String
`safelong`        | Number
`string`          | unquoted String
`uuid`            | unquoted String
_Enum_            | unquoted variant name
`any`             | UNSUPPORTED
`optional<T>`     | UNSUPPORTED
`list<T>`         | UNSUPPORTED
`set<T>`          | UNSUPPORTED
`map<K, V>`       | UNSUPPORTED
_Object_          | UNSUPPORTED
_Union_           | UNSUPPORTED


## CANONICAL Format
The Canonical format defines an additional representation of Conjure-defined types, collections and primitives for use
when a type has multiple valid formats that are conceptually equivalent. Implementations of Conjure clients/servers
MUST convert types (even if implicitly) from their JSON/Plain format to their canonical form when determining equality.

Conjure Type | Canonical Representation                           | Comments |
------------ | ------------------------                           | -------- |
bearertoken  | No ambiguity                                       | In accordance with [RFC 7519](https://tools.ietf.org/html/rfc7519).
binary       | No ambiguity                                       | Represented as a [Base64](https://tools.ietf.org/html/rfc4648#section-4) encoded string, except for when it is a request/response body where it is raw binary.
boolean      | No ambiguity                                       |
datetime     | Formatted according to `YYYY-MM-DDTHH:mm:ss±hh:mm` | In accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).
double       | No ambiguity                                       | As defined by [IEEE 754 standard](http://ieeexplore.ieee.org/document/4610935/).
integer      | No ambiguity                                       | Signed 32 bits, value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
rid          | No ambiguity                                       | In accordance with the [Resource Identifier](https://github.com/palantir/resource-identifier) definition.
safelong     | No ambiguity                                       | Integer with value ranging from -2<sup>53</sup> + 1 to 2<sup>53</sup> - 1.
string       | No ambiguity                                       |
uuid         | No ambiguity                                       | In accordance with [RFC 4122](https://tools.ietf.org/html/rfc4122).
any          | N/A                                                | May be any of the above types or an `object` with any fields.

**Examples:**
```
Conjure Type |     JSON representation     |  CANONICAL representation   |    PLAIN representation    |
------------ | --------------------------- | --------------------------- | -------------------------- |
datetime     | "2018-07-19T08:11:21Z"      | "2018-07-19T08:11:21+00:00" | 2018-07-19T08:11:21Z       |
datetime     | "2018-07-19T08:11:21+00:00" | "2018-07-19T08:11:21+00:00" | 2018-07-19T08:11:21+00:00  |
datetime     | "2018-07-19T08:11:21-00:00" | "2018-07-19T08:11:21+00:00" | 2018-07-19T08:11:21-00:00  |
datetime     | "20180719T081121Z"          | "2018-07-19T08:11:21+00:00" | 20180719T081121Z           |
datetime     | "2018-07-19T05:11:21+03:00" | "2018-07-19T05:11:21+03:00" | 2018-07-19T05:11:21+03:00  |
double       | 1                           | 1.0                         | 1                          |
double       | 1.00000                     | 1.0                         | 1.000000                   |
double       | 1.2345678                   | 1.2345678                   | 1.2345678                  |
double       | 1.23456780                  | 1.2345678                   | 1.23456780                 |
double       | "NaN"                       | "NaN"                       | NaN                        |
double       | "Infinity"                  | "Infinity"                  | Infinity                   |
double       | "-Infinity"                 | "-Infinity"                 | -Infinity                  |
```
