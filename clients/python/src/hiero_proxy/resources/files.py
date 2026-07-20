"""Files resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class FilesResource:
    """File operations — create, read, update, delete."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def create(self, contents: str, expiration_time_epoch_second: int | None = None) -> Any:
        body: dict[str, Any] = {"contents": contents}
        if expiration_time_epoch_second is not None:
            body["expirationTimeEpochSecond"] = expiration_time_epoch_second
        return self._client.post("/api/v1/files", json=body)

    def get_contents(self, file_id: str) -> Any:
        fid = require_id(file_id, "file_id")
        return self._client.get(f"/api/v1/files/{fid}/contents")

    def get_info(self, file_id: str) -> Any:
        fid = require_id(file_id, "file_id")
        return self._client.get(f"/api/v1/files/{fid}")

    def update_contents(self, file_id: str, contents: str) -> Any:
        fid = require_id(file_id, "file_id")
        return self._client.put(f"/api/v1/files/{fid}/contents", json={"contents": contents})

    def update_expiration(self, file_id: str, expiration_time_epoch_second: int) -> Any:
        fid = require_id(file_id, "file_id")
        return self._client.put(f"/api/v1/files/{fid}/expiration", json={"expirationTimeEpochSecond": expiration_time_epoch_second})

    def delete(self, file_id: str) -> Any:
        fid = require_id(file_id, "file_id")
        return self._client.delete(f"/api/v1/files/{fid}")
