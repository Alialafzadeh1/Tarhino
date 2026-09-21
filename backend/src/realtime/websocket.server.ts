import { Server as HttpServer } from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import jwt from 'jsonwebtoken';
import { prisma } from '../database/prisma';
import { env } from '../config/env';

interface AuthenticatedSocket extends WebSocket {
  userId?: string;
  isAlive?: boolean;
  subscriptions?: Set<string>;
  lastTypingTimestamp?: number;
}

export class RealtimeServer {
  private wss: WebSocketServer;
  private clients: Map<string, Set<AuthenticatedSocket>> = new Map();
  private heartbeatInterval?: NodeJS.Timeout;

  constructor(server: HttpServer) {
    this.wss = new WebSocketServer({
      server,
      path: '/realtime',
      maxPayload: 64 * 1024, // 64KB max payload security limit
    });
    this.init();
  }

  private init() {
    this.wss.on('connection', (ws: AuthenticatedSocket, req) => {
      ws.isAlive = true;
      ws.subscriptions = new Set<string>();

      // Strict WebSocket Security: Authenticate only via Bearer JWT (header or ?token= URL query)
      const url = new URL(req.url || '', `http://${req.headers.host || 'localhost'}`);
      const rawToken = url.searchParams.get('token') || (req.headers['authorization'] || '').replace(/^Bearer\s+/i, '');

      let authenticatedUserId: string | null = null;
      if (rawToken) {
        try {
          const payload = jwt.verify(rawToken, env.JWT_SECRET) as { userId: string };
          authenticatedUserId = payload.userId;
        } catch {
          // Token invalid
        }
      }

      // DO NOT trust X-User-Id header alone!
      if (authenticatedUserId) {
        this.registerClient(authenticatedUserId, ws);
        ws.send(JSON.stringify({ type: 'CONNECTED', userId: authenticatedUserId }));
      }

      ws.on('pong', () => {
        ws.isAlive = true;
      });

      ws.on('message', async (data) => {
        try {
          if (data.toString().length > 64 * 1024) {
            ws.send(JSON.stringify({ type: 'ERROR', code: 'PAYLOAD_TOO_LARGE', message: 'پیام بیش از حد مجاز است' }));
            return;
          }
          const msg = JSON.parse(data.toString());
          await this.handleClientMessage(ws, msg);
        } catch {
          ws.send(JSON.stringify({ type: 'ERROR', code: 'MALFORMED_JSON', message: 'فرمت داده نامعتبر است' }));
        }
      });

      ws.on('close', () => {
        if (ws.userId) {
          this.unregisterClient(ws.userId, ws);
        }
      });
    });

    // Heartbeat check every 25 seconds
    this.heartbeatInterval = setInterval(() => {
      this.wss.clients.forEach((client: AuthenticatedSocket) => {
        if (!client.isAlive) {
          return client.terminate();
        }
        client.isAlive = false;
        client.ping();
      });
    }, 25000);
  }

  private registerClient(userId: string, ws: AuthenticatedSocket) {
    ws.userId = userId;
    if (!this.clients.has(userId)) {
      this.clients.set(userId, new Set());
    }
    this.clients.get(userId)!.add(ws);

    // Update presence in DB
    prisma.user.update({
      where: { id: userId },
      data: { isOnline: true, lastSeen: new Date() },
    }).catch(() => {});

    this.broadcastPresence(userId, true);
  }

  private unregisterClient(userId: string, ws: AuthenticatedSocket) {
    const userSockets = this.clients.get(userId);
    if (userSockets) {
      userSockets.delete(ws);
      if (userSockets.size === 0) {
        this.clients.delete(userId);
        prisma.user.update({
          where: { id: userId },
          data: { isOnline: false, lastSeen: new Date() },
        }).catch(() => {});
        this.broadcastPresence(userId, false);
      }
    }
  }

