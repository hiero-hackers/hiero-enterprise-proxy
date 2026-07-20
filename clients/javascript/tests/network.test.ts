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

describe("NetworkResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("getExchangeRates() calls GET /api/v1/network/exchange-rates", async () => {
    const response = {
      currentRate: { centEquivalent: 12, hbarEquivalent: 1, expirationTimeEpochSecond: 1700000000 },
      nextRate: { centEquivalent: 13, hbarEquivalent: 1, expirationTimeEpochSecond: 1700003600 },
    };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.network.getExchangeRates();
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/network/exchange-rates",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getFees() calls GET /api/v1/network/fees", async () => {
    const response = [
      { gas: 171, transactionType: "ContractCall" },
      { gas: 371, transactionType: "ContractCreate" },
    ];
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.network.getFees();
    expect(result).toEqual(response);
  });

  it("getStake() calls GET /api/v1/network/stake", async () => {
    const response = {
      maxStakeReward: 17500000000000000,
      stakeTotal: 5000000000000000,
      stakingPeriodDuration: 1440,
    };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.network.getStake();
    expect(result.stakeTotal).toBe(5000000000000000);
  });

  it("getSupplies() calls GET /api/v1/network/supplies", async () => {
    const response = {
      releasedSupply: "3987242498080780000",
      totalSupply: "5000000000000000000",
    };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.network.getSupplies();
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/network/supplies",
      expect.objectContaining({ method: "GET" })
    );
  });
});
