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

describe("FilesResource", () => {
  let client: HieroProxyClient;

  beforeEach(() => {
    client = new HieroProxyClient({ baseUrl: "http://localhost:8080" });
    mockFetch.mockReset();
  });

  it("create() calls POST /api/v1/files", async () => {
    const response = { fileId: "0.0.800" };
    mockFetch.mockResolvedValue(mockResponse(response, 201));

    const result = await client.files.create("SGVsbG8=", 1700000000);
    expect(result).toEqual(response);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/files");
    expect(call[1].method).toBe("POST");
    expect(JSON.parse(call[1].body)).toEqual({ contents: "SGVsbG8=", expirationTimeEpochSecond: 1700000000 });
  });

  it("getContents() calls GET /api/v1/files/:id/contents", async () => {
    const response = { fileId: "0.0.800", contents: "SGVsbG8=" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.files.getContents("0.0.800");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/files/0.0.800/contents",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("getInfo() calls GET /api/v1/files/:id", async () => {
    const response = { fileId: "0.0.800", size: 1024, deleted: false, expirationTimeEpochSecond: 1700000000 };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.files.getInfo("0.0.800");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/files/0.0.800",
      expect.objectContaining({ method: "GET" })
    );
  });

  it("updateContents() calls PUT /api/v1/files/:id/contents", async () => {
    const response = { message: "File contents updated" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.files.updateContents("0.0.800", "V29ybGQ=");
    expect(result).toEqual(response);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/files/0.0.800/contents");
    expect(call[1].method).toBe("PUT");
    expect(JSON.parse(call[1].body)).toEqual({ contents: "V29ybGQ=" });
  });

  it("updateExpiration() calls PUT /api/v1/files/:id/expiration", async () => {
    const response = { message: "Expiration updated" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.files.updateExpiration("0.0.800", 1800000000);
    expect(result).toEqual(response);
    const call = mockFetch.mock.calls[0];
    expect(call[0]).toBe("http://localhost:8080/api/v1/files/0.0.800/expiration");
    expect(call[1].method).toBe("PUT");
    expect(JSON.parse(call[1].body)).toEqual({ expirationTimeEpochSecond: 1800000000 });
  });

  it("delete() calls DELETE /api/v1/files/:id", async () => {
    const response = { message: "File deleted" };
    mockFetch.mockResolvedValue(mockResponse(response));

    const result = await client.files.delete("0.0.800");
    expect(result).toEqual(response);
    expect(mockFetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/files/0.0.800",
      expect.objectContaining({ method: "DELETE" })
    );
  });

  it("getContents() throws if fileId is empty", async () => {
    await expect(client.files.getContents("")).rejects.toThrow("fileId is required");
  });

  it("delete() throws if fileId is empty", async () => {
    await expect(client.files.delete("  ")).rejects.toThrow("fileId is required");
  });
});
