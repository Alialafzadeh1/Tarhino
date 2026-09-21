import dotenv from 'dotenv';
dotenv.config();

/**
 * Strict environment configuration and validation for Tarhi Noo Backend.
 * Fails fast in production if required secrets or connections are missing.
 */

export interface EnvConfig {
  NODE_ENV: 'development' | 'staging' | 'production' | 'test';
  PORT: number;
  HOST: string;
  DATABASE_URL: string;
  JWT_SECRET: string;
  JWT_EXPIRES_IN: string;
  JWT_REFRESH_SECRET: string;
  JWT_REFRESH_EXPIRES_IN: string;
  S3_ENDPOINT: string;
  S3_REGION: string;
  S3_BUCKET_NAME: string;
  S3_ACCESS_KEY_ID: string;
  S3_SECRET_ACCESS_KEY: string;
  STORAGE_PUBLIC_URL: string;
  GEMINI_API_KEY: string;
  OPENAI_API_KEY?: string;
  AI_DEFAULT_PROVIDER: 'gemini' | 'openai';
  FIREBASE_PROJECT_ID: string;
  FIREBASE_CLIENT_EMAIL: string;
  FIREBASE_PRIVATE_KEY: string;
  ALLOWED_ORIGINS: string[];
  MAX_UPLOAD_SIZE_BYTES: {
    IMAGE: number;
    VIDEO: number;
    AUDIO: number;
    VOICE: number;
    FILE: number;
  };
}

export function validateAndLoadEnv(): EnvConfig {
  const nodeEnv = (process.env.NODE_ENV || 'development').toLowerCase() as 'development' | 'staging' | 'production' | 'test';
  const isProduction = nodeEnv === 'production';
  const isStaging = nodeEnv === 'staging';
  const isStrict = isProduction || isStaging;

  const missing: string[] = [];

  function requireEnv(key: string, devFallback?: string): string {
    const val = process.env[key];
    if (!val || val.trim() === '') {
      if (isStrict) {
        missing.push(key);
        return '';
      }
      return devFallback || '';
    }
    return val.trim();
  }

  // Mandatory production variables - no default secrets allowed in production!
  const DATABASE_URL = requireEnv('DATABASE_URL', 'postgresql://tarhinoo_user:tarhinoo_pass@localhost:5432/tarhinoo_db?schema=public');
  const JWT_SECRET = requireEnv('JWT_SECRET', 'dev_tarhinoo_only_secret_do_not_use_in_production_abcdef123456');
  const JWT_REFRESH_SECRET = requireEnv('JWT_REFRESH_SECRET', 'dev_tarhinoo_only_refresh_secret_do_not_use_in_production_abcdef123456');

  const S3_ENDPOINT = requireEnv('S3_ENDPOINT', 'https://s3.eu-central-1.amazonaws.com');
  const S3_REGION = requireEnv('S3_REGION', 'eu-central-1');
  const S3_BUCKET_NAME = requireEnv('S3_BUCKET_NAME', 'tarhinoo-media-assets');
  const S3_ACCESS_KEY_ID = requireEnv('S3_ACCESS_KEY_ID', 'dev_storage_access_key');
  const S3_SECRET_ACCESS_KEY = requireEnv('S3_SECRET_ACCESS_KEY', 'dev_storage_secret_key');
  const STORAGE_PUBLIC_URL = process.env.STORAGE_PUBLIC_URL || 'https://storage.tarhinoo.com';

  const GEMINI_API_KEY = requireEnv('GEMINI_API_KEY', '');
  const FIREBASE_PROJECT_ID = requireEnv('FIREBASE_PROJECT_ID', '');
  const FIREBASE_CLIENT_EMAIL = requireEnv('FIREBASE_CLIENT_EMAIL', '');
  const FIREBASE_PRIVATE_KEY = requireEnv('FIREBASE_PRIVATE_KEY', '');

  if (isStrict && missing.length > 0) {
    const msg = `[CRITICAL FATAL] Missing required production environment variables: ${missing.join(', ')}. Server startup aborted.`;
    console.error(msg);
    throw new Error(msg);
  }

  // Parse allowed origins with strict security
  const rawOrigins = process.env.ALLOWED_ORIGINS || (nodeEnv === 'development' || nodeEnv === 'test' ? '*' : '');
  const allowedOrigins = rawOrigins
    .split(',')
    .map((o) => o.trim())
    .filter((o) => o.length > 0);

  if (nodeEnv === 'production') {
    if (allowedOrigins.length === 0 || allowedOrigins.includes('*')) {
      const corsErr = '[CRITICAL FATAL] In production (NODE_ENV=production), ALLOWED_ORIGINS must be an explicit whitelist of domains and cannot be empty or "*". Server startup aborted.';
      console.error(corsErr);
      throw new Error(corsErr);
    }
  } else if (isStaging) {
    if (allowedOrigins.length === 0 || allowedOrigins.includes('*')) {
      const corsErr = '[CRITICAL FATAL] In staging (NODE_ENV=staging), ALLOWED_ORIGINS must be an explicit whitelist of staging domains. Server startup aborted.';
      console.error(corsErr);
      throw new Error(corsErr);
    }
  }

  return {
    NODE_ENV: nodeEnv,
    PORT: parseInt(process.env.PORT || '8080', 10),
    HOST: process.env.HOST || '0.0.0.0',
    DATABASE_URL,
    JWT_SECRET,
    JWT_EXPIRES_IN: process.env.JWT_EXPIRES_IN || '15m',
    JWT_REFRESH_SECRET,
    JWT_REFRESH_EXPIRES_IN: process.env.JWT_REFRESH_EXPIRES_IN || '30d',
    S3_ENDPOINT,
    S3_REGION,
    S3_BUCKET_NAME,
    S3_ACCESS_KEY_ID,
    S3_SECRET_ACCESS_KEY,
    STORAGE_PUBLIC_URL,
    GEMINI_API_KEY,
    OPENAI_API_KEY: process.env.OPENAI_API_KEY,
    AI_DEFAULT_PROVIDER: (process.env.AI_DEFAULT_PROVIDER || 'gemini') as 'gemini' | 'openai',
    FIREBASE_PROJECT_ID,
    FIREBASE_CLIENT_EMAIL,
    FIREBASE_PRIVATE_KEY: FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
    ALLOWED_ORIGINS: allowedOrigins.length > 0 ? allowedOrigins : ['*'],
    MAX_UPLOAD_SIZE_BYTES: {
      IMAGE: parseInt(process.env.MAX_UPLOAD_IMAGE_MB || '15', 10) * 1024 * 1024,
      VIDEO: parseInt(process.env.MAX_UPLOAD_VIDEO_MB || '100', 10) * 1024 * 1024,
      AUDIO: parseInt(process.env.MAX_UPLOAD_AUDIO_MB || '25', 10) * 1024 * 1024,
      VOICE: parseInt(process.env.MAX_UPLOAD_VOICE_MB || '25', 10) * 1024 * 1024,
      FILE: parseInt(process.env.MAX_UPLOAD_FILE_MB || '50', 10) * 1024 * 1024,
    },
  };
}

export const env = validateAndLoadEnv();
