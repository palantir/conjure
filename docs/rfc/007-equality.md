# RFC: How to handle duplicate keys in maps and sets

13 Nov 2018

_Recommendations for how Conjure serialization and deserialization should treat duplicate keys in maps and sets._

[JSON format]: ../spec/wire.md#5-json-format

## Proposal

When serializing or deserializing Conjure `set` or `map` keys, duplicate set items / map keys are not allowed.
Client must never send these, and it's recommended that servers reject them.

Some Conjure values have multiple equivalent representations, therefore we need to define equality carefully.
This RFC proposes an equivalence relation that languages should adhere to. 

## Eq function

The equivalence function is defined as such:

> `eq(a, b)` compares two [JSON values][JSON format] of the same de-aliased Conjure type, returning true or false

It's helpful to define a couple of other functions in terms of conjure types:
 * `fields(obj: conjure object) -> list<string>` returns a list of the object's fields.
 * `keys(map: map<K, V>) -> list<K>` returns a list of the map's keys.
 * `length(col: collection<T>) -> integer` returns the length of the collection (which may be `list` or `set`).
 * `obj.field` returns the field named `field` of that object
 * `map[k]` returns the value at field `k` if it exists in the map, or `null` is there is no such value.
 * `collection[i]` (where collection is `set` or `list`) returns the item at position `i` (0..length(collection))
 


If not mentioned below, `eq` for a conjure type is the result of a comparison on the raw JSON values, depending 
on the JSON type.

| Conjure type          | Compared as                  |
| --------------------- | -----------                  |
| `any`                 | ??                             |
| `bearertoken`         | [string_eq][]                |
| `binary`              | ?                             |
| `boolean`             | [boolean_eq][]                             |
| `datetime`            | [datetime_eq][]                             |
| `double`              | [float_eq][]                             |
| `integer`             | [integer_eq][] |
| `rid`                 | [string_eq][]   |
| `safelong`            | [integer_eq][]                             |
| `string`              | [string_eq][]                              |
| `uuid`                | [string_eq][]   |
| _Enum_                |                              |
| _Object_              |                              |
| _Union_               |                              |
| `list<T>`             |                              |
| `set<T>`              |                              |
| `map<K, V>`           |                              |
| `optional<T>`         |                              |

### string_eq

`string_eq(a, b)` returns true iff the unicode code point representations of `a` and `b` are the same.

Therefore, `string_eq("\x102", "2")` should be `true`.

### Integer

`integer_eq(a, b)` for integer values should compare the numbers according to their byte representations as 32-bit or 64-bit numbers.
This is safe since integer values and byte representations form a bijective relation.

### Safelong

`eq(a, b)` for safelong values should compare the numbers according to their byte representations.
This is safe since long values and byte representations form a bijective relation.

### Object

```
eq(obj1, obj2) = {
    for field in fields(obj1) {
        let result = eq(obj1[field], obj2[field])
        match result {
            true   => continue
            false  => return false
        }
    }
}
```

### Union

```
value(u) = u[u.type]
eq(u1, u2) = {
    let result = eq(u1.type, u2.type)
    match result {
        true  => eq(value(u1), value(u2))
        false => return false
    }
}
```

### Set

For two sets `a` and `b`, `eq(a, b)` is true if:

* `a` and `b` contain the same number of elements
* there exists an ordering of `b` such that `eq(a[i], b[i])` for every `i` between `0` and `length(a)` (exclusive)
    As a consequence of the proposal (set items must be distinct), we also know that `eq(a[i], b[j])` should be false
    for any `i != j`.

### Map

```
compare(m1, m2) = TODO same as object
```

### Double
The canonical JSON format of a double must conform to these constraints:

1. Non-scientific notation must be used
1. At least one decimal point must be used, even if it is `.0`
1. No superfluous trailing `0` decimals outside of the above case

**Examples**:

|     JSON representation     |  Canonical representation   |
| --------------------------- | --------------------------- |
| `-0`                        | `-0.0`                      |
| `0`                         | `0.0`                       |
| `1`                         | `1.0`                       |
| `1.00000`                   | `1.0`                       |
| `"1e1"`                     | `10.0`                      |
| `1.2345678`                 | `1.2345678`                 |
| `1.23456780`                | `1.2345678`                 |
| `"NaN"`                     | `"NaN"`                     |
| `"Infinity"`                | `"Infinity"`                |
| `"-Infinity"`               | `"-Infinity"`               |

#### Ordering

Below is a table outlining the relative order of representative double values.
`compare(a, b)` is LT iff `a` appears higher than `b` in the table, EQ if they are equal, GT otherwise.

| Canonical representation |
| ------------------------ |
| `"-Infinity"`            |
| ...                      |
| `-0.0`                   |
| `0.0`                    |
| ...                      |
| `"Infinity"`             |
| `"NaN"`                  |


### Datetime
The canonical JSON format of a datetime is a string formatted according to `YYYY-MM-DDTHH:mm:ss.SSS±hh:mm`, in accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).

**Examples**:

|     JSON representation           |  Canonical representation         |
| --------------------------------- | --------------------------------- |
| `"2018-07-19T08:11:21Z"`          | `"2018-07-19T08:11:21.000+00:00"` |
| `"2018-07-19T08:11:21+00:00"`     | `"2018-07-19T08:11:21.000+00:00"` |
| `"2018-07-19T08:11:21-00:00"`     | `"2018-07-19T08:11:21.000+00:00"` |
| `"2018-07-19T08:11:21.123-00:00"` | `"2018-07-19T08:11:21.123+00:00"` |
| `"20180719T081121Z"`              | `"2018-07-19T08:11:21.000+00:00"` |
| `"2018-07-19T05:11:21+03:00"`     | `"2018-07-19T05:11:21.000+03:00"` |
 
Canonical datetimes are ordered the same as [strings](#string).
