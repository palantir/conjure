# Conjure definitions

A Conjure definition is made up of one or more source [YAML](http://yaml.org/) files. Each file may define multiple _types_, _services_ and _errors_. Types may also be imported from other files. Source files must end in `.yml`.  Here is a suggested structure:

```
your-project/src/main/conjure/foo.yml
your-project/src/main/conjure/bar.yml
your-project/src/main/conjure/baz.yml
```

The Conjure compiler requires each file to conform to the [ConjureSourceFile][] structure, specified below:

  - [ConjureSourceFile][]
    - [TypesDefinition][]
      - [ExternalTypeDefinition][]
        - [ExternalImportDefinition][]
      - [NamedTypesDefinition][]
        - [TypeName][]
        - [ConjureType][]
        - [ContainerType][]
        - [BuiltIn][]
        - [AliasDefinition][]
        - [ObjectTypeDefinition][]
        - [FieldDefinition][]
        - [UnionTypeDefinition][]
        - [EnumTypeDefinition][]
        - [EnumValueDefinition][]
      - [ErrorDefinition][]
      - [ErrorCode][]
    - [ServiceDefinition][]
      - [AuthDefinition][]
      - [EndpointDefinition][]
      - [ArgumentDefinition][]
      - [ArgumentDefinition.ParamType][]
    - [DocString][]

Note: All field names in the specification are **case sensitive**. In the following description, if a field is not explicitly **REQUIRED** or described with a MUST or SHALL, it can be considered OPTIONAL.

<!-- This markdown document uses non-breaking dashes '&#8209;' and non-breaking spaces '&nbsp;' to ensure that table rows look nice. -->

## ConjureSourceFile
[ConjureSourceFile]: #conjuresourcefile
Each source file must be a YAML object with the following allowed fields:

Field | Type | Description
---|:---:|---
types | [TypesDefinition][] | The types to be included in the definition.
services | Map[`string`&nbsp;&rarr;&nbsp;[ServiceDefinition][]] | A  map between a service name and its definition. Service names MUST be in PascalCase.

## TypesDefinition
[TypesDefinition]: #typesdefinition
The object specifies the types available in the Conjure definition.

Field | Type | Description
---|:---:|---
conjure&#8209;imports | Map[`string`&nbsp;&rarr;&nbsp;`string`] | A map between a namespace and a relative path to a Conjure definition file. Namespace aliases MUST match `^[_a-zA-Z][_a-zA-Z0-9]*$`
imports | Map[`string`&nbsp;&rarr;&nbsp;[ExternalTypeDefinition][]] | A map between a type alias and its external definition. Type aliases MUST be in PascalCase.
definitions | [NamedTypesDefinition][] | The types specified in this definition.

### conjure-imports
For example, one file called `common.yml` might define a Conjure type called `ProductId`:

```yaml
types:
  definitions:
    default-package: com.palantir.product
    objects:
      ProductId:
        alias: string
```

A separate file in the same directory, `example.yml`, can then reference types defined in `common.yml`:

```yaml
types:
  conjure-imports:
    common: common.yml
  definitions:
    default-package: com.palantir.product
    objects:
      SomeRequest:
        id: common.ProductId
```

## ExternalTypeDefinition
[ExternalTypeDefinition]: #externaltypedefinition
A type that is not defined within Conjure. Usage of external types is strongly discouraged because Conjure is unable to validate that external types match the serialization format of the base type. They are intended only to migrate existing APIs to Conjure.

Field | Type | Description
---|:---:|---
base&#8209;type | [ConjureType][] | A `base-type` is provided as a hint to generators for how to handle this type when no external type reference is provided. Note that the serialization format of the `base-type` fallback should match the format of the imported type. If the imported type is a non-primitive JSON object, then a `base-type` of any should be used.
external | [ExternalImportDefinition][] | The external types to reference.

**Example:**
```yaml
types:
  imports:
    SomeDataType:
      base-type: string
      external:
        java: com.palantir.package.someDataType
```

## ExternalImportDefinition
[ExternalImportDefinition]: #externalimportdefinition
References to types that are not defined within Conjure.

Field | Type | Description
---|:---:|---
java | `string` | The fully qualified Java type.

## NamedTypesDefinition
[NamedTypesDefinition]: #namedtypesdefinition
The object specifies the types that are defined in the Conjure definition.

Field | Type | Description
---|:---:|---
default&#8209;package | `string` |
objects | Map[[TypeName][] &rarr; [AliasDefinition][] or [ObjectTypeDefinition][] or [UnionTypeDefinition][] or [EnumTypeDefinition][]] | A map between type names and type definitions.
errors | Map[[TypeName][]&nbsp;&rarr;&nbsp;[ErrorDefinition][]] |A map between type names and error definitions.

Package names are used by generator implementations to determine the output location and language-specific namespacing. Package names should follow the Java style naming convention: `com.example.name`.


## ConjureType
[ConjureType]: #conjuretype
A ConjureType is either a reference to an existing [TypeName][], a [ContainerType][] or a [BuiltIn][].


## TypeName
[TypeName]: #typename
Named types must be in PascalCase and be unique within a package.


## ContainerType
[ContainerType]: #containertype
Container types like `optional<T>`, `list<T>`, `set<T>` and `map<K, V>` can be referenced using their lowercase names, where variables like `T`, `K` and `V` can be substituted for a Conjure named type, a built-in or more container types:

**Examples:**
```
optional<datetime>
list<double>
map<string, boolean>
set<SomeExistingType>
map<rid, optional<datetime>>
```


## BuiltIn
[BuiltIn]: #builtin
Built-in types are always lowercase, to distinguish them from user-defined types which are PascalCase.

**Examples:**
```
any
bearertoken
binary
boolean
datetime
double
integer
rid
safelong
string
uuid
```

## AliasDefinition
[AliasDefinition]: #aliasdefinition
Definition for an alias complex data type.

Field | Type | Description
---|:---:|---
alias | [ConjureType][] | **REQUIRED**. The Conjure type to be aliased.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [NamedTypesDefinition][].


## ObjectTypeDefinition
[ObjectTypeDefinition]: #objecttypedefinition
Definition for an object complex data type.

Field | Type | Description
---|:---:|---
fields | Map[`string` &rarr; [FieldDefinition][] or [ConjureType][]] | **REQUIRED**. A map from field names to type names.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [NamedTypesDefinition][].

Field names must appear in either lowerCamelCase, or kebab-case, or snake_case. Code generators will respect casing for wire format, but may convert case formats to conform with language restrictions. As a result, field names must be unique independent of case format (e.g. an object may not define both caseFormat and case-format as fields).

**Examples:**
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


## FieldDefinition
[FieldDefinition]: #fielddefinition
Definition for a field in a complex data type.

Field | Type | Description
---|:---:|---
type | [ConjureType][] | **REQUIRED**. The name of the type of the field. It MUST be a type name that exists within the Conjure definition.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
deprecated | [DocString][] | Documentation for why this field is deprecated. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.


## UnionTypeDefinition
[UnionTypeDefinition]: #uniontypedefinition
Definition for a union complex data type.

Field | Type | Description
---------- | ---- | -----------
union | Map[`string` &rarr; [FieldDefinition][] or [ConjureType][]] | **REQUIRED**. A map from union names to type names. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. Union names MUST be in PascalCase.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [NamedTypesDefinition][].

It is common for a generator to also generate a visitor interface for each union, to facilitate consumption and customization of behavior depending on the wrapped type. The interface includes a visit() method for each wrapped type, as well as a visitUnknown(String unknownType) method which is executed when the wrapped object does not match any of the known member types.

**Example:**
```yaml
Payload:
  package: com.palantir.example
  union:
    serviceLog: ServiceLog
    notAServiceLog: RequestLog
```


## EnumTypeDefinition
[EnumTypeDefinition]: #enumtypedefinition
Definition for an enum complex data type.

Field | Type | Description
---|:---:|---
values | List[string or [EnumValueDefinition][]] | **REQUIRED**. A list of enumeration values. All elements in the list MUST be unique and be UPPERCASE.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [NamedTypesDefinition][].

**Example:**
```yaml
LoadState:
  values:
    - LOADING
    - LOADED
    - ERROR
```

## EnumValueDefinition
[EnumValueDefinition]: #enumvaluedefinition
Definition for a single value within an enumeration.

Field | Type | Description
---|:---:|---
value | string | **REQUIRED**. The enumeration value. Value MUST be unique and be UPPERCASE.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
deprecated | [DocString][] | Documentation for why this value is deprecated. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.

## ErrorDefinition
[ErrorDefinition]: #errordefinition
Definition for an error type.

Field | Type | Description
---|:---:|---
namespace | `string` | **REQUIRED**. The namespace of the error. The namespace MUST be in PascalCase.
code | [ErrorCode][] | **REQUIRED**. The general category for the error.
safe&#8209;args | Map[`string` &rarr; [FieldDefinition][]&nbsp;or&nbsp;[ConjureType][]] | **REQUIRED**. A map from argument names to type names. These arguments are considered safe in accordance with the SLS specification. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
unsafe&#8209;args | Map[`string` &rarr; [FieldDefinition][]&nbsp;or&nbsp;[ConjureType][]] | **REQUIRED**. A map from argument names to type names. These arguments are considered unsafe in accordance with the SLS specification. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.


## ErrorCode
[ErrorCode]: #errorcode
A field describing the error category. MUST be one of the following strings, with HTTP status codes defined in the [wire spec](/docs/spec/wire.md#34-conjure-errors):
* PERMISSION_DENIED
* INVALID_ARGUMENT
* NOT_FOUND
* CONFLICT
* REQUEST_ENTITY_TOO_LARGE
* FAILED_PRECONDITION
* INTERNAL
* TIMEOUT
* CUSTOM_CLIENT
* CUSTOM_SERVER


## ServiceDefinition
[ServiceDefinition]: #servicedefinition
A service is a collection of endpoints.

Field | Type | Description
---|:---:|---
name | [TypeName][] | **REQUIRED** A human readable name for the service.
package | `string` | **REQUIRED** The package of the service.
base&#8209;path | [PathString][] | **REQUIRED** The base path of the service. The path MUST have a leading `/`. The base path is prepended to each endpoint path to construct the final URL. [Path parameters][] are not allowed.
default&#8209;auth | [AuthDefinition][] | **REQUIRED** The default authentication mechanism for all endpoints in the service.
docs | [DocString][] | Documentation for the service. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
endpoints | Map[`string`&nbsp;&rarr;&nbsp;[EndpointDefinition][]] | **REQUIRED** A map of endpoint names to endpoint definitions.

## PathString
[PathString]: #pathstring
A PathString consists of segments separated by forward slashes, `/`. Segments may be literals (any alphanumeric string beginning with a letter and may contain the characters `.`, `_`, `-`) or path parameters (see below).

When comparing multiple paths, the path with the longest concrete path should be matched first.

## Path parameters
[Path parameters]: #path-parameters
Curly braces, `{}`, can be used to mark a section of a PathString as parameterized.

**Examples:**
Given a Conjure definition with the following paths, a request with path `/branch/foo` would be matched to `/branch/foo`.
```
/branch/{branchPath}
/branch/foo
```

Assuming the definition contains multiple path parameters (as shown below), a request with path `path/dataset/fetch` would be matched to `/path/dataset/{arg}`.
```
/path/{arg}/fetch
/path/dataset/{arg}
```


## AuthDefinition
[AuthDefinition]: #authdefinition
A field describing an authentication mechanism. It is a `string` which MUST be of the following:
* `none`: do not apply authorization requirements
* `header`: apply an `Authorization` header argument/requirement to every endpoint.
* `cookie:{{name}}`: apply a cookie argument/requirement to every endpoint, where `{{name}}` should be replaced with your desired cookie name.


## EndpointDefinition
[EndpointDefinition]: #endpointdefinition
An object representing an endpoint. An endpoint describes a method, arguments and return type.

Field | Type | Description
---|:---:|---
http | `string` | **REQUIRED** The operation and path for the endpoint. It MUST follow the shorthand `<method> <path>`, where `<method>` is one of GET, DELETE, POST, or PUT, and `<path>` is a [PathString][].
auth | [AuthDefinition][] | The authentication mechanism for the endpoint. Overrides `default-auth` in [ServiceDefinition][].
returns | [ConjureType][] | The name of the return type of the endpoint. The value MUST be a type name that exists within the Conjure definition. If not specified, then the endpoint does not return a value.
args | Map[`string` &rarr; [ArgumentDefinition][]&nbsp;or&nbsp;[ConjureType][]] | A map between argument names and argument definitions. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. Furthermore, if a `string` the argument will default to `auto` [ArgumentDefinition.ParamType][].
docs | [DocString][] | Documentation for the endpoint. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
deprecated | [DocString][] | Documentation for the deprecation of the endpoint. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
tags | Set[`string`] | Set of tags that serves as additional metadata for the endpoint.


## ArgumentDefinition
[ArgumentDefinition]: #argumentdefinition
An object representing an argument to an endpoint.

Field | Type | Description
---|:---:|---
type | [ConjureType][] | **REQUIRED**. The type of the value of the argument. The type name MUST exist within the Conjure definition. If this ArgumentDefinition has a param-type of `body` then there are no restrictions on the type. 
If the param-type is `path` then the de-aliased type MUST be an enum or a primitive (except `binary`, and `bearertoken`).
If the param-type is `query` then the de-aliased type MUST be an enum or a primitive (except `binary` and `bearertoken`), or a container (list, set, optional) of one of these.
If the param-type is `header` then the de-aliased type MUST be an enum or a primitive (except binary), or an optional of one of these.
markers | List[`string`] | List of types that serve as additional metadata for the argument. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. Prefer to use tags instead of markers.
tags | Set[`string`] | Set of tags that serves as additional metadata for the argument.
param&#8209;id | `string` | An identifier to use as a parameter value. If the param type is `header` or `query`, this field may be populated to define the identifier that is used over the wire. If this field is undefined for the `header` or `query` param types, the argument name is used as the wire identifier. Population of this field is invalid if the param type is not `header` or `query`.
param&#8209;type | [ArgumentDefinition.ParamType][] | The type of the endpoint parameter. If omitted the default type is `auto`.

Arguments with parameter type `body` MUST NOT be of type `optional<binary>`, or, intuitively, of a type that reduces to `optional<binary>` via unfolding of alias definitions and nested `optional`.


## ArgumentDefinition.ParamType
[ArgumentDefinition.ParamType]: #argumentdefinitionparamtype
A field describing the type of an endpoint parameter. It is a `string` which MUST be one of the following:
- `auto`: defined as the singluar body parameter or a path parameter if the name of the argument definition matches a path parameter
- `path`: defined as a path parameter; the argument name must appear in the request line.
- `body`: defined as the singular body parameter.
- `header`: defined as a header parameter.
- `query`: defined as a querystring parameter.


## DocString
[DocString]: #docstring
Throughout the specification `docs` fields are noted as supporting CommonMark markdown formatting.
Where Conjure tooling renders rich text it MUST support, at a minimum, markdown syntax as described by [CommonMark 0.27](http://spec.commonmark.org/0.27/).
