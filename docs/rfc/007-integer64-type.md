# RFC: Conjure 64-bit Integer Type

2019-02-14

_The 54-bit Integer is Not Enough_

Conjure currently supports three numeric types: `integer`, `double`, and `safelong`. Unfortunately for many use-cases, 2<sup>53</sup> - 1 is not large enough.

These cases are relatively common and often result in one of three dangerous workarounds:
1. Use safelong because it exists. We often measure nanoseconds, however a safelong can only represent three months of nanoseconds, which is long enough that it's unlikely to be caught in testing.
1. Represent 64-bit integer values as strings in Conjure. This is only as correct as the application code which sets and parses values, which is precisely the problem Conjure is built to solve. In this case the Conjure definition fails to accurately describe the wire API.
1. Use external type imports to polyfill the 64-bit integer type. This is incredibly dangerous, often resulting in code that behaves inconsistently between languages.

## Proposal

Add a new Conjure type, `integer64`, with semantics matching `integer` except for the allowed values, and JSON serialized form.

For the immediate future, typescript clients may represent these values using branded strings.
Once a serialization layer is implemented for the typescript client, they may take advantage of the `integer64`
type using the proposed [BigInt](https://github.com/tc39/proposal-bigint) type which is already supported by
chrome, with a [polyfill](https://github.com/GoogleChromeLabs/jsbi) for other browsers.

### Allowed values

The `integer64` type supports signed 64-bit values ranging from -2<sup>63</sup> to 2<sup>63</sup> - 1.

### JSON Format

The `integer64` type is encoded to JSON as a JSON string containing the base-10 value, otherwise javascript
clients may transparently truncate numeric values. The [BigInt](https://github.com/tc39/proposal-bigint)
specification does not modify how JSON is parsed, so values must use existing JSON types.

For example, take a type defined as:

```yml
types:
  definitions:
    objects:
      Example:
        fields:
          integerField: integer
          safelongField: safelong
          integer64Field: integer64
```

Assuming each field takes the value `123`, the above example is serialized to JSON as:

```json
{
  "integerField": 123,
  "safelongField": 123,
  "integer64Field": "123"
}
```

#### The case against using JSON numeric format

We use JSON numbers elsewhere in Conjure and they, by definition, better capture the integer data type. However,
in javascript `JSON.parse` is the most common JSON parsing function, and it results in _incorrect data_ when
used with numeric values beyond 54-bits. Even if we do provide a serialization layer capable of handling large
integer values, applications are still likely to attempt usage of `JSON.parse` resulting in data loss.

Many existing applications already polyfill the 64-bit integer type using string encoding with the
following block:

```yml
types:
  imports:
    Long:
      base-type: string
      external:
        java: java.lang.Long
```

String encoded values allow these applications to upgrade and take advantage of the new type without breaking
existing clients. Without this implementation, it would not be possible to remove support for external type
imports without an impossibly severe API break.

### PLAIN Format

The PLAIN format of an `integer64` value is the base-10 numerical value.

## Alternatives Considered

### Arbitrary-Precision Integers

While arbitrary-precision integers are more flexible, the goal of the Conjure language is to describe data as
accurately as possible, which does not align with arbitrary precision. The need for such a type is dramatically
less common than 64-bit values, and the existing `binary` type provides sufficient utility to represent large
values.

### Use External Type Imports

Assuming java servers, and external type import for `java.lang.Long` may unblock in some cases, except it is
difficult to guarantee correctness and compatibility with typescript clients as Conjure implementations evolve over time.
This is not an option for non-java servers, none of which support external type imports.
We would like to remove external type imports from Conjure entirely. Without a mechanism to support 64-bit integers
many consumers will be unable to accept new versions of Conjure.

### Serialization using JSON Numeric encoding

See [The case against using JSON numeric format](#the-case-against-using-json-numeric-format).
