"""Tests for the files resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_create():
    route = respx.post(f"{BASE_URL}/api/v1/files").mock(
        return_value=httpx.Response(201, json={"fileId": "0.0.800"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.files.create("SGVsbG8=")
    assert result["fileId"] == "0.0.800"
    assert route.called


@respx.mock
def test_get_contents():
    route = respx.get(f"{BASE_URL}/api/v1/files/0.0.800/contents").mock(
        return_value=httpx.Response(200, json={"fileId": "0.0.800", "contents": "SGVsbG8="})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.files.get_contents("0.0.800")
    assert result["contents"] == "SGVsbG8="


@respx.mock
def test_get_info():
    route = respx.get(f"{BASE_URL}/api/v1/files/0.0.800").mock(
        return_value=httpx.Response(200, json={"fileId": "0.0.800", "size": 1024, "deleted": False})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.files.get_info("0.0.800")
    assert result["size"] == 1024


@respx.mock
def test_update_contents():
    route = respx.put(f"{BASE_URL}/api/v1/files/0.0.800/contents").mock(
        return_value=httpx.Response(200, json={"message": "File contents updated"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.files.update_contents("0.0.800", "V29ybGQ=")
    assert result["message"] == "File contents updated"


@respx.mock
def test_delete():
    route = respx.delete(f"{BASE_URL}/api/v1/files/0.0.800").mock(
        return_value=httpx.Response(200, json={"message": "File deleted"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.files.delete("0.0.800")
    assert result["message"] == "File deleted"


def test_get_contents_empty_id():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="file_id is required"):
        client.files.get_contents("")
