#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# generate.sh — Regenerate SDK clients from the OpenAPI spec.
#
# Usage:
#   ./openapi/generate.sh [js|python|all]
#
# Prerequisites:
#   - Docker (used to run openapi-generator-cli)
#   - Or: npm install @openapitools/openapi-generator-cli -g
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SPEC_FILE="$SCRIPT_DIR/spec.yaml"
GENERATOR_VERSION="v7.6.0"

TARGET="${1:-all}"

# ─── Validate spec exists ────────────────────────────────────────────────────
if [ ! -f "$SPEC_FILE" ]; then
  echo "ERROR: OpenAPI spec not found at $SPEC_FILE"
  echo "Run the proxy server and execute: ./openapi/export-spec.sh"
  exit 1
fi

# ─── Generator command (prefer Docker for reproducibility) ───────────────────
generate() {
  local language="$1"
  local output_dir="$2"
  local generator="$3"
  local extra_args="${4:-}"

  echo "▶ Generating $language client → $output_dir"

  docker run --rm \
    -v "$ROOT_DIR:/workspace" \
    openapitools/openapi-generator-cli:$GENERATOR_VERSION \
    generate \
    -i /workspace/openapi/spec.yaml \
    -g "$generator" \
    -o "/workspace/$output_dir" \
    $extra_args

  echo "✓ $language client generated."
}

# ─── JavaScript/TypeScript ───────────────────────────────────────────────────
generate_js() {
  generate "JavaScript/TypeScript" "clients/javascript/src/generated" "typescript-fetch" \
    "--additional-properties=supportsES6=true,npmName=@hiero-hackers/proxy-sdk,npmVersion=1.0.0"
}

# ─── Python ──────────────────────────────────────────────────────────────────
generate_python() {
  generate "Python" "clients/python/src/hiero_proxy/generated" "python" \
    "--additional-properties=packageName=hiero_proxy.generated,projectName=hiero-proxy-sdk,packageVersion=1.0.0"
}

# ─── Main ────────────────────────────────────────────────────────────────────
case "$TARGET" in
  js|javascript)
    generate_js
    ;;
  python|py)
    generate_python
    ;;
  all)
    generate_js
    generate_python
    ;;
  *)
    echo "Usage: $0 [js|python|all]"
    exit 1
    ;;
esac

echo ""
echo "Done. Review generated code in clients/ before committing."
