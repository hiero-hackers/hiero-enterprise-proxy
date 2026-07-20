"""Tests for the transactions resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_get_by_id():
    route = respx.get(f"{BASE_URL}/api/v1/transactions/0.0.123%401700000000.000000000").mock(
        return_value=httpx.Response(200, json={"transactionId": "0.0.123@1700000000.000000000", "result": "SUCCESS"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.transactions.get_by_id("0.0.123@1700000000.000000000")
    assert result["result"] == "SUCCESS"
    assert route.called


@respx.mock
def test_get_by_account_no_filters():
    route = respx.get(f"{BASE_URL}/api/v1/transactions/account/0.0.456").mock(
        return_value=httpx.Response(200, json=[{"transactionId": "tx1"}])
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.transactions.get_by_account("0.0.456")
    assert len(result) == 1


@respx.mock
def test_get_by_account_with_filters():
    route = respx.get(f"{BASE_URL}/api/v1/transactions/account/0.0.789?type=CRYPTOTRANSFER&result=SUCCESS").mock(
        return_value=httpx.Response(200, json=[])
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.transactions.get_by_account("0.0.789", type="CRYPTOTRANSFER", result="SUCCESS")
    assert result == []
    assert route.called


def test_get_by_id_empty():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="transaction_id is required"):
        client.transactions.get_by_id("")


def test_get_by_account_empty():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="account_id is required"):
        client.transactions.get_by_account("  ")
