import type { ProxyErrorBody } from "./types";

/**
 * Base error class for all Hiero Proxy SDK errors.
 */
export class HieroProxyError extends Error {
  public readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "HieroProxyError";
    this.status = status;
  }
}

/**
 * Thrown when the proxy returns a 4xx client error.
 */
export class ClientError extends HieroProxyError {
  constructor(message: string, status: number) {
    super(message, status);
    this.name = "ClientError";
  }
}

/**
 * Thrown when the proxy returns a 5xx server error.
 */
export class ServerError extends HieroProxyError {
  constructor(message: string, status: number) {
    super(message, status);
    this.name = "ServerError";
  }
}

/**
 * Thrown when a network/connection error occurs (proxy unreachable, timeout, etc).
 */
export class NetworkError extends HieroProxyError {
  constructor(message: string) {
    super(message, 0);
    this.name = "NetworkError";
  }
}

/**
 * Parse a proxy error response body into the appropriate error class.
 */
export function toProxyError(status: number, body: ProxyErrorBody | string): HieroProxyError {
  const message = typeof body === "string" ? body : body.message;

  if (status >= 500) {
    return new ServerError(message, status);
  }
  return new ClientError(message, status);
}
