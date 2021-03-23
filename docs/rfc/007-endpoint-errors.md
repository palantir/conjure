# [RFC] Conjure Endpoint Errors

**Date:** 2020/03/19

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
and should be handled by consumers.

## Existing Documentation

### PRs for example implementation (in Java, using checked exceptions)

* Root PR (Conjure API changes): https://github.com/palantir/conjure/pull/816
* Conjure Java API Changes: https://github.com/palantir/conjure-java-runtime-api/pull/634
* Conjure Java & Dialogue implementation: https://github.com/palantir/conjure-java/pull/1275

## Example Conjure and Java implementations

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

### Example CLient Implementation via Exceptions


**Dialogue Client (Exceptions)**

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

### Example Client Implementation via Union Types


**Dialogue Client ()**

```
MyConjureServiceBlocking myConjureService = 
    witchcraft.conjureClients()
        .client(MyConjureServiceBlocking.class, "my-conjure-service").get();

List<ResourceIdentifier> myUnsafeResult = myConjureService.getSomeResult(AuthHeader.valueOf("authHeader"));
// v- is this null in the failure case?
List<ResourceIdentifier> mySafeResult1 = myConjureService.getSomeResult(AuthHeader.valueOf("authHeader"), MyConjureServiceErrors.getSomeResultBuilder()
    .blueMoon(e -> handleBlueMoonEvent())
    .build());
    
// v- could wrap the response into { ok: boolean, result: List<ResourceIdentifier> }, which throws a runtime exception
//    if you try to access `result` but `ok == false`.
SomeResultResponse mySafeResult2 = myConjureService.getSomeResult(AuthHeader.valueOf("authHeader"), MyConjureServiceErrors.getSomeResultBuilder()
    .blueMoon(e -> handleBlueMoonEvent())
    .build());
    
// v- alternative implementation where you force logic splitting
myConjureService.getSomeResult(AuthHeader.valueOf("authHeader"), MyConjureServiceErrors.getSomeResultBuilder()
    .blueMoon(e -> handleBlueMoonEvent())
    .ok(mySafeResult3 -> handleOk(mySafeResult3))
    .build());
```

## Proposal

This proposal should be viewed in two distinct parts - declared (and handled) exceptions on Conjure endpoints, and the 
language implementations of such a feature.

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
* Producers are able to express the different failure states of their endpoints in greater detail, and have conviction that those failure states are being handled by consumers.
* Changing the failure states of an endpoint now visibly affects the API definition - changes in behavior are properly captured in code (rather than just documentation).

**Cons:**

* To be effective, these failure cases will need to be ‘forced’ upon consumers via the compiler. This could be as complex 
  data types (perhaps unions of {success, error1, error2, ...} or via checked exceptions. This would entail some degree 
  of (justified) work migrating to a with-errors world to wire each failure case into the correct behavior in client code.
* Given the new flexibility, producers may more eagerly throw errors in their code, and ‘old’ clients will have a hard 
  time dealing with these without upgrading.
* Producers might be incentivized to cram *all* the possible outcomes of their endpoint as errors (see the 
  `UserNotAuthorized` example above), and as such unduly burden consumers of their endpoint with things that should 
  probably just be `RuntimeException`s. This in turn might have knock-on effects in the codebase depending on the exact
  implementation used.
* Introducing further types (whether as checked exceptions or otherwise) that determine control flow may result in them 
  spreading throughout a code base and result in further coupling to the RPC layer. As an example, using checked exceptions
  could encourage the consumer to throw the exception upwards rather than dealing with it at the callsite.
  
### Mandatory vs. Optional

As discussed above, declared errors can be either made to be optional, or mandatory. Both versions come with their own
pros and cons, and picking one side or the other inevitably involves making sacrifices.

**The case for mandatory handling**

_Forcing_ consumers to handle your exceptional cases is beneficial to the _producer_. You can have confidence that
consumers will be encouraged to handle the exceptional cases, even if they are not reading your documentation. Acknowledging
the presence of exceptional cases will be required at the minimum.

There are benefits as a consumer as well - additional errors, behaviors, or edge cases will be exposed to you on upgrading
your client, and you should (in theory) never miss out on a new case (assuming you keep your client relatively up to date).

**The case for optional handling**

Optional handling favors the _consumer_ - existing code and behaviors will not have to change at the outset, and they
can opt into improved error handling as necessary. The obvious downside is that producers could publish new errors until
the cows come home, but lazy consumers may never actually handle these edge cases.

## Implementation (Java)

Declaring errors on endpoints not only allows producers to enforce that each error case is going to be handled by a 
consumer, but it also allows us to provide the tools to make that ergonomic for consumers. Exactly how those ergonomics 
will be provided depend on the language. There were two main approaches that were considered:

**Enriched Types**

With an expressive type system, it is possible to express the different possible outcomes as an enriched version of the 
normal return type. This could be done, for example, by using union types, which already exist in the Conjure universe. 
Some languages are more amiable to union types than others. 

**Checked Exceptions**

Java provides checked exceptions as a method of enforcing that exceptional states are handled, and declared errors on 
endpoints fit nicely into that.

**Why The Demo Uses Checked Exceptions**

As of now, `SerializableError` does not lend itself to complex data types as complex error objects are flattened into a 
`Map<String, String>`, which makes parsing it out very difficult. Checked exceptions preserve the existing status-quo 
(that is, not messing with the reality of  `SerializableError`), but still improve expressivity and enforce errors being 
actually handled. 

## Open Questions ##

1) Should handling errors on endpoints be mandatory? While it seems like a good idea in principle, could it cause 'bad'
   behavior in client or server code?
2) In the Java implementation, are checked exceptions the way to go? There are several other possible implementations
   and ideas that may be more in-line with other languages, such as using a union type via a visitor pattern (which 
   can be made much less verbose via a 'Visitor Builder').
3) How important is implementation consistency across languages? Would it be acceptable to have union types in TS, but
   exceptions in Java, for instance?
4) How will the implementation function in a world where `SerializableError` kinda sucks still? What is the intermediary
   case, and what is the 'ideal' case?
