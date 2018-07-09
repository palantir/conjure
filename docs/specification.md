# Conjure Specification

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT
RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be interpreted as described in [BCP
14](https://tools.ietf.org/html/bcp14) [RFC2119](https://tools.ietf.org/html/rfc2119)
[RFC8174](https://tools.ietf.org/html/rfc8174) when, and only when, they appear in all capitals, as shown here.

## Introduction

The Conjure Specification defines a language-agnostic interface description language and framework for defining RESTful
APIs.

## Table of Contents
- [Specification](#specification)
    - [Format](#format)
    - [Data Types](#dataTypes)
        - [Primitive Data Types](#primitiveDataTypes)
        - [Collection Data Types](#collectionDataTypes)
        - [Complex Data Types](#complexDataTypes)
    - [Structure](#structure)
        - [Conjure Object](#conjureObject)
        - [Types Object](#typesObject)
        - [External Type Object](#externalTypeObject)
        - [Defined Types Object](#definedTypesObject)
        - [Alias Definition](#aliasDefinition)
        - [Object Definition](#objectDefinition)
        - [Field Definition](#fieldDefinition)
        - [Union Definition](#unionDefinition)
        - [Enum Definition](#enumDefinition)
        - [Error Definition](#errorDefinition)
        - [Error Code Field](#errorCodeField)
        - [Service Object](#serviceObject)
        - [Auth Field](#authField)
        - [Endpoint Object](#endpointObject)
        - [Argument Object](#argumentObject)
        - [Param Type Field](#paramTypeField)
    - [Rich Text Formatting](#richText)

## Specification

### Format

A Conjure definition that conforms to the Conjure Specification is a JSON object, which is often represented in the YAML
format. A definition may consist of one or several Conjure documents. A definition may be split across several documents
for programmer convenience, but the file boundaries contain no semantic value. Parsers may combine the documents into a
single document for generation purposes.

All field names in the specification are **case sensitive**.

### <a name="dataTypes"></a>Data Types

The data types in the Conjure Specification are based on the types supported by the [JSON Schema Specification Wright
Draft 00](https://tools.ietf.org/html/draft-wright-json-schema-00#section-4.2). Note that `integer` as a type is also
supported and is defined as a JSON number without a fraction or exponent part. `null` is not supported as a type (see
[`optional`](#dataTypeOptional) for an alternative solution).

#### <a name="primitiveDataTypes"></a>Primitive Data Types
The primitive data types defined by the Conjure Specification are:

Conjure Name | Type     | Comments
----------- | ------    | -------- 
bearertoken | `string`  |
binary      | `string`  | // TODO
boolean     | `boolean` |
datetime    | `datetime` | // TODO
double      | `number`  | As defined by [IEEE 754 standard](http://ieeexplore.ieee.org/document/4610935/).
integer     | `integer` | Signed 32 bits, value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
rid         | `string`  | In accordance with the [Resource Identifer](https://github.com/palantir/resource-identifier) definition.
safelong    | `integer` | Integer with value ranging from -2<sup>53</sup> - 1 to 2<sup>53</sup> - 1.
string      | `string`  |
any         |           | May be any of the above types or an `object` with any fields

#### <a name="collectionDataTypes"></a>Collection Data Types

The collection data types defined by the Conjure Specification are:

Conjure Name | type     | Comments
----------- | ------    | -------- 
list\<V>    | Array[V]  | An array where all the elements are of type V. If the associated field is omitted then the value is an empty list.
set\<V>     | Array[V]  | An array where all elements are of type V and are unique. // TODO: equality/ordering
map\<K, V>  | Map[K, V] | A map where all keys are of type K  and all values are of type V. K MUST be a Conjure primitive type or an alias of a Conjure primitive type.
<a name=”dataTypeOptional”></a>optional\<V> | V or `null` | The value is considered absent if it is null or its associated field is omitted. When used within a [complex data type](#complexDataTypes), the field MAY be omitted if the optional is absent.

#### <a name="complexDataTypes"></a>Complex Data Types

Conjure Name | type | Comments
----------- | ------ | --------
alias       |  -    | An Alias is a type that is a shorthand name for another type. An Alias MUST be serialized in the same way the aliased type would be.
enum        | `string` | An Enum is a type that represents a fixed set of `string` values. Clients MUST deserialize a value which is not contained within the enumeration as `"UNKNOWN"`.
error       | `object` | An Error is a type that represents a structured, non-successful response. It includes three required fields: `errorCode`, `errorName`, `errorInstanceId`, and an optional field `parameters`.
object      | `object` | An Object is a type that represents an `object` with a predefined set of fields with associated types.
union       | `object` | A Union is a type that represents a possibility from a fixed set of Objects. A Union is an object with a type field which specifies the name of the possibility and a field which is the name of the possibility which contains the payload. // TODO remove type field

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

### <a name="structure"></a>Structure

In the following description, if a field is not explicitly **REQUIRED** or described with a MUST or SHALL, it can be
considered OPTIONAL.

#### <a name="conjureObject"></a>Conjure Object

This is the root document object of a Conjure definition.

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
<a name="conjureTypes"></a>types | [Types Object](#typesObject) | The types to be included in the definition.
<a name="conjureServices"></a>services | Map[`string`, [Service Object](#serviceObject)] | A  map between a service name and its definition. Service names MUST be in PascalCase.


#### <a name="typesObject"></a>Types Object

The object specifies the types available in the Conjure definition.

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
<a name="typeConjureImport"></a>conjure-imports | Map[`string`, `string`] | A map between a namespace alias and a relative path to a Conjure definition. Namespace aliases MUST match `^[_a-zA-Z][_a-zA-Z0-9]*$`
<a name="typeImports"></a>imports | Map[`string`, [External Type Object](#externalTypeObject)] | A map between a type alias and its external definition. Type aliases MUST be in PascalCase.
<a name="typeDefinitions"></a>definitions | [Type Definitions Object](#definedTypesObject) | The types specified in this definition.

#### <a name="externalTypeObject"></a>External Type Object

A type that is not defined within Conjure. Usage of external types is not recommended and is intended only to migrate existing APIs to Conjure.

##### Fixed Fields
Field Name | Type | Description
---|:---:|---
base-type | `string` | MUST be a a primitive data type.
external | Map[`string`, `string`] | A map between a language name and its associated fully qualified type.

A `base-type` is provided as a hint to generators for how to handle this type when no external type reference is provided. Note that 
the serialization format of the `base-type` fallback should match the format of the imported type. If the imported type is a non-primitive JSON object, then a `base-type` of any should be used.

Each generator specifies what key they will consume within the `external` map.

##### External Type Object Example
```yaml
imports:
  SomeDataType:
    base-type: string
    external:
      java: com.palantir.package.someDataType
      typescript: @palantir/package
```

#### <a name="definedTypesObject"></a>Defined Types Object

The object specifies the types that are defined in the Conjure definition. 

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
default-package | `string` | 
<a name="typeDefinitions"></a>definitions | Map[`string`, [Alias Definition](#aliasDefinition) \| [Object Definition](#errorDefinition) \| [Union Definition](#unionDefinition) \| [Enum Definition](#enumDefinition)] | A map between type names and type definitions. 
<a name="typeErrors"></a>errors | Map[`string`, [Error Definition](#errorDefinition)] |A map between type names and error definitions.

Package names are used by generator implementations to determine the output location and language-specific namespacing. Package names should follow the Java style naming convention: `com.example.name`.

Type names MUST be in PascalCase and be unique within a package. // TODO: consider type name uniqueness regardless of package

#### <a name="aliasDefinition"></a>Alias Definition

Definition for an alias complex data type.

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
<a name="aliasAlias"></a>alias | `string` | **REQUIRED**. The name of the type to be aliased.
<a name="objectDocs"></a>docs | `string` | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
<a name="objectPackage"></a>package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [Defined Types Object](#definedTypesObject). 

#### <a name="objectDefinition"></a>Object Definition

Definition for an object complex data type.

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
<a name="objectFields"></a>fields | Map[`string`, [Field Definition](#fieldDefinition) \| `string`] | **REQUIRED**. A map from field names to type names. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
<a name="objectDocs"></a>docs | `string` | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
<a name="objectPackage"></a>package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [Defined Types Object](#definedTypesObject). 

Field names must appear in either lowerCamelCase, or kebab-case, or snake_case. Code generators will respect casing for wire format, but may convert case formats to conform with language restrictions. As a result, field names must be unique independent of case format (e.g. an object may not define both caseFormat and case-format as fields).

##### Object Definition Examples
```yaml
TypeAlias:
  docs: Some documentation about the whole type
  package: com.palantir.example
  fields:
    foo: string
    bar:
      type: string
      docs: Some documentation about the specific field
```


#### <a name="fieldDefinition"></a>Field Definition

Definition for a field in a complex data type.

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
<a name="fieldType"></a>type | `string` | **REQUIRED**. The name of the type of the field. It MUST be a type name that exists within the Conjure definition.
<a name="fieldDocs"></a>docs | `string` | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.


#### <a name="unionDefinition"></a> Union Definition

Definition for a union complex data type.

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
<a name="unionUnion"></a>union | Map[`string`, [Field Definition](#fieldDefinition) \| `string`] | **REQUIRED**. A map from union names to type names. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. Union names MUST be in PascalCase.
<a name="unionDocs"></a>docs | `string` | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
<a name="unionPackage"></a>package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [Defined Types Object](#definedTypesObject). 

It is common for a generator to also generate a visitor interface for each union, to facilitate consumption and customization of behavior depending on the wrapped type. The interface includes a visit() method for each wrapped type, as well as a visitUnknown(String unknownType) method which is executed when the wrapped object does not match any of the known member types. 

##### Union Definition Example
```yaml
Payload:
  package: com.palantir.example
  union:
    serviceLog: ServiceLog
    notAServiceLog: RequestLog
```

#### <a name="enumDefinition"></a>Enum Definition

Definition for an enum complex data type.

##### Fixed Fields

Field Name | Type | Description
---|:---:|---
<a name="enumValues"></a>values | Array[`string`] | **REQUIRED**. A list of enumeration values. All elements in the list MUST be unique and be UPPERCASE.
<a name="enumDocs"></a>docs | `string` | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
<a name="enumPackage"></a>package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [Defined Types Object](#definedTypesObject). 


##### Enum Definition Example
```yaml
LoadState:
  values:
    - LOADING
    - LOADED
    - ERROR
```

#### <a name="errorDefinition"></a>Error Definition

Definition for an error type.

##### Fixed Fields
Field Name | Type | Description
---|:---:|---
<a name="errorNamespace"></a>namespace | `string` | **REQUIRED**. The namespace of the error. The namespace MUST be in PascalCase.
<a name="errorCode"></a>code | [Error Code](#errorCode) | **REQUIRED**. The general category for the error.
<a name="errorSafeArgs"></a>safe-args | Map[`string`, [Field Definition](#fieldDefinition) \| `string`] | **REQUIRED**. A map from argument names to type names. These arguments are considered safe in accordance with the SLS specification. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. // TODO: this shouldn't be required
<a name="errorUnsafeArgs"></a>unsafe-args | Map[`string`, [Field Definition](#fieldDefinition) \| `string`] | **REQUIRED**. A map from argument names to type names. These arguments are considered unsafe in accordance with the SLS specification. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. // TODO: this shouldn't be required
<a name="errorDocs"></a>docs | `string` | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.

#### <a name="errorCodeField"></a>Error Code Field

A field describing the error category. MUST be one of the following strings, which have associated HTTP status codes:
* PERMISSION_DENIED (403)
* INVALID_ARGUMENT(400)
* NOT_FOUND (404)
* CONFLICT (409)
* REQUEST_ENTITY_TOO_LARGE (413)
* FAILED_PRECONDITION(500)
* INTERNAL (500)
* TIMEOUT (500)
* CUSTOM_CLIENT (400)
* CUSTOM_SERVER (500)

#### <a name="serviceObject"></a>Service Object

An object representing a service. A service is a collection of endpoints.

##### Fixed Fields
Field name | Type | Description
---|:---:|---
<a name="serviceName"></a>name | `string` | **REQUIRED** A human readable name for the service. 
<a name="servicePackage"></a>package | `string` | **REQUIRED** The package of the service.
<a name="serviceBasePath"></a>base-path | [Path Segment Field](#pathSegmentField) | **REQUIRED** The base path of the service. The path MUST have a leading `/`. The base path is prepended to each endpoint path to construct the final URL. [Path templating](#pathTemplating) is not allowed.
<a name="serviceDefaultAuth"></a>default-auth | [Auth Field](#authField) | **REQUIRED** The default authentication mechanism for all endpoints in the service.
<a name="serviceEndpoints"></a>endpoints | Map[`string`, [Endpoint Object](#endpointObject)] | **REQUIRED** A map of endpoint names to endpoint definitions. 
<a name="serviceDocs"></a>docs | `string` | Documentation for the service. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.

#### <a name="pathSegmentField"></a> Path Segment Field
A field describing an extendible path. A path segment MAY have [Path templating](#pathTemplating).

When comparing multiple paths, the path with the longest concrete path should be matched first. 

##### <a name="pathTemplating"></a>Path Templating
Path templating refers to the usage of curly braces ({}) to mark a section of a URL path as replaceable using path parameters. The template may include `:.+` and `:.*` regular expressions with the following semantics and limitations:
- `:.+` A non-greedy match of one or more path segments should be performed for the parameter
- `:.*` A non-greedy match of zero or more path segments should be performed for the parameter. `:.*` is only supported if the template is the final segment of the path

##### Path Resolution Examples
Assuming the following paths, the concrete definition `/branch/foo` would be matched first.
```
/branch/{branchPath}
/branch/foo
```

Assuming the following paths with request URL `path/dataset/fetch`, the path `/path/dataset/{arg}` should be matched
first.
```
/path/{arg}/fetch
/path/dataset/{arg}
```

#### <a name="authField"></a>Auth Field
A field describing an authentication mechanism. It is a `string` which MUST be of the following:
* `none`: do not apply authorization requirements
* `header`: apply an `Authorization` header argument/requirement to every endpoint.
* `cookie:<cookie name>`: apply a cookie argument/requirement to every endpoint with cookie name `<cookie name>`.

### <a name="endpointObject"></a>Endpoint Object
An object representing an endpoint. An endpoint describes a method, arguments and return type.

##### Fixed Fields
Field name | Type | Description
---|:---:|---
<a name="endpointHttp"></a>http | `string` | **REQUIRED** The operation and path for the endpoint. It MUST follow the shorthand `<method> <path>`, where `<method>` is one of GET, DELETE, POST, or PUT, and `<path>` is a [Path Segment Field](#pathSegmentField).
<a name="endpointAuth"></a>auth | [Auth Field](#authField) | The authentication mechanism for the endpoint. Overrides `default-auth` in [Service Object](#serviceObject).
<a name="endpointReturns"></a>returns | `string` | The name of the return type of the endpoint. The value MUST be a type name that exists within the Conjure definition. If not specified, then the endpoint does not return a value.
<a name="endpointArgs"></a>args | Map[`string`, [Argument Object](#argumentObject) \| `string`] | A map between argument names and argument definitions. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. Furthermore, if a `string` the argument will default to `auto` [Param Type Field](#paramTypeField).
<a name="endpointDocs"></a>docs | `string` | Documentation for the endpoint. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.

// TODO: markers


### <a name="argumentObject"></a>Argument Object
An object representing an argument to an endpoint.

##### Fixed Fields
Field name | Type | Description
---|:---:|---
<a name="argumentType"></a>type | `string` | **REQUIRED**. The type of the value of the argument. The type name MUST exist within the Conjure definition.
<a name="argumentDeprecated"></a>deprecated | `string` | Documentation for why this argument is deprecated. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
<a name="argumentParamId"></a>param-id | `string` | An identifier to use as a parameter value. If the param type is `header` or `query`, this field may be populated to define the identifier that is used over the wire. If this field is undefined for the `header` or `query` param types, the argument name is used as the wire identifier. Population of this field is invalid if the param type is not `header` or `query`.
<a name="argumentParamType"></a>param-type | [Param Type Field](#paramTypeField) | The type of the endpoint parameter. If omitted the default type is `auto`.

// TODO(forozco): add markers

### <a name="paramTypeField"></a>Param Type Field
A field describing the type of an endpoint parameter. It is a `string` which MUST be one of the following:
- `path`: defined as a path parameter; the argument name must appear in the request line.
- `body`: defined as the singular body parameter.
- `header`: defined as a header parameter.
- `query`: defined as a querystring parameter.
- `auto`: argument is treated as a path parameter if the argument name appears between braces in the request line and as a body argument otherwise.

### <a name="richText"></a>Rich Text Formatting
Throughout the specification `docs` fields are noted as supporting CommonMark markdown formatting.
Where Conjure tooling renders rich text it MUST support, at a minimum, markdown syntax as described by [CommonMark 0.27](http://spec.commonmark.org/0.27/). 
