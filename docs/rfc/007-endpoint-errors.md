# [RFC] Conjure Endpoint Errors

**Date:** 2020/03/19

**Stakeholders:**
* *Anyone that produces or consumes conjure-generated code.*

* * *

## **Context**

Currently, errors can be defined in Conjure type definitions. These errors can then be thrown by services as `ServiceException`s (which are `RuntimeException`s), and if a client knows about them, they can be caught during runtime by clients (packaged up as `RemoteException`s).

There are multiple issues with this. As a consumer, you only know that an endpoint may throw an error by reading the documentation or the implementation itself. As a producer, you are limited in the expressivity of what you can say with your exception, as you have no guarantee that a consumer will handle your exception (or even read your documentation).

This RFC introduces *declared errors* on Conjure endpoints. These errors represent real outcomes of a call to an endpoint, and must be handled by consumers.

## Existing Documentation

### PRs

* Root PR (Conjure API changes): https://github.com/palantir/conjure/pull/816
* Conjure Java API Changes: https://github.com/palantir/conjure-java-runtime-api/pull/634
* Conjure Java & Dialogue implementation: https://github.com/palantir/conjure-java/pull/1275

## Example

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


**Undertow Service**

```
@Generated("com.palantir.conjure.java.services.UndertowServiceInterfaceGenerator")
public interface MyConjureService {
    /**
     * Returns some result, but once in a blue moon throws an error
     * @apiNote {@code GET /}
     */
    List<ResourceIdentifier> getSomeResult(AuthHeader authHeader)
            throws MyConjureServiceErrors.BlueMoonServiceException;
```

**Undertow Resource**

```
public final class MyConjureResource implements MyConjureService {

   ...

   @Override
   public List<ResourceIdentifier> getSomeResult(AuthHeader authHeader)
          throws BlueMoonServiceException {
      throw MyConjureServiceErrors.throwIfBlueMoon(isBlueMoon);
   }
```


**Dialogue Client**

```
MyConjureServiceBlocking myConjureService = 
    witchcraft.conjureClients()
        .client(MyConjureServiceBlocking.class, "my-conjure-service").get();

try {
    myConjureService.getSomeResult(AuthHeader.valueOf("authHeader"));
} catch (BlueMoonRemoteException e) { // <- checked exception so has to be caught!
    handleBlueMoonEvent();
}
```



## Proposal

This proposal should be viewed in two distinct parts - declared (and handled) exceptions on Conjure endpoints, and the language implementations of such a feature.

### Intent

The primary intent of this proposal is to provide more expressivity around errors in a Conjure API, while minimizing the change surface-area, especially around wire format changes. Our proposed change introduces *no* wire changes, so should be purely additive and not dramatically affect back-compat.

### Declared Errors

**Semantics:** To declare an error on an endpoint is to say “This is a very real outcome of calling this endpoint. You should handle this outcome to ensure that you can react accordingly”. It should be something closely tied to the functionality of that endpoint - that is to say, `getFoo` declaring `FooNotReadyYet` would be a good example, but `UserNotAuthorized` would be undeclared / passed through as normal (as it is a standard expectation of interacting with an authorized service).

**Pros:**

* Consumers now know the *expected* outcomes of calling an endpoint in a first-class way and can handle those outcomes accordingly.
* Producers are able to express the different failure states of their endpoints in greater detail, and have conviction that those failure states are being handled by consumers.
* Changing the failure states of an endpoint now visibly affects the API definition - changes in behavior are properly captured in code (rather than just documentation).

**Cons:**

* To be effective, these failure cases will need to be ‘forced’ upon consumers via the compiler. This could be as complex data types (perhaps unions of {success, error1, error2, ...} or via checked exceptions. This would entail some degree of (justified) work migrating to a with-errors world to wire each failure case into the correct behavior in client code.
* Given the new flexibility, producers may more eagerly throw errors in their code, and ‘old’ clients will have a hard time dealing with these without upgrading.
* Producers might be incentivized to cram *all* the possible outcomes of their endpoint as errors (see the `UserNotAuthorized` example above), and as such unduly burden consumers of their endpoint with things that should probably just be `RuntimeException`s.
* Introducing further types (whether as checked exceptions or otherwise) that determine control flow may result in them spreading throughout a code base and result in further coupling to the RPC layer.

### Implementation

Declaring errors on endpoints not only allows producers to enforce that each error case is going to be handled by a consumer, but it also allows us to provide the tools to make that ergonomic for consumers. Exactly how those ergonomics will be provided depend on the language. There were two main approaches that were considered:

**Enriched Types**

With an expressive type system, it is possible to express the different possible outcomes as an enriched version of the normal return type. This could be done, for example, by using union types, which already exist in the Conjure universe. Some languages are more amiable to union types than others - even now Java doesn’t really provide the necessary tools to do this in a succinct way (ie. sealed classes with switch’ing over instances such as [Sealed classes | Kotlin](https://kotlinlang.org/docs/sealed-classes.html#sealed-classes-and-when-expression)).

**Checked Exceptions**

Java provides checked exceptions as a method of enforcing that exceptional states are handled, and declared errors on endpoints fit nicely into that.

**Why We Chose Checked Exceptions**

As of now, `SerializableError` does not lend itself to complex data types as complex error objects are flattened into a `Map<String, String>`, which makes parsing it out very difficult. Checked exceptions preserve the existing status-quo (that is, not messing with the reality of  `SerializableError`), but still improve expressivity and enforce errors being actually handled. Additionally, they are just more ergonomic in our current version of Java.
