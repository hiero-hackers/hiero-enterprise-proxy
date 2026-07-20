export interface ContractCreateRequest {
  bytecode: string;
}

export interface ContractCallRequest {
  functionName: string;
}

export interface ContractResponse {
  contractId: string;
  deleted: boolean;
  createdTimestampEpochSecond: number | null;
  expirationTimestampEpochSecond: number | null;
  evmAddress: string | null;
  memo: string | null;
}

export interface ContractCallResultResponse {
  gasUsed: number;
  costInTinybars: number;
}
