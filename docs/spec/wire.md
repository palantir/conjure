# Conjure Wire Specification
_This document defines how clients and servers should behave based on endpoints and types defined in a Conjure IR file._

<!-- WARNING: the markdown titles below are used to derive permalinks, changing them will break links -->

<!--
Generated using https://github.com/jonschlinkert/markdown-toc:
  $ markdown-toc docs/spec/wire.md --no-firsth1
-->

<!-- toc -->

- [1. Conventions](#_1-conventions)
- [2. HTTP requests](#_2-http-requests)
  * [2.1. Path parameters](#_21-path-parameters)
  * [2.2. Query parameters](#_22-query-parameters)
  * [2.3. Body parameter](#_23-body-parameter)
  * [2.4. Headers](#_24-headers)
    + [2.4.1. Content-Type Header](#_241-content-type-header)
    + [2.4.2. Accept header](#_242-accept-header)
    + [2.4.3. User-agent](#_243-user-agent)
    + [2.4.4. Header Authorization](#_244-header-authorization)
    + [2.4.5. Cookie Authorization](#_245-cookie-authorization)
    + [2.4.6. Additional headers](#_246-additional-headers)
- [3. HTTP responses](#_3-http-responses)
  * [3.1. Status codes](#_31-status-codes)
  * [3.2. Response body](#_32-response-body)
  * [3.3. Content-Type header](#_33-content-type-header)
  * [3.4. Conjure errors](#_34-conjure-errors)
- [4. Behaviour](#_4-behaviour)
  * [4.1. Forward compatible clients](#_41-forward-compatible-clients)
  * [4.2. Servers reject unknown fields](#_42-servers-reject-unknown-fields)
  * [4.3. Servers tolerate extra headers](#_43-servers-tolerate-extra-headers)
  * [4.4. Round-trip of unknown variants](#_44-round-trip-of-unknown-variants)
  * [4.5. CORS and HTTP preflight requests](#_45-cors-and-http-preflight-requests)
  * [4.6. HTTP/2](#_46-http2)
  * [4.7. Clients tolerate void endpoints returning JSON](#_47-clients-tolerate-void-endpoints-returning-json)
- [5. JSON format](#_5-json-format)
  * [5.1. Built-in types](#_51-built-in-types)
  * [5.2. Container types](#_52-container-types)
  * [5.3. Named types](#_53-named-types)
  * [5.4. Union JSON format](#_54-union-json-format)
  * [5.5. Conjure Errors](#_55-conjure-errors)
  * [5.6. Deserialization](#_56-deserialization)
    + [5.6.1. Coercing JSON `null` / absent to Conjure types](#_561-coercing-json-null--absent-to-conjure-types)
    + [5.6.2. No automatic casting](#_562-no-automatic-casting)
- [6. Smile format](#_6-smile-format)
  * [6.1. Built-in types](#_61-built-in-types)
  * [6.2. Container types](#_62-container-types)
  * [6.3. Named types](#_63-named-types)
  * [6.4. Union Smile format](#_64-union-smile-format)
  * [6.5. Deserialization](#_65-deserialization)
- [7. PLAIN format](#_7-plain-format)

<!-- tocstop -->

<!-- these are just markdown link definitions, they do not get rendered -->
[JSON format]: #5-json-format
[Smile format]: #6-smile-format
[PLAIN format]: #7-plain-format
[URL encoded]: https://tools.ietf.org/html/rfc3986#section-2.1
[Path parameters]: ./conjure_definitions.md#path-templating


## 1. Conventions
For convenience, we define a _de-alias_ function which recursively collapses the Conjure _Alias_ type and is an identity function otherwise:

```
de-alias(Alias of T) -> de-alias(T)
de-alias(T) -> T
```

## 2. HTTP requests
_This section assumes familiarity with HTTP concepts as defined in [RFC2616 Hypertext Transfer Protocol -- HTTP/1.1](https://tools.ietf.org/html/rfc2616)._

### 2.1. Path parameters
[Path parameters][] are interpolated into the path string, where values are serialized using the [PLAIN format][] and must also be [URL encoded][] to ensure reserved characters are transmitted unambiguously.

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

### 2.2. Query parameters
Parameters of type `query` must be translated into a [query string](https://tools.ietf.org/html/rfc3986#section-3.4), with the Conjure `paramId` used as the query key. If a value of de-aliased type `optional<T>` is not present, then the key must be omitted from the query string.  Otherwise, the inner value must be serialized using the [PLAIN format][] and any reserved characters [URL encoded][].

For example, the following Conjure endpoint contains some query parameters:
```yaml
demoEndpoint:
  http: GET /recipes
  args:
    filter:
      param-type: query
      type: optional<string>
    limit:
      param-type: query
      type: optional<integer>
    categories:
      param-id: category
      param-type: query
      type: list<string>
```

These examples illustrate how an `optional<T>` value should be omitted if the value is not present
```
/recipes?filter=Hello%20World&limit=10
/recipes?filter=Hello%20World
/recipes
```

For query parameters of type `list<T>` or `set<T>`, each value should result in one `key=value` pair, separated by `&`. Note that the order of values must be preserved for `list<T>`, but is semantically unimportant for `set<T>`. E.g.:
```
/recipes?category=foo&category=bar&category=baz
```

### 2.3. Body parameter
The endpoint `body` argument must be serialized using the [JSON format][], unless:
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

In this case, if `newName` is not present, then the [JSON format][] allows clients to send an HTTP body containing `null` or send an empty body.  If `newName` is present, then the body will include JSON quotes, e.g. `"Joe blogs"`.

### 2.4. Headers
Conjure `header` parameters must be serialized in the [PLAIN format][] and transferred as [HTTP Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers). Header names are case insensitive. Parameters of Conjure type `optional<T>` must be omitted entirely if the value is not present, otherwise just serialized using the [PLAIN Format][].

#### 2.4.1. Content-Type Header
A `Content-Type` header must be added if the endpoint defines a `body` argument.
- If the de-aliased body type is `binary`, the Content-Type `application/octet-stream` must be used.
- Otherwise, clients must use `application/json`.

_Note that the default encoding for `application/json` content type is [`UTF-8`](http://www.ietf.org/rfc/rfc4627.txt)._

#### 2.4.2. Accept header
Clients must send an `Accept` header for all requests. For requests to endpoints returning binary, clients must declare that the `application/octet-stream` MIME type is acceptable. For all other requests, clients must declare that the `application/json` MIME type is acceptable. Clients may also optionally declare that the `application/x-jackson-smile` MIME type is acceptable to request the server use Smile instead of JSON.

For example, the following are valid `Accept` headers:
```
For an endpoint returning binary:
Accept: application/octet-stream
Accept: */*

For an endpoint not returning binary:
Accept: application/json
Accept: */*
Accept: application/x-jackson-smile, application/json;q=0.8
```

#### 2.4.3. User-agent
Where possible, requests must include a `User-Agent` header defined below using [ABNF notation](https://tools.ietf.org/html/rfc5234#appendix-B.1) and regexes:

```
User-Agent        = commented-product *( WHITESPACE commented-product )
commented-product = product | product WHITESPACE paren-comments
product           = name "/" version
paren-comments    = "(" comments ")"
comments          = comment-text *( delim comment-text )
delim             = "," | ";"

comment-text      = [^,;()]+
name              = [a-zA-Z][a-zA-Z0-9\-]*
version           = [0-9]+(\.[0-9]+)*(-rc[0-9]+)?(-[0-9]+-g[a-f0-9]+)?
```

For example, the following are valid user agents:

```
foo/1.0.0
my-service/1.0.0-rc3-18-g773fc1b conjure-java-runtime/4.6.0 okhttp3/3.11.0
bar/0.0.0 (nodeId:myNode)
Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36
```

_Note, this is more restrictive than the User-Agent definition in [RFC 7231](https://tools.ietf.org/html/rfc7231#section-5.5.3). Requests from some browsers may not comply with these requirements as it is impossible to override browser User-Agent headers._

#### 2.4.4. Header Authorization
For endpoints with `auth` of type `header`, clients must send a header with name `Authorization` and case-sensitive value `Bearer {{string}}` where `{{string}}` is a user-provided string.

#### 2.4.5. Cookie Authorization
For endpoints with `auth` of type `cookie`, clients must send a cookie header with value `{{cookieName}}={{value}}`, where `{{cookieName}}` comes from the IR and `{{value}}` is a user-provided value.

#### 2.4.6. Additional headers
Clients may inject additional headers (e.g. for Zipkin tracing, or `Fetch-User-Agent`), as long as these do not clash with any headers already specified in the endpoint definition.


## 3. HTTP responses
### 3.1. Status codes
Conjure servers must respond to successful requests with HTTP status [`200 OK`](https://tools.ietf.org/html/rfc2616#section-10.2.1) unless:

  - the endpoint does not return a value: servers must send [`204 No Content`](https://tools.ietf.org/html/rfc2616#section-10.2.5).
  - the de-aliased return type is `optional<T>` and the value is not present: servers must send [`204 No Content`](https://tools.ietf.org/html/rfc2616#section-10.2.5).
  - the de-aliased return type is a `map`, `list` or `set`: it is recommended to send `204` but servers may send `200` if the HTTP body is `[]` or `{}`.

Using `204` in this way ensures that clients calling a Conjure endpoint with `optional<binary>` return type can differentiate between a non-present optional (`204`) and a present binary value containing zero bytes (`200`).

Further non-successful status codes are defined in the Conjure errors section below.

### 3.2. Response body
Conjure servers must serialize return values using the [JSON format][] defined below, unless:

  - the de-aliased return type is `optional<T>` and the value is not present: servers must return an empty HTTP body
  - the de-aliased return type is `optional<binary>` and the value is present: servers must write the raw bytes as an octet-stream
  - the de-aliased return type is `binary`: servers must write the raw bytes as an octet-stream
  - the client has indicated that the `application/x-jackson-smile` MIME type is acceptable and a non-binary value is present: servers may serialize return values using the [Smile format][] defined below

### 3.3. Content-Type header
Conjure servers must send a `Content-Type` header according to the endpoint's return value:

  - if the endpoint returns [`204 No Content`](https://tools.ietf.org/html/rfc2616#section-10.2.5), servers must send no `Content-Type` header.
  - if the de-aliased return type is `binary`, servers must send `Content-Type: application/octet-stream`,
  - otherwise, servers must send `Content-Type: application/json` if encoding the response as JSON,
  - or `Content-Type: application/x-jackson-smile` if encoding the response as Smile.

### 3.4. Conjure errors
In order to send a Conjure error, servers must serialize the error using the [JSON format][] (even if the client has indicated that a Smile response is also acceptable). In addition, servers must send an HTTP status code corresponding to the error's code.

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


## 4. Behaviour
### 4.1. Forward compatible clients
Clients must tolerate extra headers, unknown fields in JSON objects and unknown variants of Conjure enums and unions. This ensures that old clients will continue to work, even if a newer version of a server includes extra fields in a JSON response.

### 4.2. Servers reject unknown fields
Servers must request reject all unexpected JSON fields. This helps developers notice bugs and mistakes quickly, instead of allowing silent failures.

### 4.3. Servers tolerate extra headers
Servers must tolerate extra headers not defined in the endpoint definition. This is important because proxies frequently modify requests to include additional headers, e.g. `X-Forwarded-For`.

### 4.4. Round-trip of unknown variants
Clients should be able to round trip unknown variants of enums and unions.

### 4.5. CORS and HTTP preflight requests
In order to be compatible with browser [preflight requests](https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request), servers must support the HTTP `OPTIONS` method.

### 4.6. HTTP/2
The Conjure wire specification is compatible with HTTP/2, but it is not required.

### 4.7. Clients tolerate void endpoints returning JSON
Clients must tolerate an endpoint that is expected to return no value to return an arbitrary JSON value.

## 5. JSON format
This format describes how all Conjure types are serialized into and deserialized from JSON ([RFC 7159](https://tools.ietf.org/html/rfc7159)).

### 5.1. Built-in types
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
`any`             | N/A                                                | May be any of the above types, an `object` with `any` fields, or a `list` with `any` elements.

### 5.2. Container types
Conjure&nbsp;Type | JSON&nbsp;Type                | Comments |
----------------- | ----------------------------- | -------- |
`optional<T>`     | `JSON(T)`&nbsp;or&nbsp;`null` | If present, must be serialized as `JSON(e)`. If the value appears inside a JSON Object, then the corresponding key should be omitted. Alternatively, the field may be set to `null`. Inside JSON Array, a non-present Conjure optional value must be serialized as JSON `null`.
`list<T>`         | Array                         | Each element, e, of the list is serialized using `JSON(e)`. Order must be maintained.
`set<T>`          | Array                         | Each element, e, of the set is serialized using `JSON(e)`. Order is insignificant but it is recommended to preserve order where possible.
`map<K, V>`       | Object                        | A key k is serialized as a string with contents `PLAIN(k)`. Values are serialized using `JSON(v)`. For any (key,value) pair where the value is of de-aliased type `optional<?>`, the key should be omitted from the JSON Object if the value is absent, however, the key may remain if the value is set to `null`.

### 5.3. Named types
Conjure&nbsp;Type | JSON&nbsp;Type | Comments |
----------------- | ---------------| -------- |
_Object_          | Object         | Keys are obtained from the Conjure object's fields and values using `JSON(v)`. For any (key,value) pair where the value is of `optional<?>` type, the key must be omitted from the JSON Object if the value is absent.
_Enum_            | String         | String representation of the enum value
_Union_           | Object         | (See union JSON format below)
_Alias_(x)        | `JSON(x)`      | Aliases are serialized exactly the same way as their corresponding de-aliased Conjure types.

### 5.4. Union JSON format
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
  ```
  ```json
  // In this example, the variant is `bar` and the inner type is a Conjure `list<string>`
  {
    "type": "bar",
    "bar": ["Hello", "world"]
  }
  ```

### 5.5. Conjure Errors
Conjure Errors are serialized as JSON objects with the following keys:
1. `errorCode` - the JSON string representation of one of the supported [Conjure error codes](/docs/spec/conjure_definitions.md#errorcode).
1. `errorName` - a JSON string identifying the error, e.g. `Recipe:RecipeNotFound`.
1. `errorInstanceId` - a JSON string containing the unique identifier, `uuid` type, for this error instance.
1. `parameters` - a JSON map with string keys and JSON values, providing additional information regarding the nature of the error.

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
          "indicesSearched": ["recipes-main", "recipes-user-submitted", "recipes-seasonal"],
          "suggestions": [{
              "name": "roasted broccoli with lemon",
              "similarity": 0.93
          }, {
              "name": "roasted broccolini with garlic"
              "similarity": 0.89
          }]
      }
  }
  ```

### 5.6. Deserialization
#### 5.6.1. Coercing JSON `null` / absent to Conjure types
If a JSON key is absent or the value is `null`, two rules apply:

- Conjure `optional`, `list`, `set`, `map` types must be initialized to their empty variants,
- Attempting to coerce null/absent to any other Conjure type must cause an error, i.e. missing JSON keys must cause an error.

_Note: this rule means that the Conjure type `optional<optional<T>>` would be ambiguously deserialized from `null`: it could be  `Optional.empty()` or `Optional.of(Optional.empty())`. To avoid this ambiguity, Conjure ensures definitions do not contain this type._

#### 5.6.2. No automatic casting
Unexpected JSON types should not be automatically coerced to a different expected type. For example, if a Conjure definition specifies a field is `boolean`, the JSON strings `"true"` and `"false"` should not be accepted.

## 6. Smile format
This format describes how all Conjure types are serialized into and deserialized from [Smile](https://github.com/FasterXML/smile-format-specification). Smile is designed to be a binary equivalent of JSON, so much of the encoding behavior specified for JSON carries over directly to Smile. Any differences are noted in the comments sections below.

Serializers may use optional Smile features: raw binary encoding, string deduplication, and property deduplication. The encoded data must include the standard Smile header, and may include the Smile end of stream token.

### 6.1. Built-in types
Conjure&nbsp;Type | Smile Type | Comments |
----------------- | ---------- | ---------|
`bearertoken`     | String     |
`binary`          | Binary     | Smile natively supports binary data, so no Base64 encoding is necessary.
`boolean`         | Boolean    |
`datetime`        | String     |
`double`          | Double     | Non-finite values are not handled specially.
`integer`         | Integer    |
`rid`             | String     |
`safelong`        | Long       |
`string`          | String     |
`uuid`            | Binary     | UUIDs are encoded as 16 big-endian bytes.
`any`             | N/A        |

### 6.2. Container types
Conjure&nbsp;Type | Smile Type                   | Comments |
----------------- | ---------------------------- | ---------|
`optional<T>`     | `Smile(T)`&nbsp;or&nbsp;Null |
`list<T>`         | Array                        |
`set<T>`          | Array                        |
`map<K, V>`       | Object                       |

### 6.3. Named types
Conjure&nbsp;Type | Smile Type | Comments |
----------------- | ---------- | ---------|
_Object_          | Object     |
_Enum_            | String     |
_Union_           | Object     |
_Alias_(x)        | `Smile(x)` |

### 6.4. Union Smile format
Conjure Union types are serialized as Smile objects with the same structure as the JSON format.

### 6.5. Deserialization
The deserialization rules for JSON apply to Smile.

## 7. PLAIN format
The PLAIN format describes an unquoted string representation of a _subset_ of de-aliased conjure types.
The types listed below have a PLAIN format representation, while those omitted do not.

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
