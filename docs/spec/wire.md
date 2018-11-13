# Conjure Wire Specification
_This document defines how clients and servers should behave based on endpoints and types defined in a Conjure IR file._

<!-- WARNING: the markdown titles below are used to derive permalinks, changing them will break links -->

<!--
Generated using https://github.com/jonschlinkert/markdown-toc:
  $ markdown-toc docs/spec/wire.md --no-firsth1
-->
<!-- TOC -->
- [1. Conventions](#1-conventions)
- [2. HTTP requests](#2-http-requests)
  * [2.1. Path parameters](#21-path-parameters)
  * [2.2. Query parameters](#22-query-parameters)
  * [2.3. Body parameter](#23-body-parameter)
  * [2.4. Headers](#24-headers)
    + [2.4.1. Content-Type Header](#241-content-type-header)
    + [2.4.2. Accept header](#242-accept-header)
    + [2.4.3. User-agent](#243-user-agent)
    + [2.4.4. Header Authorization](#244-header-authorization)
    + [2.4.5. Cookie Authorization](#245-cookie-authorization)
    + [2.4.6. Additional headers](#246-additional-headers)
- [3. HTTP responses](#3-http-responses)
  * [3.1. Status codes](#31-status-codes)
  * [3.2. Response body](#32-response-body)
  * [3.3. Content-Type header](#33-content-type-header)
  * [3.4. Conjure errors](#34-conjure-errors)
- [4. Behaviour](#4-behaviour)
  * [4.1. Forward compatible clients](#41-forward-compatible-clients)
  * [4.2. Servers reject unknown fields](#42-servers-reject-unknown-fields)
  * [4.3. Servers tolerate extra headers](#43-servers-tolerate-extra-headers)
  * [4.4. Round-trip of unknown variants](#44-round-trip-of-unknown-variants)
  * [4.5. CORS and HTTP preflight requests](#45-cors-and-http-preflight-requests)
  * [4.6. HTTP/2](#46-http2)
- [5. JSON format](#5-json-format)
  * [5.1. Built-in types](#51-built-in-types)
  * [5.2. Container types](#52-container-types)
  * [5.3. Named types](#53-named-types)
  * [5.4. Union JSON format](#54-union-json-format)
  * [5.5. Conjure Errors](#55-conjure-errors)
  * [5.6. Deserialization](#56-deserialization)
    + [5.6.1. Coercing JSON `null` / absent to Conjure types](#561-coercing-json-null--absent-to-conjure-types)
    + [5.6.2. Dedupe `set` / `map` keys using Canonical JSON format](#562-dedupe-set--map-keys-using-canonical-json-format)
    + [5.6.3. No automatic casting](#563-no-automatic-casting)
- [6. PLAIN format](#6-plain-format)
- [7. Canonical JSON Format](#7-canonical-json-format)
  * [7.1. Canonical double](#71-canonical-double)
  * [7.2. Canonical datetime](#72-canonical-datetime)
<!-- /TOC -->

<!-- these are just markdown link definitions, they do not get rendered -->
[JSON format]: #5-json-format
[PLAIN format]: #6-plain-format
[Canonical JSON format]: #7-canonical-json-format
[URL encoded]: https://tools.ietf.org/html/rfc3986#section-2.1
[Path parameters]: ./source_files.md#path-templating


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

In this case, if `newName` is not present, then the [JSON format][] allows clients to send a HTTP body containing `null` or send an empty body.  If `newName` is present, then the body will include JSON quotes, e.g. `"Joe blogs"`.

### 2.4. Headers
Conjure `header` parameters must be serialized in the [PLAIN format][] and transferred as [HTTP Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers). Header names are case insensitive. Parameters of Conjure type `optional<T>` must be omitted entirely if the value is not present, otherwise just serialized using the [PLAIN Format][].

#### 2.4.1. Content-Type Header
A `Content-Type` header must be added if the endpoint defines a `body` argument.
- If the de-aliased body type is `binary`, the Content-Type `application/octet-stream` must be used.
- Otherwise, clients must use `application/json`.

_Note that the default encoding for `application/json` content type is [`UTF-8`](http://www.ietf.org/rfc/rfc4627.txt)._

#### 2.4.2. Accept header
Clients must send an `Accept: application/json` header for all requests unless the endpoint returns binary, in which case the client must send `Accept: application/octet-stream`.

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

  - the de-aliased return type is `optional<T>` and the value is not present: servers must send [`204 No Content`](https://tools.ietf.org/html/rfc2616#section-10.2.5).
  - the de-aliased return type is a `map`, `list` or `set`: it is recommended to send `204` but servers may send `200` if the HTTP body is `[]` or `{}`.

Using `204` in this way ensures that clients calling a Conjure endpoint with `optional<binary>` return type can differentiate between a non-present optional (`204`) and a present binary value containing zero bytes (`200`).

Further non-successful status codes are defined in the Conjure errors section below.

### 3.2. Response body
Conjure servers must serialize return values using the [JSON format][] defined below, unless:

  - the de-aliased return type is `optional<T>` and the value is not present: servers must omit the HTTP body.
  - the de-aliased return type is `binary`: servers must write the raw bytes as an octet-stream.

### 3.3. Content-Type header
Conjure servers must send a `Content-Type` header according to the endpoint's return type:

  - if the de-aliased return type is `binary`, servers must send `Content-Type: application/octet-stream`,
  - otherwise, servers must send `Content-Type: application/json`.

### 3.4. Conjure errors
In order to send a Conjure error, servers must serialize the error using the [JSON format][]. In addition, servers must send a http status code corresponding to the error's code.

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
`any`             | N/A                                                | May be any of the above types or an `object` with any fields.

### 5.2. Container types
Conjure&nbsp;Type | JSON&nbsp;Type                | Comments |
----------------- | ----------------------------- | -------- |
`optional<T>`     | `JSON(T)`&nbsp;or&nbsp;`null` | If present, must be serialized as `JSON(e)`. If the value appears inside a JSON Object, then the corresponding key should be omitted. Alternatively, the field may be set to `null`. Inside JSON Array, a non-present Conjure optional value must be serialized as JSON `null`.
`list<T>`         | Array                         | Each element, e, of the list is serialized using `JSON(e)`. Order must be maintained.
`set<T>`          | Array                         | Each element, e, of the set is serialized using `JSON(e)`. Order is insignificant but it is recommended to preserve order where possible. The Array must not contain duplicate elements (as defined by the canonical format below).
`map<K, V>`       | Object                        | A key k is serialized as a string with contents `PLAIN(k)`. Values are serialized using `JSON(v)`. For any (key,value) pair where the value is of de-aliased type `optional<?>`, the key should be omitted from the JSON Object if the value is absent, however, the key may remain if the value is set to `null`. The Object must not contain duplicate keys (as defined by the canonical format below).

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

### 5.6. Deserialization
#### 5.6.1. Coercing JSON `null` / absent to Conjure types
If a JSON key is absent or the value is `null`, two rules apply:

- Conjure `optional`, `list`, `set`, `map` types must be initialized to their empty variants,
- Attempting to coerce null/absent to any other Conjure type must cause an error, i.e. missing JSON keys must cause an error.

_Note: this rule means that the Conjure type `optional<optional<T>>` would be ambiguously deserialized from `null`: it could be  `Optional.empty()` or `Optional.of(Optional.empty())`. To avoid this ambiguity, Conjure ensures definitions do not contain this type._

#### 5.6.2. Dedupe `set` / `map` keys using Canonical JSON format
When deserializing Conjure `set` or `map` keys, equivalence of two items can be determined by converting the JSON value to the [Canonical JSON format][] and then comparing byte equality.

#### 5.6.3. No automatic casting
Unexpected JSON types should not be automatically coerced to a different expected type. For example, if a Conjure definition specifies a field is `boolean`, the JSON strings `"true"` and `"false"` should not be accepted.


## 6. PLAIN format
The PLAIN format describes an unquoted [JSON](https://tools.ietf.org/html/rfc7159) representation of a _subset_ of de-aliased conjure types.
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


## 7. Canonical JSON Format
The Canonical JSON format is a constrained version of the [JSON format][] that disambiguates values for
types which have multiple distinct representations that are conceptually equivalent.
Implementations of Conjure clients/servers must convert types (even if implicitly) from their JSON/Plain format to
their canonical form when determining equality.

Aside from the cases described below, the canonical representation is the same as the [JSON representation][JSON format].

### 7.1. Canonical double
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

### 7.2. Canonical datetime
The canonical JSON format of a datetime is a string formatted according to `YYYY-MM-DDTHH:mm:ss±hh:mm`, in accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).

**Examples**:

|     JSON representation       |  Canonical representation     |
| ----------------------------- | ----------------------------- |
| `"2018-07-19T08:11:21Z"`      | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T08:11:21+00:00"` | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T08:11:21-00:00"` | `"2018-07-19T08:11:21+00:00"` |
| `"20180719T081121Z"`          | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T05:11:21+03:00"` | `"2018-07-19T05:11:21+03:00"` |
