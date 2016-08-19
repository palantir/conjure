# TypescriptEteTest App

## Directory Structure

* `build/`: Compiled files and copied assets (ignored by git)
    * `build/min/`: Output from production builds
    * `build/src/`: Output from development builds
* `src/`: Source files
* `test/`: Test files
    * `test/tsconfig.json`: TypeScript compiler configuration for tests
    * `test/typings/`: Typings for tests
* `package.json`: Dependencies and build scripts
* `tsconfig.json`: TypeScript compiler configuration for source files
* `typings/`: Typings for source files

## Tasks

The following tasks are defined in the “scripts” block in package.json:

* `npm run clean`: delete all files in `build/`
* `npm run npmShrinkwrap`: update the `npm-shrinkwrap.json` file

### Compilation

* `npm run buildDev`: build source files for development
* `npm run build`: build source files for production
* `npm start`: build source files for development and automatically refresh on file changes
    * If Multipass is enabled, go to https://localhost:8443/typescript-ete-test/ to log in first.
    * Go to https://localhost:8543/typescript-ete-test/ to use the webpack dev server.
* `npm run watch`: build source files for development and rebuild on file changes

### Testing

* `npm test`: build and run tests once
* `npm run tdd`: build and run tests continuously

## Stack

* Languages
    * [TypeScript](http://www.typescriptlang.org/docs/tutorial.html): superset of JavaScript with types
    * [Less](http://lesscss.org/): CSS preprocessor
* Libraries
    * [React](https://facebook.github.io/react/docs/thinking-in-react.html): view library with virtual DOM and one-way data flow
    * [Redux](http://redux.js.org/): state library based on [Flux](http://facebook.github.io/flux/)
    * [Blueprint](https://blueprint.yojoe.local/): Palantir CSS framework
* Test tools
    * [Karma](http://karma-runner.github.io/0.13/intro/how-it-works.html): test runner
    * [Mocha](https://mochajs.org/): test framework
    * [Istanbul](https://gotwarlost.github.io/istanbul/): code coverage
* Build tools
    * [npm scripts](https://docs.npmjs.com/misc/scripts) (along with [npm-run-all](https://github.com/mysticatea/npm-run-all)): build scripts
    * [webpack](http://webpack.github.io/docs/) (see also [Highline's webpack guide](https://github.palantir.build/elements/highline/wiki/Webpack-Guide)): module bundler

## Customizing tasks

Tasks are implemented as shell commands, so customizing them is straightforward. For instance, to warn on lint issues instead of failing builds, add `|| true` to the end of lint scripts:

```json
// package.json

"lint:tslint": "tslint -c tslint.json 'src/**/*.ts*' || true",
```

## Development

Frontend build tasks will be run as [part of the Gradle build lifecycle](https://github.com/palantir/gradle-npm-run-plugin#tasks). While developing on the frontend, use `npm run watch` to incrementally rebuild frontend code on file change, and use `npm run tdd` to run tests on file change.

## IDE setup

### Atom

Use Atom with the [Atom TypeScript](https://atom.io/packages/atom-typescript) package. Consider using the packages for [eslint](https://atom.io/packages/linter-eslint), [stylelint](https://atom.io/packages/linter-stylelint), and [tslint](https://atom.io/packages/linter-tslint).

### IntelliJ

IntelliJ Ultimate has [good TypeScript support](https://www.jetbrains.com/help/idea/2016.1/typescript-support.html), but IntelliJ CE does not. Use Atom instead.

### Eclipse

Use the [Eclipse TypeScript plugin](https://github.com/palantir/eclipse-typescript). It does not support configuration via tsconfig.json, so configure it with the same compiler options as your tsconfig.json. In particular, set module resolution to “NodeJS.”


## Installing libraries

Install libraries with npm:

```sh
$ npm install --save @elements/blueprint-components # use --save-dev for build tools
```

If the library [comes with typings](http://www.typescriptlang.org/docs/handbook/typings-for-npm-packages.html), as Blueprint Components does, you can skip the next step. Otherwise, install the typings with [Typings](https://github.com/typings/typings) (`npm install -g typings`):

```sh
$ typings install --save --ambient $PACKAGE
```

Finally, to use the library, import it:

```tsx
import { Spinner } from "@elements/blueprint-components";
// or
import * as BlueprintComponents from "@elements/blueprint-components";

const mySpinner = <Spinner/>;

// using the namespace import
const anotherSpinner = <BlueprintComponents.Spinner/>;
```

## Updating libraries

Check if any libraries are outdated with npm:

```sh
$ npm outdated
Package                         Current  Wanted  Latest  Location
@elements/blueprint-components   0.24.0  0.24.0  0.25.0  typescript-ete-test
```

Update any libraries that are out of date:

```sh
$ npm install --save @elements/blueprint-components@0.25.0 # use --save-dev for build tools
```

If the library changed major versions, you may need to install updated typings:

```sh
$ typings install --save --ambient $PACKAGE
```
