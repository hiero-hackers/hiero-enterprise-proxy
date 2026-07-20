import type { HieroProxyConfig, ResolvedConfig } from "./types";
import { NetworkError, toProxyError } from "./errors";

const DEFAULT_TIMEOUT = 30_000;

/**
 * Core HTTP client for the Hiero Enterprise Proxy.
 *
 * Handles configuration, request execution, and error mapping.
 * Endpoint-specific methods are added in resource modules (accounts, tokens, etc).
 */
export class HieroProxyClient {
  private readonly config: ResolvedConfig;

  constructor(config: HieroProxyConfig) {
    const baseUrl = config.baseUrl.replace(/\/+$/, "");
    this.config = {
      baseUrl,
      timeout: config.timeout ?? DEFAULT_TIMEOUT,
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        ...config.headers,
      },
    };
  }

  /**
   * Get the resolved base URL.
   */
  get baseUrl(): string {
    return this.config.baseUrl;
  }

  /**
   * Execute a GET request against the proxy.
   */
  async get<T>(path: string): Promise<T> {
    return this.request<T>("GET", path);
  }

  /**
   * Execute a POST request against the proxy.
   */
  async post<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>("POST", path, body);
  }

  /**
   * Execute a PUT request against the proxy.
   */
  async put<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>("PUT", path, body);
  }

  /**
   * Execute a DELETE request against the proxy.
   */
  async delete<T>(path: string): Promise<T> {
    return this.request<T>("DELETE", path);
  }

  /**
   * Core request method with timeout, error handling, and response parsing.
   */
  private async request<T>(method: string, path: string, body?: unknown): Promise<T> {
    const url = `${this.config.baseUrl}${path}`;
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.config.timeout);

    try {
      const response = await fetch(url, {
        method,
        headers: this.config.headers,
        body: body ? JSON.stringify(body) : undefined,
        signal: controller.signal,
      });

      if (!response.ok) {
        const errorBody = await response.text();
        let parsed: unknown;
        try {
          parsed = JSON.parse(errorBody);
        } catch {
          parsed = errorBody;
        }
        throw toProxyError(response.status, parsed as string);
      }

      const text = await response.text();
      if (!text) return undefined as T;
      return JSON.parse(text) as T;
    } catch (error: unknown) {
      if (error instanceof Error && error.name === "AbortError") {
        throw new NetworkError(`Request timed out after ${this.config.timeout}ms: ${method} ${path}`);
      }
      if (error instanceof TypeError) {
        throw new NetworkError(`Connection failed: ${error.message}`);
      }
      throw error;
    } finally {
      clearTimeout(timeoutId);
    }
  }
}
