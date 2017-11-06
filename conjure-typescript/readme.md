## Conjure-typescript ##

Conjure-typescript generates a TypeScript client package matching your Conjure files.

### Usage ###

#### Union Types ####
When you declare a union type in Conjure, Conjure-typescript generates code using
[TS discriminated unions](https://basarat.gitbooks.io/typescript/docs/types/discriminated-unions.html)
which lets you write flexible code with strong compiler checks. Let's take a look at how to use the code Conjure generates.

Say your Conjure file had the following type declaration, where `BigRocket` and `Laser` are types you declared previously:

```yml
DefenseItem:
  union:
    rocket: BigRocket
    laser: RedLaser
```

Let's look at an example usage of our generated TS package:

```ts
import { IDefenseItem } from "@palantir/my-conjure-package";

const weapon: IDefenseItem = getObjFromServer();

if (IDefenseItem.isRocket(weapon)) {
    // TS automatically knows `weapon` is an object of type `IDefenseItem_BigRocket` which is defined as
    // interface IDefenseItem_BigRocket { type: "rocket"; rocket: BigRocket; }
    launch(weapon.rocket);
} else if (weapon.type === "laser") {
    // This above check is equivalent to using the built-in convienience methods
    // TS now infers the type of weapon to be `IDefenseItem_RedLaser`
    powerUp(weapon.laser);
} else {
    // There's a risk that a server could've added additional members to its union type
    // As a client, we need to handle this gracefully, and Conjure provides a helpful `unhandledCase` method.
    unhandledCase(weapon, () => {
        console.warn("Unknown weapon:", weapon);
        abortMission();
    });
}
```

The `unhandledCase` method above causes a compiler error if you have unhandled cases at compile time.
This ensures you when you upgrade your Conjure TS package that you handle all potentially new union members.
In addition, it's possible that the service will have added new union members and your typings won't yet be updated.
`unhandledCase` here forces you to pass it a method in order to deal with this scenario gracefully.

`unhandledCase` uses the same ideas as the
[exhaustive checks described here](https://basarat.gitbooks.io/typescript/docs/types/discriminated-unions.html). You can define it as such in your codebase and use it in the same way for all union types:

```
function unhandledCase(obj: never, handler: () => any): never {
    handler();
    return obj;
}
```
