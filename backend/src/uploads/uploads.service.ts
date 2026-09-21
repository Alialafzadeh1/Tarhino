import { S3Client, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { v4 as uuidv4 } from 'uuid';
import { env } from '../config/env';

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
  private s3Client: S3Client | null = null;
  private bucket: string;

  constructor() {
    this.bucket = env.S3_BUCKET_NAME;

    if (env.S3_ACCESS_KEY_ID && env.S3_SECRET_ACCESS_KEY) {
      try {
        this.s3Client = new S3Client({
          region: env.S3_REGION,
          endpoint: env.S3_ENDPOINT,
          credentials: {
            accessKeyId: env.S3_ACCESS_KEY_ID,
            secretAccessKey: env.S3_SECRET_ACCESS_KEY,
          },
          forcePathStyle: true,
        });
      } catch (err) {
        console.error('[UploadsService] Failed to initialize real S3 Client', err);
      }
    }
  }

  async generatePresignedUpload(req: PresignedUploadRequest, userId: string): Promise<PresignedUploadResponse> {
    // 1. Validate file size according to configured limits
    const maxLimit = env.MAX_UPLOAD_SIZE_BYTES[req.type] || env.MAX_UPLOAD_SIZE_BYTES.FILE;
    if (req.fileSize > maxLimit) {
      throw new Error(`حجم فایل انتخابی بیش از سقف مجاز (${Math.round(maxLimit / (1024 * 1024))}MB) است.`);
    }

    // 2. Validate MIME types
    const allowedMimePrefixes: Record<string, string[]> = {
      IMAGE: ['image/'],
      VIDEO: ['video/'],
      AUDIO: ['audio/'],
      VOICE: ['audio/'],
      FILE: ['application/', 'text/', 'image/', 'video/', 'audio/'],
    };

    const allowed = allowedMimePrefixes[req.type] || ['application/'];
    const isMimeValid = allowed.some((prefix) => req.mimeType.startsWith(prefix));
    if (!isMimeValid) {
      throw new Error(`نوع فایل (${req.mimeType}) با نوع انتخاب شده سازگار نیست.`);
    }

    const ext = req.fileName.split('.').pop()?.toLowerCase() || 'bin';
    const storageKey = `uploads/${userId}/${Date.now()}_${uuidv4()}.${ext}`;
    const expiresIn = 3600; // 1 hour

    if (!this.s3Client) {
      if (env.NODE_ENV === 'production') {
        throw new Error('STORAGE_UNAVAILABLE: Object storage is not initialized on the production server.');
      }
      // Dev mode local mock S3 URL only
      return {
        uploadUrl: `${env.STORAGE_PUBLIC_URL}/dev-mock-upload/${storageKey}`,
        publicUrl: `${env.STORAGE_PUBLIC_URL}/${storageKey}`,
        storageKey,
        expiresInSeconds: expiresIn,
      };
    }

    const command = new PutObjectCommand({
      Bucket: this.bucket,
      Key: storageKey,
      ContentType: req.mimeType,
      Metadata: {
        userId,
        originalName: encodeURIComponent(req.fileName),
      },
    });

    const uploadUrl = await getSignedUrl(this.s3Client, command, { expiresIn });
    const publicUrl = `${env.STORAGE_PUBLIC_URL.replace(/\/$/, '')}/${storageKey}`;

    return {
      uploadUrl,
      publicUrl,
      storageKey,
      expiresInSeconds: expiresIn,
    };
  }

  async generatePresignedDownloadUrl(storageKey: string, expiresIn = 3600): Promise<string> {
    if (!this.s3Client) {
      return `${env.STORAGE_PUBLIC_URL}/${storageKey}`;
    }

    const command = new GetObjectCommand({
      Bucket: this.bucket,
      Key: storageKey,
    });

    return getSignedUrl(this.s3Client, command, { expiresIn });
  }

  isConfigured(): boolean {
    return this.s3Client !== null;
  }
}

export const uploadsService = new UploadsService();
