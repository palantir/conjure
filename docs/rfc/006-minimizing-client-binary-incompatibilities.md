# RFC: Minimizing client binary incompatibilities caused by wire-compatible API changes

18 Sep 2018

_Recommendations for how conjure-generators can avoid imposing client-side compilation breaks when wire-compatible changes are made to an API._

Wire-compatible changes can be divided into three categories:

1. **Endpoint argument changes** - e.g. adding a new optional header, re-ordering endpoint parameters
1. **Transparent name changes** - e.g. renaming a Conjure service or type, changing endpoint parameter names
1. **Type changes** - e.g. changing a field from `datetime` -> `string`, or introducing a new alias

This RFC recommends avoiding imposing compilation breaks on users as a result of the first category (endpoint argument changes). API changes that fall in category (2) or (3) are not addressed by this RFC.

## Proposal

Instead of exposing the arguments of an endpoint as a list of typed values, generated interfaces should model the
arguments for an endpoint as a mapping from argument name to its typed value.

Note: stability should not come at the cost of ergonomic usage. Generated interfaces should remain idiomatic, safe and easy to use.

## Example

Consider the following Conjure Definition:

```yaml
services:
  TestService:
    name: Test Service
    package: test.api

    endpoints:
      postEndpoint:
        http: POST /test/{name}
        args:
          name:
            type: string
            param-type: path
          birthday:
            type: string
            param-type: query
```

Previously, generated interfaces would look like:

```java
public interface TestService {
    @POST
    @Path("test/{name}")
    void postEndpoint(@PathParam("name") String name, @QueryParam("birthday") Optional<String> birthday);
}
```

```typescript
class TestService {
    public postEndpoint(name: string, birthday?: string | null): Promise<void> {}
}
```

```python
class TestService(Service):
    def postEndpoint(self, name, birthday=None):
        # type: (str, str) -> None
```

Interfaces that follow this proposal could instead look like:

```java
public interface TestService {
    PostEndpointBuilder postEndpoint() {}

    public interface PostEndpointBuilder {
        public PostEndpointBuilder name(String name) {}
        public PostEndpointBuilder birthday(String birthday) {}
        public void call();
    }
}
```

```typescript
class TestService {
    public postEndpoint(params: {name: string, birthday?: string | null}): Promise<void> {}
}
```

```python
class TestService(Service):
    def postEndpoint(self, *args, name, birthday=None):
        # type: (str, str) -> None
```

## Alternatives Considered

The approach outlined above does not support all classes of wire compatible API changes. In particular, the proposal is
still susceptible to breaks caused by parameter renaming. To avoid such breaks, generators would have to expose
more complicated interfaces which model parameters differently based on their type.

Query and header parameters may be modeled as mappings from names to typed value since the name is part of the wire
format. Body parameters can be handled trivially since there is at most 1 per request, but path parameters must be
handled differently since names are simply syntactic sugar.

Path parameters would be treated as a mapping from the index of the path segment to a typed value. This would allow for
renaming of the parameter as well as the unconventional, but technically valid, case where a constant segment
of a path becomes a parameter.

For example, consider the path segment `/foo/bar/{baz}` then `{baz}` is segment 2 of the path and would be
referenced as such by the consumer.

This approach was not chosen, because of the negative impact such a model would have on consumers of the interfaces.
