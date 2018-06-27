# RFC: Language repository structure

27 Jun 2018

## Goals

1. **Bring your own client** - clients should be decoupled from conjurized API so that users can substitute their own client implementation, or upgrade an existing client implementation without changing their conjurized API dependency
1. **Multiple conjurized APIs can coexist** - users must be able to depend on APIs coming from multiple different generator versions without encountering a diamond dependency
1. **Support multiple flavours** of client / server specifications - e.g. clients might be blocking or non-blocking, use dynamic proxies or not, and  servers could be netty, jetty etc.

## Conjurized API

The output of a Conjure generator should not contain client/server logic.
In the case of services, it should be a minimal specification of the user defined API.

This allows users to upgrade/swap out their client implementation, as long as it conforms to some predefined contract.

This contract couples generated code and implementations and therefore should be incredibly stable.

E.g. for conjure-java, the default contract for both clients and servers is jax-rs annotations.

## Suggested Repository Structure

### Contracts

* client contracts
* server contracts
* shared objects (e.g. SafeLong for conjure-java)

### Generator

* client API generators
    * generated artifact depends on client contract and object artifact
* server API generators
    * generated artifact depends on server contract and object artifact
* object generators
    * generated artifact containing objects depends on common runtime library

### Implementation

<!-- TODO -->


## Alternatives considered

### Everything in one repository

Makes it hard for maintainers to comply with Goals 1 and 2, because one PR to this repository could inadvertently change the contract between generated code and client implementations, such that there might be two conjurized APIs that cannot be satisfied by a single client implementation.

Goal 1 in particular is hard to comply with because implementations don't have a clear contract to adhere to.

### Generated artifacts contain client implementations inline

Violates Goal 1 (bring your own client) - users can't upgrade the client to fix a bug in e.g. the failover behaviour without having to ask the API maintainer to upgrade the generator and re-publish the relevant conjurized API (which might require back-porting).

