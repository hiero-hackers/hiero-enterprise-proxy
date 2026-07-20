"""Tests for the accounts resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_create_account():
    route = respx.post(f"{BASE_URL}/api/v1/accounts").mock(
        return_value=httpx.Response(201, json={"accountId": "0.0.123", "publicKey": "abc", "privateKey": "def"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.accounts.create()
    assert result["accountId"] == "0.0.123"
    assert route.called


@respx.mock
def test_create_account_with_balance():
    route = respx.post(f"{BASE_URL}/api/v1/accounts").mock(
        return_value=httpx.Response(201, json={"accountId": "0.0.456", "publicKey": "x", "privateKey": "y"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.accounts.create(initial_balance_in_hbar=10)
    assert result["accountId"] == "0.0.456"
    assert route.calls[0].request.content == b'{"initialBalanceInHbar":10}'


@respx.mock
def test_get_balance():
    route = respx.get(f"{BASE_URL}/api/v1/accounts/0.0.123/balance").mock(
        return_value=httpx.Response(200, json={"accountId": "0.0.123", "balanceHbar": "10 \u210f"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.accounts.get_balance("0.0.123")
    assert result["balanceHbar"] == "10 \u210f"
    assert route.called


@respx.mock
def test_get_info():
    route = respx.get(f"{BASE_URL}/api/v1/accounts/0.0.123").mock(
        return_value=httpx.Response(200, json={"accountId": "0.0.123", "evmAddress": "0xabc", "balanceTinybars": 100, "ethereumNonce": 0, "pendingRewardTinybars": 0})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.accounts.get_info("0.0.123")
    assert result["evmAddress"] == "0xabc"


@respx.mock
def test_transfer_from_operator():
    route = respx.post(f"{BASE_URL}/api/v1/accounts/0.0.456/transfer/operator").mock(
        return_value=httpx.Response(200, json={"message": "Transfer successful"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.accounts.transfer_from_operator("0.0.456", 100)
    assert result["message"] == "Transfer successful"


def test_get_balance_empty_id():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="account_id is required"):
        client.accounts.get_balance("")
