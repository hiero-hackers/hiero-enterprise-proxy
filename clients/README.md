# Hiero Enterprise Proxy — SDK Clients

This directory contains auto-generated + hand-written SDK clients for the Hiero Enterprise Proxy API.

## Available Clients

| Language | Directory | Package | Status |
|---|---|---|---|
| JavaScript/TypeScript | `javascript/` | `@hiero-hackers/proxy-sdk` | Planned |
| Python | `python/` | `hiero-proxy-sdk` | Planned |

## How it works

1. The proxy server exposes an OpenAPI 3.0 spec at `/v3/api-docs.yaml`.
2. The spec is committed to `openapi/spec.yaml` as the single source of truth.
3. Clients are generated from that spec using `openapi/generate.sh`.
4. A thin handwritten wrapper is added on top for better developer experience.

## Contributing a new client

1. Add a new generator target in `openapi/generate.sh`.
2. Create a `clients/<language>/` directory.
3. Add generated code to `src/generated/` (auto-generated, do not edit).
4. Add wrapper code to `src/` (handwritten, reviewable).
5. Add tests and a README.
