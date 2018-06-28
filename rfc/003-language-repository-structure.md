# RFC: Language repository structure

27 Jun 2018

_Recommendations for how to decompose and version functionality related to one Conjure generator._

## Goals

1. **Bring your own client** - clients should be decoupled from generated Conjure APIs so that users can substitute their own client implementation, or upgrade an existing implementation without changing their conjurized API dependency.
1. **Multiple conjurized APIs can coexist** - users must be able to depend on APIs coming from multiple different generator versions without encountering a diamond dependency.
1. **Support multiple flavours** of client / server specifications - e.g. clients might be blocking or non-blocking, use dynamic proxies or not, and  servers could be netty, jetty etc; the repository structure should not preclude this.
1. **Contributor-friendly** - repositories should be consistently named and easily discoverable so that contributors can easily open PRs and file issues.

## Generated code should be loosely coupled to clients

The output of a Conjure generator should not contain client/server logic, instead it should just comply with a _contract_. In the case of Conjure services, it should be a minimal specification of the user defined API. This allows users to upgrade/swap out their client implementation, as long as the new client conforms to the same contract.

This contract affects _all_ generated code and client implementations, so should be as stable as possible.

*Good example*: In the generated TypeScript code below, the 'contract' is the `IHttpApiBridge` and `IHttpEndopintOptions` interfaces.  You can see the generated code doesn't actually contain any logic, but delegates the real work to the user-supplied `client`.  This client can be upgraded independently, as long as it still complies with the contract.

```typescript
export class SomeGeneratedService {
    constructor(private client: IHttpApiBridge) {}

    public myEndpoint(body: SomeRequestBody): Promise<void> {
        const options: IHttpEndpointOptions = {
            data: body,
            endpointPath: "/my-endpoint",
            method: "POST",
            requestMediaType: MediaType.APPLICATION_JSON,
            responseMediaType: MediaType.APPLICATION_JSON,
        };
        return this.client.callEndpoint<void>(options);
    }
}
```

*Bad example*: In the snippet below, the generated code is not decoupled from the actual fetch implementation.  This is sub-optimal because users are not able to substitute their own client and would not even be able to pick up improvements/bugfixes to the `parseResponseBasedOnContentType` or `rejectNon2XXResponses` functions without asking the API author to re-publish this API with a new conjure version

```typescript
export class SomeGeneratedService {
    constructor(private url: string) {}

    public myEndpoint(body: SomeRequestBody): Promise<void> {
        return fetch(url + "/my-endpoint", {
            credentials: "same-origin",
            method: "POST",
            headers: {
                Authorization: `Bearer ${getAuthHeaderSomehow()}`
            }
        })
        .then(parseResponseBasedOnContentType)
        .then(rejectNon2XXResponses);
    }
}
```


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

