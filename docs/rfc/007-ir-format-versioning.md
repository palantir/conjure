# RFC: IR format versioning

18 Mar 2019

_In order to make changes to the IR, we need to decide on a versioning scheme that best communicates the impact of our changes, as this will likely have an impact on how other tools that interact with the IR are versioned.  This RFC proposes a single digit versioning scheme, where all IR changes increment this number._

## Proposal

Today, all IR documents start with:

```js
{
  "version" : 1,
  ...
}
```

This RFC proposes that _all_ changes to the IR format increment this number:

```js
{
  "version" : 2,
  ...
}
```

The implications of this are that:

- **consumers detect unsupported features** - A conjure generator which is feature complete (i.e. supports all concepts in the current IR) can immediately detect if it is passed an IR document that may contain unsupported features.  For example, a conjure-docs generator which converts IRv1 -> HTML could warn users if they tried to invoke it with an IRv2 document, as important information contained in the IRv2 document might not be fully captured in the resulting HTML (e.g. if IRv2 introduced new ways of marking fields as deprecated, the resulting HTML might still advertise them as non-deprecated)
- **consumers do best-effort generation** - when parsing an IR document, consumers should attempt to run even if the IR format version is higher or lower than they expected (as some of the IR changes might be benign or irrelevant). The version number is then used to present a helpful user-facing message if generation failed.

This single digit versioning scheme is mainly justified by considering the main alternative, a `major.minor` scheme to try and differentiate breaking and non-breaking changes:

## Alternative considered: `major.minor` versioning

Possible changes to the IR format can only be modelled as two categories: semantic additions or restrictions.

- **additive changes** - extend the language with new functionality, allow expressing new concepts. All previously valid IR files would still be considered valid files in the new format.
- **restrictive changes** - remove some language concept that was previously supported, or introduce some new stricter validation some previously valid IR files would no longer be considered valid.

(If the behaviour of language feature is changed, this can be modelled as removing the old feature and adding a new one)

There are also two types of interaction with the IR: producers and consumers.

- **producers** - API authors who write conjure YML and thereby produce (and publish) Conjure IR
- **consumers** - conjure generators consume IR and emit language-specific code

A contradiction arises because a major.minor versioning scheme can only really satisfy IR consumers or IR producers, it can't meaningfully communicate breaks to both:


&nbsp;                     | Additive IR Change     | Restrictive IR Change |
-------------------------- | ---------------------- | --------------------- |
**Producers**              | Don't care (minor rev) | Breaking (major rev)
**Consumers**              | Breaking (major rev)   | Don't care (minor rev)

Producers don't care about additive changes to the IR because they can keep taking upgrades whether the new features are relevant to them or not.  Producers do care about restrictive changes to the IR as this could potentially make their current API definitions invalid, thereby even . This suggests additive changes should increment minor, and restrictive should increment major.

However, consumers have the opposite perspective. Consumers don't care about restrictive changes to the IR - if some Conjure feature was banned, the same generators could keep running - they would just happen to no longer encounter certain categories of input. Additive changes on the other hand do require changes to generators in order to be supported - these can therefore be considered breaking.

## Draft releases

In order to allow iterating on an unreleased or experimental IR format, an additional 'draft' field could be defined in a dedicated RFC.
