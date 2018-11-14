# RFC: How to handle duplicate keys in maps and sets

13 Nov 2018

_Recommendations for how conjure deserialization logic should treat duplicate keys in maps and sets._

[JSON format]: ../spec/wire.md#5-json-format
[Canonical JSON format]: #canonical-json-format

## Proposal

When deserializing Conjure `set` or `map` keys, duplicate set items / map keys (according to the equivalence defined below)
are not allowed. Sets or maps containing duplicates should fail to deserialize.

Equivalence of two conjure values must be determined in a way that is isomorphic to comparing using the `compare` 
function defined below.

## Compare function

The compare function is defined as such:

> `compare(a, b)` compares two values of the same Conjure type, returning LT, EQ or GT;

It's helpful to define a couple of other functions in terms of conjure types:
 * `sort(values: list<T>) -> list<T>` sorts the values incrementally according to `compare`.
 * `fields(obj: conjure object) -> list<string>` returns a list of the object's fields.
 * `keys(map: map<K, V>) -> list<K>` returns a list of the map's keys.
 * `length(col: collection<T>) -> integer` returns the length of the collection (which may be `list` or `set`).
 * `obj.field` returns the field named `field` of that object
 * `map[k]` returns the value at field `k` if it exists in the map, or `null` is there is no such value.
 * `collection[i]` (where collection is `set` or `list`) returns the item at position `i` (0..length(collection))

If not mentioned below, `compare` for a conjure type is the result of a comparison on the raw JSON values, depending 
on the JSON type.

| JSON type             | Compared as |
| --------------------- | ----------- |
| string                | [conjure strings](#string) |
| integer               | trivial integer comparison |
| floating point number | [conjure doubles](#double) |

### String

`compare` for string values is the result of comparing `a` and `b` alphanumerically.

### Object

```
compare(obj1, obj2) = {
    for field in sort(fields(obj1)) {
        let cmp = compare(obj1[field], obj2[field])
        match cmp {
            EQ => continue
            _  => return cmp
        }
    }
}
```

### Union

```
value(u) = u[u.type]
compare(u1, u2) = {
    let cmp = compare(u1.type, u2.type)
    match cmp {
        EQ => compare(value(u1), value(u2))
        _  => cmp
    }
}
```

### Set

```
compare(s1, s2) = {
    let len_cmp = compare(length(s1), length(s2))
    if len_cmp != EQ {
        return len_cmp
    } 
    for i = 0..length(s1) {
        let cmp = compare(s1[i], s2[i])
        match cmp {
            EQ => continue
            _  => return cmp
        }
    }
}
```

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
