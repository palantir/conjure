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

We propose that every revision of the Conjure wire format be labelled with a format identifier.  The current PLAIN+JSON
format shall be labelled `application/json; conjure=1`. For allow for backwards compatibility, clients and servers
should interpret a `application/json` header without a `conjure=<version>` parameter as the `application/json;
conjure=1` format.

### Format capabilities

We assume that clients and servers implement both serialization and deserialization for a given format:

- A server can consume requests with a given format iff it can produce responses with the same format.
- A client can produce requests with a given format iff it can consume responses with the same format.

### Negotiation protocol

**Requests.** 
Clients send the format identifier used to encode the request as a Content-Type header, and a format list as an Accept 
header. The format indicates the preference-ordered list of formats that the client supports. The supported formats
must include the format use to encode the request, i.e., the format specified in the Content-Type request header.

**Responses.**
Servers that do not support the request format respond with Conjure error UNSUPPORTED/415. Otherwise, if the server
does support the request format, it uses the most-preferred (as per Accept request header) format to encode the
response and advertise the chosen format in the response Content-Type header. 

Every response (including non-success responses) must send a preference-ordered format list of supported formats as
Accept response header. (Note that this is a non-standard header for HTTP 1.1 responses.)

**Negotiation.**
Format negotiation takes place in the context of a "session" of subsequent requests made by a client to a server (or
service). A client may assume, loosely, that a session is active to a given server or service as long as it receives
responses with non-error responses.

At the beginning of a session, clients have no knowledge of the list of formats supported by the server. Clients may
update the list of formats supported by a given server or service as per the formats advertised in the Accept headers of
responses in the session. Typically, a client will pick its most preferred format that is advertised by the server
in the previous response of the session. To bootstrap the negotiation, clients shall assume that servers support the
most recent variant of the `application/json` format known to the client, and use this version for the first request of
every session.

**Example.** The following sequence of two requests and corresponding responses are between a client that supports CBOR
version 2, CBOR version 1, and JSON version 1, and that prefers formats in that order, and a server that supports CBOR
version 1 and JSON version 1. To bootstrap the session, the client encodes the first request with the latest known JSON
format, version 1. The servers encodes the response with the format most preferred by the client that it also supports
itself, CBOR version 1. The second request is encoded with the format most preferred by the client that the server
supports, CBOR version 1.


```text
Client ---------> Server
Content-Type: application/json; conjure=1
Accept: application/cbor; conjure=2, application/cbor; conjure=1, application/json; conjure=1

Client <--------- Server
Content-Type: application/cbor; conjure=1
Accept: application/cbor; conjure=1, application/json; conjure=1

Client ---------> Server
Content-Type: application/cbor; conjure=1
Accept: application/cbor; conjure=2, application/cbor; conjure=1, application/json; conjure=1

Client <--------- Server
Content-Type: application/cbor; conjure=1
Accept: application/cbor; conjure=1, application/json; conjure=1
```

In the rare case that server does not support the bootstrap format, the error response will carry a list of supported
formats (see above) from which the client can choose when retrying the request.
