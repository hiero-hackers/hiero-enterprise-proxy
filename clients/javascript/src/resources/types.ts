// ─── Account Responses ────────────────────────────────────────────────────────

export interface AccountResponse {
  accountId: string;
  publicKey: string;
  privateKey: string;
}

export interface BalanceResponse {
  accountId: string;
  balanceHbar: string;
}

export interface AccountInfoResponse {
  accountId: string;
  evmAddress: string;
  balanceTinybars: number;
  ethereumNonce: number;
  pendingRewardTinybars: number;
}

export interface SuccessResponse {
  message: string;
}

// ─── Account Requests ─────────────────────────────────────────────────────────

export interface CreateAccountRequest {
  initialBalanceInHbar?: number;
}

export interface UpdateKeyRequest {
  currentPrivateKey: string;
}

export interface UpdateMemoRequest {
  privateKey: string;
  memo: string;
}

export interface UpdateAccountRequest {
  currentPrivateKey: string;
  memo: string;
}

export interface DeleteAccountRequest {
  privateKey: string;
}

export interface DeleteAccountToRecipientRequest {
  accountPrivateKey: string;
  recipientPrivateKey: string;
}

export interface OperatorTransferRequest {
  toAccountId: string;
  amountInHbar: number;
}

export interface TransferRequest {
  fromAccountPrivateKey: string;
  toAccountId: string;
  amountInHbar: number;
}

// ─── Network Responses ────────────────────────────────────────────────────────

export interface ExchangeRate {
  centEquivalent: number;
  hbarEquivalent: number;
  expirationTimeEpochSecond: number;
}

export interface ExchangeRatesResponse {
  currentRate: ExchangeRate;
  nextRate: ExchangeRate;
}

export interface NetworkFeeResponse {
  gas: number;
  transactionType: string;
}

export interface NetworkStakeResponse {
  maxStakeReward: number;
  maxStakeRewardPerHbar: number;
  maxTotalReward: number;
  nodeRewardFeeFraction: number;
  reservedStakingRewards: number;
  rewardBalanceThreshold: number;
  stakeTotal: number;
  stakingPeriodDuration: number;
  stakingPeriodsStored: number;
  stakingRewardFeeFraction: number;
  stakingRewardRate: number;
  stakingStartThreshold: number;
  unreservedStakingRewardBalance: number;
}

export interface NetworkSuppliesResponse {
  releasedSupply: string;
  totalSupply: string;
}
