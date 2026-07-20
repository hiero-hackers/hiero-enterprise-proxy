// ─── Fungible Token Types ─────────────────────────────────────────────────────

export interface CreateTokenRequest {
  name: string;
  symbol: string;
  treasuryAccountId?: string;
  treasuryKey?: string;
  supplyKey?: string;
}

export interface TokenCreatedResponse {
  tokenId: string;
}

export interface TokenInfoResponse {
  tokenId: string;
  name: string;
  symbol: string;
  type: string;
  memo: string | null;
  decimals: number;
  supplyType: string;
  totalSupply: string;
  maxSupply: string;
  treasuryAccountId: string;
  deleted: boolean;
}

export interface TokenBalanceResponse {
  accountId: string;
  balance: number;
  decimals: number;
}

export interface TokenResponse {
  tokenId: string;
  balance: number;
  decimals: number;
}

export interface TokenSupplyResponse {
  totalSupply: number;
}

export interface MintTokenRequest {
  amount: number;
  supplyKey?: string;
}

export interface BurnTokenRequest {
  amount: number;
  supplyKey?: string;
}

export interface TokenAssociateRequest {
  accountId: string;
  accountKey: string;
}

export interface TokenBatchAssociateRequest {
  tokenIds: string[];
  accountId: string;
  accountKey: string;
}

export interface TokenOperatorTransferRequest {
  toAccountId: string;
  amount: number;
}

export interface TokenTransferRequest {
  fromAccountKey: string;
  toAccountId: string;
  amount: number;
}

// ─── NFT Types ────────────────────────────────────────────────────────────────

export interface CreateNftTypeRequest {
  name: string;
  symbol: string;
  treasuryAccountId?: string;
  treasuryKey?: string;
  supplierKey?: string;
}

export interface NftTypeCreatedResponse {
  tokenId: string;
}

export interface NftResponse {
  tokenId: string;
  serialNumber: number;
  ownerId: string;
  metadata: string;
}

export interface NftMintedResponse {
  serialNumber: number;
}

export interface NftMintedBatchResponse {
  serialNumbers: number[];
}

export interface MintNftRequest {
  metadata: string;
  supplyKey?: string;
}

export interface MintNftsRequest {
  metadataList: string[];
  supplyKey?: string;
}

export interface BurnNftsRequest {
  serialNumbers: number[];
  supplyKey?: string;
}

export interface NftTransferRequest {
  fromAccountId: string;
  fromAccountKey: string;
  toAccountId: string;
}

export interface NftBatchTransferRequest {
  serialNumbers: number[];
  fromAccountId: string;
  fromAccountKey: string;
  toAccountId: string;
}

// ─── Topic Types ──────────────────────────────────────────────────────────────

export interface TopicCreatedResponse {
  topicId: string;
  submitPrivateKey: string | null;
  submitPublicKey: string | null;
}

export interface TopicResponse {
  topicId: string;
  memo: string;
  deleted: boolean;
  createdTimestamp: string;
  autoRenewPeriod: number;
  hasAdminKey: boolean;
  hasSubmitKey: boolean;
}

export interface TopicKeyRotationResponse {
  topicId: string;
  newPrivateKey: string;
  newPublicKey: string;
}

export interface TopicUpdatedResponse {
  topicId: string;
  newAdminPrivateKey: string;
  newAdminPublicKey: string;
  newSubmitPrivateKey: string;
  newSubmitPublicKey: string;
  memo: string;
}

export interface TopicMessageResponse {
  topicId: string;
  sequenceNumber: number;
  message: string;
  consensusTimestamp: string;
  payerAccountId: string;
  runningHashBase64: string;
  runningHashVersion: number;
  chunked: boolean;
}

export interface SubmitMessageRequest {
  message: string;
  submitKey?: string;
}

export interface SubmitBinaryMessageRequest {
  messageBase64: string;
  submitKey?: string;
}

export interface CreateTopicWithAdminKeyRequest {
  adminPrivateKey: string;
  memo?: string;
}

export interface CreatePrivateTopicWithAdminKeyRequest {
  adminPrivateKey: string;
  memo?: string;
}

export interface UpdateTopicMemoRequest {
  adminPrivateKey: string;
  memo: string;
}

export interface UpdateTopicKeyRequest {
  adminPrivateKey: string;
}

export interface UpdateTopicRequest {
  currentAdminPrivateKey: string;
  memo: string;
}

export interface DeleteTopicRequest {
  adminPrivateKey: string;
}
