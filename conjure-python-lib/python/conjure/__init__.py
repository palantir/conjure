from typing import List, Set, Dict, Tuple, Text, Optional, Type, Any, Union
from functools import wraps
import inspect
import json
from enum import Enum

class ConjureType:
    pass

DecodableType = Union[int, float, bool, str, ConjureType, List[Any], Dict[Any, Any]]

class ListType(ConjureType):
    item_type = None # type: Type[DecodableType]
    def __init__(self, item_type):
        # type: (Type[DecodableType]) -> None
        self.item_type = item_type

class DictType(ConjureType):
    key_type = None # type: Type[DecodableType]
    value_type = None # type: Type[DecodableType]
    def __init__(self, key_type, value_type):
        # type: (Type[DecodableType], Type[DecodableType]) -> None
        self.key_type = key_type
        self.value_type = value_type

class BinaryType(ConjureType):
    pass

class ConjureEnumType(ConjureType, Enum):
    pass

class ConjureBeanType(ConjureType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        '''fields is a mapping from constructor argument name to the field definition'''
        return {}

    def __eq__(self, other):
        # type: (Any) -> bool
        if not isinstance(other, self.__class__):
            return False
        for attr in self._fields():
            if getattr(self, attr) != getattr(other, attr):
                return False
        return True

    def __ne__(self, other):
        # type: (Any) -> bool
        return not self == other

    def __repr__(self):
        # type: () -> str
        fields = ['{}={}'.format(field_def.identifier, getattr(self, attr)) 
            for attr, field_def in self._fields().items()]
        return "{}({})".format(self.__class__.__name__, ', '.join(fields))

ConjureTypeType = Union[ConjureType, Type[DecodableType]]

class ConjureFieldDefinition:
    identifier = None # type: str
    field_type = None # type: ConjureTypeType
    optional = None # type: bool
    def __init__(self, identifier, field_type, optional = False):
        # type: (str, ConjureTypeType, bool) -> None
        self.identifier = identifier
        self.field_type = field_type
        self.optional = optional

class ConjureEncoder(json.JSONEncoder):
    '''Transforms a conjure type into json'''

    @classmethod
    def encode_conjure_type(cls, obj):
        # type: (ConjureBeanType) -> Any
        encoded = {} # type: Dict[str, Any]
        for attribute_name, field_definition in obj._fields().items():
            encoded[field_definition.identifier] = cls.do_encode(getattr(obj, attribute_name))
        return encoded

    @classmethod
    def do_encode(cls, obj):
        # type: (Any) -> Any
        if isinstance(obj, ConjureBeanType):
            return cls.encode_conjure_type(obj)
        elif isinstance(obj, ConjureEnumType):
            return obj.value
        elif isinstance(obj, list):
            return list(map(cls.do_encode, obj))
        elif isinstance(obj, dict):
            return dict(map(lambda x : (cls.do_encode(x[0]), cls.do_encode(x[1])), obj.items()))
        else:
            return obj

    def default(self, obj):
        # type: (Any) -> Any
        return self.do_encode(obj)

class ConjureDecoder:
    '''Decodes json into a conjure object'''

    @classmethod
    def decode_conjure_bean_type(cls, obj, conjure_type):
        deserialized = {} # type: Dict[str, Any]
        for arg_name, conjure_field_definition in conjure_type._fields().items():
            field_identifier = conjure_field_definition.identifier

            if field_identifier not in obj:
                if not conjure_field_definition.optional:
                    raise Exception('field {0} not found in object {1}'.format(field_identifier, obj))
                else:
                    deserialized[arg_name] = None
            else:
                value = obj[field_identifier]
                field_type = conjure_field_definition.field_type
                deserialized[arg_name] = cls.do_decode(value, field_type)
        return conjure_type(**deserialized)

    @classmethod
    def decode_conjure_enum_type(cls, obj, conjure_type):
        if obj in conjure_type.__members__:
            return conjure_type[obj]
        else:
            return conjure_type['UNKNOWN']

    @classmethod
    def decode_dict(cls, obj, key_type, item_type):
        # type: (Dict[Any, Any], ConjureTypeType, ConjureTypeType) -> Dict[Any, Any]
        if not isinstance(obj, dict):
            raise Exception("expected a python dict")
        
        return dict(map(lambda x : (cls.do_decode(x[0], key_type), cls.do_decode(x[1], item_type)), obj.items()))

    @classmethod
    def decode_list(cls, obj, item_type):
        # type: (List[Any], ConjureTypeType) -> List[Any]
        if not isinstance(obj, list):
            raise Exception("expected a python list")
        
        return list(map(lambda x: cls.do_decode(x, item_type), obj))

    @classmethod
    def do_decode(cls, obj, obj_type):
        # type: (Any, ConjureTypeType) -> Any
        if inspect.isclass(obj_type) and issubclass(obj_type, ConjureBeanType): # type: ignore
            return cls.decode_conjure_bean_type(obj, obj_type) # type: ignore
        elif inspect.isclass(obj_type) and issubclass(obj_type, ConjureEnumType): # type: ignore
            return cls.decode_conjure_enum_type(obj, obj_type)
        elif isinstance(obj_type, DictType):
            return cls.decode_dict(obj, obj_type.key_type, obj_type.value_type)
        elif isinstance(obj_type, ListType):
            return cls.decode_list(obj, obj_type.item_type)
        else:
            return obj

    def decode(self, obj, obj_type):
        # type: (Any, ConjureTypeType) -> Any
        return self.do_decode(obj, obj_type)

    def read_from_string(self, string_value, obj_type):
        # type: (str, ConjureTypeType) -> Any
        deserialized = json.loads(string_value)
        return self.decode(deserialized, obj_type)
