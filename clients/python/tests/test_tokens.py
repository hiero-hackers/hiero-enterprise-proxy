"""Tests for the tokens resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_create_token():
    route = respx.post(f"{BASE_URL}/api/v1/tokens").mock(
        return_value=httpx.Response(201, json={"tokenId": "0.0.700"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.tokens.create("MyToken", "MTK", 1000, decimals=2)
    assert result["tokenId"] == "0.0.700"
    assert route.called


@respx.mock
def test_get_info():
    route = respx.get(f"{BASE_URL}/api/v1/tokens/0.0.700").mock(
        return_value=httpx.Response(200, json={"tokenId": "0.0.700", "name": "MyToken", "symbol": "MTK"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.tokens.get_info("0.0.700")
    assert result["name"] == "MyToken"


@respx.mock
def test_mint():
    route = respx.post(f"{BASE_URL}/api/v1/tokens/0.0.700/mint").mock(
        return_value=httpx.Response(200, json={"newTotalSupply": 1500})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.tokens.mint("0.0.700", 500)
    assert result["newTotalSupply"] == 1500


@respx.mock
def test_transfer_from_operator():
    route = respx.post(f"{BASE_URL}/api/v1/tokens/0.0.700/transfer/operator").mock(
        return_value=httpx.Response(200, json={"message": "Transfer successful"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.tokens.transfer_from_operator("0.0.700", "0.0.456", 100)
    assert result["message"] == "Transfer successful"


def test_get_info_empty_id():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="token_id is required"):
        client.tokens.get_info("")
