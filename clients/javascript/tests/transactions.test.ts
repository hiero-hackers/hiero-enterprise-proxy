import { describe, it, expect, vi, beforeEach } from "vitest";
import { HieroProxyClient } from "../src/client";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

function mockResponse(body: unknown, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    text: () => Promise.resolve(JSON.stringify(body)),
  };
}

describe("TransactionsResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("getById() calls GET /api/v1/transactions/:id", async () => {
    const response = {
      transactionId: "0.0.123@1700000000.000000000",
      transactionType: "CRYPTOTRANSFER",
      result: "SUCCESS",
      scheduled: false,
      chargedTxFee: 100000,
      maxFee: "200000",
      consensusTimestampEpochSecond: 1700000001,
      validStartTimestampEpochSecond: 1700000000,
      validDurationSeconds: "120",
      entityId: "0.0.456",
      node: "0.0.3",
      nonce: 0,
      parentConsensusTimestampEpochSecond: null,
      transfers: [{ accountId: "0.0.123", amount: -100, isApproval: false }],
      tokenTransfers: [],
      nftTransfers: [],
      stakingRewardTransfers: [],
    };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.transactions.getById("0.0.123@1700000000.000000000");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/transactions/0.0.123%401700000000.000000000",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getByAccount() calls GET /api/v1/transactions/account/:id without filters", async () => {
    const response = [{ transactionId: "0.0.123@1700000000.000000000", transactionType: "CRYPTOTRANSFER", result: "SUCCESS", scheduled: false, chargedTxFee: 100000, maxFee: "200000", consensusTimestampEpochSecond: 1700000001, validStartTimestampEpochSecond: 1700000000, validDurationSeconds: "120", entityId: null, node: null, nonce: 0, parentConsensusTimestampEpochSecond: null, transfers: [], tokenTransfers: [], nftTransfers: [], stakingRewardTransfers: [] }];
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.transactions.getByAccount("0.0.123");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/transactions/account/0.0.123",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getByAccount() appends query params when filters are provided", async () => {
    mockFetch.mockResolvedValue(mockResponse([]));

    await client.transactions.getByAccount("0.0.456", {
      type: "CRYPTOTRANSFER",
      result: "SUCCESS",
      modification: "DEBIT",
    });

    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe(
      "http://localhost:8080/api/v1/transactions/account/0.0.456?type=CRYPTOTRANSFER&result=SUCCESS&modification=DEBIT"
    );
  });

  it("getByAccount() omits empty filter values", async () => {
    mockFetch.mockResolvedValue(mockResponse([]));

    await client.transactions.getByAccount("0.0.789", { result: "FAIL" });

    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/transactions/account/0.0.789?result=FAIL");
  });

  it("getById() throws if transactionId is empty", async () => {
    await expect(client.transactions.getById("")).rejects.toThrow("transactionId is required");
  });

  it("getByAccount() throws if accountId is empty", async () => {
    await expect(client.transactions.getByAccount("  ")).rejects.toThrow("accountId is required");
  });
});
