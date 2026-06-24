# Hiero Enterprise Proxy

A Spring Boot REST proxy server that exposes the [hiero-enterprise-java](https://github.com/hiero-ledger/hiero-enterprise-java) SDK as a documented HTTP API. It provides a Swagger UI for all Hiero network operations without requiring direct SDK integration in client applications.

## Features

- Full **Account** management — create, query, update, delete, transfer HBAR
- Full **Topic (HCS)** management — create public/private topics, submit messages, query topic info and message history
- Interactive **Swagger UI** at `http://localhost:8080/swagger-ui/index.html`
- Structured error responses following RFC 7807
- No Git submodules — all dependencies are resolved from Maven Central

## Prerequisites

- Java 21
- A funded [Hedera testnet account](https://portal.hedera.com)

## Getting Started

### 1. Configure environment

Create a `.env` file in the project root:

```env
HEDERA_ACCOUNT_ID=0.0.xxxxx
HEDERA_PRIVATE_KEY=your_operator_private_key
HEDERA_NETWORK=hedera-testnet
```

### 2. Run the server

**Linux / macOS:**
```bash
./mvnw spring-boot:run -pl hiero-proxy-server
```

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run -pl hiero-proxy-server
```

The server starts on port `8080`. Open `http://localhost:8080/swagger-ui/index.html` to explore all endpoints interactively.

## API Reference

### Accounts — `/api/v1/accounts`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/accounts` | Create a new account (optional initial HBAR balance) |
| `GET` | `/api/v1/accounts/{accountId}/balance` | Get account HBAR balance |
| `GET` | `/api/v1/accounts/operator/balance` | Get operator HBAR balance |
| `GET` | `/api/v1/accounts/{accountId}/info` | Get detailed account info from mirror node |
| `PUT` | `/api/v1/accounts/{accountId}/key` | Rotate account key pair (server generates new key) |
| `PUT` | `/api/v1/accounts/{accountId}/memo` | Update account memo |
| `PUT` | `/api/v1/accounts/{accountId}` | Atomic key rotation + memo update |
| `POST` | `/api/v1/accounts/transfer` | Transfer HBAR from operator to an account |
| `POST` | `/api/v1/accounts/{accountId}/transfer` | Transfer HBAR between user accounts |
| `DELETE` | `/api/v1/accounts/{accountId}` | Delete account — balance transfers to operator |
| `DELETE` | `/api/v1/accounts/{accountId}/to/{recipientAccountId}` | Delete account — balance transfers to recipient |

### Topics — `/api/v1/topics`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/topics` | Create a public topic |
| `POST` | `/api/v1/topics/private` | Create a private topic (server generates submit key) |
| `POST` | `/api/v1/topics/with-admin-key` | Create a public topic with a custom admin key |
| `POST` | `/api/v1/topics/private/with-admin-key` | Create a private topic with a custom admin key |
| `GET` | `/api/v1/topics/{topicId}` | Get topic info from mirror node |
| `GET` | `/api/v1/topics/{topicId}/messages` | Get all messages for a topic |
| `GET` | `/api/v1/topics/{topicId}/messages/{sequenceNumber}` | Get a specific message by sequence number |
| `PUT` | `/api/v1/topics/{topicId}/memo` | Update topic memo |
| `PUT` | `/api/v1/topics/{topicId}/admin-key` | Rotate topic admin key (server generates new key) |
| `PUT` | `/api/v1/topics/{topicId}/submit-key` | Rotate topic submit key (server generates new key) |
| `PUT` | `/api/v1/topics/{topicId}` | Atomic admin key + submit key + memo update |
| `POST` | `/api/v1/topics/{topicId}/messages` | Submit a text message to a topic |
| `POST` | `/api/v1/topics/{topicId}/messages/binary` | Submit a binary message (Base64-encoded) to a topic |
| `DELETE` | `/api/v1/topics/{topicId}` | Delete a topic |

### Fungible Tokens — `/api/v1/tokens`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/tokens` | Create a fungible token |
| `GET` | `/api/v1/tokens/{tokenId}` | Get detailed token info from mirror node |
| `GET` | `/api/v1/tokens/{tokenId}/balances` | Get all account balances for a token from mirror node |
| `GET` | `/api/v1/tokens/{tokenId}/balances/{accountId}` | Get token balance for an account from mirror node |
| `GET` | `/api/v1/tokens/account/{accountId}` | Get all tokens associated with an account from mirror node |
| `POST` | `/api/v1/tokens/associate` | Batch associate an account with multiple tokens |
| `DELETE` | `/api/v1/tokens/associate` | Batch dissociate an account from multiple tokens |
| `POST` | `/api/v1/tokens/{tokenId}/associate` | Associate an account with a token |
| `DELETE` | `/api/v1/tokens/{tokenId}/associate` | Dissociate an account from a token |
| `POST` | `/api/v1/tokens/{tokenId}/mint` | Mint new token units to the treasury |
| `POST` | `/api/v1/tokens/{tokenId}/burn` | Burn token units from the treasury |
| `POST` | `/api/v1/tokens/{tokenId}/transfer` | Transfer tokens from the operator to an account |
| `POST` | `/api/v1/tokens/{tokenId}/transfer/{fromAccountId}` | Transfer tokens between user accounts |

## Key Design Decisions

**Server-side key generation** — key creation and rotation endpoints generate fresh ED25519 key pairs on the server and return them in the response. This is consistent across account creation, account key rotation, and topic key rotation. The caller must save the returned private key — it is only shown once.

**Structured success responses** — mutation endpoints that have no data to return respond with a `SuccessResponse` containing a contextual message rather than an empty body.

**Maven Central dependencies** — the `hiero-enterprise-java` library is consumed as a versioned Maven dependency (`org.hiero:hiero-enterprise-spring:0.20.0`). No Git submodules are required.

## Project Structure

```
hiero-enterprise-proxy/
├── hiero-proxy-server/          # Spring Boot application
│   └── src/main/java/org/hiero/proxy/server/
│       ├── config/              # OpenAPI / Swagger UI configuration
│       ├── controller/          # REST controllers (AccountController, TopicController)
│       ├── dto/
│       │   ├── request/         # Inbound HTTP request body records
│       │   └── response/        # Outbound HTTP response records with from() factories
│       └── exception/           # Global exception handler and ErrorResponse
└── pom.xml                      # Root Maven POM
```
