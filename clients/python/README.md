# Hiero Enterprise Proxy — Python SDK

Python client library for the [Hiero Enterprise Proxy](https://github.com/hiero-hackers/hiero-enterprise-proxy) REST API.

## Installation

```bash
pip install "hiero-enterprise-proxy @ git+https://github.com/hiero-hackers/hiero-enterprise-proxy.git#subdirectory=clients/python"
```

> **Coming soon:** `pip install hiero-enterprise-proxy` once published to PyPI.

## Quick Start

```python
from hiero_proxy import HieroProxyClient

client = HieroProxyClient(base_url="http://localhost:8080")

# Create an account
account = client.accounts.create(initial_balance_in_hbar=10)
print(account["accountId"])  # 0.0.12345

# Check balance
balance = client.accounts.get_balance("0.0.12345")
print(balance["balanceInHbar"])

# Deploy a contract
contract = client.contracts.deploy(bytecode="0x608060...")
print(contract["contractId"])

# Query blocks
blocks = client.blocks.list()
```

## Async Usage

```python
import asyncio
from hiero_proxy import AsyncHieroProxyClient

async def main():
    async with AsyncHieroProxyClient(base_url="http://localhost:8080") as client:
        account = await client.accounts.create(initial_balance_in_hbar=10)
        print(account["accountId"])

asyncio.run(main())
```

## Resources

| Resource | Operations |
|----------|-----------|
| `accounts` | create, get_balance, get_info, update_key, update_memo, update, delete, delete_to_recipient, transfer_from_operator, transfer |
| `network` | get_exchange_rates, get_fees, get_staking_info, get_supply |
| `tokens` | create, get_info, get_balance, get_all_balances, get_by_account, associate, dissociate, batch_associate, batch_dissociate, mint, burn, transfer_from_operator, transfer |
| `nfts` | create_type, list_by_type, get_instance, list_by_owner, list_by_owner_and_type, associate, dissociate, batch_associate, batch_dissociate, mint, mint_batch, burn, transfer, transfer_batch |
| `topics` | create, create_private, create_with_admin_key, create_private_with_admin_key, get_info, update_memo, rotate_admin_key, rotate_submit_key, update, delete, submit_message, submit_binary_message, get_messages, get_message |
| `contracts` | deploy, call, list, get_info |
| `files` | create, get_contents, get_info, update_contents, update_expiration, delete |
| `blocks` | list, get_by_number, get_by_hash |
| `transactions` | get_by_id, get_by_account |

## Requirements

- Python 3.10+
- httpx

## License

Apache-2.0
