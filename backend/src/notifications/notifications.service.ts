import { prisma } from '../database/prisma';

export interface PushNotificationPayload {
  userId: string;
  title: string;
  body: string;
  type: string;
  data?: Record<string, any>;
}

export class NotificationService {
  async registerDeviceToken(userId: string, token: string, platform = 'android', appVersion?: string) {
    return prisma.deviceToken.upsert({
      where: { token },
      update: { userId, platform, appVersion, lastSeen: new Date() },
      create: { userId, token, platform, appVersion },
    });
  }

  async sendPushNotification(payload: PushNotificationPayload) {
    // 1. Store notification in database
    await prisma.notification.create({
      data: {
        userId: payload.userId,
        title: payload.title,
        body: payload.body,
        type: payload.type,
        data: payload.data ? JSON.stringify(payload.data) : undefined,
      },
    });

    // 2. Fetch device tokens for FCM dispatch
    const deviceTokens = await prisma.deviceToken.findMany({
      where: { userId: payload.userId },
      select: { token: true },
    });

    // FCM dispatch logic
    if (process.env.FIREBASE_PROJECT_ID && deviceTokens.length > 0) {
      // In production with service account, dispatch via FCM REST v1 or firebase-admin
      // tokens.forEach(t => sendFcm(t.token, payload))
    }
  }
}

export const notificationService = new NotificationService();
