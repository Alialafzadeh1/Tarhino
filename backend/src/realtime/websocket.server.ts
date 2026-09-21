import { Server as HttpServer } from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import jwt from 'jsonwebtoken';
import { prisma } from '../database/prisma';

interface AuthenticatedSocket extends WebSocket {
  userId?: string;
  isAlive?: boolean;
  subscriptions?: Set<string>;
}

export class RealtimeServer {
  private wss: WebSocketServer;
  private clients: Map<string, Set<AuthenticatedSocket>> = new Map();

  constructor(server: HttpServer) {
    this.wss = new WebSocketServer({ server, path: '/realtime' });
    this.init();
  }

  private init() {
    const JWT_SECRET = process.env.JWT_SECRET || 'tarhinoo_super_secret_jwt_key_production_2026';

    this.wss.on('connection', (ws: AuthenticatedSocket, req) => {
      ws.isAlive = true;
      ws.subscriptions = new Set<string>();

      // Extract token from query or headers
      const url = new URL(req.url || '', `http://${req.headers.host}`);
      const token = url.searchParams.get('token') || (req.headers['authorization'] || '').replace('Bearer ', '');

      let authenticatedUserId: string | null = null;
      if (token) {
        try {
          const payload = jwt.verify(token, JWT_SECRET) as { userId: string };
          authenticatedUserId = payload.userId;
        } catch (e) {
          // Token invalid or expired
        }
      }

      if (!authenticatedUserId) {
        // Allow connection if header provided, or wait for AUTH message
        const headerUserId = req.headers['x-user-id'] as string;
        if (headerUserId) authenticatedUserId = headerUserId;
      }

      if (authenticatedUserId) {
        this.registerClient(authenticatedUserId, ws);
      }

      ws.on('pong', () => {
        ws.isAlive = true;
      });

      ws.on('message', async (data) => {
        try {
          const msg = JSON.parse(data.toString());
          await this.handleClientMessage(ws, msg);
        } catch (err) {
          // Invalid json ignored
        }
      });

      ws.on('close', () => {
        if (ws.userId) {
          this.unregisterClient(ws.userId, ws);
        }
      });
    });

    // Heartbeat check every 25 seconds
    setInterval(() => {
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

    // Update presence in DB and broadcast
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
    switch (msg.type) {
      case 'AUTH':
        if (msg.token) {
          try {
            const JWT_SECRET = process.env.JWT_SECRET || 'tarhinoo_super_secret_jwt_key_production_2026';
            const payload = jwt.verify(msg.token, JWT_SECRET) as { userId: string };
            this.registerClient(payload.userId, ws);
            ws.send(JSON.stringify({ type: 'CONNECTED', userId: payload.userId }));
          } catch {
            ws.send(JSON.stringify({ type: 'AUTH_EXPIRED' }));
          }
        }
        break;

      case 'SUBSCRIBE_CONVERSATION':
        if (msg.conversationId && ws.subscriptions) {
          ws.subscriptions.add(`conv:${msg.conversationId}`);
        }
        break;

      case 'UNSUBSCRIBE_CONVERSATION':
        if (msg.conversationId && ws.subscriptions) {
          ws.subscriptions.delete(`conv:${msg.conversationId}`);
        }
        break;

      case 'SUBSCRIBE_CHANNEL':
        if (msg.channelId && ws.subscriptions) {
          ws.subscriptions.add(`channel:${msg.channelId}`);
        }
        break;

      case 'UNSUBSCRIBE_CHANNEL':
        if (msg.channelId && ws.subscriptions) {
          ws.subscriptions.delete(`channel:${msg.channelId}`);
        }
        break;

      case 'TYPING':
        if (msg.conversationId && ws.userId) {
          this.broadcastToConversation(msg.conversationId, {
            type: 'TYPING',
            conversationId: msg.conversationId,
            userId: ws.userId,
            isTyping: !!msg.isTyping,
          }, ws.userId);
        }
        break;
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

  public isHealthy(): boolean {
    return this.wss !== undefined;
  }
}

export let realtimeServerInstance: RealtimeServer | null = null;
export function setRealtimeServer(instance: RealtimeServer) {
  realtimeServerInstance = instance;
}
