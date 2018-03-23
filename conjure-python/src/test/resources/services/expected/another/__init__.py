# this is package another
from conjure import *
from httpremoting import Service
from product import CreateDatasetRequest
from product.datasets import BackingFileSystem
from product.datasets import Dataset
from remoting.exceptions import HTTPError
from typing import Dict
from typing import List
from typing import Optional
from typing import Set
from typing import Tuple

class TestService(Service):
    '''A Markdown description of the service.'''

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

        _path = '/catalog/fileSystems'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), DictType(str, BackingFileSystem))

    def createDataset(self, authHeader, request, testHeaderArg):
        # type: (str, CreateDatasetRequest, str) -> Dataset

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
            'Test-Header': testHeaderArg,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = ConjureEncoder().default(request) # type: Any

        _path = '/catalog/datasets'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'POST',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), Dataset)

    def getDataset(self, authHeader, datasetRid):
        # type: (str, str) -> Optional[Dataset]

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

        _path = '/catalog/datasets/{datasetRid}'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), OptionalType(Dataset))

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

        _path = '/catalog/datasets/{datasetRid}/raw'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            stream=True,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _raw = _response.raw
        _raw.decode_content = True
        return _raw

    def maybeGetRawData(self, authHeader, datasetRid):
        # type: (str, str) -> Optional[Any]

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

        _path = '/catalog/datasets/{datasetRid}/raw-maybe'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), OptionalType(BinaryType()))

    def uploadRawData(self, authHeader, input):
        # type: (str, Any) -> None

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = ConjureEncoder().default(input) # type: Any

        _path = '/catalog/datasets/upload-raw'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'POST',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        return

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

        _path = '/catalog/datasets/{datasetRid}/branches'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

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

        _path = '/catalog/datasets/{datasetRid}/branchesDeprecated'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), ListType(str))

    def resolveBranch(self, authHeader, datasetRid, branch):
        # type: (str, str, str) -> Optional[str]

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

        _path = '/catalog/datasets/{datasetRid}/branches/{branch}/resolve'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), OptionalType(str))

    def testParam(self, authHeader, datasetRid):
        # type: (str, str) -> Optional[str]

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

        _path = '/catalog/datasets/{datasetRid}/testParam'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), OptionalType(str))

    def testQueryParams(self, authHeader, something, implicit):
        # type: (str, str, str) -> int

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader,
        } # type: Dict[str, Any]

        _params = {
            'different': something,
            'implicit': implicit,
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = None # type: Any

        _path = '/catalog/test-query-params'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), int)

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

        _path = '/catalog/boolean'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

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

        _path = '/catalog/double'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

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

        _path = '/catalog/integer'
        _path = _path.format(**_path_params)

        _response = self._requests_session.request( # type: ignore
            'GET',
            self._uri + _path,
            params=_params,
            headers=_headers,
            json=_json)

        try:
            _response.raise_for_status()
        except HTTPError as e:
            detail = e.response.json() if e.response is not None else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), int)

