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

describe("BlocksResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("list() calls GET /api/v1/blocks", async () => {
    const response = [{ count: 1, hapiVersion: "0.47.0", hash: "0xabc", name: "block-1", number: 1, previousHash: "0x000", size: 256, fromTimestampEpochSecond: 100, toTimestampEpochSecond: 200, gasUsed: 0, logsBloom: null }];
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.blocks.list();
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/blocks",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getByNumber() calls GET /api/v1/blocks/number/:n", async () => {
    const response = { count: 5, hapiVersion: "0.47.0", hash: "0xdef", name: "block-42", number: 42, previousHash: "0xaaa", size: 512, fromTimestampEpochSecond: 300, toTimestampEpochSecond: 400, gasUsed: 21000, logsBloom: "0x0" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.blocks.getByNumber(42);
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/blocks/number/42",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getByHash() calls GET /api/v1/blocks/hash/:hash", async () => {
    const response = { count: 5, hapiVersion: "0.47.0", hash: "0xdef456", name: "block-10", number: 10, previousHash: "0xaaa", size: 128, fromTimestampEpochSecond: null, toTimestampEpochSecond: null, gasUsed: 0, logsBloom: null };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.blocks.getByHash("0xdef456");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/blocks/hash/0xdef456",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getByNumber() throws if blockNumber is negative", async () => {
    await expect(client.blocks.getByNumber(-1)).rejects.toThrow("blockNumber must not be negative");
  });

  it("getByHash() throws if hash is empty", async () => {
    await expect(client.blocks.getByHash("")).rejects.toThrow("hash is required");
  });
});
