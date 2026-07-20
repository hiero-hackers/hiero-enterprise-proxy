"""Transactions resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class TransactionsResource:
    """Transaction queries — get by ID or account."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def get_by_id(self, transaction_id: str) -> Any:
        tid = require_id(transaction_id, "transaction_id")
        return self._client.get(f"/api/v1/transactions/{tid}")

    def get_by_account(
        self,
        account_id: str,
        *,
        type: str | None = None,
        result: str | None = None,
        modification: str | None = None,
    ) -> Any:
        aid = require_id(account_id, "account_id")
        params: list[str] = []
        if type:
            params.append(f"type={type}")
        if result:
            params.append(f"result={result}")
        if modification:
            params.append(f"modification={modification}")
        query = f"?{'&'.join(params)}" if params else ""
        return self._client.get(f"/api/v1/transactions/account/{aid}{query}")
