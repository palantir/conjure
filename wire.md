Conjure Wire Format
-------------------

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
