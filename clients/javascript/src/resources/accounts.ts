import type { HieroProxyClient } from "../client";
import type {
  AccountResponse,
  BalanceResponse,
  AccountInfoResponse,
  SuccessResponse,
  CreateAccountRequest,
  UpdateKeyRequest,
  UpdateMemoRequest,
  UpdateAccountRequest,
  DeleteAccountRequest,
  DeleteAccountToRecipientRequest,
  OperatorTransferRequest,
  TransferRequest,
} from "./types";

/** Validates that an account ID is a non-empty string matching the Hiero format. */
function requireAccountId(accountId: string, paramName = "accountId"): string {
  if (!accountId || typeof accountId !== "string" || !accountId.trim()) {
    throw new Error(`${paramName} is required and must be a non-empty string`);
  }
  return encodeURIComponent(accountId.trim());
}

/**
 * Accounts resource — create, query, update, delete accounts and transfer HBAR.
 */
export class AccountsResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * Create a new account on the Hiero network.
   * @param initialBalanceInHbar Optional initial HBAR balance (default: 0).
   */
  async create(initialBalanceInHbar?: number): Promise<AccountResponse> {
    const body: CreateAccountRequest | undefined =
      initialBalanceInHbar !== undefined ? { initialBalanceInHbar } : undefined;
    return this.client.post("/api/v1/accounts", body);
  }

  /**
   * Get the HBAR balance of a specific account.
   */
  async getBalance(accountId: string): Promise<BalanceResponse> {
    const id = requireAccountId(accountId);
    return this.client.get(`/api/v1/accounts/${id}/balance`);
  }

  /**
   * Get the operator account's HBAR balance.
   */
  async getOperatorBalance(): Promise<BalanceResponse> {
    return this.client.get("/api/v1/accounts/operator/balance");
  }

  /**
   * Get detailed account info from the mirror node.
   */
  async getInfo(accountId: string): Promise<AccountInfoResponse> {
    const id = requireAccountId(accountId);
    return this.client.get(`/api/v1/accounts/${id}/info`);
  }

  /**
   * Rotate the key pair of an account. Returns the new key pair.
   * @param accountId The account to update.
   * @param currentPrivateKey The current private key to authorize the rotation.
   */
  async updateKey(accountId: string, currentPrivateKey: string): Promise<AccountResponse> {
    const id = requireAccountId(accountId);
    const body: UpdateKeyRequest = { currentPrivateKey };
    return this.client.put(`/api/v1/accounts/${id}/key`, body);
  }

  /**
   * Update the memo field of an account.
   * @param accountId The account to update.
   * @param privateKey The account's private key.
   * @param memo The new memo value.
   */
  async updateMemo(accountId: string, privateKey: string, memo: string): Promise<SuccessResponse> {
    const id = requireAccountId(accountId);
    const body: UpdateMemoRequest = { privateKey, memo };
    return this.client.put(`/api/v1/accounts/${id}/memo`, body);
  }

  /**
   * Update both the key and memo of an account atomically.
   */
  async update(accountId: string, currentPrivateKey: string, memo: string): Promise<AccountResponse> {
    const id = requireAccountId(accountId);
    const body: UpdateAccountRequest = { currentPrivateKey, memo };
    return this.client.put(`/api/v1/accounts/${id}`, body);
  }

  /**
   * Delete an account. Remaining balance goes to the operator.
   */
  async delete(accountId: string, privateKey: string): Promise<SuccessResponse> {
    const id = requireAccountId(accountId);
    const body: DeleteAccountRequest = { privateKey };
    return this.client.delete(`/api/v1/accounts/${id}`, body);
  }

  /**
   * Delete an account. Remaining balance goes to a specified recipient.
   */
  async deleteToRecipient(
    accountId: string,
    recipientAccountId: string,
    accountPrivateKey: string,
    recipientPrivateKey: string
  ): Promise<SuccessResponse> {
    const id = requireAccountId(accountId);
    const recipientId = requireAccountId(recipientAccountId, "recipientAccountId");
    const body: DeleteAccountToRecipientRequest = { accountPrivateKey, recipientPrivateKey };
    return this.client.delete(`/api/v1/accounts/${id}/to/${recipientId}`, body);
  }

  /**
   * Transfer HBAR from the operator to a target account.
   */
  async transferFromOperator(toAccountId: string, amountInHbar: number): Promise<SuccessResponse> {
    const body: OperatorTransferRequest = { toAccountId, amountInHbar };
    return this.client.post("/api/v1/accounts/transfer", body);
  }

  /**
   * Transfer HBAR between user accounts.
   */
  async transfer(
    fromAccountId: string,
    fromAccountPrivateKey: string,
    toAccountId: string,
    amountInHbar: number
  ): Promise<SuccessResponse> {
    const id = requireAccountId(fromAccountId, "fromAccountId");
    const body: TransferRequest = { fromAccountPrivateKey, toAccountId, amountInHbar };
    return this.client.post(`/api/v1/accounts/${id}/transfer`, body);
  }
}
