# OpenAPI Specification & Client Generation

This folder contains the **source-of-truth OpenAPI spec** for the Hiero Enterprise Proxy and scripts to generate SDK clients from it.

## Files

| File | Purpose |
|---|---|
| `spec.yaml` | Committed OpenAPI 3.0 spec (exported from the running proxy) |
| `export-spec.sh` | Re-export the spec from a running proxy (Linux/macOS) |
| `export-spec.ps1` | Re-export the spec from a running proxy (Windows) |
| `generate.sh` | Generate JS and/or Python clients from the spec |

## Workflow

### 1. Update the spec after API changes

Start the proxy, then re-export:

**Linux/macOS**
```bash
./mvnw spring-boot:run -pl hiero-proxy-server &
sleep 5
./openapi/export-spec.sh
```

**Windows (PowerShell)**
```powershell
# Start proxy in another terminal, then:
.\openapi\export-spec.ps1
```

### 2. Regenerate SDK clients

```bash
./openapi/generate.sh all      # both JS + Python
./openapi/generate.sh js       # JavaScript/TypeScript only
./openapi/generate.sh python   # Python only
```

Requires Docker (runs `openapi-generator-cli` in a container for reproducibility).

### 3. Review and commit

Always review generated code before committing. The generation scripts output to `clients/*/src/generated/` directories.

## Versioning

The spec version in `spec.yaml` (`info.version`) must match the proxy release tag. When cutting a release:

1. Export fresh spec.
2. Regenerate clients.
3. Run tests.
4. Tag and publish.
