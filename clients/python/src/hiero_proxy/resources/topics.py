"""Topics resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class TopicsResource:
    """Topic operations — create, submit messages, query, update, delete."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def create(self, memo: str = "") -> Any:
        return self._client.post("/api/v1/topics", json={"memo": memo} if memo else None)

    def create_private(self, memo: str = "") -> Any:
        return self._client.post("/api/v1/topics/private", json={"memo": memo} if memo else None)

    def create_with_admin_key(self, memo: str = "") -> Any:
        return self._client.post("/api/v1/topics/admin", json={"memo": memo} if memo else None)

    def create_private_with_admin_key(self, memo: str = "") -> Any:
        return self._client.post("/api/v1/topics/private/admin", json={"memo": memo} if memo else None)

    def get_info(self, topic_id: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.get(f"/api/v1/topics/{tid}")

    def update_memo(self, topic_id: str, memo: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.put(f"/api/v1/topics/{tid}/memo", json={"memo": memo})

    def rotate_admin_key(self, topic_id: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.put(f"/api/v1/topics/{tid}/keys/admin")

    def rotate_submit_key(self, topic_id: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.put(f"/api/v1/topics/{tid}/keys/submit")

    def update(self, topic_id: str, memo: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.put(f"/api/v1/topics/{tid}", json={"memo": memo})

    def delete(self, topic_id: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.delete(f"/api/v1/topics/{tid}")

    def submit_message(self, topic_id: str, message: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.post(f"/api/v1/topics/{tid}/message", json={"message": message})

    def submit_binary_message(self, topic_id: str, message: str, private_key: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.post(f"/api/v1/topics/{tid}/message/binary", json={
            "message": message, "privateKey": private_key,
        })

    def get_messages(self, topic_id: str) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.get(f"/api/v1/topics/{tid}/messages")

    def get_message(self, topic_id: str, sequence_number: int) -> Any:
        tid = require_id(topic_id, "topic_id")
        return self._client.get(f"/api/v1/topics/{tid}/messages/{sequence_number}")
