export interface IBackingFileSystem {
    'baseUri': string;
    'configuration': { [key: string]: string };
    /** The name by which this file system is identified. */
    'fileSystemId': string;
}
