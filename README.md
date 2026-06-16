# Hiero Enterprise Proxy

A domain-driven Spring Boot proxy server that integrates with the `hiero-enterprise-java` SDK to interact with the Hedera network.

## Project Structure

This project uses Git submodules to reference the core SDK.
- `lib/hiero-enterprise-java`: The core Hedera enterprise SDK submodule.
- `hiero-proxy-server`: Our domain-driven Spring Boot application.

## Getting Started

### 1. Environment Configuration
Create a `.env` file in the root directory based on the configuration in `application.properties`:
```env
HEDERA_ACCOUNT_ID=0.0.xxxxx
HEDERA_PRIVATE_KEY=your_private_key_here
HEDERA_NETWORK=hedera-testnet
```

### 2. Running the Server
We use the Maven Wrapper (`mvnw`) to ensure consistent builds across all environments without requiring developers to manually install Maven.

Run the Spring Boot application from the root directory:
**Linux/macOS:**
```bash
./mvnw spring-boot:run -pl hiero-proxy-server
```
**Windows:**
```powershell
.\mvnw.cmd spring-boot:run -pl hiero-proxy-server
```

### 3. Testing the Endpoints
Once running, you can test the account creation endpoint:
```bash
curl -X POST http://localhost:8080/api/v1/accounts
```
