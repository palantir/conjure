# Conjure Wire Specification

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT
RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [BCP
14](https://tools.ietf.org/html/bcp14) [RFC2119](https://tools.ietf.org/html/rfc2119)
[RFC8174](https://tools.ietf.org/html/rfc8174) when, and only when, they appear in all capitals, as shown here.

## Introduction

The Conjure Wire Specification defines the serialization format of Conjure defined types.

## Table of Contents
- [Specification](#specification)
    - [Formats](#format)
        - [Json Format](#jsonFormat)
        - [Plain Format](#plainFormat)
        - [Canonical Format](#canonicalFormat)
    - [Data Types](#dataTypes)
        - [Primitive Data Types](#primitiveDataTypes)
        - [Collection Data Types](#collectionDataTypes)
        - [Complex Data Types](#complexDataTypes)

## Specification

### Formats

#### JSON Format

The JSON format defines the JSON representation of Conjure-defined types, collections and primitives. Implementations 
of Conjure clients/servers MUST expect all HTTP requests and responses, and header parameters to be represented in this way.
The data types in the Conjure Specification are based on the types supported by the [JSON Schema Specification Wright
Draft 00](https://tools.ietf.org/html/draft-wright-json-schema-00#section-4.2). 

#### Plain Format

The Plain format defines the raw representation of Conjure primitives. There is no raw representation of
Conjure-defined types and collections. Implementations of Conjure clients/servers MUST expect all path and query
parameters to be represented in this way.

#### Canonical Format

The Canonical format defines an additional representation of Conjure-defined types, collections and primitives for use
when a type has multiple valid formats that are conceptually equivalent. Implementations of Conjure clients/servers 
MUST convert types (even if implicitly) from their JSON/Plain format to their canonical form when determining equality.

### Data Types

#### <a name="primitiveDataTypes"></a>Primitive Data Types

The primitive data types defined by the Conjure Specification are:

Conjure Name | JSON Type |     Plain Type    | Canonical Representation | Comments |
------------ | --------- | ----------------- | ------------------------ | -------- |
bearertoken  | `string`  | unquoted `string` | No ambiguity             | In accordance with [RFC 7519](https://tools.ietf.org/html/rfc7519).  
binary       | `string`  | unquoted `string` | No ambiguity             | Represented as a [Base64]() encoded string, except for when it is a request/response body where it is raw binary.
boolean      | `boolean` | `boolean`         | No ambiguity             |
datetime     | `string`  | unquoted `string` | Formatted according to `YYYY-MM-DDTHH:mm:ssZ`   | In accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).
double       | `number`  | `number`          | With at least 1 decimal  | As defined by [IEEE 754 standard](http://ieeexplore.ieee.org/document/4610935/).
integer      | `integer` | `number`          | No ambiguity             | Signed 32 bits, value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
rid          | `string`  | unquoted `string` | No ambiguity             | In accordance with the [Resource Identifier](https://github.com/palantir/resource-identifier) definition.
safelong     | `integer` | `number`          | No ambiguity             | Integer with value rangng from -2<sup>53</sup> - 1 to 2<sup>53</sup> - 1.
string       | `string`  | unquoted `string` | No ambiguity             | 
uuid         | `string`  | unquoted `string` | No ambiguity             | In accordance with [RFC 4122](https://tools.ietf.org/html/rfc4122).
any          | N/A       | N/A               | N/A                      | May be any of the above types or an `object` with any fields.

Example format conversions to canonical format:

Conjure Name |     JSON Representation     |   Plain Representation    | Canonical Representation |
------------ | --------------------------- | ------------------------- | ------------------------ |
datetime     | "2018-07-19T08:11:21+00:00" | 2018-07-19T08:11:21+00:00 | "2018-07-19T08:11:21Z"   
datetime     | "20180719T081121Z"          | 20180719T081121Z          | "2018-07-19T08:11:21Z"   
double       | 1                           | 1                         | 1.0
double       | 1.00000                     | 1.000000                  | 1.0
double       | 1.2345678                   | 1.2345678                 | 1.2345678

#### <a name="collectionDataTypes"></a>Collection Data Types
 
Collections can be omitted if they are empty or in the case of optionals absent. The collection data types defined by 
the Conjure Specification are:

Conjure Name |  JSON Type  | Canonical Representation | Comments
------------ | ----------- | ------------------------ | --------
list\<V>     | Array[V]    | No ambiguity             | An array where all the elements are of type V. If the associated field is omitted then the value is an empty list.
set\<V>      | Array[V]    | No ambiguity             | An array where all elements are of type V and are unique. The order the elements appear in the list does not impact equality.
map\<K, V>   | Map[K,V]   | No ambiguity             | A map where all keys are of type K  and all values are of type V. K MUST be a Conjure primitive type or an alias of a Conjure primitive type.
optional\<V> | V or `null` | `null` if absent otherwise the value | The value is considered absent if it is null or its associated field is omitted. 

#### <a name="complexDataTypes"></a>Complex Data Types

The complex data types defined by the Conjure Specification are:

Conjure Name | JSON Type | Canonical Representation | Comments
------------ | --------- | ------------------------ | --------
alias        |  -        | No ambiguity             | An Alias is a type that is a shorthand name for another type. An Alias MUST be serialized in the same way the aliased type would be.
enum         | `string`  | No ambiguity             | An Enum is a type that represents a fixed set of `string` values. Clients MUST deserialize a value which is not contained within the enumeration as `"UNKNOWN"`.
error        | `object`  | No ambiguity             | An Error is a type that represents a structured, non-successful response. It includes three required fields: `errorCode`, `errorName`, `errorInstanceId`, and an optional field `parameters`.
object       | `object`  | No ambiguity             | An Object is a type that represents an `object` with a predefined set of fields with associated types.
union        | `object`  | No ambiguity             | A Union is a type that represents a possibility from a fixed set of Objects. A Union is an object with a type field which specifies the name of the possibility and a field which is the name of the possibility which contains the payload. // TODO remove type field


#### 
An example error type would be serialized as follows:
```json
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
```json
{
  "type": "serviceLog",
  "serviceLog": {
    // fields of ServiceLog object
  }
}
```
