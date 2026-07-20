# @hiero-hackers/proxy-sdk

Official JavaScript/TypeScript client SDK for the [Hiero Enterprise Proxy](https://github.com/hiero-hackers/hiero-enterprise-proxy).

> **Note:** This is a client for the REST proxy — not a replacement for the native Hiero SDK.  
> Use this when you want fast HTTP integration without managing gRPC connections or Java dependencies.

## Installation

```bash
npm install @hiero-hackers/proxy-sdk
```

## Quick Start

```typescript
import { HieroProxyClient } from "@hiero-hackers/proxy-sdk";

const client = new HieroProxyClient({
  baseUrl: "http://localhost:8080",
});

// Get account balance
const balance = await client.get("/api/v1/accounts/0.0.12345/balance");
console.log(balance);

// Create a new account
const account = await client.post("/api/v1/accounts");
console.log(account);
```

## Configuration

```typescript
const client = new HieroProxyClient({
  baseUrl: "http://localhost:8080",  // Required: proxy URL
  timeout: 10000,                    // Optional: request timeout in ms (default: 30000)
  headers: {                         // Optional: custom headers
    "X-Request-Id": "my-app",
  },
});
```

## Error Handling

All errors thrown by the SDK extend `HieroProxyError`:

```typescript
import { HieroProxyClient, ClientError, ServerError, NetworkError } from "@hiero-hackers/proxy-sdk";

try {
  await client.get("/api/v1/accounts/0.0.99999/balance");
} catch (error) {
  if (error instanceof ClientError) {
    // 4xx — bad request, not found, etc.
    console.log(error.status, error.message);
  } else if (error instanceof ServerError) {
    // 5xx — proxy or network issue
    console.log(error.status, error.message);
  } else if (error instanceof NetworkError) {
    // Connection failed or timeout
    console.log(error.message);
  }
}
```

## API Methods

The client provides typed HTTP methods that map directly to the proxy API:

| Method | Usage |
|---|---|
| `client.get(path)` | GET request |
| `client.post(path, body?)` | POST request |
| `client.put(path, body?)` | PUT request |
| `client.delete(path)` | DELETE request |

Full endpoint-specific methods (e.g., `client.accounts.create()`) are coming in future releases.

## Requirements

- Node.js 18+ (uses native `fetch`)
- A running Hiero Enterprise Proxy instance

## Development

```bash
cd clients/javascript
npm install
npm test        # run tests
npm run build   # build for publishing
```

## License

Apache-2.0
