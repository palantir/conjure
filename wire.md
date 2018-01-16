Conjure Wire Format
-------------------

This document explains the JSON serialization format for Conjure-defined types. Every Conjure object (including
primitives, complex objects, containers, etc.) is representable as a JSON object such that, intuitively,
`deserialize(serialize(object)) == object`.


## Recursive definition of the JSON serialization format

The JSON format `json(o)` for a Conjure object `o` can be recursively defined as follows:
- If `o` is a primitive Conjure type, then `json(o)` is the corresponding primitive JSON type:
  - Conjure `string` → JSON `string`
  - Conjure `rid` → JSON `string`
  - Conjure `bearertoken` → JSON `string`
  - Conjure `integer` → JSON `number`
  - Conjure `safelong` → JSON `number`
  - Conjure `double` → JSON `number` | JSON `string` literal "NaN"
  - Conjure `boolean` → JSON `boolean`.
- If `o` is a Conjure `object`, then `json(o)` is a JSON `object` whose keys and values are obtained from `o`'s keys and
  values using `json(·)`. For any key with Conjure type `optional<?>`, the key/value pair may be omitted from the JSON map
  if the value is absent in `o`; alternatively, the value may be serialized using the serialization definition of
  `optional`, see below.
- If `o` is a Conjure `union` object `Union[<name>: <value>]` (where <name> is the name of one of the variants and
<value> is the corresponding value), then `json(o)` is the JSON `object`
  `{ "type" : "<name>", "<name>" : json(<value>) }` (with slight abuse of notation).
- If `o` is a Conjure `map`, then `json(o)` is a JSON `object` whose keys and values are serialized using `json(·)`
- If `o` is a Conjure collection (`list`, `set`), then `json(o)` is a JSON `array` whose elements are serialized using
  `json(.)`
- If `o` is a Conjure `alias` object `alias[v]`, then `json(o) = json(v)`
- If `o` is a Conjure `optional` object `optional[v]`, then `json(o)` is JSON `null` if `v` is absent, and `json(v)`
  otherwise
- If `o` is a Conjure `binary` object `binary[d]`, then `json(o)` is the Base64-encoded JSON `string` representation
  of the binary data `d`
- If `o` is a Conjure `datetime`, then `json(o)` is the ISO-8601-formatted JSON `string` representation of the date
- If `o` is a Conjure `enum`, then `json(o)` is the JSON `string` representing the enum value
- If `o` is a Conjure `any` object, then the serialization format is undefined. Implementations should strive to
  serialize nested or complex `any` objects as naturally corresponding JSON `objects`.


## Edge cases considered

The following is a non-exhaustive list of serialization/deserialization edge cases.

### Empty or null `optional`, `map`, `set` and `list` deserialize without error.

given:
```yaml
Obj:
  fields:
    ex: optional<string>
```

when any:
```json
{}
{"ex": null}
```

then:
```
Obj[ex: empty]
```

### Primitives may not be null.

given:
```yaml
Obj:
  fields:
    ex: string
```

when any:
```json
{}
{"ex": null}
```

then:
```
err
```

### Binary fields serialized as base64, deserialized from base64.
given:
```yaml
Obj:
  fields:
    ex: binary
```

then:
```
Obj[ex: 0x00 0x01 0x02] -> {"ex": "AAEC"}
{"ex": "AAEC"} -> Obj[ex: 0x00 0x01 0x02]
```

### Objects that implement equality must implement deep equality.
Objects must, in particular, be well behaved for any nested Conjure types.
- binary
- optional, map, set list
- primitives

given:
```yaml
A:
  fields:
    ex: set<B>
B:
  fields:
    op: optional<string>
C:
  fields:
    bin: optional<binary>
```

then, for `A`s:
```json
{"ex": []} == {}
{"ex": [{"op": null}]} == {[{}]}
{"ex": [{"op": "a"}]} == {"ex": [{"op": "a"}]}
{"ex": [{"op": "a"}, {"op": "b"}]} == {"ex": [{"op": "a"}, {"op": "b"}]}
{"ex": [{"op": "a"}]} != {"ex": [{"op": "b"}]}
{"ex": [{"op": "a"}]} != {"ex": [{"op": "a"}, {"op": "b"}]}
```

then, for `C`s:
```json
{"bin": "AAEC"} == {"bin": "AAEC"}
```

### Warning: `NaN` doesn't implement equality

Note, [Java specifies `NaN != NaN`](https://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html), so any Conjure object containing a `NaN` value will not be equal after
round-trip serialization.

### Objects that implement hashCode must implement deep hashCode.
Objects must, in particular, be well behaved for any nested Conjure types. For any
two objects defined as equal, implemented hashCode must also be equal.

### (Java) ignoreUnknownProperties ignores properties during deserialization.
given:
```yaml
Obj:
  fields:
    ex: optional<string>
```

when:
```json    
{"unk": "data"}
```

then:
```
Obj[ex: empty]
```

### Enumerations of any case deserialize correctly.
given:
```yaml
Enum:
  values:
    - AAA
    - BBB
```

when any of:
```json
"AAA"
"aaa"
"aAa"
"Aaa"
```

then:
```
Enum[AAA]
```

### (Java) ignoreUnknownEnumValues saves unknown enumeration values during deserialization and serializes original value.
given:
```yaml
Enum:
  values:
    - AAA
    - BBB
```

when:
```json
"CCC"
```

then:
```
Enum[CCC]
```

and then, when serialized, again:
```json
"CCC"
```

### (Java) alias types are equal when content is equal.
given:
```yaml
Alias:
  alias: string
```

then:
```
"AAA" == "AAA"
