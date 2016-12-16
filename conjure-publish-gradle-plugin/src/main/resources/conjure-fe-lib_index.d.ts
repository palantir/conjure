export const DefaultHttpApiBridge: any;
export interface IHttpApiBridge {
    callEndpoint: <T>(...args: any[]) => any;
}