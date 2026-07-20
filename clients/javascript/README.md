# @hiero-hackers/proxy-sdk

Official JavaScript/TypeScript client SDK for the [Hiero Enterprise Proxy](https://github.com/hiero-hackers/hiero-enterprise-proxy).

> **Note:** This is a client for the REST proxy — not a replacement for the native Hiero SDK.  
> Use this when you want fast HTTP integration without managing gRPC connections or Java dependencies.

## Installation

npm doesn't support installing from a subdirectory of a git repo, so for now use clone + link:

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

> **Coming soon:** `npm install @hiero-hackers/proxy-sdk` once published to npm.

## Quick Start

```typescript
import { HieroProxyClient } from "@hiero-hackers/proxy-sdk";

const client = new HieroProxyClient({
  baseUrl: "http://localhost:8080",
});

// Create an account funded with 10 HBAR
const account = await client.accounts.create(10);
console.log(account.accountId); // 0.0.12345

// Create a fungible token
const token = await client.tokens.create("MyToken", "MTK", 1_000_000);
await client.tokens.mint(token.tokenId, 500);

// Create a topic and send a message
const topic = await client.topics.create("my event bus");
await client.topics.submitMessage(topic.topicId, "Hello Hiero");
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
  await client.accounts.getBalance("0.0.99999");
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

## Resources

| Resource | Operations |
|----------|-----------|
| `client.accounts` | create, getBalance, getInfo, updateKey, updateMemo, update, delete, deleteToRecipient, transferFromOperator, transfer |
| `client.network` | getExchangeRates, getFees, getStakingInfo, getSupply |
| `client.tokens` | create, getInfo, getBalance, getAllBalances, getByAccount, associate, dissociate, batchAssociate, batchDissociate, mint, burn, transferFromOperator, transfer |
| `client.nfts` | createType, listByType, getInstance, listByOwner, listByOwnerAndType, associate, dissociate, batchAssociate, batchDissociate, mint, mintBatch, burn, transfer, transferBatch |
| `client.topics` | create, createPrivate, createWithAdminKey, createPrivateWithAdminKey, getInfo, updateMemo, rotateAdminKey, rotateSubmitKey, update, delete, submitMessage, submitBinaryMessage, getMessages, getMessage |
| `client.contracts` | deploy, call, list, getInfo |
| `client.files` | create, getContents, getInfo, updateContents, updateExpiration, delete |
| `client.blocks` | list, getByNumber, getByHash |
| `client.transactions` | getById, getByAccount |

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
