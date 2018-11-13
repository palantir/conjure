# RFC: How to handle duplicate keys in maps and sets

13 Nov 2018

_Recommendations for how conjure deserialization logic should treat duplicate keys in maps and sets._

[JSON format]: ../spec/wire.md#5-json-format
[Canonical JSON format]: #canonical-json-format

## Proposal

When deserializing Conjure `set` or `map` keys, duplicate set items / map keys (according to the equivalence defined below)
are not allowed. Sets or maps containing duplicates should fail to deserialize.

Equivalence of two conjure values can be determined by converting their JSON values to the [Canonical JSON format][] and 
then comparing byte equality.

## Canonical JSON Format
The Canonical JSON format is a constrained version of the [JSON format][] that disambiguates values for
types which have multiple distinct representations that are conceptually equivalent.
Implementations of Conjure clients/servers must convert types (even if implicitly) from their JSON/Plain format to
their canonical form when determining equality.

Aside from the cases described below, the canonical representation is the same as the [JSON representation][JSON format].

### Canonical double
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

### Canonical datetime
The canonical JSON format of a datetime is a string formatted according to `YYYY-MM-DDTHH:mm:ss±hh:mm`, in accordance with [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601).

**Examples**:

|     JSON representation       |  Canonical representation     |
| ----------------------------- | ----------------------------- |
| `"2018-07-19T08:11:21Z"`      | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T08:11:21+00:00"` | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T08:11:21-00:00"` | `"2018-07-19T08:11:21+00:00"` |
| `"20180719T081121Z"`          | `"2018-07-19T08:11:21+00:00"` |
| `"2018-07-19T05:11:21+03:00"` | `"2018-07-19T05:11:21+03:00"` |
 
