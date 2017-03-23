import json

from conjure import ConjureDecoder, ConjureEncoder
from catalog.api.datasets import BackingFileSystem
from catalog.api import ExplicitCreateDatasetRequest

decoder = ConjureDecoder()

def test_serde_backing_file_system():
    serialized = '''
    {
        "fileSystemId": "my-fs",
        "baseUri": "http://example.com",
        "configuration": {"apiKey": "my-key"}
    }
    '''

    deserialized = decoder.read_from_string(serialized, BackingFileSystem)

    assert isinstance(deserialized, BackingFileSystem)
    assert deserialized.fileSystemId == 'my-fs'
    assert deserialized.baseUri == 'http://example.com'
    assert deserialized.configuration == {'apiKey': 'my-key'}

    round_tripped = decoder.read_from_string(json.dumps(deserialized, cls=ConjureEncoder), BackingFileSystem)
    assert round_tripped == deserialized


def test_serde_explicit_create_dataset_request():
    serialized = '''
    {
        "backingFileSystem": {
            "fileSystemId": "my-fs",
            "baseUri": "http://example.com",
            "configuration": {"apiKey": "my-key"}
        },
        "path": "my/path"
    }
    '''

    deserialized = decoder.read_from_string(serialized, ExplicitCreateDatasetRequest)

    assert isinstance(deserialized, ExplicitCreateDatasetRequest)
    assert isinstance(deserialized.backingFileSystem, BackingFileSystem)
    assert deserialized.backingFileSystem.fileSystemId == 'my-fs'
    assert deserialized.path == 'my/path'

    round_tripped = decoder.read_from_string(json.dumps(deserialized, cls=ConjureEncoder), ExplicitCreateDatasetRequest)
    assert round_tripped == deserialized

