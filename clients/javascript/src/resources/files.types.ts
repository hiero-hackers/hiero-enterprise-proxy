export interface FileCreateRequest {
  contents: string;
  expirationTimeEpochSecond?: number;
}

export interface FileUpdateContentsRequest {
  contents: string;
}

export interface FileUpdateExpirationRequest {
  expirationTimeEpochSecond: number;
}

export interface FileCreatedResponse {
  fileId: string;
}

export interface FileContentsResponse {
  fileId: string;
  contents: string;
}

export interface FileInfoResponse {
  fileId: string;
  size: number;
  deleted: boolean;
  expirationTimeEpochSecond: number;
}
