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

describe("ContractsResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("deploy() calls POST /api/v1/contracts with bytecode", async () => {
    const response = { contractId: "0.0.500", deleted: false, createdTimestampEpochSecond: 123, expirationTimestampEpochSecond: 456, evmAddress: "0xabc", memo: null };
    mockFetch.mockResolvedValue(mockResponse(response, 201));

    const result = await client.contracts.deploy("0x608060");
    expect(result).toEqual(response);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/contracts");
    expect(call[1].method).toBe("POST");
    expect(JSON.parse(call[1].body)).toEqual({ bytecode: "0x608060" });
  });

  it("call() calls POST /api/v1/contracts/:id/call", async () => {
    const response = { gasUsed: 21000, costInTinybars: 500000 };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.contracts.call("0.0.500", "greet");
    expect(result).toEqual(response);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/contracts/0.0.500/call");
    expect(JSON.parse(call[1].body)).toEqual({ functionName: "greet" });
  });

  it("list() calls GET /api/v1/contracts", async () => {
    const response = [{ contractId: "0.0.500", deleted: false, createdTimestampEpochSecond: null, expirationTimestampEpochSecond: null, evmAddress: null, memo: null }];
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.contracts.list();
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/contracts",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getInfo() calls GET /api/v1/contracts/:id", async () => {
    const response = { contractId: "0.0.500", deleted: false, createdTimestampEpochSecond: 123, expirationTimestampEpochSecond: 456, evmAddress: "0xabc", memo: "test" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.contracts.getInfo("0.0.500");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/contracts/0.0.500",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("call() throws if contractId is empty", async () => {
    await expect(client.contracts.call("", "greet")).rejects.toThrow("contractId is required");
  });

  it("getInfo() throws if contractId is empty", async () => {
    await expect(client.contracts.getInfo("  ")).rejects.toThrow("contractId is required");
  });
});
