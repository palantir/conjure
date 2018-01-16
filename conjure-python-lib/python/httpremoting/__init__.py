from typing import TypeVar, Type, List, Any
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.poolmanager import PoolManager
from requests.packages.urllib3.util.ssl_ import create_urllib3_context
from requests.packages.urllib3.util import Retry
import requests
import random


class SslConfiguration:
    trust_store_path = None  # type: str

    def __init__(self, trust_store_path):
        # type: (str) -> None
        self.trust_store_path = trust_store_path


class ServiceConfiguration:
    api_token = None  # type: str
    security = SslConfiguration  # type: Any
    connect_timeout = None  # type: int
    read_timeout = None  # type: int
    write_timeout = None  # type: int
    uris = []  # type: List[str]
    max_num_retries = 3  # type: int
    backoff_slot_size = 500  # type: int


class Service:
    _requests_session = None  # type: requests.Session
    _uris = None  # type: List[str]

    def __init__(self, requests_session, uris):
        # type: (requests.Session, List[str]) -> None
        self._requests_session = requests_session
        self._uris = uris

    @property
    def _uri(self):
        # type: () -> str
        '''returns a random uri'''
        return random.choice(self._uris)


T = TypeVar('T')


# https://testssl.sh/openssl-rfc.mappping.html
CIPHERS = (
    'ECDHE-RSA-AES256-SHA384:'
    'ECDHE-RSA-AES128-SHA256:'
    'ECDH-RSA-AES256-SHA384:'
    'ECDH-RSA-AES128-SHA256:'
    'AES128-SHA256:'
    'AES256-SHA256:'
    'ECDHE-RSA-AES256-SHA:'
    'ECDHE-RSA-AES128-SHA:'
    'ECDH-RSA-AES256-SHA:'
    'ECDH-RSA-AES128-SHA256:'
    'AES256-SHA:'
    'AES128-SHA:'
    'TLS_FALLBACK_SCSV'
)


class TransportAdapter(HTTPAdapter):
    '''Transport adapter that allows customising ssl things'''
    def init_poolmanager(self, connections, maxsize,
                         block=False, **pool_kwargs):
        self._pool_connections = connections
        self._pool_maxsize = maxsize
        self._pool_block = block

        ssl_context = create_urllib3_context(ciphers=CIPHERS)

        self.poolmanager = PoolManager(
            num_pools=connections,
            maxsize=maxsize,
            block=block,
            strict=True,
            ssl_context=ssl_context,
            **pool_kwargs)


class RequestsClient:

    @classmethod
    def create(cls, service_class, user_agent, service_config):
        # type: (Type[T], str, ServiceConfiguration) -> T

        # setup retry to match java remoting
        # https://github.com/palantir/http-remoting/tree/3.12.0#quality-of-service-retry-failover-throttling
        retry = Retry(
            total=service_config.max_num_retries,
            status_forcelist=[308, 429, 503],
            backoff_factor=float(service_config.backoff_slot_size) / 1000
        )

        transport_adapter = TransportAdapter(max_retries=retry)

        # create a session, for shared connection polling, user agent, etc
        session = requests.Session()

        session.headers = {
            'User-Agent': user_agent
        }

        if service_config.security is not None:
            session.verify = service_config.security.trust_store_path

        for uri in service_config.uris:
            session.mount(uri, transport_adapter)

        return service_class(session, service_config.uris)  # type: ignore
