"""Accounts resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class AccountsResource:
    """Account operations — create, query, update, delete, transfer."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def create(self, initial_balance_in_hbar: int | None = None) -> Any:
        body = {"initialBalanceInHbar": initial_balance_in_hbar} if initial_balance_in_hbar is not None else None
        return self._client.post("/api/v1/accounts", json=body)

    def get_balance(self, account_id: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.get(f"/api/v1/accounts/{aid}/balance")

    def get_operator_balance(self) -> Any:
        return self._client.get("/api/v1/accounts/operator/balance")

    def get_info(self, account_id: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.get(f"/api/v1/accounts/{aid}")

    def update_key(self, account_id: str, current_private_key: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.put(f"/api/v1/accounts/{aid}/key", json={"currentPrivateKey": current_private_key})

    def update_memo(self, account_id: str, private_key: str, memo: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.put(f"/api/v1/accounts/{aid}/memo", json={"privateKey": private_key, "memo": memo})

    def update(self, account_id: str, current_private_key: str, memo: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.put(f"/api/v1/accounts/{aid}", json={"currentPrivateKey": current_private_key, "memo": memo})

    def delete(self, account_id: str, private_key: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.delete(f"/api/v1/accounts/{aid}", json={"privateKey": private_key})

    def delete_to_recipient(self, account_id: str, recipient_account_id: str, private_key: str) -> Any:
        aid = require_id(account_id, "account_id")
        rid = require_id(recipient_account_id, "recipient_account_id")
        return self._client.delete(f"/api/v1/accounts/{aid}/to/{rid}", json={"privateKey": private_key})

    def transfer_from_operator(self, account_id: str, amount: int) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.post(f"/api/v1/accounts/{aid}/transfer/operator", json={"amount": amount})

    def transfer(self, from_account_id: str, to_account_id: str, amount: int, private_key: str) -> Any:
        fid = require_id(from_account_id, "from_account_id")
        return self._client.post(
            f"/api/v1/accounts/{fid}/transfer",
            json={"toAccountId": to_account_id, "amount": amount, "privateKey": private_key},
        )
