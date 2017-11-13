import conjure

def test_version():
    assert conjure.__version__ is not None
    assert isinstance(conjure.__version__, str)
    assert len(conjure.__version__) > 1
