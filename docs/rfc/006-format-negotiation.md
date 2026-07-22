# RFC: Conjure Format Negotiation

2018-10-04

Today, Conjure supports one wire format: PLAIN for primitive data types in headers, path parameters, and query
parameters, and JSON for complex objects (and primitives) in HTTP bodies. In the future, we may want to introduce new
versions of this wire format, or entirely new wire formats like CBOR. This RFC defines a negotiation strategy by which
clients and servers agree on the wire format used for requests and responses.

We have the following requirements for the format negotiation protocol:
- The protocol should in almost all cases not incur additional round-trips
- Clients and servers will eventually agree on the protocol that is (1) most preferred by the client and (2)
  supported by the server. That, clients drive the negotiation, constrained by server capabilities.

### Definitions

- A *version* is a positive integer
- A *Conjure format identifier* is a string of the `application/<format>; conjure=<version>` where `<format>` is a
  non-empty string over `[a-z-]` (e.g., `json`) and `<version>` is a version string (as above)
- A *Conjure format list* is a comma-separated, ordered list of Conjure format identifiers
- The `Accept` and `Content-Type` HTTP headers are defined as per the
  [HTTP 1.1 spec](https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html). Note that `Accept: <format list>` is a
  valid HTTP 1.1 Accept header, and that `Content-Type: <format identifier>` is a valid HTTP 1.1 Content-Type header.

### Wire format versioning

A Conjure wire format comprises specifications for Conjure parameters and return values. Today, the canonical format
uses JSON objects for `body` request and response objects and PLAIN for all other parameters.

We propose that every revision of a Conjure wire format be labelled with a format identifier. The current PLAIN+JSON
format shall be labelled `application/json; conjure=1`. To allow for backwards compatibility, clients and servers
should interpret a `application/json` header without a `conjure=<version>` parameter as the `application/json;
conjure=1` format.

### Format capabilities

We assume that clients and servers implement both serialization and deserialization for a given format:

- A server can consume requests with a given format iff it can produce responses with the same format.
- A client can produce requests with a given format iff it can consume responses with the same format.

### Negotiation protocol

**Requests.** 
Clients send the format identifier used to encode the request as a `Content-Type` header, and a format list as an
`Accept` header. The format indicates the preference-ordered list of formats that the client supports. The supported
formats must include the format use to encode the request, i.e., the format specified in the `Content-Type` request
header.

**Responses.**
Servers that do not support the request format respond with HTTP status code `415 Unsupported Media Type`. Otherwise, if
the server does support the request format, it uses the most-preferred (as per `Accept` request header) format to encode
the response and advertise the chosen format in the response `Content-Type` header. Upon receiving a `415` response,
clients may choose to repeat the request encoded with a different (i.e., typically older) format.

**Binary data** Conjure supports endpoints accepting `binary` request parameters or returning `binary` responses. In
accordance with the wire spec, a request carrying binary data in the request body must include a `Content-Type:
application/octet-stream` request header, and a server returning a `binary` response must include a `Content-Type:
application/octet-stream` response header. Further, when calling endpoints returning a `binary` response, clients must
include in the `Accept` format list:

- `application/octet-stream`
- the list of supported Conjure formats and versions (e.g., `application/json; version=2`)

The rationale for including both the `octet-stream` and the `json` (or `cbor`, etc.) formats is that the former is used
when returning binary data and the later for returning structured Conjure errors. The `application/octet-stream` format
is not versioned.


### Discussion

**Clients in control.** Clients are in control of the format negotiation in the sense that rank the list of acceptable
protocols based on their preference and let the server merely "chooses" based on its support for the most preferred
formats. Further, clients are in control of the trade-off between choosing the newest or most preferred versus an older
or more widely supported format. The former approach unlocks new formats and features more quickly, but may result in an
additional round-trip when the client has to reissue the request encoded with a different and hopefully supported
format. A more complex negotiation mechanism (e.g., based on OPTIONS endpoints or `Accept-Post` response headers) is
possible, but outside the scope of this RFC.

**OPTIONS.** This RFC does not propose to make the set of server-supported formats available as through an OPTIONS
request. A client receiving a `415` error from a server can instead retry the request with the maximal list of
client-supports formats and let the server pick the most preferred one.

**Blue/green, server vs service.** This RFC does not propose any explicit mechanism for distinguishing between the
different servers making up a remote service. For example, a service undergoing a blue/green migration between versions
that support different sets of formats may pick different response formats depending on which server handles the
request. We submit that this will rarely cause issues since clients and servers would still be able to agree on a format
and even switching between different formats for subsequent requests would be acceptable.

**Example: Conservative client.** The following sequence of two requests and corresponding responses are between a client
that supports CBOR version 2, CBOR version 1, and JSON version 1, and that prefers formats in that order, and a server
that supports CBOR version 1 and JSON version 1. To bootstrap a session, the client makes conservative assumptions about
the server's capabilities and encodes the first request with a widely supported JSON format, version 1. The servers
encodes the response with the format most preferred by the client that it also supports itself, CBOR version 1. The
second request is encoded with the format most preferred by the client that the server supports, CBOR version 1.

```text
Request
  Content-Type: application/json; conjure=1
  Accept: application/cbor; conjure=2, application/cbor; conjure=1, application/json; conjure=1

Response
  Content-Type: application/cbor; conjure=1

Request
  Content-Type: application/cbor; conjure=1
  Accept: application/cbor; conjure=2, application/cbor; conjure=1, application/json; conjure=1

Response
  Content-Type: application/cbor; conjure=1
```

**Example: Cutting edge client.** If a client accepts only the most cutting edge version of a format, an older server
may not be able to pick a supported format. The client may choose to error, or can alternatively retry the request
with an older format.

```text
Request
  Content-Type: application/json; conjure=2
  Accept: application/json; conjure=2

Response
  Status: 415

Request
  Content-Type: application/json; conjure=1
  Accept: application/json; conjure=1

Response
  Content-Type: application/json; conjure=1
```

**Example: Old server.** A server that predates this RFC may ignore the format instructions and decode the request
w.r.t. the canonical JSON format (i.e., `application/json; conjure=1`). The response should carry the appropriate
content type as per the wire spec.

```text
Request
  Content-Type: application/json; conjure=2
  Accept: application/json; conjure=2
  # Note that the server will interpret the version 2 request as if it were version 1

Response
  Content-Type: application/json
  # Clients may assume that application/json is the version 1 JSON format
```

**Example: JSON request, binary response.** A request to an endpoint returning `binary` data must include
both the `octet-stream` and a standard Conjure format list in the `Accept` header.

```text
Request
  Content-Type: application/json; conjure=2
  Accept: application/octet-stream, application/json; conjure=2

Response
  Content-Type: application/octet-stream
```

In case the server returns a structured Conjure error, it formats the error according to the most prefered of the
accepted Conjure formats.

```text
Request
  Content-Type: application/json; conjure=2
  Accept: application/octet-stream, application/json; conjure=2

Response
  Content-Type: application/json; conjure=2
  Status: 4xx or 5xx
  # body contains Conjure error in JSON version 2 format
```
