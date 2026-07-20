export interface HbarTransferResponse {
  accountId: string;
  amount: number;
  isApproval: boolean;
}

export interface TokenTransferResponse {
  tokenId: string;
  accountId: string;
  amount: number;
  isApproval: boolean;
}

export interface NftTransferInfoResponse {
  tokenId: string | null;
  serialNumber: number;
  senderAccountId: string | null;
  receiverAccountId: string | null;
  isApproval: boolean;
}

export interface StakingRewardTransferResponse {
  accountId: string;
  amount: number;
}

export interface TransactionInfoResponse {
  transactionId: string;
  transactionType: string;
  result: string;
  scheduled: boolean;
  chargedTxFee: number;
  maxFee: string;
  consensusTimestampEpochSecond: number;
  validStartTimestampEpochSecond: number;
  validDurationSeconds: string;
  entityId: string | null;
  node: string | null;
  nonce: number;
  parentConsensusTimestampEpochSecond: number | null;
  transfers: HbarTransferResponse[];
  tokenTransfers: TokenTransferResponse[];
  nftTransfers: NftTransferInfoResponse[];
  stakingRewardTransfers: StakingRewardTransferResponse[];
}

export interface TransactionFilterOptions {
  type?: string;
  result?: "SUCCESS" | "FAIL";
  modification?: "CREDIT" | "DEBIT";
}
