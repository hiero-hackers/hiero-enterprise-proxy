"""Core HTTP client for the Hiero Enterprise Proxy."""

from __future__ import annotations

from typing import Any

import httpx

from hiero_proxy.errors import ClientError, NetworkError, ServerError
from hiero_proxy.resources.accounts import AccountsResource
from hiero_proxy.resources.blocks import BlocksResource
from hiero_proxy.resources.contracts import ContractsResource
from hiero_proxy.resources.files import FilesResource
from hiero_proxy.resources.network import NetworkResource
from hiero_proxy.resources.nfts import NftsResource
from hiero_proxy.resources.tokens import TokensResource
from hiero_proxy.resources.topics import TopicsResource
from hiero_proxy.resources.transactions import TransactionsResource

_DEFAULT_TIMEOUT = 30.0


def _raise_for_status(response: httpx.Response) -> None:
    """Raise appropriate SDK error for non-2xx responses."""
    if response.is_success:
        return
    try:
        body = response.json()
        message = body.get("message", response.text)
    except Exception:
        message = response.text
    if 400 <= response.status_code < 500:
        raise ClientError(message, status=response.status_code)
    if response.status_code >= 500:
        raise ServerError(message, status=response.status_code)


class HieroProxyClient:
    """Synchronous client for the Hiero Enterprise Proxy API."""

    def __init__(
        self,
        base_url: str,
        *,
        timeout: float = _DEFAULT_TIMEOUT,
        headers: dict[str, str] | None = None,
    ) -> None:
        self._base_url = base_url.rstrip("/")
        self._client = httpx.Client(
            base_url=self._base_url,
            timeout=timeout,
            headers={"Content-Type": "application/json", "Accept": "application/json", **(headers or {})},
        )
        self.accounts = AccountsResource(self)
        self.network = NetworkResource(self)
        self.tokens = TokensResource(self)
        self.nfts = NftsResource(self)
        self.topics = TopicsResource(self)
        self.contracts = ContractsResource(self)
        self.files = FilesResource(self)
        self.blocks = BlocksResource(self)
        self.transactions = TransactionsResource(self)

    def get(self, path: str) -> Any:
        """Execute a GET request."""
        return self._request("GET", path)

    def post(self, path: str, json: Any | None = None) -> Any:
        """Execute a POST request."""
        return self._request("POST", path, json=json)

    def put(self, path: str, json: Any | None = None) -> Any:
        """Execute a PUT request."""
        return self._request("PUT", path, json=json)

    def delete(self, path: str, json: Any | None = None) -> Any:
        """Execute a DELETE request."""
        return self._request("DELETE", path, json=json)

    def _request(self, method: str, path: str, *, json: Any | None = None) -> Any:
        try:
            response = self._client.request(method, path, json=json)
        except httpx.TimeoutException as exc:
            raise NetworkError(f"Request timed out: {method} {path}") from exc
        except httpx.ConnectError as exc:
            raise NetworkError(f"Connection failed: {exc}") from exc
        _raise_for_status(response)
        if not response.text:
            return None
        return response.json()

    def close(self) -> None:
        """Close the underlying HTTP client."""
        self._client.close()

    def __enter__(self) -> HieroProxyClient:
        return self

    def __exit__(self, *_: Any) -> None:
        self.close()


class AsyncHieroProxyClient:
    """Async client for the Hiero Enterprise Proxy API."""

    def __init__(
        self,
        base_url: str,
        *,
        timeout: float = _DEFAULT_TIMEOUT,
        headers: dict[str, str] | None = None,
    ) -> None:
        self._base_url = base_url.rstrip("/")
        self._client = httpx.AsyncClient(
            base_url=self._base_url,
            timeout=timeout,
            headers={"Content-Type": "application/json", "Accept": "application/json", **(headers or {})},
        )
        self.accounts = AccountsResource(self)
        self.network = NetworkResource(self)
        self.tokens = TokensResource(self)
        self.nfts = NftsResource(self)
        self.topics = TopicsResource(self)
        self.contracts = ContractsResource(self)
        self.files = FilesResource(self)
        self.blocks = BlocksResource(self)
        self.transactions = TransactionsResource(self)

    async def get(self, path: str) -> Any:
        """Execute a GET request."""
        return await self._request("GET", path)

    async def post(self, path: str, json: Any | None = None) -> Any:
        """Execute a POST request."""
        return await self._request("POST", path, json=json)

    async def put(self, path: str, json: Any | None = None) -> Any:
        """Execute a PUT request."""
        return await self._request("PUT", path, json=json)

    async def delete(self, path: str, json: Any | None = None) -> Any:
        """Execute a DELETE request."""
        return await self._request("DELETE", path, json=json)

    async def _request(self, method: str, path: str, *, json: Any | None = None) -> Any:
        try:
            response = await self._client.request(method, path, json=json)
        except httpx.TimeoutException as exc:
            raise NetworkError(f"Request timed out: {method} {path}") from exc
        except httpx.ConnectError as exc:
            raise NetworkError(f"Connection failed: {exc}") from exc
        _raise_for_status(response)
        if not response.text:
            return None
        return response.json()

    async def close(self) -> None:
        """Close the underlying HTTP client."""
        await self._client.aclose()

    async def __aenter__(self) -> AsyncHieroProxyClient:
        return self

    async def __aexit__(self, *_: Any) -> None:
        await self.close()
