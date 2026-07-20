# Hiero Enterprise Proxy

A REST gateway to the [Hiero](https://hiero.org) distributed ledger. Run one container, get a fully documented HTTP API, and build on Hiero from **any language** — no Java SDK required.

```
Your app  →  HTTP/JSON  →  Hiero Enterprise Proxy  →  Hiero Network
```

## Quickstart

You need a [Hedera testnet account](https://portal.hedera.com) and Docker.

```bash
docker run \
  -e HEDERA_ACCOUNT_ID=0.0.xxxxx \
  -e HEDERA_PRIVATE_KEY=your_private_key \
  -e HEDERA_NETWORK=hedera-testnet \
  -p 8080:8080 \
  ghcr.io/hiero-hackers/hiero-enterprise-proxy:latest
```

Open `http://localhost:8080` — interactive API explorer at `/swagger-ui/index.html`.

<details>
<summary><strong>Windows PowerShell</strong></summary>

```powershell
docker run `
  -e HEDERA_ACCOUNT_ID=0.0.xxxxx `
  -e HEDERA_PRIVATE_KEY=your_private_key `
  -e HEDERA_NETWORK=hedera-testnet `
  -p 8080:8080 `
  ghcr.io/hiero-hackers/hiero-enterprise-proxy:latest
```
</details>

<details>
<summary><strong>Docker Compose (any OS)</strong></summary>

```bash
git clone https://github.com/hiero-hackers/hiero-enterprise-proxy.git
cd hiero-enterprise-proxy

# Create a .env file with your credentials
echo "HEDERA_ACCOUNT_ID=0.0.xxxxx" > .env
echo "HEDERA_PRIVATE_KEY=your_private_key" >> .env
echo "HEDERA_NETWORK=hedera-testnet" >> .env

docker compose up
```
</details>

## Client SDKs

Official client libraries that wrap this proxy. Both SDKs are in this repo and install directly from GitHub — no registry publish yet (npm/PyPI coming soon).

### Python

Install in one command (Python 3.10+):

```bash
pip install "hiero-enterprise-proxy @ git+https://github.com/hiero-hackers/hiero-enterprise-proxy.git#subdirectory=clients/python"
```

```python
from hiero_proxy import HieroProxyClient

client = HieroProxyClient(base_url="http://localhost:8080")

account = client.accounts.create(initial_balance_in_hbar=10)
print(account["accountId"])  # 0.0.12345

token = client.tokens.create("MyToken", "MTK", 1_000_000)
client.tokens.mint(token["tokenId"], 500)
```

Async is also supported:

```python
from hiero_proxy import AsyncHieroProxyClient

async with AsyncHieroProxyClient(base_url="http://localhost:8080") as client:
    info = await client.network.get_exchange_rates()
```

### JavaScript / TypeScript

npm doesn't support installing from a subdirectory of a git repo, so for now use clone + link (Node 18+):

```bash
git clone https://github.com/hiero-hackers/hiero-enterprise-proxy.git
cd hiero-enterprise-proxy/clients/javascript
npm install && npm run build
npm link
```

Then in your project:

```bash
npm link @hiero-hackers/proxy-sdk
```

```typescript
import { HieroProxyClient } from "@hiero-hackers/proxy-sdk";

const client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });

const account = await client.accounts.create(10);
console.log(account.accountId); // 0.0.12345

const token = await client.tokens.create("MyToken", "MTK", 1_000_000);
await client.tokens.mint(token.tokenId, 500);
```

> **Coming soon:** `npm install @hiero-hackers/proxy-sdk` once published to npm.

Both SDKs cover all proxy endpoints: accounts, tokens, NFTs, topics, contracts, files, blocks, and transactions.

## API Coverage

| Domain | What you can do |
|--------|----------------|
| **Accounts** | Create, fund, transfer HBAR, rotate keys, query balance |
| **Fungible Tokens** | Create, mint, burn, transfer, associate/dissociate |
| **NFTs** | Create type, mint, burn, transfer, query by owner |
| **Topics** | Create public/private, submit messages, query history |
| **Smart Contracts** | Deploy bytecode, call functions, query state |
| **Files** | Create, read contents, update, delete |
| **Blocks & Transactions** | Query by number, hash, or transaction ID |
| **Network** | Exchange rates, fees, staking info, supply |

## Configuration

| Variable | Required | Default |
|----------|----------|---------|
| `HEDERA_ACCOUNT_ID` | Yes | — |
| `HEDERA_PRIVATE_KEY` | Yes | — |
| `HEDERA_NETWORK` | No | `hedera-testnet` |
| `SERVER_PORT` | No | `8080` |

## Development

```bash
# Run without Docker (Java 21 required)
./mvnw spring-boot:run -pl hiero-proxy-server    # Linux/macOS
.\mvnw.cmd spring-boot:run -pl hiero-proxy-server  # Windows
```

## License

Apache-2.0

**Key behaviour to know:**

- Key generation happens server-side. When you create an account or rotate a key, the proxy generates a fresh ED25519 key pair and returns the private key once in the response. Save it — it won't be shown again.
- All errors follow the same shape: `{ "status": <http-code>, "message": "<reason>" }`.
- The operator account (from your env) pays all transaction fees.

## CI/CD

Every push to `main` builds and publishes a multi-platform image (`linux/amd64` + `linux/arm64`) to:

```
ghcr.io/hiero-hackers/hiero-enterprise-proxy:latest
```

Versioned releases are tagged from semver git tags (`v1.2.3` → `:v1.2.3`, `:1.2`, `:1`).

## Contributing

Contributions are welcome. Please sign your commits (`git commit -s`) — the repo enforces DCO.

Built on [`org.hiero:hiero-enterprise-spring:0.20.0`](https://central.sonatype.com/artifact/org.hiero/hiero-enterprise-spring) from Maven Central.
