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

<!-- these are just markdown link definitions, they do not get rendered -->
[JSON format]: #json-format
[PLAIN format]: #plain-format
[CANONICAL format]: #canonical-format

## HTTP requests
This section assumes familiarity with HTTP concepts as defined in [RFC2616 Hypertext Transfer Protocol -- HTTP/1.1](https://tools.ietf.org/html/rfc2616).

1. **SSL/TLS** - Conjure clients MUST support requests using Transport Layer Security (TLS) and MAY optionally support HTTP requests.

1. **HTTP Methods** - Conjure clients MUST support the following HTTP methods: `GET`, `POST`, `PUT`, `DELETE`.

1. **Path parameters** - For Conjure endpoints that have user-defined path parameters, clients MUST interpolate values for each of these path parameters. Values MUST be serialized using the [PLAIN format][] and must also be [URL encoded](https://tools.ietf.org/html/rfc3986#section-2.1) to ensure reserved characters are transmitted unambiguously.

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

1. **Headers** - For Conjure endpoints that define `header` parameters, clients must translate these to [HTTP Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers). Header names are case insensitive. Header values must be serialized using the [PLAIN format][]. Parameters of Conjure type `optional<T>` MUST be omitted entirely if the value is not present, otherwise just serialized as `PLAIN(T)`.

1. **User-agent** - Requests MUST include a `User-Agent` header.

1. **Header Authorization** - If an endpoint defines an `auth` field of type `header`, clients MUST send a header with name `Authorization` and case-sensitive value `Bearer {{string}}` where `{{string}}` is a user-provided string.

1. **Cookie Authorization** - If an endpoint defines an `auth` field of type `cookie`, clients MUST send a cookie header with value `{{cookieName}}={{value}}`, where `{{cookieName}}` comes from the IR and `{{value}}` is a user-provided value.

1. **Additional headers** - Clients MAY inject additional headers (e.g. for Zipkin tracing, or `Fetch-User-Agent`), as long as these do not clash with any headers already in the endpoint definition.

1. **Query parameters** - If an endpoint specifies one or more parameters of type `query`, clients MUST convert these (key,value) pairs into a [query string](https://tools.ietf.org/html/rfc3986#section-3.4) to be appended to the request URL. If any value of type `optional<T>` is not present, then the key must be omitted from the query string.  Otherwise, the value MUST be serialized as `PLAIN(T)` and any reserved characters percent encoded.

  For example, the following Conjure endpoints contains two query parameters:
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

1. **Body serialization** - If an endpoint defines an argument of type `body` clients MUST serialize the user-provided value using the [JSON format][]. TODO content-length ??? TODO binary streaming upload ??, TODO string examples. TODO empty containers. TODO nulls.


## HTTP responses
1. **Status codes** - Conjure servers MUST respond with `204` status code if an endpoint returns `optional<T>` and `<T>` is not present. Servers MUST respond with `200` status code for all other successful requests, including empty maps, sets, and lists.

1. **Response body** - Conjure servers MUST serialize return values using the `JSON` encoding scheme defined below. The body MUST be omitted if the return type is `optional<T>` and `T` is not present. Return type `binary` must be written directly to the body. TODO define (optional<binary> where binary is empty or not, optional.empty) and content length.

1. **Content-type** - Conjure servers MUST respond to requests with the `Content-Type` header corresponding to the endpoint's return type.
  ```
    binary -> "application/octet-stream"
    alias<binary> -> "application/octet-stream"
    <everything else> -> "application/json;charset=utf-8"
  ```
1. **Errors** - If Conjure servers return errors, they MUST serialize the errors using the `JSON` encoding scheme defined below. In addition, the servers MUST send a http status code corresponding to the error codes defined in the IR.
  ```
  PERMISSION_DENIED (403)
  INVALID_ARGUMENT(400)
  NOT_FOUND (404)
  CONFLICT (409)
  REQUEST_ENTITY_TOO_LARGE (413)
  FAILED_PRECONDITION (500)
  INTERNAL (500)
  TIMEOUT (500)
  CUSTOM_CLIENT (400)
  CUSTOM_SERVER (500)
  ```


## Behaviour
1. **Forward compatible clients** Clients MUST tolerate extra headers, unknown fields in JSON objects and unknown variants of enums and unions. This ensures that old clients will continue to work with new servers.

1. **Client base url** Conjure endpoint definitions only specify http path suffix without scheme, host, or port. Clients MUST allow users to specify server base url.

1. **Servers reject unknown fields** Servers MUST request reject all unknown JSON fields. This helps developers notice bugs/mistakes instead of silent failures. (TODO, make this more convincing)

1. **Servers tolerate extra headers** Servers MUST tolerate extra headers not defined by the endpoints. This is important because proxies frequently append extra headers to the incoming requests.

1. **Set and map key equality** TODO mention canonical form and byte equality

1. **Round-trip of unknown variants** TODO ask Mark.

1. **GZIP compression** It is recommended that servers and clients support gzip compression as it is often more performant.

1. **CORS and HTTP preflight requests** Browsers perform preflight requests with the `OPTIONS` http method before sending real requests. Servers MUST support this method to be browser compatible. TODO: add access-control-allowed-headers. TODO: refer to INFO sec quip doc.

1. **HTTP/2** It is recommended that clients and servers both support HTTP/2. Clients and servers MUST support HTTP/1 and HTTP/1.1. TODO(remove HTTP/1?)


## JSON format
This format defines a recursive function `JSON(t)` which maps all Conjure types, `t`, to JSON types defined in [RFC 7159](https://tools.ietf.org/html/rfc7159).

**Built-in types:**

Conjure&nbsp;Type | JSON Type                                          | Comments |
----------------- | ---------------------------------------------------| -------- |
`bearertoken`     | String                                             | In accordance with [RFC 7519](https://tools.ietf.org/html/rfc7519).
`binary`          | String                                             | Represented as a [Base64]() encoded string.
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

Conjure&nbsp;Type | JSON&nbsp;Type | Comments |
----------------- | -------------- | -------- |
`optional<T>`     |                | If present, serializes as the JSON representation of `T`, otherwise field should be omitted.
`list<T>`         | Array          | Each element, e, of the list is serialized using `JSON(e)`. Order must be maintained.
`set<T>`          | Array          | Each element, e, of the set is serialized using `JSON(e)`. Order is unimportant. The Array MUST not contain duplicate elements (as defined by the canonical format below).
`map<K, V>`       | Object         | A key k is serialized as `PLAIN(k)`. Values are serialized using `JSON(v)`. For any (key,value) pair where the value is of `optional<?>` type, the key MUST be omitted from the JSON Object if the value is absent. The Object must not contain duplicate keys (as defined by the canonical format below).

**Named types:**

Conjure&nbsp;Type | JSON&nbsp;Type | Comments |
----------------- | ---------------| -------- |
_Object_          | Object         | Keys are obtained from the Conjure object's fields and values using `JSON(v)`. For any (key,value) pair where the value is of `optional<?>` type, the key MUST be omitted from the JSON Object if the value is absent.
_Enum_            | String         | String representation of the enum value
_Union_           | Object         | (See union JSON format below)
_Alias(of x)_     | `JSON(x)`      | An Alias of any Conjure type is serialized in exactly the same way as that Conjure type.

**Union JSON format:**

Conjure Union types are serialized as a JSON Object with exactly two keys:

1. `type` key - this determines the variant of the union, e.g. `foo`
1. `{{variant}}` key - this key must match the variant determined above, and the value is `JSON(v)`.

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
`binary`          | unquoted base64 String
`boolean`         | Boolean
`datetime`        | unquoted String
`double`          | Number or `NaN` or `Infinity` or `-Infinity`
`integer`         | Number
`rid`             | unquoted String
`safelong`        | Number
`string`          | unquoted String
`uuid`            | unquoted String
_Enum_            | unquoted variant name
_Alias_ of T      | `PLAIN(T)`
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
binary       | No ambiguity                                       | Represented as a [Base64]() encoded string, except for when it is a request/response body where it is raw binary.
boolean      | No ambiguity                                       |
datetime     | Formatted according to `YYYY-MM-DDTHH:mm:ss±hh:mm` | In accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).
double       | No ambiguity                                       | As defined by [IEEE 754 standard](http://ieeexplore.ieee.org/document/4610935/).
integer      | No ambiguity                                       | Signed 32 bits, value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
rid          | No ambiguity                                       | In accordance with the [Resource Identifier](https://github.com/palantir/resource-identifier) definition.
safelong     | No ambiguity                                       | Integer with value rangng from -2<sup>53</sup> - 1 to 2<sup>53</sup> - 1.
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
alias        |  -        | No ambiguity             | An Alias is a type that is a shorthand name for another type. An Alias MUST be serialized in the same way the aliased type would be.
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
