# Conjure Wire Specification

_This document defines how Conjure clients and servers should make and receive network requests and reponses over HTTP._

This document describes how endpoints and types defined in a Conjure IR file should result in network requests/response.

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT
RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [BCP
14](https://tools.ietf.org/html/bcp14) [RFC2119](https://tools.ietf.org/html/rfc2119)
[RFC8174](https://tools.ietf.org/html/rfc8174) when, and only when, they appear in all capitals, as shown here.

## HTTP Requests

TODO link to some official HTTP spec.

1. **SSL/TLS** - Conjure clients MUST support requests using TLS (HTTPS) (TODO versions) and MAY optionally support insecure HTTP requests.

1. **HTTP Methods** - Conjure clients MUST send requests using the HTTP Method specified in the IR.

1. **Path parameters** - For Conjure endpoints which have user-defined path parameters, the client MUST interpolate values for each of these path parameters. Values must be serialized using the PLAIN format and also _URL encoded_ (TODO link) to ensure special characters don't break.

  ```
  /some/url/{owner}/{repo}/pulls/{id}/{file}/{line}
  ->
  /some/url/joe/recipe-server/pulls/123/var%2Fconf%2Finstall.yml/53
  ```

1. **Headers** - Conjure endpoints which define 'headers' must be translated to HTTP Headers (TODO link). Header names are case insensitive.  Header values must be serialized using the PLAIN format. Header values which are `optional<T>` must be omitted entirely if the value is not present, otherwise just serialized as `PLAIN(T)`.

1. **User-agent** - Requests MUST include a `User-Agent` header. (TODO specify an exact format????)

1. **Authorization** - If an endpoint's `auth` field is present, clients must behave as follows:
  - `HeaderAuthType` - Clients MUST send a header with name `Authorization` and case-sensitive value `Bearer {{string}}` where `{{string}}` is a user-provided string.
  - `CookieAuthType` - Clients MUST send a cookie header with name `cookie` and value `{{cookieName}}={{value}}`, where `{{cookieName}}` comes from the IR and `{{value}}` is a user-provided value.

1. **Additional headers** - Clients MAY inject additional headers (e.g. for Zipkin tracing, or `Fetch-User-Agent`), as long as these do not clash with any headers already defined in the IR.

1. **Query parameters** - If an endpoint specifies one or more parameters of type `query`, then a client MUST add a query string to the outgoing URL as per the HTTP spec (TODO link). If any value of type `optional<T>` is not present, then the query key must be omitted.  Otherwise, it must be serialized as `PLAIN(T)` and url encoded (TODO link). TODO clarify if lists/sets/maps/binary are allowed.

  ```
  /some/url/search?string=foo%20bar&offset=60&limit=20
  ```

1. **Body serialization** - If an endpoint defines an argument of type `body` clients MUST serialize the user-provided value using the `JSON` encoding scheme defined below. TODO content-length ??? TODO binary streaming upload ??, TODO string examples. TODO empty containers. TODO nulls.

## HTTP Responses

1. **Status codes** - Conjure servers MUST respond with `204` status code if an endpoint returns `optional<T>` where `<T>` is not present. Servers MUST respond with `200` status code for all other successful requests, including empty maps, sets, and lists.

1. **Response body** - Conjure servers MUST serialize return values using the `JSON` encoding scheme defined below. The body MUST be omitted if the return type is `optional<T>` and `T` is not present. Return type `binary` must be written directly to the body. TODO define (optional<binary> where binary is empty or not, optional.empty) and content length.

1. **Content-type** - Conjure servers MUST respond to requests with the `Content-Type` header corresponding to the endpoint's return type. TODO(double check text/plain content type)
  ```
    binary -> "application/octet-stream"
    alias<binary> -> "application/octet-stream"
    <everything else> -> "aplication/json;charset=utf-8"
  ```
1. **Errors** - If Conjure servers return errors, they MUST serialize the erorrs using the `JSON` encoding scheme defined below. In addition, the servers MUST send a http status code correponding the error codes defined in the IR.
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

## Behavior
1. **Forward compatible clients** Clients MUST tolerate extra headers, unknown fields in JSON objects and unknown variants of enums and unions. This ensures that old clients will continue to work with new servers.

1. **Client base url** Conjure endpoint definitions only specify http path suffix without scheme, host, or port. Clients MUST allow users to specify server base url.

1. **Servers reject unknown fields** Servers MUST requst reject all unknown JSON fields. This helps developers notice bugs/mistakes. (TODO, make this more convincing)

1. **Servers tolerate extra headers** Servers MUST tolerate extra headers not defined by the endpoints. This is important because proxies frequently append extra headers to the incoming requests.

1. **Set and map key equality** TODO mention canonical form and byte equality

1. **Round-trip of unknown variants** TODO ask Mark.

1. **GZIP compression** It is recommended that servers and clients support gzip compression as it is often more performant. TODO add motivation.

1. **CORS and HTTP preflight requests** Browsers perform preflight requests with the `OPTIONS` http method before sending real requests. Servers MUST support this method to be browser compatible. TODO: add acccess-control-allowed-headers. TODO: refer to INFO sec quip doc.

1. **HTTP/2** It is recommended that clients and servers both support HTTP/2. Clients and Servers MUST support HTTP/1 and HTTP/1.1. TODO(remove HTTP/1?)


## JSON format

This format maps all Conjure types to JSON types defined in [RFC 7159](https://tools.ietf.org/html/rfc7159).

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

Conjure&nbsp;Type | JSON&nbsp;Type                                                          | Comments |
----------------- | ----------------------------------------------------------------------- | -------- |
`optional<T>`     |                                                                         | If present, serializes as the JSON representation of `T`, otherwise field should be omitted.
`list<T>`         | Array                                                                   | Each element, e, of the list is serialized using `JSON(e)`. Order must be maintained.
`set<T>`          | Array                                                                   | Each element, e, of the set is serialized using `JSON(e)`. Order is unimportant.
`map<K, V>`       | Object                                                                  | A key k is serialized as `PLAIN(k)`. Values are serialized using `JSON(v)`. For any (key,value) pair where the value is of `optional<?>` type, the key MUST be omitted from the JSON Object if the value is absent.


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

TODO explain why optional<optional<T>> is banned
TODO strings are UTF8

**Deserialization**
TODO: `any` may not deserialize into null
TODO: coerce nulls and absent to empties (for optional, list, set, map)
TODO: set & map key deduping using canonical equality.
TODO: Explicitly disallow casting nulls -> primitives,
TODO: Explicitly do not allow casting between types
TODO: Explicitly require fields to be present

## PLAIN format

This format describes an unquoted representation of _some_ Conjure types, suitable for usage in path parameters, query parameters and header parameters.

```
any -> ???
bearertoken -> unquoted string
binary -> unquoted base64 string
boolean -> raw boolean
datetime -> unquoted string
double -> raw number or Infinity or
integer -> raw number
rid -> unquoted string
safelong -> raw number
string -> unquoted string
uuid -> unquoted string

optional<T> -> UNSUPPORTED
list<T> -> UNSUPPORTED
set<T> -> UNSUPPORTED
map<K, V> -> UNSUPPORTED

object -> UNSUPPORTED
enum -> raw variant name
union -> UNSUPPORTED
alias of T -> PLAIN(T)
```

<!--
TODO

- should we mention CIPHERS????

- strictness / leniency - can you expect that a server or a client is already compliant, should you be defensive?? (server-side : you must be defensive and reject unknowns, client-side not as important??)

- key ordering of objects / maps ??? {"foo": 1, "bar": 2} equals {"bar": 2, "foo": 1}
-->

- [Formats](#format)
    - [Json Format](#jsonFormat)
    - [Plain Format](#plainFormat)
    - [Canonical Format](#canonicalFormat)
- [Data Types](#dataTypes)
    - [Primitive Data Types](#primitiveDataTypes)
    - [Collection Data Types](#collectionDataTypes)
    - [Complex Data Types](#complexDataTypes)

### Plain Format

### Canonical Format

The Canonical format defines an additional representation of Conjure-defined types, collections and primitives for use
when a type has multiple valid formats that are conceptually equivalent. Implementations of Conjure clients/servers
MUST convert types (even if implicitly) from their JSON/Plain format to their canonical form when determining equality.

## Data Types

### <a name="primitiveDataTypes"></a>Primitive Data Types

The primitive data types defined by the Conjure Specification are:

Conjure Name | JSON Type |     Plain Type    | Canonical Representation | Comments |
------------ | --------- | ----------------- | ------------------------ | -------- |
bearertoken  | `string`  | unquoted `string` | No ambiguity             | In accordance with [RFC 7519](https://tools.ietf.org/html/rfc7519).
binary       | `string`  | unquoted `string` | No ambiguity             | Represented as a [Base64]() encoded string, except for when it is a request/response body where it is raw binary.
boolean      | `boolean` | `boolean`         | No ambiguity             |
datetime     | `string`  | unquoted `string` | Formatted according to `YYYY-MM-DDTHH:mm:ss±hh:mm`   | In accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).
double       | `number` \| `string`  | `number` \| unquoted `string`          | Number with at least 1 decimal or "NaN" or "Infinity" or "-Infinity"  | As defined by [IEEE 754 standard](http://ieeexplore.ieee.org/document/4610935/).
integer      | `integer` | `number`          | No ambiguity             | Signed 32 bits, value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
rid          | `string`  | unquoted `string` | No ambiguity             | In accordance with the [Resource Identifier](https://github.com/palantir/resource-identifier) definition.
safelong     | `integer` | `number`          | No ambiguity             | Integer with value rangng from -2<sup>53</sup> - 1 to 2<sup>53</sup> - 1.
string       | `string`  | unquoted `string` | No ambiguity             |
uuid         | `string`  | unquoted `string` | No ambiguity             | In accordance with [RFC 4122](https://tools.ietf.org/html/rfc4122).
any          | N/A       | N/A               | N/A                      | May be any of the above types or an `object` with any fields.

Example format conversions to canonical format:

Conjure Name |     JSON Representation     |    Plain Representation    |  Canonical Representation   |
------------ | --------------------------- | -------------------------- | --------------------------- |
datetime     | "2018-07-19T08:11:21Z"      | 2018-07-19T08:11:21Z       | "2018-07-19T08:11:21+00:00"
datetime     | "2018-07-19T08:11:21+00:00" | 2018-07-19T08:11:21+00:00  | "2018-07-19T08:11:21+00:00"
datetime     | "2018-07-19T08:11:21-00:00" | 2018-07-19T08:11:21-00:00  | "2018-07-19T08:11:21+00:00"
datetime     | "20180719T081121Z"          | 20180719T081121Z           | "2018-07-19T08:11:21+00:00"
datetime     | "2018-07-19T05:11:21+03:00" | 2018-07-19T05:11:21+03:00  | "2018-07-19T05:11:21+03:00"
double       | 1                           | 1                          | 1.0
double       | 1.00000                     | 1.000000                   | 1.0
double       | 1.2345678                   | 1.2345678                  | 1.2345678
double       | 1.23456780                  | 1.23456780                 | 1.2345678
double       | "NaN"                       | NaN                        | "NaN"
double       | "Infinity"                  | Infinity                   | "Infinity"
double       | "-Infinity"                 | -Infinity                  | "-Infinity"

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
