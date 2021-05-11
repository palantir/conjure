# Contributing

The team welcomes contributions!  To make code changes to one of the Conjure repos:

- Fork the repo and make a branch
- Write your code (ideally with tests) and make sure the CircleCI build passes
- Open a PR (optionally linking to a github issue)

If your change affects just one language or client, you'll probably just need to work in one of the following repos.  See the respective CONTRIBUTING documents for how to set up a dev environment.

  - [gradle-conjure](https://github.com/palantir/gradle-conjure)
  - [conjure-java](https://github.com/palantir/conjure-java)
  - [conjure-java-runtime](https://github.com/palantir/conjure-java-runtime)
  - [conjure-typescript](https://github.com/palantir/conjure-typescript)
  - [conjure-python](https://github.com/palantir/conjure-python)
  - [conjure-python-client](https://github.com/palantir/conjure-python-client)

For speculative changes, consider opening a GitHub issue to describe the problem you encountered and the motivation behind solving it.  If it affects more than one language, feel free to open the issue on this repo.

## Local development on docs

Our docs are built using [docsify](https://docsify.js.org/) which just renders markdown documents from the `/docs` folder. You can preview the docs site locally:

```
$ npm install -g docsify-cli
$ docsify serve
```

Markdown links can be checked using [liche](https://github.com/raviqqe/liche):

```
$ go get -u github.com/raviqqe/liche
$ liche -d . -r . -v
```
