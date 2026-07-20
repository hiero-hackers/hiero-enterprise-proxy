import type { HieroProxyClient } from "../client";
import type { BlockResponse } from "./blocks.types";
import { requireId } from "./validation";

/**
 * Blocks resource — query blocks by number or hash.
 */
export class BlocksResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * List blocks (most recent first).
   */
  async list(): Promise<BlockResponse[]> {
    return this.client.get("/api/v1/blocks");
  }

  /**
   * Get a block by its sequential number.
   */
  async getByNumber(blockNumber: number): Promise<BlockResponse> {
    if (blockNumber < 0) {
      throw new Error("blockNumber must not be negative");
    }
    return this.client.get(`/api/v1/blocks/number/${blockNumber}`);
  }

  /**
   * Get a block by its EVM-compatible hash.
   */
  async getByHash(hash: string): Promise<BlockResponse> {
    const h = requireId(hash, "hash");
    return this.client.get(`/api/v1/blocks/hash/${h}`);
  }
}
