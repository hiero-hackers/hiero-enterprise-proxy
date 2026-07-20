import type { HieroProxyClient } from "../client";
import type {
  TopicCreatedResponse,
  TopicResponse,
  TopicKeyRotationResponse,
  TopicUpdatedResponse,
  TopicMessageResponse,
  SubmitMessageRequest,
  SubmitBinaryMessageRequest,
  CreateTopicWithAdminKeyRequest,
  CreatePrivateTopicWithAdminKeyRequest,
  UpdateTopicMemoRequest,
  UpdateTopicKeyRequest,
  UpdateTopicRequest,
  DeleteTopicRequest,
} from "./tokens-topics-types";
import type { SuccessResponse } from "./types";

/** Validates that an ID is non-empty and URL-encodes it. */
function requireId(value: string, paramName: string): string {
  if (!value || typeof value !== "string" || !value.trim()) {
    throw new Error(`${paramName} is required and must be a non-empty string`);
  }
  return encodeURIComponent(value.trim());
}

/**
 * Topics resource — create, query, update, delete topics; submit and read messages.
 */
export class TopicsResource {
  constructor(private readonly client: HieroProxyClient) {}

  // ─── Creation ─────────────────────────────────────────────────────────────

  /**
   * Create a public topic (operator as admin key).
   */
  async create(memo?: string): Promise<TopicCreatedResponse> {
    const body = memo ? { memo } : undefined;
    return this.client.post("/api/v1/topics", body);
  }

  /**
   * Create a private topic (generates submit key, operator as admin key).
   */
  async createPrivate(memo?: string): Promise<TopicCreatedResponse> {
    const body = memo ? { memo } : undefined;
    return this.client.post("/api/v1/topics/private", body);
  }

  /**
   * Create a public topic with a custom admin key.
   */
  async createWithAdminKey(adminPrivateKey: string, memo?: string): Promise<TopicCreatedResponse> {
    const body: CreateTopicWithAdminKeyRequest = { adminPrivateKey, memo };
    return this.client.post("/api/v1/topics/with-admin-key", body);
  }

  /**
   * Create a private topic with a custom admin key (generates submit key).
   */
  async createPrivateWithAdminKey(adminPrivateKey: string, memo?: string): Promise<TopicCreatedResponse> {
    const body: CreatePrivateTopicWithAdminKeyRequest = { adminPrivateKey, memo };
    return this.client.post("/api/v1/topics/private/with-admin-key", body);
  }

  // ─── Queries ──────────────────────────────────────────────────────────────

  /**
   * Get topic info from the mirror node.
   */
  async getInfo(topicId: string): Promise<TopicResponse> {
    const id = requireId(topicId, "topicId");
    return this.client.get(`/api/v1/topics/${id}`);
  }

  // ─── Updates ──────────────────────────────────────────────────────────────

  /**
   * Update topic memo.
   */
  async updateMemo(topicId: string, adminPrivateKey: string, memo: string): Promise<SuccessResponse> {
    const id = requireId(topicId, "topicId");
    const body: UpdateTopicMemoRequest = { adminPrivateKey, memo };
    return this.client.put(`/api/v1/topics/${id}/memo`, body);
  }

  /**
   * Rotate the admin key of a topic. Returns the new key pair.
   */
  async rotateAdminKey(topicId: string, adminPrivateKey: string): Promise<TopicKeyRotationResponse> {
    const id = requireId(topicId, "topicId");
    const body: UpdateTopicKeyRequest = { adminPrivateKey };
    return this.client.put(`/api/v1/topics/${id}/admin-key`, body);
  }

  /**
   * Rotate the submit key of a private topic. Returns the new key pair.
   */
  async rotateSubmitKey(topicId: string, adminPrivateKey: string): Promise<TopicKeyRotationResponse> {
    const id = requireId(topicId, "topicId");
    const body: UpdateTopicKeyRequest = { adminPrivateKey };
    return this.client.put(`/api/v1/topics/${id}/submit-key`, body);
  }

  /**
   * Atomically update admin key, submit key, and memo.
   */
  async update(topicId: string, currentAdminPrivateKey: string, memo: string): Promise<TopicUpdatedResponse> {
    const id = requireId(topicId, "topicId");
    const body: UpdateTopicRequest = { currentAdminPrivateKey, memo };
    return this.client.put(`/api/v1/topics/${id}`, body);
  }

  // ─── Deletion ─────────────────────────────────────────────────────────────

  /**
   * Delete a topic.
   */
  async delete(topicId: string, adminPrivateKey: string): Promise<SuccessResponse> {
    const id = requireId(topicId, "topicId");
    const body: DeleteTopicRequest = { adminPrivateKey };
    return this.client.delete(`/api/v1/topics/${id}`, body);
  }

  // ─── Messages ─────────────────────────────────────────────────────────────

  /**
   * Submit a text message to a topic.
   * For private topics, provide the submitKey.
   */
  async submitMessage(topicId: string, message: string, submitKey?: string): Promise<SuccessResponse> {
    const id = requireId(topicId, "topicId");
    const body: SubmitMessageRequest = { message, submitKey };
    return this.client.post(`/api/v1/topics/${id}/messages`, body);
  }

  /**
   * Submit a binary message (Base64-encoded) to a topic.
   * For private topics, provide the submitKey.
   */
  async submitBinaryMessage(topicId: string, messageBase64: string, submitKey?: string): Promise<SuccessResponse> {
    const id = requireId(topicId, "topicId");
    const body: SubmitBinaryMessageRequest = { messageBase64, submitKey };
    return this.client.post(`/api/v1/topics/${id}/messages/binary`, body);
  }

  /**
   * Get all messages for a topic.
   */
  async getMessages(topicId: string): Promise<TopicMessageResponse[]> {
    const id = requireId(topicId, "topicId");
    return this.client.get(`/api/v1/topics/${id}/messages`);
  }

  /**
   * Get a specific message by sequence number.
   */
  async getMessage(topicId: string, sequenceNumber: number): Promise<TopicMessageResponse> {
    const id = requireId(topicId, "topicId");
    return this.client.get(`/api/v1/topics/${id}/messages/${sequenceNumber}`);
  }
}
