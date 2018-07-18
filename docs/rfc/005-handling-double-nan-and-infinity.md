# RFC: Handling double NaN and Infinity

17 Jul 2018

_[IEEE 754](https://en.wikipedia.org/wiki/IEEE_754)'s NaN/Infinity frequently appear in real-world data, but they are underspecified in Conjure - in fact, the [JSON spec](https://tools.ietf.org/html/rfc4627) itself has no provision for them:_

> Numeric values that cannot be represented as sequences of digits
   (such as Infinity and NaN) are not permitted.

In addition, NaN is frequently a source of programmer error as it is not orderable, so languages often just return false for any comparison (e.g. both x > NaN and x < NaN return false).  In addition, 'NaN' has multiple possible binary representations because it can be produced from multiple different non-sensical mathematical operations, e.g. 0/0, sqrt(-1).

## Proposal

Amend the Conjure specification to explicitly define a JSON wire representation for NaN and positive and negative Infinity:

| IEEE 754 double    | Canonical JSON representation |
|--------------------|-------------------------------|
| NaN                | JSON string `"NaN"`           |
| positive infinity  | JSON string `"Infinity"`      |
| negative infinity  | JSON string `"-Infinity"`     |
| positive zero      | JSON number `0.0`             |
| negative zero      | JSON number `-0.0`            |
| (all other values) | JSON number                   |

_Note: this definition is case sensitive, so "nan" and "infinity" are not valid, and neither is "+Infinity"._

To comply with the principle of least-surprise for NaN handling, we have two additional goals:

1. A received conjure `set<double>` should never contain more than one NaN
2. A received conjure `map<double, T>` should never contain more than one NaN key

To achieve these goals, we can define the canonical representation of NaN as the json string `"NaN"`, so that languages can deserialize JSON lists and de-dupe NaN values appropriately.

## Note on positive and negative zero

[IEEE 754](https://en.wikipedia.org/wiki/IEEE_754) also includes provision for both a positive and negative zero. In the table above, these are both considered valid, distinct values that can appear in sets and also as map keys (as they are not considered equal). Beware that java has inconsistent behaviour here between unboxed and boxed variants - we want the boxed behaviour.

```java
// Java handles zero equality differently for unboxed and boxed values!
System.out.println(0.0d == -0.0d); // prints 'true'
System.out.println(new Double(0.0d).equals(new Double(-0.0d))); // prints 'false'
```

## Language implications

Most languages already have a concept of Infinity and NaN, so we'd need to write some code to convert the JSON strings received from the wire into usable first-class concepts, e.g.:

* in Java, this could be `Double.NaN`, `Double.POSITIVE_INFINITY`, `Double.NEGATIVE_INFINITY`
* in JavaScript, this could be `NaN`, `Infinity`, `-Infinity`
* in Python this could be `float('nan')`, `float('inf')`, `-float('inf')`
* in Rust this could be `std::f64::NAN`, `std::f64::INFINITY`, `std::f64::NEG_INFINITY`

## Alternative: Ban NaN and Infinity

This entire problem could be avoided by re-defining the Conjure 'double' type to be a modified IEEE 754 type, where Infinity and NaN are _not considered valid_. This could be implemented by throwing errors at serialization time or by introducing some new SafeDouble class.

Pros:

* New services implemented using Conjure APIs will not introduce more NaNs into the world. Users of Conjure APIs can be sure that they don't have to handle NaN or Infinity.

Cons:

* Real users currently rely on Conjure to convey external data that is outside their control, where it is not practical to just ban NaN or Infinity. All these users would be forced to invent their own JSON representation of the concept of 'NaN' and 'Infinity', e.g. as a union type. These would be extremely inconvenient to use from most client languages, so servers and clients would end up writing adapters to handle their wire-formats. This alternative ends up defeating one of the original goals of Conjure, which was to stop the proliferation of these custom wire-format adapters.

