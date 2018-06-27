# RFC: Language repository structure

27 Jun 2018

## Goals

1. **Bring your own client** - clients should be decoupled from conjurized API so that users can substitute their own client implementation, or upgrade an existing client implementation without changing their conjurized API dependency
1. **Multiple conjurized APIs can coexist** - users must be able to depend on APIs coming from multiple different generator versions without encountering a diamond dependency
1. **Support multiple flavours** of client / server specifications - e.g. clients might be blocking or non-blocking, use dynamic proxies or not, and  servers could be netty, jetty etc.

## Suggested Repository Structure

### API

* client APIs
* server APIs
* common runtime library

### Generator

* client API generators
    * generated artifact depends on client API and object artifact
* server API generators
    * generated artifact depends on server API and object artifact
* object generators
    * generated artifact containing objects depends on common runtime library

### Implementation

* client implementations


# Alternatives considered

### Everything in one repository

* makes it hard for maintainers to comply with Goal #1
