export interface IHttpEndpointOptions {
    endpointPath: string;
    endpointName: string;
    method: string;
    requestMediaType: string;
    responseMediaType: string;
    requiredHeaders: string[];
    pathArguments: any[];
    queryArguments: any;
    data?: any;
}

export interface IHttpApiBridge {
    callEndpoint<T>(parameters: IHttpEndpointOptions): Promise<T>;
}
