"""Blocks resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class BlocksResource:
    """Block queries — list, get by number or hash."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def list(self) -> Any:
        return self._client.get("/api/v1/blocks")

    def get_by_number(self, block_number: int) -> Any:
        if block_number < 0:
            raise ValueError("block_number must not be negative")
        return self._client.get(f"/api/v1/blocks/number/{block_number}")

    def get_by_hash(self, block_hash: str) -> Any:
        h = require_id(block_hash, "block_hash")
        return self._client.get(f"/api/v1/blocks/hash/{h}")
