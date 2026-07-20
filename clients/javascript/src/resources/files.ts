import type { HieroProxyClient } from "../client";
import type {
  FileCreateRequest,
  FileUpdateContentsRequest,
  FileUpdateExpirationRequest,
  FileCreatedResponse,
  FileContentsResponse,
  FileInfoResponse,
} from "./files.types";
import type { SuccessResponse } from "./types";
import { requireId } from "./validation";

/**
 * Files resource — create, read, update, and delete files on the Hiero File Service.
 */
export class FilesResource {
  constructor(private readonly client: HieroProxyClient) {}

  /**
   * Create a file on the ledger.
   * @param contents Base64-encoded file contents.
   * @param expirationTimeEpochSecond Optional expiration timestamp.
   */
  async create(contents: string, expirationTimeEpochSecond?: number): Promise<FileCreatedResponse> {
    const body: FileCreateRequest = { contents, expirationTimeEpochSecond };
    return this.client.post("/api/v1/files", body);
  }

  /**
   * Get file contents (Base64-encoded).
   */
  async getContents(fileId: string): Promise<FileContentsResponse> {
    const id = requireId(fileId, "fileId");
    return this.client.get(`/api/v1/files/${id}/contents`);
  }

  /**
   * Get file info (size, deleted status, expiration).
   */
  async getInfo(fileId: string): Promise<FileInfoResponse> {
    const id = requireId(fileId, "fileId");
    return this.client.get(`/api/v1/files/${id}`);
  }

  /**
   * Update file contents.
   * @param contents New Base64-encoded contents.
   */
  async updateContents(fileId: string, contents: string): Promise<SuccessResponse> {
    const id = requireId(fileId, "fileId");
    const body: FileUpdateContentsRequest = { contents };
    return this.client.put(`/api/v1/files/${id}/contents`, body);
  }

  /**
   * Update file expiration time.
   */
  async updateExpiration(fileId: string, expirationTimeEpochSecond: number): Promise<SuccessResponse> {
    const id = requireId(fileId, "fileId");
    const body: FileUpdateExpirationRequest = { expirationTimeEpochSecond };
    return this.client.put(`/api/v1/files/${id}/expiration`, body);
  }

  /**
   * Delete a file.
   */
  async delete(fileId: string): Promise<SuccessResponse> {
    const id = requireId(fileId, "fileId");
    return this.client.delete(`/api/v1/files/${id}`);
  }
}
