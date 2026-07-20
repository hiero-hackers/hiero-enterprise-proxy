import type { HieroProxyClient } from "../client";
import type {
  ContractCreateRequest,
  ContractCallRequest,
  ContractResponse,
  ContractCallResultResponse,
} from "./contracts.types";
import { requireId } from "./validation";

/**
 * Smart Contracts resource — deploy, call, and query contracts.
 */
export class ContractsResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * Deploy a smart contract.
   * @param bytecode Hex-encoded contract bytecode.
   */
  async deploy(bytecode: string): Promise<ContractResponse> {
    const body: ContractCreateRequest = { bytecode };
    return this.client.post("/api/v1/contracts", body);
  }

  /**
   * Call a smart contract function.
   */
  async call(contractId: string, functionName: string): Promise<ContractCallResultResponse> {
    const id = requireId(contractId, "contractId");
    const body: ContractCallRequest = { functionName };
    return this.client.post(`/api/v1/contracts/${id}/call`, body);
  }

  /**
   * List all contracts from the mirror node.
   */
  async list(): Promise<ContractResponse[]> {
    return this.client.get("/api/v1/contracts");
  }

  /**
   * Get contract info by ID.
   */
  async getInfo(contractId: string): Promise<ContractResponse> {
    const id = requireId(contractId, "contractId");
    return this.client.get(`/api/v1/contracts/${id}`);
  }
}
