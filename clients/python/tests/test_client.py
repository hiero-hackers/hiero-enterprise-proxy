"""Tests for the base client error handling."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient, ClientError, ServerError, NetworkError


BASE_URL = "http://localhost:8080"


@respx.mock
def test_client_error_on_404():
    respx.get(f"{BASE_URL}/api/v1/accounts/0.0.999").mock(
        return_value=httpx.Response(404, json={"message": "Account not found"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ClientError) as exc_info:
        client.accounts.get_info("0.0.999")
    assert exc_info.value.status == 404
    assert "Account not found" in str(exc_info.value)


@respx.mock
def test_server_error_on_500():
    respx.get(f"{BASE_URL}/api/v1/network/fees").mock(
        return_value=httpx.Response(500, json={"message": "Internal error"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ServerError) as exc_info:
        client.network.get_fees()
    assert exc_info.value.status == 500


@respx.mock
def test_timeout_raises_network_error():
    respx.get(f"{BASE_URL}/api/v1/blocks").mock(side_effect=httpx.ReadTimeout("timed out"))
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(NetworkError, match="timed out"):
        client.blocks.list()


@respx.mock
def test_connection_error_raises_network_error():
    respx.get(f"{BASE_URL}/api/v1/blocks").mock(side_effect=httpx.ConnectError("refused"))
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(NetworkError, match="Connection failed"):
        client.blocks.list()


def test_context_manager():
    with HieroProxyClient(base_url=BASE_URL) as client:
        assert client._base_url == BASE_URL
