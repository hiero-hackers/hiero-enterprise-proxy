"""Tests for the contracts resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_deploy():
    route = respx.post(f"{BASE_URL}/api/v1/contracts").mock(
        return_value=httpx.Response(201, json={"contractId": "0.0.500", "deleted": False, "evmAddress": "0xabc"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.contracts.deploy("0x608060")
    assert result["contractId"] == "0.0.500"
    assert route.called


@respx.mock
def test_call():
    route = respx.post(f"{BASE_URL}/api/v1/contracts/0.0.500/call").mock(
        return_value=httpx.Response(200, json={"gasUsed": 21000, "costInTinybars": 500000})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.contracts.call("0.0.500", "greet")
    assert result["gasUsed"] == 21000


@respx.mock
def test_list():
    route = respx.get(f"{BASE_URL}/api/v1/contracts").mock(
        return_value=httpx.Response(200, json=[{"contractId": "0.0.500"}])
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.contracts.list()
    assert len(result) == 1


@respx.mock
def test_get_info():
    route = respx.get(f"{BASE_URL}/api/v1/contracts/0.0.500").mock(
        return_value=httpx.Response(200, json={"contractId": "0.0.500", "deleted": False, "memo": "test"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.contracts.get_info("0.0.500")
    assert result["memo"] == "test"


def test_call_empty_id():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="contract_id is required"):
        client.contracts.call("", "greet")
