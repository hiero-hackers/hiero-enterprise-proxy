"""Tests for the async client."""

import httpx
import pytest
import respx

from hiero_proxy import AsyncHieroProxyClient, ClientError, NetworkError


BASE_URL = "http://localhost:8080"


@respx.mock
@pytest.mark.asyncio
async def test_async_create_account():
    route = respx.post(f"{BASE_URL}/api/v1/accounts").mock(
        return_value=httpx.Response(201, json={"accountId": "0.0.123", "publicKey": "abc", "privateKey": "def"})
    )
    async with AsyncHieroProxyClient(base_url=BASE_URL) as client:
        result = await client.accounts.create()
        assert result["accountId"] == "0.0.123"
        assert route.called


@respx.mock
@pytest.mark.asyncio
async def test_async_get_blocks():
    route = respx.get(f"{BASE_URL}/api/v1/blocks").mock(
        return_value=httpx.Response(200, json=[{"number": 1}])
    )
    async with AsyncHieroProxyClient(base_url=BASE_URL) as client:
        result = await client.blocks.list()
        assert result == [{"number": 1}]


@respx.mock
@pytest.mark.asyncio
async def test_async_client_error():
    respx.get(f"{BASE_URL}/api/v1/accounts/0.0.999").mock(
        return_value=httpx.Response(404, json={"message": "Not found"})
    )
    async with AsyncHieroProxyClient(base_url=BASE_URL) as client:
        with pytest.raises(ClientError) as exc_info:
            await client.accounts.get_info("0.0.999")
        assert exc_info.value.status == 404


@respx.mock
@pytest.mark.asyncio
async def test_async_timeout():
    respx.get(f"{BASE_URL}/api/v1/blocks").mock(side_effect=httpx.ReadTimeout("timed out"))
    async with AsyncHieroProxyClient(base_url=BASE_URL) as client:
        with pytest.raises(NetworkError, match="timed out"):
            await client.blocks.list()
