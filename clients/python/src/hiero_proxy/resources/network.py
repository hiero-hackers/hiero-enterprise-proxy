"""Network resource."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any

if TYPE_CHECKING:
    from hiero_proxy.client import AsyncHieroProxyClient, HieroProxyClient


class NetworkResource:
    """Network queries — exchange rates, fees, staking, supply."""

    def __init__(self, client: HieroProxyClient | AsyncHieroProxyClient) -> None:
        self._client = client

    def get_exchange_rates(self) -> Any:
        return self._client.get("/api/v1/network/exchangerates")

    def get_fees(self) -> Any:
        return self._client.get("/api/v1/network/fees")

    def get_staking_info(self) -> Any:
        return self._client.get("/api/v1/network/staking")

    def get_supply(self) -> Any:
        return self._client.get("/api/v1/network/supply")
