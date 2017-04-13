# this is package foundry.catalog.api
from conjure import *
from foundry.catalog.api.datasets import BackingFileSystem
from foundry.catalog.api.datasets import Dataset
from httpremoting import Service
from typing import Dict
from typing import List
from typing import Optional
from typing import Set
from typing import Tuple

class CreateDatasetRequest(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'file_system_id': ConjureFieldDefinition('fileSystemId', str),
            'path': ConjureFieldDefinition('path', str)
        }

    _file_system_id = None # type: str
    _path = None # type: str

    def __init__(self, file_system_id, path):
        # type: (str, str) -> None
        self._file_system_id = file_system_id
        self._path = path

    @property
    def file_system_id(self):
        # type: () -> str
        return self._file_system_id

    @property
    def path(self):
        # type: () -> str
        return self._path

class ExplicitCreateDatasetRequest(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'backing_file_system': ConjureFieldDefinition('backingFileSystem', BackingFileSystem),
            'path': ConjureFieldDefinition('path', str)
        }

    _backing_file_system = None # type: BackingFileSystem
    _path = None # type: str

    def __init__(self, backing_file_system, path):
        # type: (BackingFileSystem, str) -> None
        self._backing_file_system = backing_file_system
        self._path = path

    @property
    def backing_file_system(self):
        # type: () -> BackingFileSystem
        return self._backing_file_system

    @property
    def path(self):
        # type: () -> str
        return self._path

class TestService(Service):

    def getFileSystems(self, authHeader):
        # type: (str) -> Dict[str, BackingFileSystem]

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/fileSystems'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), DictType(str, BackingFileSystem))

    def createDataset(self, authHeader, request):
        # type: (str, CreateDatasetRequest) -> Dataset

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = ConjureEncoder().default(request) # type: Any

        _path = '/datasets'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'POST',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), Dataset)

    def getDataset(self, authHeader, datasetRid):
        # type: (str, str) -> Dataset

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
            'datasetRid': datasetRid,
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/datasets/{datasetRid}'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), Dataset)

    def getRawData(self, authHeader, datasetRid):
        # type: (str, str) -> Any

        _headers = {
            'Accept': 'application/octet-stream',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
            'datasetRid': datasetRid,
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/datasets/{datasetRid}/raw'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            stream=True,
            json=_json)

        _response.raise_for_status()

        _raw = _response.raw
        _raw.decode_content = True
        return _raw

    def maybeGetRawData(self, authHeader, datasetRid):
        # type: (str, str) -> Any

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
            'datasetRid': datasetRid,
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/datasets/{datasetRid}/raw-maybe'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), BinaryType())

    def getBranches(self, authHeader, datasetRid):
        # type: (str, str) -> List[str]

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
            'datasetRid': datasetRid,
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/datasets/{datasetRid}/branches'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), ListType(str))

    def getBranchesDeprecated(self, authHeader, datasetRid):
        # type: (str, str) -> List[str]

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
            'datasetRid': datasetRid,
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/datasets/{datasetRid}/branchesDeprecated'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), ListType(str))

    def resolveBranch(self, authHeader, datasetRid, branch):
        # type: (str, str, str) -> str

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
            'datasetRid': datasetRid,
            'branch': branch,
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/datasets/{datasetRid}/branches/{branch}/resolve'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), str)

    def testParam(self, authHeader, datasetRid):
        # type: (str, str) -> str

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
            'dataset-rid': datasetRid,
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/datasets/{dataset-rid}/testParam'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), str)

    def testBoolean(self, authHeader):
        # type: (str) -> bool

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/boolean'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), bool)

    def testDouble(self, authHeader):
        # type: (str) -> float

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/double'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), float)

    def testInteger(self, authHeader):
        # type: (str) -> int

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/integer'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        _response.raise_for_status()

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), int)

