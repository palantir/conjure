from typing import List, Dict, Optional, Type, Any, Union
import inspect
import json
from enum import Enum


class ConjureType:
    pass


DecodableType = Union[
    int, float, bool, str,
    ConjureType, List[Any], Dict[Any, Any]]


class ListType(ConjureType):
    item_type = None  # type: Type[DecodableType]

    def __init__(self, item_type):
        # type: (Type[DecodableType]) -> None
        self.item_type = item_type


class DictType(ConjureType):
    key_type = None  # type: Type[DecodableType]
    value_type = None  # type: Type[DecodableType]

    def __init__(self, key_type, value_type):
        # type: (Type[DecodableType], Type[DecodableType]) -> None
        self.key_type = key_type
        self.value_type = value_type


class OptionalType(ConjureType):
    item_type = None  # type: Type[DecodableType]

    def __init__(self, item_type):
        # type: (Type[DecodableType]) -> None
        self.item_type = item_type


class BinaryType(ConjureType):
    pass


class ConjureEnumType(ConjureType, Enum):

    # override __format__ of ConjureEnumType to prevent issue with default
    # Enum behaviour due to lack of __format__ method on ConjureType
    def __format__(self, format_spec):
        return self.__str__()


class ConjureBeanType(ConjureType):

    @classmethod
    def _fields(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        '''_fields is a mapping from constructor argument
        name to the field definition'''
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


class ConjureUnionType(ConjureType):
    _type = None  # type: str

    @property
    def type(self):
        # type: () -> str
        '''the member name present in this union'''
        return self._type

    @classmethod
    def _options(cls):
        # type: () -> Dict[str, ConjureFieldDefinition]
        '''_options defines a mapping from each member in the union
        to the field definition for that type'''
        return {}

    def __eq__(self, other):
        # type: (Any) -> bool
        if not isinstance(other, self.__class__):
            return False
        assert isinstance(other, ConjureUnionType)
        return other.type == self.type \
            and getattr(self, self.type) == getattr(other, self.type)

    def __ne__(self, other):
        # type: (Any) -> bool
        return not self == other

    def __repr__(self):
        # type: () -> str
        fields = ['{}={}'.format(field_def.identifier, getattr(self, attr))
                  for attr, field_def in self._options().items()
                  if getattr(self, attr) is not None]
        return "{}({})".format(self.__class__.__name__, ', '.join(fields))


class ConjureFieldDefinition:
    identifier = None  # type: str
    field_type = None  # type: ConjureTypeType
    optional = None  # type: bool

    def __init__(self, identifier, field_type, optional=False):
        # type: (str, ConjureTypeType, bool) -> None
        self.identifier = identifier
        self.field_type = field_type
        self.optional = optional


class ConjureEncoder(json.JSONEncoder):
    '''Transforms a conjure type into json'''

    @classmethod
    def encode_conjure_bean_type(cls, obj):
        # type: (ConjureBeanType) -> Any
        '''Encodes a conjure bean into json'''
        encoded = {}  # type: Dict[str, Any]
        for attribute_name, field_definition in obj._fields().items():
            encoded[field_definition.identifier] = cls.do_encode(
                getattr(obj, attribute_name))
        return encoded

    @classmethod
    def encode_conjure_union_type(cls, obj):
        # type: (ConjureUnionType) -> Any
        '''Encodes a conjure union into json'''
        encoded = {}  # type: Dict[str, Any]
        encoded['type'] = obj.type

        defined_field_definition = obj._options()[obj.type]
        encoded[defined_field_definition.identifier] = cls.do_encode(
            getattr(obj, obj.type))

        return encoded

    @classmethod
    def do_encode(cls, obj):
        # type: (Any) -> Any
        '''Encodes the passed object into json'''
        if isinstance(obj, ConjureBeanType):
            return cls.encode_conjure_bean_type(obj)
        elif isinstance(obj, ConjureUnionType):
            return cls.encode_conjure_union_type(obj)
        elif isinstance(obj, ConjureEnumType):
            return obj.value
        elif isinstance(obj, list):
            return list(map(cls.do_encode, obj))
        elif isinstance(obj, dict):
            return dict(map(lambda x:
                            (cls.do_encode(x[0]), cls.do_encode(x[1])),
                            obj.items()))
        else:
            return obj

    def default(self, obj):
        # type: (Any) -> Any
        return self.do_encode(obj)


class ConjureDecoder(object):
    '''Decodes json into a conjure object'''

    @classmethod
    def decode_conjure_bean_type(cls, obj, conjure_type):
        '''Decodes json into a conjure bean type (a plain bean, not enum or union).

        Args:
            obj: the json object to decode
            conjure_type: a class object which is the bean type
                we're decoding into
        Returns:
            A instance of a bean of type conjure_type.
        '''

        deserialized = {}  # type: Dict[str, Any]
        for arg_name, conjure_field_definition in \
                conjure_type._fields().items():
            field_identifier = conjure_field_definition.identifier

            if field_identifier not in obj:
                if not conjure_field_definition.optional:
                    raise Exception('field {0} not found in object {1}'.format(
                        field_identifier, obj))
                else:
                    deserialized[arg_name] = None
            else:
                value = obj[field_identifier]
                field_type = conjure_field_definition.field_type
                deserialized[arg_name] = cls.do_decode(value, field_type)
        return conjure_type(**deserialized)

    @classmethod
    def decode_conjure_union_type(cls, obj, conjure_type):
        '''Decodes json into a conjure union type.

        Args:
            obj: the json object to decode
            conjure_type: a class object which is the union type
                we're decoding into
        Returns:
            An instance of type conjure_type.
        '''

        type_of_union = obj['type']  # type: str
        conjure_field_definition = conjure_type._options()[type_of_union]

        deserialized = {}  # type: Dict[str, Any]

        if type_of_union not in obj:
            if not conjure_field_definition.optional:
                raise Exception('field {0} not found in object {1}'.format(
                    type_of_union, obj))
            else:
                deserialized[type_of_union] = None
        else:
            value = obj[type_of_union]
            field_type = conjure_field_definition.field_type
            deserialized[type_of_union] = cls.do_decode(value, field_type)
        return conjure_type(**deserialized)

    @classmethod
    def decode_conjure_enum_type(cls, obj, conjure_type):
        '''Decodes json into a conjure enum type.

        Args:
            obj: the json object to decode
            conjure_type: a class object which is the enum type
                we're decoding into.
        Returns:
            An instance of enum of type conjure_type.
        '''
        if obj in conjure_type.__members__:
            return conjure_type[obj]
        else:
            return conjure_type['UNKNOWN']

    @classmethod
    def decode_dict(cls,
                    obj,  # type: Dict[Any, Any]
                    key_type,  # ConjureTypeType
                    item_type  # ConjureTypeType
                    ):  # type: (...) -> Dict[Any, Any]
        '''Decodes json into a dictionary, handling conversion of the keys/values
        (the keys/values may themselves require conversion).

        Args:
            obj: the json object to decode
            key_type: a class object which is the conjure type
                of the keys in this dict
            item_type: a class object which is the conjure type
                of the values in this dict
        Returns:
            A python dictionary, where the keys are instances of type key_type
            and the values are of type value_type.
        '''
        if not isinstance(obj, dict):
            raise Exception("expected a python dict")

        return dict(map(lambda x:
                        (cls.do_decode(x[0], key_type),
                         cls.do_decode(x[1], item_type)),
                        obj.items()))

    @classmethod
    def decode_list(cls, obj, element_type):
        # type: (List[Any], ConjureTypeType) -> List[Any]
        '''Decodes json into a list, handling conversion of the elements.

        Args:
            obj: the json object to decode
            element_type: a class object which is the conjure type of
                the elements in this list.
        Returns:
            A python list where the elements are instances of type
                element_type.
        '''
        if not isinstance(obj, list):
            raise Exception("expected a python list")

        return list(map(lambda x: cls.do_decode(x, element_type), obj))

    @classmethod
    def decode_optional(cls, obj, object_type):
        # type: (Optional[Any], ConjureTypeType) -> Optional[Any]
        '''Decodes json into an element, returning None if the provided object is None.

        Args:
            obj: the json object to decode
            object_type: a class object which is the conjure type of
                the object if present.
        Returns:
            The decoded obj or None if no obj is provided.
        '''
        if obj is None:
            return None

        return cls.do_decode(obj, object_type)

    @classmethod
    def do_decode(cls, obj, obj_type):
        # type: (Any, ConjureTypeType) -> Any
        '''Decodes json into the specified type

        Args:
            obj: the json object to decode
            element_type: a class object which is the type we're decoding into.
        '''
        if inspect.isclass(obj_type) and \
                issubclass(obj_type, ConjureBeanType):  # type: ignore
            return cls.decode_conjure_bean_type(obj, obj_type)  # type: ignore
        elif inspect.isclass(obj_type) and \
                issubclass(obj_type, ConjureUnionType):  # type: ignore
            return cls.decode_conjure_union_type(obj, obj_type)
        elif inspect.isclass(obj_type) and \
                issubclass(obj_type, ConjureEnumType):  # type: ignore
            return cls.decode_conjure_enum_type(obj, obj_type)
        elif isinstance(obj_type, DictType):
            return cls.decode_dict(obj, obj_type.key_type, obj_type.value_type)
        elif isinstance(obj_type, ListType):
            return cls.decode_list(obj, obj_type.item_type)
        elif isinstance(obj_type, OptionalType):
            return cls.decode_optional(obj, obj_type.item_type)
        else:
            return obj

    def decode(self, obj, obj_type):
        # type: (Any, ConjureTypeType) -> Any
        return self.do_decode(obj, obj_type)

    def read_from_string(self, string_value, obj_type):
        # type: (str, ConjureTypeType) -> Any
        deserialized = json.loads(string_value)
        return self.decode(deserialized, obj_type)
