# this is package example.api
from conjure import *
from typing import Dict
from typing import List
from typing import Optional
from typing import Set
from typing import Tuple

class StringExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'string': ConjureFieldDefinition('string', str)
        }

    _string = None # type: str

    def __init__(self, string):
        # type: (str) -> None
        self._string = string

    @property
    def string(self):
        # type: () -> str
        return self._string

class IntegerExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'integer': ConjureFieldDefinition('integer', int)
        }

    _integer = None # type: int

    def __init__(self, integer):
        # type: (int) -> None
        self._integer = integer

    @property
    def integer(self):
        # type: () -> int
        return self._integer

class SafeLongExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'safeLongValue': ConjureFieldDefinition('safeLongValue', int)
        }

    _safeLongValue = None # type: int

    def __init__(self, safeLongValue):
        # type: (int) -> None
        self._safeLongValue = safeLongValue

    @property
    def safeLongValue(self):
        # type: () -> int
        return self._safeLongValue

class DoubleExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'doubleValue': ConjureFieldDefinition('doubleValue', float)
        }

    _doubleValue = None # type: float

    def __init__(self, doubleValue):
        # type: (float) -> None
        self._doubleValue = doubleValue

    @property
    def doubleValue(self):
        # type: () -> float
        return self._doubleValue

class BinaryExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'binary': ConjureFieldDefinition('binary', BinaryType())
        }

    _binary = None # type: Any

    def __init__(self, binary):
        # type: (Any) -> None
        self._binary = binary

    @property
    def binary(self):
        # type: () -> Any
        return self._binary

class OptionalExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'item': ConjureFieldDefinition('item', str)
        }

    _item = None # type: str

    def __init__(self, item):
        # type: (str) -> None
        self._item = item

    @property
    def item(self):
        # type: () -> str
        return self._item

class ListExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'items': ConjureFieldDefinition('items', ListType(str))
        }

    _items = None # type: List[str]

    def __init__(self, items):
        # type: (List[str]) -> None
        self._items = items

    @property
    def items(self):
        # type: () -> List[str]
        return self._items

class SetExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'items': ConjureFieldDefinition('items', ListType(str))
        }

    _items = None # type: List[str]

    def __init__(self, items):
        # type: (List[str]) -> None
        self._items = items

    @property
    def items(self):
        # type: () -> List[str]
        return self._items

class MapExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'items': ConjureFieldDefinition('items', DictType(str, str))
        }

    _items = None # type: Dict[str, str]

    def __init__(self, items):
        # type: (Dict[str, str]) -> None
        self._items = items

    @property
    def items(self):
        # type: () -> Dict[str, str]
        return self._items

class EnumExample(ConjureEnumType):
    '''This enumerates the numbers 1:2.
'''

    ONE = 'ONE'
    '''ONE'''
    TWO = 'TWO'
    '''TWO'''
    UNKNOWN = 'UNKNOWN'
    '''UNKNOWN'''

class BooleanExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'coin': ConjureFieldDefinition('coin', bool)
        }

    _coin = None # type: bool

    def __init__(self, coin):
        # type: (bool) -> None
        self._coin = coin

    @property
    def coin(self):
        # type: () -> bool
        return self._coin

class AnyExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'any': ConjureFieldDefinition('any', object)
        }

    _any = None # type: Any

    def __init__(self, any):
        # type: (Any) -> None
        self._any = any

    @property
    def any(self):
        # type: () -> Any
        return self._any

class AnyMapExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'items': ConjureFieldDefinition('items', DictType(str, object))
        }

    _items = None # type: Dict[str, Any]

    def __init__(self, items):
        # type: (Dict[str, Any]) -> None
        self._items = items

    @property
    def items(self):
        # type: () -> Dict[str, Any]
        return self._items

class PrimitiveOptionalsExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'num': ConjureFieldDefinition('num', float),
            'bool': ConjureFieldDefinition('bool', bool),
            'integer': ConjureFieldDefinition('integer', int)
        }

    _num = None # type: float
    _bool = None # type: bool
    _integer = None # type: int

    def __init__(self, num, bool, integer):
        # type: (float, bool, int) -> None
        self._num = num
        self._bool = bool
        self._integer = integer

    @property
    def num(self):
        # type: () -> float
        return self._num

    @property
    def bool(self):
        # type: () -> bool
        return self._bool

    @property
    def integer(self):
        # type: () -> int
        return self._integer

class ManyFieldExample(ConjureBeanType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        return {
            'string': ConjureFieldDefinition('string', str),
            'integer': ConjureFieldDefinition('integer', int),
            'integerExample': ConjureFieldDefinition('integerExample', IntegerExample),
            'doubleValue': ConjureFieldDefinition('doubleValue', float),
            'optionalItem': ConjureFieldDefinition('optionalItem', str),
            'items': ConjureFieldDefinition('items', ListType(str)),
            'set': ConjureFieldDefinition('set', ListType(str)),
            'map': ConjureFieldDefinition('map', DictType(str, str))
        }

    _string = None # type: str
    _integer = None # type: int
    _integerExample = None # type: IntegerExample
    _doubleValue = None # type: float
    _optionalItem = None # type: str
    _items = None # type: List[str]
    _set = None # type: List[str]
    _map = None # type: Dict[str, str]

    def __init__(self, string, integer, integerExample, doubleValue, optionalItem, items, set, map):
        # type: (str, int, IntegerExample, float, str, List[str], List[str], Dict[str, str]) -> None
        self._string = string
        self._integer = integer
        self._integerExample = integerExample
        self._doubleValue = doubleValue
        self._optionalItem = optionalItem
        self._items = items
        self._set = set
        self._map = map

    @property
    def string(self):
        # type: () -> str
        return self._string

    @property
    def integer(self):
        # type: () -> int
        return self._integer

    @property
    def integerExample(self):
        # type: () -> IntegerExample
        return self._integerExample

    @property
    def doubleValue(self):
        # type: () -> float
        return self._doubleValue

    @property
    def optionalItem(self):
        # type: () -> str
        return self._optionalItem

    @property
    def items(self):
        # type: () -> List[str]
        return self._items

    @property
    def set(self):
        # type: () -> List[str]
        return self._set

    @property
    def map(self):
        # type: () -> Dict[str, str]
        return self._map

