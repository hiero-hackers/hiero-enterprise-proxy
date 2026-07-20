/**
 * Configuration options for the Hiero Proxy SDK client.
 */
export interface HieroProxyConfig {
  /**
   * Base URL of the running Hiero Enterprise Proxy instance.
   * @example "http://localhost:8080"
   */
  baseUrl: string;

  /**
   * Request timeout in milliseconds (default: 30000).
   */
  timeout?: number;

  /**
   * Custom headers to include with every request.
   */
  headers?: Record<string, string>;
}

/**
 * Internal normalized config with defaults applied.
 */
export interface ResolvedConfig {
  baseUrl: string;
  timeout: number;
  headers: Record<string, string>;
}

/**
 * Standard error response shape from the proxy.
 */
export interface ProxyErrorBody {
  status: number;
  message: string;
}
