"""Tests for the topics resource."""

import httpx
import respx
import pytest

from hiero_proxy import HieroProxyClient


BASE_URL = "http://localhost:8080"


@respx.mock
def test_create():
    route = respx.post(f"{BASE_URL}/api/v1/topics").mock(
        return_value=httpx.Response(201, json={"topicId": "0.0.1000"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.topics.create()
    assert result["topicId"] == "0.0.1000"
    assert route.called


@respx.mock
def test_get_info():
    route = respx.get(f"{BASE_URL}/api/v1/topics/0.0.1000").mock(
        return_value=httpx.Response(200, json={"topicId": "0.0.1000", "memo": "test"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.topics.get_info("0.0.1000")
    assert result["memo"] == "test"


@respx.mock
def test_submit_message():
    route = respx.post(f"{BASE_URL}/api/v1/topics/0.0.1000/message").mock(
        return_value=httpx.Response(200, json={"sequenceNumber": 1})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.topics.submit_message("0.0.1000", "Hello world")
    assert result["sequenceNumber"] == 1


@respx.mock
def test_get_messages():
    route = respx.get(f"{BASE_URL}/api/v1/topics/0.0.1000/messages").mock(
        return_value=httpx.Response(200, json=[{"sequenceNumber": 1, "message": "hi"}])
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.topics.get_messages("0.0.1000")
    assert len(result) == 1


@respx.mock
def test_delete():
    route = respx.delete(f"{BASE_URL}/api/v1/topics/0.0.1000").mock(
        return_value=httpx.Response(200, json={"message": "Topic deleted"})
    )
    client = HieroProxyClient(base_url=BASE_URL)
    result = client.topics.delete("0.0.1000")
    assert result["message"] == "Topic deleted"


def test_get_info_empty_id():
    client = HieroProxyClient(base_url=BASE_URL)
    with pytest.raises(ValueError, match="topic_id is required"):
        client.topics.get_info("")
