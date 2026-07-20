"""Tokens resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class TokensResource:
    """Fungible token operations — create, mint, burn, transfer, associate."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def create(self, name: str, symbol: str, initial_supply: int, decimals: int = 0) -> Any:
        return self._client.post("/api/v1/tokens", json={
            "name": name, "symbol": symbol, "initialSupply": initial_supply, "decimals": decimals,
        })

    def get_info(self, token_id: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.get(f"/api/v1/tokens/{tid}")

    def get_balance(self, token_id: str, account_id: str) -> Any:
        tid = require_id(token_id, "token_id")
        aid = require_id(account_id, "account_id")
        return self._client.get(f"/api/v1/tokens/{tid}/balance/{aid}")

    def get_all_balances(self, token_id: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.get(f"/api/v1/tokens/{tid}/balances")

    def get_by_account(self, account_id: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.get(f"/api/v1/tokens/account/{aid}")

    def associate(self, token_id: str, account_id: str, private_key: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/tokens/{tid}/associate", json={
            "accountId": account_id, "privateKey": private_key,
        })

    def dissociate(self, token_id: str, account_id: str, private_key: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/tokens/{tid}/dissociate", json={
            "accountId": account_id, "privateKey": private_key,
        })

    def batch_associate(self, token_ids: list[str], account_id: str, private_key: str) -> Any:
        return self._client.post("/api/v1/tokens/associate", json={
            "tokenIds": token_ids, "accountId": account_id, "privateKey": private_key,
        })

    def batch_dissociate(self, token_ids: list[str], account_id: str, private_key: str) -> Any:
        return self._client.post("/api/v1/tokens/dissociate", json={
            "tokenIds": token_ids, "accountId": account_id, "privateKey": private_key,
        })

    def mint(self, token_id: str, amount: int) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/tokens/{tid}/mint", json={"amount": amount})

    def burn(self, token_id: str, amount: int) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/tokens/{tid}/burn", json={"amount": amount})

    def transfer_from_operator(self, token_id: str, to_account_id: str, amount: int) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/tokens/{tid}/transfer/operator", json={
            "toAccountId": to_account_id, "amount": amount,
        })

    def transfer(self, token_id: str, from_account_id: str, to_account_id: str, amount: int, private_key: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/tokens/{tid}/transfer", json={
            "fromAccountId": from_account_id, "toAccountId": to_account_id, "amount": amount, "privateKey": private_key,
        })
