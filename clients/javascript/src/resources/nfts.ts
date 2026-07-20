import type { HieroProxyClient } from "../client";
import type {
  CreateNftTypeRequest,
  NftTypeCreatedResponse,
  NftResponse,
  NftMintedResponse,
  NftMintedBatchResponse,
  MintNftRequest,
  MintNftsRequest,
  BurnNftsRequest,
  NftTransferRequest,
  NftBatchTransferRequest,
  TokenAssociateRequest,
  TokenBatchAssociateRequest,
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
 * NFTs resource — create types, mint, burn, transfer, query NFT instances.
 */
export class NftsResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * Create a new NFT token type.
   */
  async createType(request: CreateNftTypeRequest): Promise<NftTypeCreatedResponse> {
    return this.client.post("/api/v1/nfts", request);
  }

  /**
   * List all NFT instances of a type (excludes burned).
   */
  async listByType(tokenId: string): Promise<NftResponse[]> {
    const id = requireId(tokenId, "tokenId");
    return this.client.get(`/api/v1/nfts/${id}/instances`);
  }

  /**
   * Get a specific NFT instance by serial number.
   */
  async getInstance(tokenId: string, serialNumber: number): Promise<NftResponse> {
    const id = requireId(tokenId, "tokenId");
    return this.client.get(`/api/v1/nfts/${id}/instances/${serialNumber}`);
  }

  /**
   * List all NFT instances owned by an account.
   */
  async listByOwner(ownerId: string): Promise<NftResponse[]> {
    const id = requireId(ownerId, "ownerId");
    return this.client.get(`/api/v1/nfts/owner/${id}/instances`);
  }

  /**
   * List NFT instances of a type owned by a specific account.
   */
  async listByOwnerAndType(tokenId: string, ownerId: string): Promise<NftResponse[]> {
    const tid = requireId(tokenId, "tokenId");
    const oid = requireId(ownerId, "ownerId");
    return this.client.get(`/api/v1/nfts/${tid}/instances/owner/${oid}`);
  }

  /**
   * Associate an account with an NFT type.
   */
  async associate(tokenId: string, accountId: string, accountKey: string): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: TokenAssociateRequest = { accountId, accountKey };
    return this.client.post(`/api/v1/nfts/${id}/associate`, body);
  }

  /**
   * Dissociate an account from an NFT type.
   */
  async dissociate(tokenId: string, accountId: string, accountKey: string): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: TokenAssociateRequest = { accountId, accountKey };
    return this.client.delete(`/api/v1/nfts/${id}/associate`, body);
  }

  /**
   * Batch associate an account with multiple NFT types.
   */
  async batchAssociate(tokenIds: string[], accountId: string, accountKey: string): Promise<SuccessResponse> {
    const body: TokenBatchAssociateRequest = { tokenIds, accountId, accountKey };
    return this.client.post("/api/v1/nfts/associate", body);
  }

  /**
   * Batch dissociate an account from multiple NFT types.
   */
  async batchDissociate(tokenIds: string[], accountId: string, accountKey: string): Promise<SuccessResponse> {
    const body: TokenBatchAssociateRequest = { tokenIds, accountId, accountKey };
    return this.client.delete("/api/v1/nfts/associate", body);
  }

  /**
   * Mint a single NFT instance.
   * @param metadata Base64-encoded metadata string.
   */
  async mint(tokenId: string, metadata: string, supplyKey?: string): Promise<NftMintedResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: MintNftRequest = { metadata, supplyKey };
    return this.client.post(`/api/v1/nfts/${id}/mint`, body);
  }

  /**
   * Mint multiple NFT instances in a single transaction.
   * @param metadataList Array of Base64-encoded metadata strings.
   */
  async mintBatch(tokenId: string, metadataList: string[], supplyKey?: string): Promise<NftMintedBatchResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: MintNftsRequest = { metadataList, supplyKey };
    return this.client.post(`/api/v1/nfts/${id}/mint/batch`, body);
  }

  /**
   * Burn NFT instances by serial number.
   */
  async burn(tokenId: string, serialNumbers: number[], supplyKey?: string): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: BurnNftsRequest = { serialNumbers, supplyKey };
    return this.client.post(`/api/v1/nfts/${id}/burn`, body);
  }

  /**
   * Transfer a single NFT instance.
   */
  async transfer(
    tokenId: string,
    serialNumber: number,
    fromAccountId: string,
    fromAccountKey: string,
    toAccountId: string
  ): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: NftTransferRequest = { fromAccountId, fromAccountKey, toAccountId };
    return this.client.post(`/api/v1/nfts/${id}/transfer/${serialNumber}`, body);
  }

  /**
   * Transfer multiple NFT instances in a single transaction.
   */
  async transferBatch(
    tokenId: string,
    serialNumbers: number[],
    fromAccountId: string,
    fromAccountKey: string,
    toAccountId: string
  ): Promise<SuccessResponse> {
    const id = requireId(tokenId, "tokenId");
    const body: NftBatchTransferRequest = { serialNumbers, fromAccountId, fromAccountKey, toAccountId };
    return this.client.post(`/api/v1/nfts/${id}/transfer`, body);
  }
}
