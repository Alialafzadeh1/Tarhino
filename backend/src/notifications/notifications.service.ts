import * as admin from 'firebase-admin';
import { App, cert } from 'firebase-admin/app';
import { getMessaging, MulticastMessage } from 'firebase-admin/messaging';
import { prisma } from '../database/prisma';
import { env } from '../config/env';

export interface PushNotificationPayload {
  userId: string;
  title: string;
  body: string;
  type: 'NEW_MESSAGE' | 'MENTION' | 'GROUP_INVITE' | 'CHANNEL_POST' | 'REACTION' | 'AI_RESULT' | 'SYSTEM';
  data?: Record<string, any>;
}

export class NotificationService {
  private fcmApp: App | null = null;

  constructor() {
    if (env.FIREBASE_PROJECT_ID && env.FIREBASE_CLIENT_EMAIL && env.FIREBASE_PRIVATE_KEY) {
      try {
        this.fcmApp = admin.initializeApp(
          {
            credential: cert({
              projectId: env.FIREBASE_PROJECT_ID,
              clientEmail: env.FIREBASE_CLIENT_EMAIL,
              privateKey: env.FIREBASE_PRIVATE_KEY,
            }),
          },
          'tarhinoo-fcm'
        );
        console.log('[NotificationService] Firebase Admin SDK initialized successfully.');
      } catch (err) {
        console.error('[NotificationService] Failed to initialize Firebase Admin SDK:', err);
      }
    }
  }

  async registerDeviceToken(userId: string, token: string, platform = 'android', appVersion?: string) {
    return prisma.deviceToken.upsert({
      where: { token },
      update: { userId, platform, appVersion, lastSeen: new Date() },
      create: { userId, token, platform, appVersion },
    });
  }

  async unregisterDeviceToken(token: string) {
    return prisma.deviceToken.deleteMany({ where: { token } });
  }

  async sendPushNotification(payload: PushNotificationPayload) {
    // 1. Store persistent notification in DB for in-app center
    try {
      await prisma.notification.create({
        data: {
          userId: payload.userId,
          title: payload.title,
          body: payload.body,
          type: payload.type,
          data: payload.data ? payload.data : undefined,
        },
      });
    } catch (err) {
      console.error('[NotificationService] Failed to record notification in database:', err);
    }

    // 2. Fetch device tokens for the user
    const deviceTokens = await prisma.deviceToken.findMany({
      where: { userId: payload.userId },
      select: { token: true },
    });

    if (deviceTokens.length === 0) return;

    if (!this.fcmApp) {
      if (env.NODE_ENV === 'development') {
        console.log(`[NotificationService:DEV] Push notification dispatched for user ${payload.userId}: "${payload.title}"`);
      }
      return;
    }

    // 3. Real FCM multicast dispatch via Firebase Admin
    const tokens = deviceTokens.map((d) => d.token);
    const message: MulticastMessage = {
      tokens,
      notification: {
        title: payload.title,
        body: payload.body,
      },
      data: {
        type: payload.type,
        ...(payload.data
          ? Object.fromEntries(Object.entries(payload.data).map(([k, v]) => [k, String(v)]))
          : {}),
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'tarhinoo_messenger_channel',
          sound: 'default',
        },
      },
    };

    try {
      const messaging = getMessaging(this.fcmApp);
      const response = await messaging.sendEachForMulticast(message);
      if (response.failureCount > 0) {
        // Clean up invalid or stale tokens
        response.responses.forEach((resp: any, idx: number) => {
          if (!resp.success) {
            const errCode = resp.error?.code;
            if (
              errCode === 'messaging/invalid-registration-token' ||
              errCode === 'messaging/registration-token-not-registered'
            ) {
              const badToken = tokens[idx];
              prisma.deviceToken.deleteMany({ where: { token: badToken } }).catch(() => {});
            }
          }
        });
      }
    } catch (err) {
      console.error('[NotificationService] Failed to send multicast FCM:', err);
    }
  }
}

export const notificationService = new NotificationService();
