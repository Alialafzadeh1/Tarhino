import { v4 as uuidv4 } from 'uuid';

export interface PresignedUploadRequest {
  fileName: string;
  mimeType: string;
  fileSize: number;
  type: 'IMAGE' | 'VIDEO' | 'AUDIO' | 'VOICE' | 'FILE';
}

export interface PresignedUploadResponse {
  uploadUrl: string;
  publicUrl: string;
  storageKey: string;
  expiresInSeconds: number;
}

export class UploadsService {
  private publicStorageUrl: string;

  constructor() {
    this.publicStorageUrl = process.env.STORAGE_PUBLIC_URL || 'https://storage.tarhinoo.com';
  }

  generatePresignedUpload(req: PresignedUploadRequest, userId: string): PresignedUploadResponse {
    const ext = req.fileName.split('.').pop() || 'bin';
    const storageKey = `uploads/${userId}/${Date.now()}_${uuidv4()}.${ext}`;
    const uploadUrl = `${this.publicStorageUrl}/direct-upload/${storageKey}?signature=${uuidv4()}&expires=3600`;
    const publicUrl = `${this.publicStorageUrl}/${storageKey}`;

    return {
      uploadUrl,
      publicUrl,
      storageKey,
      expiresInSeconds: 3600,
    };
  }

  isConfigured(): boolean {
    return !!process.env.S3_BUCKET_NAME || !!process.env.STORAGE_PUBLIC_URL;
  }
}

export const uploadsService = new UploadsService();
