# Concepts

_Conjure offers the following abstractions to use when defining your APIs. To see the JSON representation of these types, see the [Wire specification](/docs/spec/wire.md)._


### HTTP endpoints
- `GET`, `PUT`, `POST`, `DELETE` - [HTTP methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods).
- _Query parameters_ - e.g. `https://example.com/api/something?foo=bar&baz=2`
- _Path parameters_ - Parsed sections of URLs e.g. `https://example.com/repo/{owner}/{repo}/pulls/{id}`
- _Headers_ - A non-case sensitive string name associated with a Conjure value (see [docs](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers)).
- _Cookie auth_ - A [HTTP Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies), often used for authentication.


### Named types
Users may define the following kinds of named types. These can be referenced by their name elsewhere in a Conjure definition.

  - _Object_ - a collection of named fields, each of which has their own Conjure type.
  - _Enum_ - a type consisting of named string variants, e.g. "RED", "GREEN", "BLUE".
  - _Alias_ - a named shorthand for another Conjure type, purely for readability.
  - _Union_ - a type representing different named variants, each of which can contain different types. (Also known as 'algebraic data types' or 'tagged unions')

### Container types
  - `list<T>` - an ordered sequence of items of type `T`.
  - `map<K, V>` - values of type `V` each indexed by a unique key of type `K` (keys are unordered).
  - `optional<T>` - represents a value of type `T` which is either present or not present.
  - `set<T>` - a collection of distinct values of type `T`.

### Built-in types
  - `any` - a catch-all type which can represent arbitrary JSON including lists, maps, strings, numbers or booleans.
  - `bearertoken` - a string [Json Web Token (JWT)](https://jwt.io/)
  - `binary` - a sequence of binary.
  - `boolean` - `true` or `false`
  - `datetime` - an [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) value e.g. `2018-07-25T10:20:32+01:00`
  - `f32` - a 32-bit floating point number specified by [IEEE 754](https://ieeexplore.ieee.org/document/4610935/), which includes NaN, +/-Infinity and signed zero.
  - `double` - a 64-bit floating point number specified by [IEEE 754](https://ieeexplore.ieee.org/document/4610935/), which includes NaN, +/-Infinity and signed zero.
  - `integer` - a signed 32-bit integer value ranging from -2<sup>31</sup> to 2<sup>31</sup> - 1.
  - `rid` - a [Resource Identifier](https://github.com/palantir/resource-identifier), e.g. `ri.recipes.main.ingredient.1234`
  - `safelong` - a signed 53-bit integer that can be safely represented by browsers without loss of precision, value ranges from -2<sup>53</sup> + 1 to 2<sup>53</sup> - 1
  - `string` - a sequence of UTF-8 characters
  - `uuid` - [Universally Unique Identifier](https://en.wikipedia.org/wiki/Universally_unique_identifier#Versions) (aka guid) represented as a string

### Opaque types
When migrating an existing API, it may be useful to use the following 'escape hatch' type.  These are not recommended for normal APIs because Conjure can't introspect these external types to figure out what their JSON structure will be, so they're effectively opaque.

  - _External Reference_ - a reference to a non-Conjure type, with an associated fallback Conjure type

### Errors
Conjure allows defining named, structured errors so that clients can expect specific pieces of information on failure or handle entire categories of problems in one fell swoop.

- _Structured errors_ - have the following properties:
  - _Name_ -  a user chosen description of the error e.g. `RecipeLocked`
  - _Namespace_ - a user chosen category of the error e.g. `RecipeErrors`
  - _Code_ - one of the pre-defined categories.
  - _Args_ - a map from string keys to Conjure types
