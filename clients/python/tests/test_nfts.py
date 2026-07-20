"""Tests for the NFTs resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_create_type():
    route = respx.post(f"{BASE_URL}/api/v1/nfts").mock(
        return_value=httpx.Response(201, json={"tokenId": "0.0.900"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.nfts.create_type("MyNFT", "MNFT")
    assert result["tokenId"] == "0.0.900"
    assert route.called


@respx.mock
def test_list_by_type():
    route = respx.get(f"{BASE_URL}/api/v1/nfts/0.0.900").mock(
        return_value=httpx.Response(200, json=[{"serialNumber": 1}, {"serialNumber": 2}])
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.nfts.list_by_type("0.0.900")
    assert len(result) == 2


@respx.mock
def test_mint():
    route = respx.post(f"{BASE_URL}/api/v1/nfts/0.0.900/mint").mock(
        return_value=httpx.Response(200, json={"serialNumber": 3})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.nfts.mint("0.0.900", "bWV0YWRhdGE=")
    assert result["serialNumber"] == 3


@respx.mock
def test_transfer():
    route = respx.post(f"{BASE_URL}/api/v1/nfts/0.0.900/transfer").mock(
        return_value=httpx.Response(200, json={"message": "Transfer successful"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.nfts.transfer("0.0.900", 1, "0.0.100", "0.0.200", "privkey")
    assert result["message"] == "Transfer successful"


def test_list_by_type_empty_id():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="token_id is required"):
        client.nfts.list_by_type("")
