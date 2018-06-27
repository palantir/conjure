# RFC: Decomposition of concerns

27 Jun 2018

# Goals

* Decoupled client implementations - users can substitute their own client implementation
* Users should be able to use generated APIs coming from two different generator versions of the same language
* Make it hard to make accidental changes to the APIs, specifically in lock-step with the generator
* Support multiple flavours of client / server specifications

# Flavours

Within one language, there can be multiple ways of producing a client or server.

For instance, clients might be blocking or non-blocking, use dynamic proxies or not.

For servers, it should be feasible to use completely different technologies (netty, jetty, ...).

# Suggested Repository Structure

### API

* client APIs (one for each supported flavour)
* server APIs (one for each supported flavour)
* common runtime library

### Generator

* client API generators (one for each supported flavour)
    * generated artifact depends on client API and object artifact
* server API generators (one for each supported flavour)
    * generated artifact depends on server API and object artifact
* object generators
    * generated artifact containing objects depends on common runtime library

### Implementation

* client implementations
