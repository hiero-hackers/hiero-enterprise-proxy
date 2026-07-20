export interface BlockResponse {
  count: number;
  hapiVersion: string;
  hash: string;
  name: string;
  number: number;
  previousHash: string;
  size: number;
  fromTimestampEpochSecond: number | null;
  toTimestampEpochSecond: number | null;
  gasUsed: number;
  logsBloom: string | null;
}
