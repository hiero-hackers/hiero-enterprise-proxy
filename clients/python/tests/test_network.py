"""Tests for the network resource."""

import httpx
import respx

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_get_exchange_rates():
    route = respx.get(f"{BASE_URL}/api/v1/network/exchangerates").mock(
        return_value=httpx.Response(200, json={"currentRate": {"centEquiv": 12, "hbarEquiv": 1}})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.network.get_exchange_rates()
    assert result["currentRate"]["hbarEquiv"] == 1
    assert route.called


@respx.mock
def test_get_fees():
    route = respx.get(f"{BASE_URL}/api/v1/network/fees").mock(
        return_value=httpx.Response(200, json={"fees": []})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.network.get_fees()
    assert result["fees"] == []


@respx.mock
def test_get_staking_info():
    route = respx.get(f"{BASE_URL}/api/v1/network/staking").mock(
        return_value=httpx.Response(200, json={"stakingPeriodDays": 1})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.network.get_staking_info()
    assert result["stakingPeriodDays"] == 1


@respx.mock
def test_get_supply():
    route = respx.get(f"{BASE_URL}/api/v1/network/supply").mock(
        return_value=httpx.Response(200, json={"totalSupplyTinybars": 5000000000000000})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.network.get_supply()
    assert result["totalSupplyTinybars"] == 5000000000000000
