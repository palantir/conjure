# RFC: Conjure 64-bit Integer Type

2019-02-14

_The 54-bit Integer is Not Enough_

Conjure currently supports three numeric types: `integer`, `double`, and `safelong`. Unfortunately for many use-cases, 2<sup>53</sup> - 1 is not large enough.

## Proposal

New conjure `integer64` type semantics matching `integer` except for the allowed values, and JSON serialized form.

### Allowed values

The `integer64` type supports signed 64-bit values ranging from -2<sup>63</sup> to 2<sup>63</sup> - 1.

### JSON Format

The `integer64` type is encoded to JSON as a JSON string containing the base-10 value.

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

Where each field takes the value `123`, would be serialized to json as:

```json
{
  "integerField": 123,
  "safelongField": 123,
  "integer64Field": "123"
}
```

### PLAIN Format

The PLAIN format of an `integer64` value is the base-10 numerical value.

## Alternatives Considered

### Arbitrary-Precision Integers

While arbitrary-precision integers are more flexible, the goal of the conjure language is to describe data as
accurately as possible, which does not align with arbitrary precision. The need for such a type is dramatically
less common than 64-bit values, and the existing `binary` type provides sufficient utility to represent large
values.

### Use External Type Imports

Assuming java servers, and external type import for `java.lang.Long` may unblock in some cases, except it is
difficult to guarantee correctness and compatibility with typescript clients as conjure implementations evolve over time.
This is not an option for non-java servers, none of which support external type imports.
We would like to remove external type imports from conjure entirely, without a mechanism to support 64-bit integers
many consumers will be unable to accept new versions of conjure.
