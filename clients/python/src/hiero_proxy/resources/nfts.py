"""NFTs resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

from hiero_proxy.resources.validation import require_id

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class NftsResource:
    """NFT operations — create types, mint, burn, transfer, query instances."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def create_type(self, name: str, symbol: str) -> Any:
        return self._client.post("/api/v1/nfts", json={"name": name, "symbol": symbol})

    def list_by_type(self, token_id: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.get(f"/api/v1/nfts/{tid}")

    def get_instance(self, token_id: str, serial_number: int) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.get(f"/api/v1/nfts/{tid}/{serial_number}")

    def list_by_owner(self, account_id: str) -> Any:
        aid = require_id(account_id, "account_id")
        return self._client.get(f"/api/v1/nfts/owner/{aid}")

    def list_by_owner_and_type(self, account_id: str, token_id: str) -> Any:
        aid = require_id(account_id, "account_id")
        tid = require_id(token_id, "token_id")
        return self._client.get(f"/api/v1/nfts/owner/{aid}/{tid}")

    def associate(self, token_id: str, account_id: str, private_key: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/nfts/{tid}/associate", json={
            "accountId": account_id, "privateKey": private_key,
        })

    def dissociate(self, token_id: str, account_id: str, private_key: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/nfts/{tid}/dissociate", json={
            "accountId": account_id, "privateKey": private_key,
        })

    def batch_associate(self, token_ids: list[str], account_id: str, private_key: str) -> Any:
        return self._client.post("/api/v1/nfts/associate", json={
            "tokenIds": token_ids, "accountId": account_id, "privateKey": private_key,
        })

    def batch_dissociate(self, token_ids: list[str], account_id: str, private_key: str) -> Any:
        return self._client.post("/api/v1/nfts/dissociate", json={
            "tokenIds": token_ids, "accountId": account_id, "privateKey": private_key,
        })

    def mint(self, token_id: str, metadata: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/nfts/{tid}/mint", json={"metadata": metadata})

    def mint_batch(self, token_id: str, metadata_list: list[str]) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/nfts/{tid}/mint/batch", json={"metadataList": metadata_list})

    def burn(self, token_id: str, serial_number: int) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/nfts/{tid}/burn", json={"serialNumber": serial_number})

    def transfer(self, token_id: str, serial_number: int, from_account_id: str, to_account_id: str, private_key: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/nfts/{tid}/transfer", json={
            "serialNumber": serial_number, "fromAccountId": from_account_id,
            "toAccountId": to_account_id, "privateKey": private_key,
        })

    def transfer_batch(self, token_id: str, serial_numbers: list[int], from_account_id: str, to_account_id: str, private_key: str) -> Any:
        tid = require_id(token_id, "token_id")
        return self._client.post(f"/api/v1/nfts/{tid}/transfer/batch", json={
            "serialNumbers": serial_numbers, "fromAccountId": from_account_id,
            "toAccountId": to_account_id, "privateKey": private_key,
        })
