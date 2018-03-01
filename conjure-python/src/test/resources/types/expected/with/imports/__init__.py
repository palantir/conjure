# this is package with.imports
from conjure import *
from httpremoting import Service
from product import AnyMapExample
from product import DateTimeAliasExample
from product import ManyFieldExample
from product import ReferenceAliasExample
from product import RidAliasExample
from product import StringAliasExample
from product import StringExample
from product.datasets import BackingFileSystem
from remoting.exceptions import HTTPError
from typing import Dict
from typing import List
from typing import Optional
from typing import Set
from typing import Tuple

class ComplexObjectWithImports(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'string': ConjureFieldDefinition('string', str),
            'imported': ConjureFieldDefinition('imported', StringExample)
        }

    _string = None # type: str
    _imported = None # type: StringExample

    def __init__(self, string, imported):
        # type: (str, StringExample) -> None
        self._string = string
        self._imported = imported

    @property
    def string(self):
        # type: () -> str
        return self._string

    @property
    def imported(self):
        # type: () -> StringExample
        return self._imported

class UnionWithImports(ConjureUnionType):

    _string = None # type: str
    _imported = None # type: AnyMapExample

    @classmethod
    def _options(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'string': ConjureFieldDefinition('string', str),
            'imported': ConjureFieldDefinition('imported', AnyMapExample)
        }

    def __init__(self, string=None, imported=None):
        if (string is not None) + (imported is not None) != 1:
            raise ValueError('a union must contain a single member')

        if string is not None:
            self._string = string
            self._type = 'string'
        if imported is not None:
            self._imported = imported
            self._type = 'imported'

    @property
    def string(self):
        # type: () -> str
        return self._string

    @property
    def imported(self):
        # type: () -> AnyMapExample
        return self._imported

AliasImportedObject = ManyFieldExample

AliasImportedPrimitiveAlias = StringAliasExample

AliasImportedReferenceAlias = ReferenceAliasExample

class ImportedAliasInMaps(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'aliases': ConjureFieldDefinition('aliases', DictType(RidAliasExample, DateTimeAliasExample))
        }

    _aliases = None # type: Dict[RidAliasExample, DateTimeAliasExample]

    def __init__(self, aliases):
        # type: (Dict[RidAliasExample, DateTimeAliasExample]) -> None
        self._aliases = aliases

    @property
    def aliases(self):
        # type: () -> Dict[RidAliasExample, DateTimeAliasExample]
        return self._aliases

class TestService(Service):

    def testEndpoint(self, importedString):
        # type: (StringExample) -> BackingFileSystem

        _headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        } # type: Dict[str, Any]

        _params = {
        } # type: Dict[str, Any]

        _path_params = {
        } # type: Dict[str, Any]

        _json = ConjureEncoder().default(importedString) # type: Any

        _path = '/catalog/testEndpoint'
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
            detail = e.response.json() if e.response else {}
            raise HTTPError('{}. Error Name: {}. Message: {}'.format(e.message, detail.get('errorName', 'UnknownError'), detail.get('message', 'No Message')), response=_response)

        _decoder = ConjureDecoder()
        return _decoder.decode(_response.json(), BackingFileSystem)

