import { describe, it, expect } from "vitest";
import { HieroProxyClient } from "../src/client";
import { HieroProxyError, ClientError, ServerError, NetworkError } from "../src/errors";

describe("HieroProxyClient", () => {
  it("should create a client with config", () => {
    const client = new HieroProxyClient({
      baseUrl: "http://localhost:8080",
    });
    expect(client.baseUrl).toBe("http://localhost:8080");
  });

  it("should strip trailing slashes from baseUrl", () => {
    const client = new HieroProxyClient({
      baseUrl: "http://localhost:8080///",
    });
    expect(client.baseUrl).toBe("http://localhost:8080");
  });

  it("should accept custom timeout and headers", () => {
    const client = new HieroProxyClient({
      baseUrl: "http://localhost:8080",
      timeout: 5000,
      headers: { "X-Custom": "value" },
    });
    expect(client.baseUrl).toBe("http://localhost:8080");
  });
});

describe("Errors", () => {
  it("HieroProxyError has status and message", () => {
    const err = new HieroProxyError("test error", 500);
    expect(err.message).toBe("test error");
    expect(err.status).toBe(500);
    expect(err.name).toBe("HieroProxyError");
    expect(err).toBeInstanceOf(Error);
  });

  it("ClientError is a HieroProxyError", () => {
    const err = new ClientError("not found", 404);
    expect(err).toBeInstanceOf(HieroProxyError);
    expect(err.name).toBe("ClientError");
    expect(err.status).toBe(404);
  });

  it("ServerError is a HieroProxyError", () => {
    const err = new ServerError("internal error", 500);
    expect(err).toBeInstanceOf(HieroProxyError);
    expect(err.name).toBe("ServerError");
    expect(err.status).toBe(500);
  });

  it("NetworkError has status 0", () => {
    const err = new NetworkError("connection refused");
    expect(err).toBeInstanceOf(HieroProxyError);
    expect(err.name).toBe("NetworkError");
    expect(err.status).toBe(0);
  });
});
