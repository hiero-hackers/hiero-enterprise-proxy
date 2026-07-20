# Hiero Enterprise Proxy — SDK Clients

This directory contains official client SDKs for the Hiero Enterprise Proxy API.

## Available Clients

| Language | Directory | Package | Install |
|---|---|---|---|
| Python | `python/` | `hiero-enterprise-proxy` | `pip install "hiero-enterprise-proxy @ git+https://github.com/hiero-hackers/hiero-enterprise-proxy.git#subdirectory=clients/python"` |
| JavaScript/TypeScript | `javascript/` | `@hiero-hackers/proxy-sdk` | Clone + link (see below) |

## How it works

1. The proxy server exposes an OpenAPI 3.0 spec at `/v3/api-docs`.
2. Each SDK is a handwritten, typed wrapper over the proxy's REST endpoints.
3. Both SDKs cover all 9 resources: accounts, network, tokens, NFTs, topics, contracts, files, blocks, and transactions.

## JavaScript install (clone + link)

npm doesn't support subdirectory git installs, so:

```bash
git clone https://github.com/hiero-hackers/hiero-enterprise-proxy.git
cd hiero-enterprise-proxy/clients/javascript
npm install && npm run build
npm link

# Then in your project:
npm link @hiero-hackers/proxy-sdk
```

## Contributing a new client

1. Create a `clients/<language>/` directory.
2. Implement wrappers for the proxy REST endpoints.
3. Add tests and a README.
4. Open a PR — sign commits with `git commit -s` (DCO required).
