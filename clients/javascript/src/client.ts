import type { HieroProxyConfig, ResolvedConfig } from "./types";
import { NetworkError, toProxyError } from "./errors";
import { AccountsResource } from "./resources/accounts";
import { NetworkResource } from "./resources/network";
import { TokensResource } from "./resources/tokens";
import { NftsResource } from "./resources/nfts";
import { TopicsResource } from "./resources/topics";

const DEFAULT_TIMEOUT = 30_000;

/**
 * Core HTTP client for the Hiero Enterprise Proxy.
 *
 * Handles configuration, request execution, and error mapping.
 * Endpoint-specific methods are available via resource properties (accounts, network, etc).
 */
export class HieroProxyClient {
  private readonly config: ResolvedConfig;

  /** Account operations — create, query, update, delete, transfer. */
  public readonly accounts: AccountsResource;

  /** Network queries — exchange rates, fees, staking, supply. */
  public readonly network: NetworkResource;

  /** Fungible token operations — create, mint, burn, transfer, associate. */
  public readonly tokens: TokensResource;

  /** NFT operations — create types, mint, burn, transfer, query instances. */
  public readonly nfts: NftsResource;

  /** Topic operations — create, submit messages, query, update, delete. */
  public readonly topics: TopicsResource;

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

    this.accounts = new AccountsResource(this);
    this.network = new NetworkResource(this);
    this.tokens = new TokensResource(this);
    this.nfts = new NftsResource(this);
    this.topics = new TopicsResource(this);
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
  async delete<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>("DELETE", path, body);
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
        throw toProxyError(response.status, parsed);
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
