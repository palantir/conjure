import { IHttpApiBridge } from "@foundry/conjure-fe-lib";

export class DuplicateExample {
    private bridge: IHttpApiBridge;

    constructor(
        bridge: IHttpApiBridge
    ) {
        this.bridge = bridge;
    }

}
