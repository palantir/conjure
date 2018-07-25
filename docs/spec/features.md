# Features

## HTTP endpoints

- GET, PUT, POST, DELETE
- Query parameters
- Path parameters
- Headers
- Cookie auth

## Types

### Complex types

Conjure supports defining the following named types. These can be referenced by their name elsewhere in a Conjure definition.

  - _Object_ - a collection of named fields, each of which has their own Conjure type.
  - _Enum_ - a type consisting of named string variants, e.g. "RED", "GREEN", "BLUE".
  - _Union_ - a type representing different named variants, each of which can contain differen types. (Also known as 'algebraic data types' or 'tagged unions')

### Collection types

  - `list<T>` - an ordered sequence of items of type `T`.
  - `map<K, V>` - values of type `V` each indexed by a unique key of type `K` (keys are unordered).
  - `optional<T>` - represents a value of type `T` which is either present or not present.
  - `set<T>` - a collection of distinct values of type `T`.

### Primitives

  - `any`
  - `bearertoken`
  - `binary`
  - `boolean`
  - `datetime`
  - `double`
  - `integer`
  - `rid`
  - `safelong`
  - `string`
  - `uuid`

### Migration types:

  - ExternalReference
