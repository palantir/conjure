import { IHttpApiBridge } from "./httpApiBridge";
import { HttpApiBridge } from "./httpApiBridgeImpl";

let apiToken: string;

export function setApiToken(newApiToken: string) {
    apiToken = newApiToken;
}

function getApiToken() {
    return apiToken;
}

export function getHttpApiBridge(url: string): IHttpApiBridge {
    return new HttpApiBridge(url, getApiToken());
}
