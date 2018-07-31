# Conjure Wire Specification

<!--
TODO

- strictness / leniency - can you expect that a server or a client is already compliant, should you be defensive?? (server-side : you must be defensive and reject unknowns, client-side not as important??)

- key ordering of objects / maps ??? {"foo": 1, "bar": 2} equals {"bar": 2, "foo": 1}
-->

_This document defines how clients and servers should behave based on endpoints and types defined in a Conjure IR file._

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT
RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [BCP
14](https://tools.ietf.org/html/bcp14) [RFC2119](https://tools.ietf.org/html/rfc2119)
[RFC8174](https://tools.ietf.org/html/rfc8174) when, and only when, they appear in all capitals, as shown here.

For convenience, we define _de-alias_, _json_, and _plain_ functions as follows:

1. _de-alias_ - recursively collapses the Conjure _Alias_ type and is an identity function otherwise
  ```
  de-alias(Alias of T) -> de-alias(T)
  de-alias(T) -> T
  ```
1. _json_ - recursively de-aliases and converts a Conjure type, `T`, into JSON types using [JSON format][].
  ```
  json(Alias of T) -> json(T)
  ```
1. _plain_ - de-aliases and serializes Conjure types to their unquoted representation using [PLAIN format][]. This only to a subset of Conjure Types.
  ```
  plain(Alias of T) -> plain(T)
  ```

<!-- these are just markdown link definitions, they do not get rendered -->
[JSON format]: #json-format
[PLAIN format]: #plain-format
[CANONICAL format]: #canonical-format

## HTTP requests
This section assumes familiarity with HTTP concepts as defined in [RFC2616 Hypertext Transfer Protocol -- HTTP/1.1](https://tools.ietf.org/html/rfc2616).

1. **HTTP Methods** - Conjure clients MUST support the following HTTP methods: `GET`, `POST`, `PUT`, `DELETE`.

1. **Path parameters** - For Conjure endpoints that have user-defined path parameters, clients MUST interpolate values for each of these path parameters. Values MUST be serialized as `plain(T)` and MUST also be [URL encoded](https://tools.ietf.org/html/rfc3986#section-2.1) to ensure reserved characters are transmitted unambiguously.

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

1. **Query parameters** - If an endpoint specifies one or more parameters of type `query`, clients MUST convert these (key,value) pairs into a [query string](https://tools.ietf.org/html/rfc3986#section-3.4) to be appended to the request URL. If a value of `de-alias`ed type `optional<T>` is not present, then the key MUST be omitted from the query string.  Otherwise, the value MUST be serialized as `plain(T)` and any reserved characters percent encoded.

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
  - the `de-alias` of the argument is type `binary`: the clients MUST write the raw binary bytes directly to the request body
  - the `de-alias` of the argument is type `optional<T>` and the value is not present: it is RECOMMENDED to send an empty request body, although clients MAY alternatively send the JSON value `null`. 
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


1. **Headers** - Conjure `header` parameters MUST be serialized in the [PLAIN format][] and transferred as [HTTP Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers). Header names are case insensitive. Parameters of Conjure type `optional<T>` MUST be omitted entirely if the value is not present, otherwise just serialized as `plain(T)`.

1. **Content-Type header** - For Conjure endpoints that define a `body` argument, a `Content-Type` header MUST be added.  If the body is of type `binary`, the content-type `application/octet-stream` MUST be used. Otherwise, clients MUST send `Content-Type: application/json`.

<!-- TODO: should clients send an 'Accept: application/json' header to allow for future format changes on the server? -->

1. **User-agent** - Requests MUST include a `User-Agent` header.

1. **Header Authorization** - If an endpoint defines an `auth` field of type `header`, clients MUST send a header with name `Authorization` and case-sensitive value `Bearer {{string}}` where `{{string}}` is a user-provided string.

1. **Cookie Authorization** - If an endpoint defines an `auth` field of type `cookie`, clients MUST send a cookie header with value `{{cookieName}}={{value}}`, where `{{cookieName}}` comes from the IR and `{{value}}` is a user-provided value.

1. **Additional headers** - Clients MAY inject additional headers (e.g. for Zipkin tracing, or `Fetch-User-Agent`), as long as these do not clash with any headers already in the endpoint definition.

## HTTP responses
1. **Status codes** - Conjure servers MUST respond to successful requests with HTTP status [`200 OK`](https://tools.ietf.org/html/rfc2616#section-10.2.1) UNLESS:

  - the `de-alias` of the return type is `optional<T>` and the value is not present: servers MUST send [`204 No Content`](https://tools.ietf.org/html/rfc2616#section-10.2.5).
  - the `de-alias` of the return type is a `map`, `list` or `set`: it is RECOMMENDED to send `204` but servers MAY send `200` if the HTTP body is `[]` or `{}`.

  Using `204` in this way ensures that clients calling a Conjure endpoint with `optional<binary>` return type can differentiate between a non-present optional (`204`) and a present binary value containing zero bytes (`200`).

  Further non-successful status codes are defined in the Conjure errors section below.

1. **Response body** - Conjure servers MUST serialize return values using the [JSON format][] defined below, UNLESS:

  - the `de-alias` of the return type is `optional<T>` and the value is not present: servers MUST omit the HTTP body.
  - the `de-alias` of the return type is `binary`: servers MUST write the binary bytes directly to the HTTP body.

1. **Content-Type header** - Conjure servers MUST send a `Content-Type` header according to the endpoint's return type:

  - if the `de-alias` of the return type is `binary`, servers MUST send `Content-Type: application/octet-stream`,
  - otherwise, servers MUST send `Content-Type: application/json;charset=utf-8`.

1. **Conjure errors** - In order to send a Conjure error, servers MUST serialize the error using the [JSON format][]. In addition, servers MUST send a http status code corresponding to the error's code.

Conjure Error type         | HTTP Status code |
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

1. **Client base url** - Clients MUST allow users to specify a base url for network requests because Conjure endpoint definitions only include domain-agnotic http path suffixes.

1. **Servers reject unknown fields** - Servers MUST request reject all unexpected JSON fields. This helps developers notice bugs and mistakes quickly, instead of allowing silent failures.

1. **Servers tolerate extra headers** - Servers MUST tolerate extra headers not defined in the endpoint definition. This is important because proxies frequently modify requests to include additional headers, e.g. `X-Forwarded-For`.

1. **Set and map key equality** - Servers SHOULD reject duplicate `map` keys or duplicate `set` items in JSON bodies. Equivalence of two items can be determined by converting the JSON value to the [CANONICAL format][] and then comparing byte equality.

1. **Round-trip of unknown variants** - TODO ask Mark.

1. **CORS and HTTP preflight requests** - Servers MUST support the HTTP `OPTIONS` method in order to be compatible with browser [preflight requests](https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request).

1. **HTTP/2** - The Conjure wire specification is compatible with HTTP/2, but it is not required.


## JSON format
This format describes the JSON representation defined in [RFC 7159](https://tools.ietf.org/html/rfc7159) of all Conjure types.

**Built-in types:**

Conjure&nbsp;Type | JSON Type                                          | Comments |
----------------- | ---------------------------------------------------| -------- |
`bearertoken`     | String                                             | In accordance with [RFC 7519](https://tools.ietf.org/html/rfc7519).
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
`set<T>`          | Array                         | Each element, e, of the set is serialized using `JSON(e)`. Order is insignificant but it is RECOMMENDED to preserve order where possible. The Array MUST not contain duplicate elements (as defined by the canonical format below).
`map<K, V>`       | Object                        | A key k is serialized as `PLAIN(k)`. Values are serialized using `JSON(v)`. For any (key,value) pair where the value is of de-aliased type `optional<?>`, the key SHOULD be omitted from the JSON Object if the value is absent, however, the key MAY remain if the value is set to `null`. The Object must not contain duplicate keys (as defined by the canonical format below).

**Named types:**

Conjure&nbsp;Type | JSON&nbsp;Type | Comments |
----------------- | ---------------| -------- |
_Object_          | Object         | Keys are obtained from the Conjure object's fields and values using `json(v)`. For any (key,value) pair where the value is of `optional<?>` type, the key MUST be omitted from the JSON Object if the value is absent.
_Enum_            | String         | String representation of the enum value
_Union_           | Object         | (See union JSON format below)
_Alias_(x)        | `json(x)`      | An Alias of any Conjure type is serialized in exactly the same way as that Conjure type.

**Union JSON format:**

Conjure Union types are serialized as a JSON Object with exactly two keys:

1. `type` key - this determines the variant of the union, e.g. `foo`
1. `{{variant}}` key - this key MUST match the variant determined above, and the value is `json(v)`.

```yaml
types:
  definitions:
    objects:

      MyUnion:
        union:
          foo: boolean
          bar: list<string>
```

```js
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

<!-- TODO: define JSON format for errors -->

**Deserialization**
TODO: `any` may not deserialize into null
TODO: coerce nulls and absent to empties (for optional, list, set, map)
TODO: set & map key deduping using canonical equality.
TODO: Explicitly disallow casting nulls -> primitives,
TODO: Explicitly do not allow casting between types
TODO: Explicitly require fields to be present
TODO explain why optional<optional<T>> is banned


## PLAIN format
This format describes an unquoted representation of a _subset_ of Conjure types, suitable for usage in path parameters, query parameters and header parameters.

Conjure&nbsp;Type | PLAIN&nbsp;Type                               |
----------------- | ----------------------------------------------|
`bearertoken`     | unquoted String
`binary`          | unquoted a Base64 encoded string in accordance with [RFC 4648](https://tools.ietf.org/html/rfc4648#section-4)
`boolean`         | Boolean
`datetime`        | unquoted String
`double`          | Number or `NaN` or `Infinity` or `-Infinity`
`integer`         | Number
`rid`             | unquoted String
`safelong`        | Number
`string`          | unquoted String
`uuid`            | unquoted String
_Enum_            | unquoted variant name
_Alias_(T)        | `plain(T)`
`any`             | UNSUPPORTED
`optional<T>`     | UNSUPPORTED
`list<T>`         | UNSUPPORTED
`set<T>`          | UNSUPPORTED
`map<K, V>`       | UNSUPPORTED
_Object_          | UNSUPPORTED
_Union_           | UNSUPPORTED


## CANONICAL Format

<!-- TODO explain more how equality is important for set items and map keys -->

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


<!-- TODO clean up everything below this line -->


### <a name="collectionDataTypes"></a>Collection Data Types

Collections can be omitted if they are empty or in the case of optionals absent. The collection data types defined by
the Conjure Specification are:

Conjure Name |  JSON Type  | Canonical Representation | Comments
------------ | ----------- | ------------------------ | --------
list\<V>     | Array[V]    | No ambiguity             | An array where all the elements are of type V. If the associated field is omitted then the value is an empty list.
set\<V>      | Array[V]    | No ambiguity             | An array where all elements are of type V and are unique. The order the elements appear in the list does not impact equality.
map\<K, V>   | Map[K,V]   | No ambiguity             | A map where all keys are of type K  and all values are of type V. K MUST be a Conjure primitive type or an alias of a Conjure primitive type. If the type is non-string, then it is serialized into a quoted version of its plain reprehension.
optional\<V> | V or `null` | `null` if absent otherwise the value | The value is considered absent if it is null or its associated field is omitted.

### <a name="complexDataTypes"></a>Complex Data Types

The complex data types defined by the Conjure Specification are:

Conjure Name | JSON Type | Canonical Representation | Comments
------------ | --------- | ------------------------ | --------
alias        |  -        | No ambiguity             | An Alias is a type that is a shorthand name for another type. An Alias MUST be serialized in the same way `the alias` of the type would be.
enum         | `string`  | No ambiguity             | An Enum is a type that represents a fixed set of `string` values.
error        | `object`  | No ambiguity             | An Error is a type that represents a structured, non-successful response. It includes three required fields: `errorCode`, `errorName`, `errorInstanceId`, and an optional field `parameters`.
object       | `object`  | No ambiguity             | An Object is a type that represents an `object` with a predefined set of fields with associated types.
union        | `object`  | No ambiguity             | A Union is a type that represents a possibility from a fixed set of Objects. A Union is a JSON object with two fields: a `type` field which specifies the name of the variant, and a field which is the name of the variant which contains the payload.

An example error type would be serialized as follows:

```js
{
  "errorCode": "INVALID_ARGUMENT",
  "errorName": "MyApplication:DatasetNotFound",
  "errorInstanceId": "xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx",
  "parameters": {
    "datasetId": "123abc",
    "userName": "yourUserName"
  }
}
```

An example union type would be serialized as follows:

```js
{
  "type": "serviceLog",
  "serviceLog": {
    // fields of ServiceLog object
  }
}
```
