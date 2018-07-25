# Conjure Source Files

A Conjure definition is made up of one or more source [YAML](http://yaml.org/) files. Each file may define multiple _types_, _services_ and _errors_. The file boundaries have no semantic value as the Conjure compiler will combine these into one single IR document. Source files must end in `.yml`.  Here is a suggested structure:

```
your-project/src/main/conjure/foo.yml
your-project/src/main/conjure/bar.yml
your-project/src/main/conjure/baz.yml
```

The Conjure compiler requires each file to conform to the [ConjureSourceFile][] structure, specified below:

  - [ConjureSourceFile][]
    - [TypesDefinition][]
      - [ExternalTypeDefinition][]
      - [NamedTypesDefinition][]
        - [AliasDefinition][]
        - [ObjectTypeDefinition][]
        - [FieldDefinition][]
        - [UnionTypeDefinition][]
        - [EnumTypeDefinition][]
      - [ErrorDefinition][]
      - [ErrorCode][]
    - [ServiceDefinition][]
      - [AuthDefinition][]
      - [EndpointDefinition][]
      - [ArgumentDefinition][]
      - [ArgumentDefinition.ParamType][]
    - [DocString][]

Note: All field names in the specification are **case sensitive**. In the following description, if a field is not explicitly **REQUIRED** or described with a MUST or SHALL, it can be considered OPTIONAL.

<!-- These are not rendered, they just allow us to specify the hash URL in one place. -->

[AliasDefinition]: #aliasdefinition
[ArgumentDefinition.ParamType]: #argumentdefinitionparamtype
[ArgumentDefinition]: #argumentdefinition
[AuthDefinition]: #authdefinition
[ConjureSourceFile]: #conjuresourcefile
[DocString]: #docstring
[EndpointDefinition]: #endpointdefinition
[EnumTypeDefinition]: #enumtypedefinition
[ErrorCode]: #errorcode
[ErrorDefinition]: #errordefinition
[ExternalTypeDefinition]: #externaltypedefinition
[FieldDefinition]: #fielddefinition
[NamedTypesDefinition]: #namedtypesdefinition
[ObjectTypeDefinition]: #objecttypedefinition
[Path templating]: #path-templating
[PathString]: #pathstring
[ServiceDefinition]: #servicedefinition
[TypesDefinition]: #typesdefinition
[UnionTypeDefinition]: #uniontypedefinition

<!-- This markdown document uses non-breaking dashes '&#8209;' and non-breaking spaces '&nbsp;' to ensure that table rows look nice. -->

## ConjureSourceFile
Each source file must be a YAML object with the following allowed fields:

Field | Type | Description
---|:---:|---
types | [TypesDefinition][] | The types to be included in the definition.
services | Map[`string`,&nbsp;[ServiceDefinition][]] | A  map between a service name and its definition. Service names MUST be in PascalCase.


## TypesDefinition
The object specifies the types available in the Conjure definition.

Field | Type | Description
---|:---:|---
conjure&#8209;imports | Map[`string`,&nbsp;`string`] | A map between a namespace alias and a relative path to a Conjure definition. Namespace aliases MUST match `^[_a-zA-Z][_a-zA-Z0-9]*$`
imports | Map[`string`,&nbsp;[ExternalTypeDefinition][]] | A map between a type alias and its external definition. Type aliases MUST be in PascalCase.
definitions | [NamedTypesDefinition][] | The types specified in this definition.


## ExternalTypeDefinition
A type that is not defined within Conjure. Usage of external types is not recommended and is intended only to migrate existing APIs to Conjure.

Field | Type | Description
---|:---:|---
base&#8209;type | `string` | MUST be a a primitive data type.
external | Map[`string`,&nbsp;`string`] | A map between a language name and its associated fully qualified type.

A `base-type` is provided as a hint to generators for how to handle this type when no external type reference is provided. Note that
the serialization format of the `base-type` fallback should match the format of the imported type. If the imported type is a non-primitive JSON object, then a `base-type` of any should be used.

Each generator specifies what key they will consume within the `external` map.

**Example:**
```yaml
imports:
  SomeDataType:
    base-type: string
    external:
      java: com.palantir.package.someDataType
      typescript: @palantir/package
```


## NamedTypesDefinition
The object specifies the types that are defined in the Conjure definition.

Field | Type | Description
---|:---:|---
default&#8209;package | `string` |
definitions | Map[`string`, [AliasDefinition][] or [ObjectTypeDefinition][] or [UnionTypeDefinition][] or [EnumTypeDefinition][]] | A map between type names and type definitions.
errors | Map[`string`,&nbsp;[ErrorDefinition][]] |A map between type names and error definitions.

Package names are used by generator implementations to determine the output location and language-specific namespacing. Package names should follow the Java style naming convention: `com.example.name`.

Type names MUST be in PascalCase and be unique within a package.


## AliasDefinition
Definition for an alias complex data type.

Field | Type | Description
---|:---:|---
alias | `string` | **REQUIRED**. The name of the type to be aliased.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
package | `string` | **REQUIRED** if `default-package` is not specified. Overrides the `default-package` in [NamedTypesDefinition][].


## ObjectTypeDefinition
Definition for an object complex data type.

Field | Type | Description
---|:---:|---
fields | Map[`string`, [FieldDefinition][]&nbsp;or&nbsp;`string`] | **REQUIRED**. A map from field names to type names. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
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
Definition for a field in a complex data type.

Field | Type | Description
---|:---:|---
type | `string` | **REQUIRED**. The name of the type of the field. It MUST be a type name that exists within the Conjure definition.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.


## UnionTypeDefinition
Definition for a union complex data type.

Field | Type | Description
---------- | ---- | -----------
union | Map[`string`, [FieldDefinition][]&nbsp;or&nbsp;`string`] | **REQUIRED**. A map from union names to type names. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. Union names MUST be in PascalCase.
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
Definition for an enum complex data type.

Field | Type | Description
---|:---:|---
values | List[`string`] | **REQUIRED**. A list of enumeration values. All elements in the list MUST be unique and be UPPERCASE.
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


## ErrorDefinition
Definition for an error type.

Field | Type | Description
---|:---:|---
namespace | `string` | **REQUIRED**. The namespace of the error. The namespace MUST be in PascalCase.
code | [ErrorCode][] | **REQUIRED**. The general category for the error.
safe&#8209;args | Map[`string`, [FieldDefinition][]&nbsp;or&nbsp;`string`] | **REQUIRED**. A map from argument names to type names. These arguments are considered safe in accordance with the SLS specification. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
unsafe&#8209;args | Map[`string`, [FieldDefinition][]&nbsp;or&nbsp;`string`] | **REQUIRED**. A map from argument names to type names. These arguments are considered unsafe in accordance with the SLS specification. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
docs | [DocString][] | Documentation for the type. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.


## ErrorCode
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


## ServiceDefinition
A service is a collection of endpoints.

Field | Type | Description
---|:---:|---
name | `string` | **REQUIRED** A human readable name for the service.
package | `string` | **REQUIRED** The package of the service.
base&#8209;path | [PathString][] | **REQUIRED** The base path of the service. The path MUST have a leading `/`. The base path is prepended to each endpoint path to construct the final URL. [Path templating][] is not allowed.
default&#8209;auth | [AuthDefinition][] | **REQUIRED** The default authentication mechanism for all endpoints in the service.
docs | [DocString][] | Documentation for the service. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
endpoints | Map[`string`,&nbsp;[EndpointDefinition][]] | **REQUIRED** A map of endpoint names to endpoint definitions.

## PathString
A field describing an extendible path. A path segment MAY have [Path templating][].

When comparing multiple paths, the path with the longest concrete path should be matched first.

## Path templating
Path templating refers to the usage of curly braces ({}) to mark a section of a URL path as replaceable using path parameters. The template may include `:.+` and `:.*` regular expressions with the following semantics and limitations:
- `:.+` A non-greedy match of one or more path segments should be performed for the parameter
- `:.*` A non-greedy match of zero or more path segments should be performed for the parameter. `:.*` is only supported if the template is the final segment of the path

**Examples:**
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


## AuthDefinition
A field describing an authentication mechanism. It is a `string` which MUST be of the following:
* `none`: do not apply authorization requirements
* `header`: apply an `Authorization` header argument/requirement to every endpoint.
* `cookie:{{name}}`: apply a cookie argument/requirement to every endpoint, where `{{name}}` should be replaced with your desired cookie name.


## EndpointDefinition
An object representing an endpoint. An endpoint describes a method, arguments and return type.

Field | Type | Description
---|:---:|---
http | `string` | **REQUIRED** The operation and path for the endpoint. It MUST follow the shorthand `<method> <path>`, where `<method>` is one of GET, DELETE, POST, or PUT, and `<path>` is a [PathString][].
markers | List[`string`] | List of types that serve as additional metadata for the endpoint. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
auth | [AuthDefinition][] | The authentication mechanism for the endpoint. Overrides `default-auth` in [ServiceDefinition][].
returns | `string` | The name of the return type of the endpoint. The value MUST be a type name that exists within the Conjure definition. If not specified, then the endpoint does not return a value.
args | Map[`string`, [ArgumentDefinition][]&nbsp;or&nbsp;`string`] | A map between argument names and argument definitions. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition. Furthermore, if a `string` the argument will default to `auto` [ArgumentDefinition.ParamType][].
docs | [DocString][] | Documentation for the endpoint. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.


## ArgumentDefinition
An object representing an argument to an endpoint.

Field | Type | Description
---|:---:|---
type | `string` | **REQUIRED**. The type of the value of the argument. The type name MUST exist within the Conjure definition. The type MUST be a primitive if the argument is a path parameter and primitive or optional of primitive if the argument is header or query parameter.
markers | List[`string`] | List of types that serve as additional metadata for the argument. If the value of the field is a `string` it MUST be a type name that exists within the Conjure definition.
deprecated | `string` | Documentation for why this argument is deprecated. [CommonMark syntax](http://spec.commonmark.org/) MAY be used for rich text representation.
param&#8209;id | `string` | An identifier to use as a parameter value. If the param type is `header` or `query`, this field may be populated to define the identifier that is used over the wire. If this field is undefined for the `header` or `query` param types, the argument name is used as the wire identifier. Population of this field is invalid if the param type is not `header` or `query`.
param&#8209;type | [ArgumentDefinition.ParamType][] | The type of the endpoint parameter. If omitted the default type is `auto`.


## ArgumentDefinition.ParamType
A field describing the type of an endpoint parameter. It is a `string` which MUST be one of the following:
- `path`: defined as a path parameter; the argument name must appear in the request line.
- `body`: defined as the singular body parameter.
- `header`: defined as a header parameter.
- `query`: defined as a querystring parameter.
- `auto`: argument is treated as a path parameter if the argument name appears between braces in the request line and as a body argument otherwise.


## DocString
Throughout the specification `docs` fields are noted as supporting CommonMark markdown formatting.
Where Conjure tooling renders rich text it MUST support, at a minimum, markdown syntax as described by [CommonMark 0.27](http://spec.commonmark.org/0.27/).
