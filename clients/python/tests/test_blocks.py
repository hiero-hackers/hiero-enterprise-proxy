"""Tests for the blocks resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_list():
    route = respx.get(f"{BASE_URL}/api/v1/blocks").mock(
        return_value=httpx.Response(200, json=[{"number": 1, "hash": "0xabc"}])
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.blocks.list()
    assert len(result) == 1
    assert result[0]["number"] == 1


@respx.mock
def test_get_by_number():
    route = respx.get(f"{BASE_URL}/api/v1/blocks/number/42").mock(
        return_value=httpx.Response(200, json={"number": 42, "hash": "0xdef"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.blocks.get_by_number(42)
    assert result["number"] == 42


@respx.mock
def test_get_by_hash():
    route = respx.get(f"{BASE_URL}/api/v1/blocks/hash/0xdef456").mock(
        return_value=httpx.Response(200, json={"number": 10, "hash": "0xdef456"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.blocks.get_by_hash("0xdef456")
    assert result["hash"] == "0xdef456"


def test_get_by_number_negative():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="block_number must not be negative"):
        client.blocks.get_by_number(-1)


def test_get_by_hash_empty():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="block_hash is required"):
        client.blocks.get_by_hash("")
