import { describe, it, expect, vi, beforeEach } from "vitest";
import { HieroProxyClient } from "../src/client";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

function mockResponse(body: unknown, status = 200) {
  return { ok: status >= 200 && status < 300, status, text: () => Promise.resolve(JSON.stringify(body)) };
}

describe("TokensResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("create() calls POST /api/v1/tokens", async () => {
    mockFetch.mockResolvedValue(mockResponse({ tokenId: "0.0.55001" }, 201));
    const result = await client.tokens.create({ name: "Test", symbol: "TST" });
    expect(result.tokenId).toBe("0.0.55001");
    expect(mockFetch.mock.calls[0][0]).toBe("http://localhost:8080/api/v1/tokens");
  });

  it("getInfo() calls GET /api/v1/tokens/:id", async () => {
    mockFetch.mockResolvedValue(mockResponse({ tokenId: "0.0.55001", name: "Test" }));
    const result = await client.tokens.getInfo("0.0.55001");
    expect(result.tokenId).toBe("0.0.55001");
  });

  it("mint() calls POST /api/v1/tokens/:id/mint", async () => {
    mockFetch.mockResolvedValue(mockResponse({ totalSupply: 6000 }));
    const result = await client.tokens.mint("0.0.55001", 1000);
    expect(result.totalSupply).toBe(6000);
    const call = mockFetch.mock.calls[0];
    expect(JSON.parse(call[1].body)).toEqual({ amount: 1000, supplyKey: undefined });
  });

  it("burn() calls POST /api/v1/tokens/:id/burn", async () => {
    mockFetch.mockResolvedValue(mockResponse({ totalSupply: 4500 }));
    await client.tokens.burn("0.0.55001", 500);
    expect(mockFetch.mock.calls[0][0]).toContain("/burn");
  });

  it("associate() calls POST /api/v1/tokens/:id/associate", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));
    await client.tokens.associate("0.0.55001", "0.0.123", "key");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toContain("/associate");
    expect(call[1].method).toBe("POST");
  });

  it("transferFromOperator() calls POST /api/v1/tokens/:id/transfer", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));
    await client.tokens.transferFromOperator("0.0.55001", "0.0.999", 250);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/tokens/0.0.55001/transfer");
    expect(JSON.parse(call[1].body)).toEqual({ toAccountId: "0.0.999", amount: 250 });
  });

  it("transfer() calls POST /api/v1/tokens/:id/transfer/:fromId", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));
    await client.tokens.transfer("0.0.55001", "0.0.111", "key", "0.0.222", 100);
    expect(mockFetch.mock.calls[0][0]).toBe("http://localhost:8080/api/v1/tokens/0.0.55001/transfer/0.0.111");
  });

  it("rejects empty tokenId", async () => {
    await expect(client.tokens.getInfo("")).rejects.toThrow("tokenId is required");
  });
});
