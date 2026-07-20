#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# export-spec.sh — Export the live OpenAPI spec from a running proxy instance.
#
# Usage:
#   ./openapi/export-spec.sh [base-url]
#
# The proxy must be running (default: http://localhost:8080).
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_URL="${1:-http://localhost:8080}"
OUTPUT="$SCRIPT_DIR/spec.yaml"

echo "▶ Fetching OpenAPI spec from $BASE_URL/v3/api-docs.yaml ..."

if command -v curl &> /dev/null; then
  curl -sf "$BASE_URL/v3/api-docs.yaml" -o "$OUTPUT"
elif command -v wget &> /dev/null; then
  wget -q "$BASE_URL/v3/api-docs.yaml" -O "$OUTPUT"
else
  echo "ERROR: Neither curl nor wget found. Install one and retry."
  exit 1
fi

echo "✓ Spec exported to: $OUTPUT"
echo "  Commit this file to keep the spec in sync with the proxy."