  private async handleClientMessage(ws: AuthenticatedSocket, msg: any) {
    // 1. Explicit message validation
    if (!msg || typeof msg !== 'object' || typeof msg.type !== 'string') {
      ws.send(JSON.stringify({ type: 'ERROR', code: 'INVALID_EVENT', message: 'نوع رویداد نامعتبر است' }));
      return;
    }

    const allowedEvents = [
      'AUTH',
      'SUBSCRIBE_CONVERSATION',
      'UNSUBSCRIBE_CONVERSATION',
      'SUBSCRIBE_CHANNEL',
      'UNSUBSCRIBE_CHANNEL',
      'TYPING',
      'HEARTBEAT',
    ];

    if (!allowedEvents.includes(msg.type)) {
      ws.send(JSON.stringify({ type: 'ERROR', code: 'UNKNOWN_EVENT_TYPE', message: `رویداد ${msg.type} پشتیبانی نمی‌شود` }));
      return;
    }

    // AUTH Handshake message
    if (msg.type === 'AUTH') {
      if (msg.token) {
        try {
          const payload = jwt.verify(msg.token, env.JWT_SECRET) as { userId: string };
          const user = await prisma.user.findUnique({ where: { id: payload.userId } });
          if (!user) {
            ws.send(JSON.stringify({ type: 'AUTH_EXPIRED', message: 'کاربر یافت نشد' }));
            ws.close(4001, 'Unauthorized');
            return;
          }
          this.registerClient(payload.userId, ws);
          ws.send(JSON.stringify({ type: 'CONNECTED', userId: payload.userId }));
        } catch {
          ws.send(JSON.stringify({ type: 'AUTH_EXPIRED', message: 'توکن نامعتبر یا منقضی شده است' }));
          ws.close(4001, 'Unauthorized');
        }
      } else {
        ws.send(JSON.stringify({ type: 'AUTH_EXPIRED', message: 'توکن ارسال نشده است' }));
        ws.close(4001, 'Unauthorized');
      }
      return;
    }

    // Require authenticated socket for all subsequent operations
    if (!ws.userId) {
      ws.send(JSON.stringify({ type: 'ERROR', code: 'UNAUTHENTICATED_SOCKET', message: 'ابتدا باید احراز هویت انجام شود' }));
      ws.close(4001, 'Unauthorized');
      return;
    }

    switch (msg.type) {
      case 'SUBSCRIBE_CONVERSATION': {
        const conversationId = String(msg.conversationId || '');
        if (!conversationId) return;

        // Security check: Verify user is an actual member of this conversation
        const isMember = await prisma.conversationMember.findUnique({
          where: { conversationId_userId: { conversationId, userId: ws.userId } },
        });

        if (!isMember) {
          ws.send(JSON.stringify({
            type: 'ERROR',
            code: 'FORBIDDEN_SUBSCRIPTION',
            message: 'شما عضو این گفتگو نیستید و مجاز به عضویت در رویدادهای آن نمی‌باشید',
          }));
          return;
        }

        ws.subscriptions?.add(`conv:${conversationId}`);
        ws.send(JSON.stringify({ type: 'SUBSCRIBED_CONVERSATION', conversationId }));
        break;
      }

      case 'UNSUBSCRIBE_CONVERSATION': {
        const conversationId = String(msg.conversationId || '');
        if (conversationId && ws.subscriptions) {
          ws.subscriptions.delete(`conv:${conversationId}`);
        }
        break;
      }

      case 'SUBSCRIBE_CHANNEL': {
        const channelId = String(msg.channelId || '');
        if (!channelId) return;

        // Security check: Verify channel exists and check public or member
        const channel = await prisma.channel.findUnique({
          where: { id: channelId },
          include: { subscribers: { where: { userId: ws.userId } } },
        });

        if (!channel) {
          ws.send(JSON.stringify({ type: 'ERROR', code: 'CHANNEL_NOT_FOUND', message: 'کانال یافت نشد' }));
          return;
        }

        if (!channel.isPublic && channel.subscribers.length === 0 && channel.ownerId !== ws.userId) {
          ws.send(JSON.stringify({ type: 'ERROR', code: 'FORBIDDEN_CHANNEL', message: 'این کانال خصوصی است' }));
          return;
        }

        ws.subscriptions?.add(`channel:${channelId}`);
        ws.send(JSON.stringify({ type: 'SUBSCRIBED_CHANNEL', channelId }));
        break;
      }

      case 'UNSUBSCRIBE_CHANNEL': {
        const channelId = String(msg.channelId || '');
        if (channelId && ws.subscriptions) {
          ws.subscriptions.delete(`channel:${channelId}`);
        }
        break;
      }

      case 'TYPING': {
        const conversationId = String(msg.conversationId || '');
        if (!conversationId) return;

        // Rate limit typing events: max 1 event per 500ms per client
        const now = Date.now();
        if (ws.lastTypingTimestamp && now - ws.lastTypingTimestamp < 500) {
          return;
        }
        ws.lastTypingTimestamp = now;

        // Verify conversation membership
        const isMember = await prisma.conversationMember.findUnique({
          where: { conversationId_userId: { conversationId, userId: ws.userId } },
        });

        if (isMember) {
          this.broadcastToConversation(
            conversationId,
            {
              type: 'TYPING',
              conversationId,
              userId: ws.userId,
              isTyping: !!msg.isTyping,
            },
            ws.userId
          );
        }
        break;
      }
    }
  }

  public broadcastToUser(userId: string, payload: any) {
    const sockets = this.clients.get(userId);
    if (sockets) {
      const data = JSON.stringify(payload);
      sockets.forEach((s) => {
        if (s.readyState === WebSocket.OPEN) {
          s.send(data);
        }
      });
    }
  }

  public broadcastToConversation(conversationId: string, payload: any, excludeUserId?: string) {
    const targetRoom = `conv:${conversationId}`;
    const data = JSON.stringify(payload);

    this.wss.clients.forEach((client: AuthenticatedSocket) => {
      if (
        client.readyState === WebSocket.OPEN &&
        client.subscriptions?.has(targetRoom) &&
        (!excludeUserId || client.userId !== excludeUserId)
      ) {
        client.send(data);
      }
    });
  }

  public broadcastToChannel(channelId: string, payload: any) {
    const targetRoom = `channel:${channelId}`;
    const data = JSON.stringify(payload);

    this.wss.clients.forEach((client: AuthenticatedSocket) => {
      if (client.readyState === WebSocket.OPEN && client.subscriptions?.has(targetRoom)) {
        client.send(data);
      }
    });
  }

  public broadcastPresence(userId: string, isOnline: boolean) {
    const data = JSON.stringify({
      type: 'PRESENCE',
      userId,
      isOnline,
      lastSeen: Date.now(),
    });
    this.wss.clients.forEach((client: AuthenticatedSocket) => {
      if (client.readyState === WebSocket.OPEN && client.userId !== userId) {
        client.send(data);
      }
    });
  }

  public close() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
    }
    this.wss.close();
  }

  public isHealthy(): boolean {
    return this.wss !== undefined;
  }
}

export let realtimeServerInstance: RealtimeServer | null = null;
export function setRealtimeServer(instance: RealtimeServer) {
  realtimeServerInstance = instance;
}
