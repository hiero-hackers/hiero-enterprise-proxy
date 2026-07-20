"""Shared validation helpers."""

from __future__ import annotations

from urllib.parse import quote


def require_id(value: str, param_name: str = "id") -> str:
    """Validate that a string param is non-empty and return it URL-encoded."""
    if not value or not isinstance(value, str) or not value.strip():
        raise ValueError(f"{param_name} is required and must be a non-empty string")
    return quote(value.strip(), safe="")
