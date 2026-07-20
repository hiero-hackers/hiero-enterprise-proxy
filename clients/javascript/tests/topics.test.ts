import { describe, it, expect, vi, beforeEach } from "vitest";
import { HieroProxyClient } from "../src/client";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

function mockResponse(body: unknown, status = 200) {
  return { ok: status >= 200 && status < 300, status, text: () => Promise.resolve(JSON.stringify(body)) };
}

describe("TopicsResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("create() calls POST /api/v1/topics", async () => {
    mockFetch.mockResolvedValue(mockResponse({ topicId: "0.0.55001", submitPrivateKey: null, submitPublicKey: null }, 201));
    const result = await client.topics.create("my memo");
    expect(result.topicId).toBe("0.0.55001");
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ memo: "my memo" });
  });

  it("createPrivate() calls POST /api/v1/topics/private", async () => {
    mockFetch.mockResolvedValue(mockResponse({ topicId: "0.0.55002", submitPrivateKey: "key", submitPublicKey: "pub" }, 201));
    const result = await client.topics.createPrivate();
    expect(result.submitPrivateKey).toBe("key");
    expect(mockFetch.mock.calls[0][0]).toBe("http://localhost:8080/api/v1/topics/private");
  });

  it("createWithAdminKey() calls POST /api/v1/topics/with-admin-key", async () => {
    mockFetch.mockResolvedValue(mockResponse({ topicId: "0.0.55003" }, 201));
    await client.topics.createWithAdminKey("adminkey", "memo");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/topics/with-admin-key");
    expect(JSON.parse(call[1].body)).toEqual({ adminPrivateKey: "adminkey", memo: "memo" });
  });

  it("getInfo() calls GET /api/v1/topics/:id", async () => {
    mockFetch.mockResolvedValue(mockResponse({ topicId: "0.0.55001", memo: "test" }));
    const result = await client.topics.getInfo("0.0.55001");
    expect(result.memo).toBe("test");
  });

  it("submitMessage() calls POST /api/v1/topics/:id/messages", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "submitted" }));
    await client.topics.submitMessage("0.0.55001", "Hello!", "submitkey");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/topics/0.0.55001/messages");
    expect(JSON.parse(call[1].body)).toEqual({ message: "Hello!", submitKey: "submitkey" });
  });

  it("submitBinaryMessage() sends messageBase64", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "submitted" }));
    await client.topics.submitBinaryMessage("0.0.55001", "SGVsbG8=");
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toContain("/messages/binary");
    expect(JSON.parse(call[1].body).messageBase64).toBe("SGVsbG8=");
  });

  it("getMessages() calls GET /api/v1/topics/:id/messages", async () => {
    mockFetch.mockResolvedValue(mockResponse([]));
    await client.topics.getMessages("0.0.55001");
    expect(mockFetch.mock.calls[0][0]).toBe("http://localhost:8080/api/v1/topics/0.0.55001/messages");
    expect(mockFetch.mock.calls[0][1].method).toBe("GET");
  });

  it("getMessage() calls GET /api/v1/topics/:id/messages/:seq", async () => {
    mockFetch.mockResolvedValue(mockResponse({ sequenceNumber: 42 }));
    const result = await client.topics.getMessage("0.0.55001", 42);
    expect(result.sequenceNumber).toBe(42);
    expect(mockFetch.mock.calls[0][0]).toContain("/messages/42");
  });

  it("delete() calls DELETE /api/v1/topics/:id with body", async () => {
    mockFetch.mockResolvedValue(mockResponse({ message: "deleted" }));
    await client.topics.delete("0.0.55001", "adminkey");
    const call = mockFetch.mock.calls[0];
    expect(call[1].method).toBe("DELETE");
    expect(JSON.parse(call[1].body)).toEqual({ adminPrivateKey: "adminkey" });
  });

  it("rejects empty topicId", async () => {
    await expect(client.topics.getInfo("")).rejects.toThrow("topicId is required");
  });
});
