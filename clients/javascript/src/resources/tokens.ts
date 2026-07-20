import type { HieroProxyClient } from "../client";
import type {
  CreateTokenRequest,
  TokenCreatedResponse,
  TokenInfoResponse,
  TokenBalanceResponse,
  TokenResponse,
  TokenSupplyResponse,
  TokenAssociateRequest,
  TokenBatchAssociateRequest,
  MintTokenRequest,
  BurnTokenRequest,
  TokenOperatorTransferRequest,
  TokenTransferRequest,
} from "./tokens-topics-types";
import type { SuccessResponse } from "./types";

/** Validates that an ID is non-empty and URL-encodes it. */
function requireId(value: string, paramName: string): string {
  if (!value || typeof value !== "string" || !value.trim()) {
    throw new Error(`${paramName} is required and must be a non-empty string`);
  }
  return encodeURIComponent(value.trim());
}

/**
 * Fungible Tokens resource — create, mint, burn, transfer, associate/dissociate HTS tokens.
 */
export class TokensResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * Create a new fungible token.
   */
  async create(request: CreateTokenRequest): Promise<TokenCreatedResponse> {
    return this.client.post("/api/v1/tokens", request);
  }

  /**
   * Get token info from the mirror node.
   */
  async getInfo(tokenId: string): Promise<TokenInfoResponse> {
    const id = requireId(tokenId, "tokenId");
    return this.client.get(`/api/v1/tokens/${id}`);
  }

  /**
   * Get the token balance for a specific account.
   */
  async getBalance(tokenId: string, accountId: string): Promise<TokenBalanceResponse> {
    const tid = requireId(tokenId, "tokenId");
    const aid = requireId(accountId, "accountId");
    return this.client.get(`/api/v1/tokens/${tid}/balances/${aid}`);
  }

  /**
   * Get all token balances (all holders).
   */
  async getAllBalances(tokenId: string): Promise<TokenBalanceResponse[]> {
    const id = requireId(tokenId, "tokenId");
    return this.client.get(`/api/v1/tokens/${id}/balances`);
  }

  /**
   * Get all tokens associated with an account.
   */
  async getByAccount(accountId: string): Promise<TokenResponse[]> {
    const id = requireId(accountId, "accountId");
    return this.client.get(`/api/v1/tokens/account/${id}`);
  }

  /**
   * Associate an account with a token.
   */
  async associate(tokenId: string, accountId: string, accountKey: string): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: TokenAssociateRequest = { accountId, accountKey };
    return this.client.post(`/api/v1/tokens/${id}/associate`, body);
  }

  /**
   * Dissociate an account from a token.
   */
  async dissociate(tokenId: string, accountId: string, accountKey: string): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: TokenAssociateRequest = { accountId, accountKey };
    return this.client.delete(`/api/v1/tokens/${id}/associate`, body);
  }

  /**
   * Batch associate an account with multiple tokens.
   */
  async batchAssociate(tokenIds: string[], accountId: string, accountKey: string): Promise<SuccessResponse> {
    const body: TokenBatchAssociateRequest = { tokenIds, accountId, accountKey };
    return this.client.post("/api/v1/tokens/associate", body);
  }

  /**
   * Batch dissociate an account from multiple tokens.
   */
  async batchDissociate(tokenIds: string[], accountId: string, accountKey: string): Promise<SuccessResponse> {
    const body: TokenBatchAssociateRequest = { tokenIds, accountId, accountKey };
    return this.client.delete("/api/v1/tokens/associate", body);
  }

  /**
   * Mint new token units.
   */
  async mint(tokenId: string, amount: number, supplyKey?: string): Promise<TokenSupplyResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: MintTokenRequest = { amount, supplyKey };
    return this.client.post(`/api/v1/tokens/${id}/mint`, body);
  }

  /**
   * Burn token units.
   */
  async burn(tokenId: string, amount: number, supplyKey?: string): Promise<TokenSupplyResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: BurnTokenRequest = { amount, supplyKey };
    return this.client.post(`/api/v1/tokens/${id}/burn`, body);
  }

  /**
   * Transfer tokens from the operator to a target account.
   */
  async transferFromOperator(tokenId: string, toAccountId: string, amount: number): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: TokenOperatorTransferRequest = { toAccountId, amount };
    return this.client.post(`/api/v1/tokens/${id}/transfer`, body);
  }

  /**
   * Transfer tokens between user accounts.
   */
  async transfer(
    tokenId: string,
    fromAccountId: string,
    fromAccountKey: string,
    toAccountId: string,
    amount: number
  ): Promise<SuccessResponse> {
    const tid = requireId(tokenId, "tokenId");
    const fid = requireId(fromAccountId, "fromAccountId");
    const body: TokenTransferRequest = { fromAccountKey, toAccountId, amount };
    return this.client.post(`/api/v1/tokens/${tid}/transfer/${fid}`, body);
  }
}
