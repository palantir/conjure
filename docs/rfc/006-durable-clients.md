# RFC: Stable Clients with respect to backwards compatible API changes

18 Sep 2018

_Currently, conjure-generators represent services as interfaces with methods that take a list of parameters.
This makes code straight forward to use and generate but allows for backwards compatible API changes to break binary
compatibility. This RFC proposes a new approach for client generation to prevent this kind of binary breaks._

## Goals

1. **Stable Interfaces** - Given an API definition and a wire compatible change to the API, a conjure generated 
client should be usable before and after the change without requiring any manual changes.
2. **Ergonomic Usage** - Stability should not come at the cost of usability. Generated interfaces should be idiomatic,
safe and easy to use.

## Context

The majority of binary breaks in clients are caused by changes in the ordering of parameters to the corresponding
endpoint method. The parameters are typically ordered by type and then alphabetically by name. Hence, changes to the 
parameter order are caused by renaming of parameters or simply by the introduction of a new parameter. 

## Proposal

Instead of exposing the arguments of an endpoint as a list of typed values, generated interfaces should model the
arguments for an endpoint as a mapping from argument name to its typed value. This more closely aligns with how
conjure-core models endpoints and binary incompatibility by the introduction of new parameters.


## Example

Consider the following Conjure Definition:

```yaml
services:
  TestService:
    name: Test Service
    package: test.api

    endpoints:
      postEndpoint:
        http: POST /test/{name}/{type}
        args:
          name:
            type: string
            param-type: path
          type:
            type: string
            param-type: query
```

Previously, generated interfaces would look like:

```typescript
class TestService {
    public postEndpoint(name: string, type?: string | null): Promise<void> {}
}
```

```python
class TestService(Service):
    def postEndpoint(self, name, type=None):
        # type: (str, str) -> None
```

Interfaces that follow this proposal would instead look like:

```typescript
class TestService {
    public postEndpoint(params: {name: string, type?: string | null}): Promise<void> {}
}
```

```python
class TestService(Service):
    def postEndpoint(self, *args, name, type=None):
        # type: (str, str) -> None
```


## Alternatives Considered

The approach outlined above does not support all classes of wire compatible API changes. In particular, the proposal is
still susceptible to breaks caused by parameter renaming. To avoid such breaks, generators would have to expose 
more complicated interfaces which model parameters differently based on their type. 

Query and header parameters may be modeled as mappings from names to typed value since the name is part of the wire
format. Body parameters can simply be handled since there is at most 1 per request, but path parameters be differently
since names are simply syntactic sugar. 

Path parameters would be treated as a mapping from the index of the path segment to a typed value. This would allow for 
renaming of the parameter as well as the unconventional, but technically valid, case where a constant segment
of a path becomes a parameter. 

For example, consider the path segment `/foo/bar/{baz}` then `{baz}` is segment 2 of the path and would be 
referenced as such by the consumer.

This approach was not chosen, because of the negative impact such a model would have on consumers of the interfaces.












