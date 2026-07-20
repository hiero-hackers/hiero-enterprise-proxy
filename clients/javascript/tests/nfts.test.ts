import { describe, it, expect, vi, beforeEach } from "vitest";
import { HieroProxyClient } from "../src/client";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

function mockResponse(body: unknown, status = 200) {
  return { ok: status >= 200 && status < 300, status, text: () => Promise.resolve(JSON.stringify(body)) };
}

describe("NftsResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("createType() calls POST /api/v1/nfts", async () => {
    mockFetch.mockResolvedValue(mockResponse({ tokenId: "0.0.66001" }, 201));
    const result = await client.nfts.createType({ name: "My NFT", symbol: "MNFT" });
    expect(result.tokenId).toBe("0.0.66001");
  });

  it("listByType() calls GET /api/v1/nfts/:id/instances", async () => {
    mockFetch.mockResolvedValue(mockResponse([]));
    await client.nfts.listByType("0.0.66001");
    expect(mockFetch.mock.calls[0][0]).toBe("http://localhost:8080/api/v1/nfts/0.0.66001/instances");
  });

  it("getInstance() calls correct path", async () => {
    mockFetch.mockResolvedValue(mockResponse({ tokenId: "0.0.66001", serialNumber: 1 }));
    const result = await client.nfts.getInstance("0.0.66001", 1);
    expect(result.serialNumber).toBe(1);
    expect(mockFetch.mock.calls[0][0]).toContain("/instances/1");
  });

  it("mint() calls POST /api/v1/nfts/:id/mint", async () => {
    mockFetch.mockResolvedValue(mockResponse({ serialNumber: 5 }, 201));
    const result = await client.nfts.mint("0.0.66001", "aXBmczovL3Rlc3Q=");
    expect(result.serialNumber).toBe(5);
  });

  it("mintBatch() calls POST /api/v1/nfts/:id/mint/batch", async () => {
    mockFetch.mockResolvedValue(mockResponse({ serialNumbers: [1, 2, 3] }, 201));
    const result = await client.nfts.mintBatch("0.0.66001", ["a", "b", "c"]);
    expect(result.serialNumbers).toEqual([1, 2, 3]);
  });

  it("burn() calls POST /api/v1/nfts/:id/burn", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "burned" }));
    await client.nfts.burn("0.0.66001", [1, 2]);
    const call = mockFetch.mock.calls[0];
    expect(JSON.parse(call[1].body)).toEqual({ serialNumbers: [1, 2], supplyKey: undefined });
  });

  it("transfer() calls POST /api/v1/nfts/:id/transfer/:serial", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));
    await client.nfts.transfer("0.0.66001", 1, "0.0.111", "key", "0.0.222");
    expect(mockFetch.mock.calls[0][0]).toBe("http://localhost:8080/api/v1/nfts/0.0.66001/transfer/1");
  });

  it("transferBatch() sends serialNumbers in body", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));
    await client.nfts.transferBatch("0.0.66001", [1, 2], "0.0.111", "key", "0.0.222");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/nfts/0.0.66001/transfer");
    expect(JSON.parse(call[1].body).serialNumbers).toEqual([1, 2]);
  });
});
