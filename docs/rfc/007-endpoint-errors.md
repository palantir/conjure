# [RFC] Conjure Endpoint Errors

**Date:** 2021/03/19

**Stakeholders:**
* *Anyone that produces or consumes conjure-generated code.*

* * *

## **Context**

Currently, errors can be defined in Conjure type definitions. These errors can then be thrown by services, and if a client 
knows about them, they can be handled during runtime by clients. The exact handling of this mechanism may vary by
the implementation (for example, in Java these are `ServicException`s, which become `RemoteException`s which can be caught
by the client).

There are multiple issues with this. As a consumer, you only know that an endpoint may throw an error by reading the 
documentation or the implementation itself. As a producer, you are limited in the expressivity of what you can say 
with your exception, as you have no guarantee that a consumer will handle your exception (or even read your documentation).

This RFC introduces *declared errors* on Conjure endpoints. These errors represent real outcomes of a call to an endpoint,
and can be leveraged to make tooling to improve handling of errors.

## PRs 

* Root PR (Conjure API changes): https://github.com/palantir/conjure/pull/816

## Example Conjure implementations

**Conjure API**

```
types:
  definitions:
    default-package: com.palantir.my.api
    errors:
      BlueMoon:
        namespace: MyConjureService
        code: CUSTOM_SERVER

services:
  MyConjureService:
    name: An example service.
    package: com.palantir.my.api
    default-auth: header
    endpoints:
      getSomeResult:
        http: GET /
        returns: list<rid>
        errors:
          - BlueMoon
        docs: Returns some result, but once in a blue moon throws an error
```

*** 

## Proposal

This proposal should be viewed in two distinct parts - declared exceptions on Conjure endpoints and how they can be handled, 
and the possible language implementations of such a feature. The exact details on language implementation will be left
to the following PRs on the respective repositories.

### Intent

The primary intent of this proposal is to provide more expressivity around errors in a Conjure API, while minimizing the 
change surface-area, especially around wire format changes. Our proposed change introduces *no* wire changes, so should 
be purely additive and not dramatically affect back-compat.

### Declared Errors

**Semantics:** To declare an error on an endpoint is to say “This is a very real outcome of calling this endpoint. You 
should handle this outcome to ensure that you can react accordingly”. It should be something closely tied to the 
functionality of that endpoint - that is to say, `getFoo` declaring `FooNotReadyYet` would be a good example, but 
`UserNotAuthorized` would be undeclared / passed through as normal (as it is a standard expectation of interacting with 
an authorized service).

**Pros:**

* Consumers now know the *expected* outcomes of calling an endpoint in a first-class way and can handle those outcomes accordingly.
* Producers are able to express the different failure states of their endpoints in greater detail.
* Changing the failure states of an endpoint now visibly affects the API definition - changes in behavior are properly captured in code (rather than just documentation).

**Cons:**

* Given the new flexibility, producers may more eagerly throw errors in their code, and ‘old’ clients will have a hard 
  time dealing with these without upgrading.
* Producers might be incentivized to cram *all* the possible outcomes of their endpoint as errors (see the 
  `UserNotAuthorized` example above), and as such unduly burden consumers of their endpoint with things that should 
  probably just be `RuntimeException`s. This in turn might have knock-on effects in the codebase depending on the exact
  implementation used.
* Introducing further types (whether as checked exceptions or otherwise) that determine control flow may result in them 
  spreading throughout a code base and result in further coupling to the RPC layer. As an example, using checked exceptions
  could encourage the consumer to throw the exception upwards rather than dealing with it at the call-site.
  
## Implementation

Generally, the client-side implementation should be done using whatever language features are best supported by the language
itself. 

### Compatibility

It is worth noting that the changes suggested in this RFC do not entail a wire-format change, and as such should not
affect backwards (or forwards) compatibility. Old servers that throw exceptions will continue to use `SerializableError`,
and clients that do not upgrade can continue to catch `SerializableError`s as they did before.

The intention instead is to better leverage Conjure types to provide better tooling for categorising errors into their
types when they are received, and to improve documentation around what errors can be thrown by an endpoint.

#### Backwards Compatibility of Error Fields

As part of upgrading errors to a first-class part of the API, we should additionally treat errors in a first-class way
when it comes to backwards compatibility. As the new suggested tooling will make errors more usable, accessing fields
will also be more important, and (just as with regular fields) removing them will become more problematic.

As such, we suggest that fields in errors should now be tracked for backwards compatibility. Adding new fields or 
declaring new errors should still be fine, however.

## Next Steps

1) Implement new error declarations into documentation generation.
2) Start work on language-specific implementations:
   1) This will be split between server and client-side improvements.
   2) Server-side will most likely just be better tooling around throwing errors that you have declared.
   3) Client-side will begin with easy (and not-controversial) wins, such as better optional error handling.
