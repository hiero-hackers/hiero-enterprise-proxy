"""Contracts resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class ContractsResource:
    """Smart contract operations — deploy, call, query."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def deploy(self, bytecode: str) -> Any:
        return self._client.post("/api/v1/contracts", json={"bytecode": bytecode})

    def call(self, contract_id: str, function_name: str) -> Any:
        cid = require_id(contract_id, "contract_id")
        return self._client.post(f"/api/v1/contracts/{cid}/call", json={"functionName": function_name})

    def list(self) -> Any:
        return self._client.get("/api/v1/contracts")

    def get_info(self, contract_id: str) -> Any:
        cid = require_id(contract_id, "contract_id")
        return self._client.get(f"/api/v1/contracts/{cid}")
