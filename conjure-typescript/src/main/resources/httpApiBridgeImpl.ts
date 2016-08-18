export class HttpApiBridge implements IHttpApiBridge {
    private static APPLICATION_JSON = "application/json";

    private baseUrl: string;
    private token: string;

    constructor(baseUrl: string,
                token?: string) {
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public callEndpoint<T>(params: IHttpEndpointOptions) {
        const url = `${this.baseUrl.replace(/\/$/, "")}/${this.buildPath(params)}${this.buildQueryString(params)}`;
        const headers: { [key: string]: string } = {};
        if (params.requiredHeaders.length > 0) {
            if (params.requiredHeaders[0].toLowerCase() !== "authorization" || params.requiredHeaders.length > 1) {
                throw new Error("Required headers contains unknown headers: " + JSON.stringify(params.requiredHeaders));
            } else {
                headers["Authorization"] = this.token;
            }
        }
        const fetchRequestInit: RequestInit = {
            headers,
            method: params.method,
        };
        if (params.data) {
            fetchRequestInit.body = JSON.stringify(params.data);
            (fetchRequestInit.headers as any)["Content-Type"] = params.requestMediaType;
        }
        return fetch(url, fetchRequestInit).then((response) => {
            if (!response.ok) {
                throw response;
            }
            switch (response.headers.get("Content-Type")) {
                case HttpApiBridge.APPLICATION_JSON:
                    return response.json();
                default:
                    return response.text();
            }
        }).catch((error) => {
            throw error;
        });
    }

    private buildPath(parameters: IHttpEndpointOptions) {
        const urlParameterRegex = /\{[^\}]+\}/;
        let path = parameters.endpointPath;
        for (let pathArgument of parameters.pathArguments) {
            pathArgument = pathArgument || "";
            path = path.replace(urlParameterRegex, pathArgument);
        }
        for (const key of Object.keys(parameters.queryArguments)) {
            if (parameters.queryArguments[key] == null) {
                delete parameters.queryArguments[key];
            }
        }
        return path.replace(/^\//, "");
    }

    private buildQueryString(parameters: IHttpEndpointOptions) {
        let query: string[] = [];
        for (const key of Object.keys(parameters.queryArguments)) {
            query.push(`${key}=${parameters.queryArguments[key]}`);
        }
        return query.length > 0 ? `?${query.join("&")}` : "";
    }
}
