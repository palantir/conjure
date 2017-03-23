# this is package foundry.catalog.api.datasets
from conjure import *
from typing import Dict
from typing import List
from typing import Optional
from typing import Set
from typing import Tuple

class BackingFileSystem(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'fileSystemId': ConjureFieldDefinition('fileSystemId', str),
            'baseUri': ConjureFieldDefinition('baseUri', str),
            'configuration': ConjureFieldDefinition('configuration', DictType(str, str))
        }

    _fileSystemId = None # type: str
    _baseUri = None # type: str
    _configuration = None # type: Dict[str, str]

    def __init__(self, fileSystemId, baseUri, configuration):
        # type: (str, str, Dict[str, str]) -> None
        self._fileSystemId = fileSystemId
        self._baseUri = baseUri
        self._configuration = configuration

    @property
    def fileSystemId(self):
        # type: () -> str
        '''The name by which this file system is identified.'''
        return self._fileSystemId

    @property
    def baseUri(self):
        # type: () -> str
        return self._baseUri

    @property
    def configuration(self):
        # type: () -> Dict[str, str]
        return self._configuration

class Dataset(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'fileSystemId': ConjureFieldDefinition('fileSystemId', str),
            'rid': ConjureFieldDefinition('rid', str)
        }

    _fileSystemId = None # type: str
    _rid = None # type: str

    def __init__(self, fileSystemId, rid):
        # type: (str, str) -> None
        self._fileSystemId = fileSystemId
        self._rid = rid

    @property
    def fileSystemId(self):
        # type: () -> str
        return self._fileSystemId

    @property
    def rid(self):
        # type: () -> str
        '''Uniquely identifies this dataset.'''
        return self._rid

