import type { HieroProxyClient } from "../client";
import type {
  ExchangeRatesResponse,
  NetworkFeeResponse,
  NetworkStakeResponse,
  NetworkSuppliesResponse,
} from "./types";

/**
 * Network resource — read-only queries for exchange rates, fees, staking, and supply.
 */
export class NetworkResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * Get current and next HBAR/USD exchange rates.
   */
  async getExchangeRates(): Promise<ExchangeRatesResponse> {
    return this.client.get("/api/v1/network/exchange-rates");
  }

  /**
   * Get EVM gas fees for smart contract operations.
   */
  async getFees(): Promise<NetworkFeeResponse[]> {
    return this.client.get("/api/v1/network/fees");
  }

  /**
   * Get network staking configuration and statistics.
   */
  async getStake(): Promise<NetworkStakeResponse> {
    return this.client.get("/api/v1/network/stake");
  }

  /**
   * Get HBAR supply figures (released and total).
   */
  async getSupplies(): Promise<NetworkSuppliesResponse> {
    return this.client.get("/api/v1/network/supplies");
  }
}
