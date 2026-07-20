import type { HieroProxyClient } from "../client";
import type { TransactionInfoResponse, TransactionFilterOptions } from "./transactions.types";
import { requireId } from "./validation";

/**
 * Transactions resource — query transactions by ID or account.
 */
export class TransactionsResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * Get a transaction by its ID.
   */
  async getById(transactionId: string): Promise<TransactionInfoResponse> {
    const id = requireId(transactionId, "transactionId");
    return this.client.get(`/api/v1/transactions/${id}`);
  }

  /**
   * Get transactions for an account with optional filters.
   */
  async getByAccount(accountId: string, options?: TransactionFilterOptions): Promise<TransactionInfoResponse[]> {
    const id = requireId(accountId, "accountId");
    const params = new URLSearchParams();
    if (options?.type) params.set("type", options.type);
    if (options?.result) params.set("result", options.result);
    if (options?.modification) params.set("modification", options.modification);
    const query = params.toString();
    const path = `/api/v1/transactions/account/${id}${query ? `?${query}` : ""}`;
    return this.client.get(path);
  }
}
