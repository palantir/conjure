#!/usr/bin/env python
from setuptools import find_packages, setup

setup(
    name='example-service',

    description='Example Service',

    # The project's main homepage.
    url='https://github.palantir.build/foundry/conjure',

    author='Palantir Technologies, Inc.',

    # You can just specify the packages manually here if your project is
    # simple. Or you can use find_packages().
    packages=find_packages(exclude=['test', 'integration']),

    # List run-time dependencies here.  These will be installed by pip when
    # your project is installed. For an analysis of "install_requires" vs pip's
    # requirements files see:
    # https://packaging.python.org/en/latest/requirements.html
    install_requires=[
        'py4j',
        'pytest',
        'pyyaml',
    ],

    # List additional groups of dependencies here (e.g. development
    # dependencies). You can install these using the following syntax,
    # for example:
    # $ pip install -e .[test]
    extras_require={
        'test': ['tox', 'mock', 'pytest', 'pytest-cov', 'coverage', 'pylint'],
    }
)
