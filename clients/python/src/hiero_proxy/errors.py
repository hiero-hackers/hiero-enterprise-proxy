"""Error classes for the Hiero Enterprise Proxy SDK."""

from __future__ import annotations


class HieroProxyError(Exception):
    """Base error for all proxy SDK errors."""

    def __init__(self, message: str, status: int | None = None) -> None:
        super().__init__(message)
        self.status = status


class ClientError(HieroProxyError):
    """4xx errors from the proxy (bad request, not found, etc.)."""


class ServerError(HieroProxyError):
    """5xx errors from the proxy (internal server error, etc.)."""


class NetworkError(HieroProxyError):
    """Connection failures, timeouts, DNS errors."""

    def __init__(self, message: str) -> None:
        super().__init__(message, status=None)
