import { describe, it, expect, vi, beforeEach } from "vitest";
import { HieroProxyClient } from "../src/client";

// Mock global fetch
const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

function mockResponse(body: unknown, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    text: () => Promise.resolve(JSON.stringify(body)),
  };
}

describe("AccountsResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("create() calls POST /api/v1/accounts with no body", async () => {
    const response = { accountId: "0.0.123", publicKey: "abc", privateKey: "def" };
    mockFetch.mockResolvedValue(mockResponse(response, 201));

    const result = await client.accounts.create();
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/accounts",
      expect.objectContaining({ method: "POST" })
    );
  });

  it("create() sends initialBalanceInHbar when provided", async () => {
    const response = { accountId: "0.0.123", publicKey: "abc", privateKey: "def" };
    mockFetch.mockResolvedValue(mockResponse(response, 201));

    await client.accounts.create(10);
    const call = mockFetch.mock.calls[0];
    expect(JSON.parse(call[1].body)).toEqual({ initialBalanceInHbar: 10 });
  });

  it("getBalance() calls GET /api/v1/accounts/:id/balance", async () => {
    const response = { accountId: "0.0.123", balanceHbar: "10 ℏ" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.accounts.getBalance("0.0.123");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/accounts/0.0.123/balance",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getOperatorBalance() calls GET /api/v1/accounts/operator/balance", async () => {
    const response = { accountId: "operator", balanceHbar: "1000 ℏ" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.accounts.getOperatorBalance();
    expect(result).toEqual(response);
  });

  it("getInfo() calls GET /api/v1/accounts/:id/info", async () => {
    const response = {
      accountId: "0.0.123",
      evmAddress: "0x000000000000000000000000000000000000007b",
      balanceTinybars: 1000000000,
      ethereumNonce: 0,
      pendingRewardTinybars: 0,
    };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.accounts.getInfo("0.0.123");
    expect(result).toEqual(response);
  });

  it("updateKey() calls PUT /api/v1/accounts/:id/key", async () => {
    const response = { accountId: "0.0.123", publicKey: "new", privateKey: "new" };
    mockFetch.mockResolvedValue(mockResponse(response));

    await client.accounts.updateKey("0.0.123", "oldkey");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/accounts/0.0.123/key");
    expect(call[1].method).toBe("PUT");
    expect(JSON.parse(call[1].body)).toEqual({ currentPrivateKey: "oldkey" });
  });

  it("updateMemo() calls PUT /api/v1/accounts/:id/memo", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));

    await client.accounts.updateMemo("0.0.123", "key123", "my memo");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/accounts/0.0.123/memo");
    expect(JSON.parse(call[1].body)).toEqual({ privateKey: "key123", memo: "my memo" });
  });

  it("transferFromOperator() calls POST /api/v1/accounts/transfer", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));

    await client.accounts.transferFromOperator("0.0.999", 50);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/accounts/transfer");
    expect(JSON.parse(call[1].body)).toEqual({ toAccountId: "0.0.999", amountInHbar: 50 });
  });

  it("transfer() calls POST /api/v1/accounts/:id/transfer", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "done" }));

    await client.accounts.transfer("0.0.111", "privkey", "0.0.222", 25);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/accounts/0.0.111/transfer");
    expect(JSON.parse(call[1].body)).toEqual({
      fromAccountPrivateKey: "privkey",
      toAccountId: "0.0.222",
      amountInHbar: 25,
    });
  });

  it("delete() calls DELETE /api/v1/accounts/:id with body", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "deleted" }));

    await client.accounts.delete("0.0.123", "privkey");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/accounts/0.0.123");
    expect(call[1].method).toBe("DELETE");
    expect(JSON.parse(call[1].body)).toEqual({ privateKey: "privkey" });
  });
});
