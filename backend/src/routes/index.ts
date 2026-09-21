import { Router, Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { v4 as uuidv4 } from 'uuid';
import { prisma, checkDatabaseHealth } from '../database/prisma';
import { authenticateToken, AuthRequest } from '../middleware/auth';
import { createRateLimiter } from '../middleware/rateLimiter';
import { realtimeServerInstance } from '../realtime/websocket.server';
import { uploadsService } from '../uploads/uploads.service';
import { aiGateway } from '../ai/ai.service';
import { notificationService } from '../notifications/notifications.service';

const router = Router();

const JWT_SECRET = process.env.JWT_SECRET || 'tarhinoo_super_secret_jwt_key_production_2026';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'tarhinoo_super_secret_refresh_jwt_key_production_2026';

// Rate limiters
const authLimiter = createRateLimiter({ windowMs: 15 * 60 * 1000, max: 20 });
const messageLimiter = createRateLimiter({ windowMs: 60 * 1000, max: 60 });
const aiLimiter = createRateLimiter({ windowMs: 60 * 1000, max: 20 });

// ==========================================
// 1. SYSTEM HEALTH
// ==========================================
router.get('/health', async (_req: Request, res: Response) => {
  const dbOk = await checkDatabaseHealth();
  const wsOk = realtimeServerInstance !== null && realtimeServerInstance.isHealthy();
  const storageOk = uploadsService.isConfigured();
  const aiOk = aiGateway.isConfigured();

  const allGood = dbOk;

  res.status(allGood ? 200 : 503).json({
    status: allGood ? 'CONNECTED' : 'DEGRADED',
    timestamp: Date.now(),
    components: {
      backend: 'CONNECTED',
      database: dbOk ? 'CONNECTED' : 'ERROR',
      realtime: wsOk ? 'CONNECTED' : 'DISCONNECTED',
      storage: storageOk ? 'CONNECTED' : 'DISCONNECTED',
      ai: aiOk ? 'CONNECTED' : 'DISCONNECTED',
    },
  });
});

// ==========================================
// 2. AUTHENTICATION
// ==========================================
router.post('/auth/register', authLimiter, async (req: Request, res: Response) => {
  try {
    const { email, password, username, displayName } = req.body;
    if (!email || !password || !username) {
      return res.status(400).json({
        success: false,
        error: { code: 'BAD_REQUEST', message: 'ایمیل، رمز عبور و نام کاربری الزامی است' },
      });
    }

    const cleanUsername = username.replace(/^@/, '').toLowerCase().trim();
    const existing = await prisma.user.findFirst({
      where: { OR: [{ email: email.toLowerCase() }, { username: cleanUsername }] },
    });

    if (existing) {
      return res.status(409).json({
        success: false,
        error: { code: 'CONFLICT', message: 'کاربری با این ایمیل یا نام کاربری از قبل وجود دارد' },
      });
    }

    const passwordHash = await bcrypt.hash(password, 12);
    const user = await prisma.user.create({
      data: {
        email: email.toLowerCase(),
        passwordHash,
        username: cleanUsername,
        displayName: displayName || cleanUsername,
        bio: 'کاربر طرحی نو',
        isOnline: true,
      },
    });

    const accessToken = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '15m' });
    const refreshToken = jwt.sign({ userId: user.id }, JWT_REFRESH_SECRET, { expiresIn: '30d' });

    await prisma.refreshToken.create({
      data: {
        userId: user.id,
        tokenHash: await bcrypt.hash(refreshToken, 8),
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
      },
    });

    res.status(201).json({
      success: true,
      data: {
        user: {
          id: user.id,
          username: user.username,
          displayName: user.displayName,
          avatarUrl: user.avatarUrl || '',
          bio: user.bio || '',
          isOnline: true,
        },
        accessToken,
        refreshToken,
        expiresIn: 900,
      },
    });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'SERVER_ERROR', message: error.message || 'خطای داخلی سرور' },
    });
  }
});

router.post('/auth/login', authLimiter, async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        error: { code: 'BAD_REQUEST', message: 'ایمیل و رمز عبور الزامی است' },
      });
    }

    const user = await prisma.user.findFirst({
      where: {
        OR: [{ email: email.toLowerCase() }, { username: email.replace(/^@/, '').toLowerCase().trim() }],
      },
    });

    if (!user || !(await bcrypt.compare(password, user.passwordHash))) {
      return res.status(401).json({
        success: false,
        error: { code: 'INVALID_CREDENTIALS', message: 'اطلاعات ورود نامعتبر است' },
      });
    }

    const accessToken = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '15m' });
    const refreshToken = jwt.sign({ userId: user.id }, JWT_REFRESH_SECRET, { expiresIn: '30d' });

    await prisma.refreshToken.create({
      data: {
        userId: user.id,
        tokenHash: await bcrypt.hash(refreshToken, 8),
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
      },
    });

    await prisma.user.update({
      where: { id: user.id },
      data: { isOnline: true, lastSeen: new Date() },
    });

    res.json({
      success: true,
      data: {
        user: {
          id: user.id,
          username: user.username,
          displayName: user.displayName,
          avatarUrl: user.avatarUrl || '',
          bio: user.bio || '',
          isOnline: true,
        },
        accessToken,
        refreshToken,
        expiresIn: 900,
      },
    });
  } catch (error: any) {
    res.status(500).json({
      success: false,
      error: { code: 'SERVER_ERROR', message: error.message || 'خطای سرور در احراز هویت' },
    });
  }
});

router.post('/auth/refresh', async (req: Request, res: Response) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      return res.status(400).json({
        success: false,
        error: { code: 'BAD_REQUEST', message: 'توکن بازیابی الزامی است' },
      });
    }

    const payload = jwt.verify(refreshToken, JWT_REFRESH_SECRET) as { userId: string };
    const user = await prisma.user.findUnique({ where: { id: payload.userId } });

    if (!user) {
      return res.status(401).json({
        success: false,
        error: { code: 'USER_NOT_FOUND', message: 'کاربر یافت نشد' },
      });
    }

    const newAccessToken = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '15m' });
    const newRefreshToken = jwt.sign({ userId: user.id }, JWT_REFRESH_SECRET, { expiresIn: '30d' });

    res.json({
      success: true,
      data: {
        user: {
          id: user.id,
          username: user.username,
          displayName: user.displayName,
          avatarUrl: user.avatarUrl || '',
          bio: user.bio || '',
          isOnline: user.isOnline,
        },
        accessToken: newAccessToken,
        refreshToken: newRefreshToken,
        expiresIn: 900,
      },
    });
  } catch {
    res.status(401).json({
      success: false,
      error: { code: 'TOKEN_EXPIRED', message: 'توکن بازیابی منقضی شده یا نامعتبر است' },
    });
  }
});

router.post('/auth/logout', authenticateToken, async (req: AuthRequest, res: Response) => {
  if (req.user?.id) {
    await prisma.user.update({
      where: { id: req.user.id },
      data: { isOnline: false, lastSeen: new Date() },
    });
  }
  res.json({ success: true });
});

// ==========================================
// 3. USERS & PROFILES
// ==========================================
router.get('/users/me', authenticateToken, async (req: AuthRequest, res: Response) => {
  const user = await prisma.user.findUnique({
    where: { id: req.user!.id },
    select: { id: true, username: true, displayName: true, avatarUrl: true, bio: true, isOnline: true },
  });
  res.json({ success: true, data: user });
});

router.get('/users/search', authenticateToken, async (req: AuthRequest, res: Response) => {
  const q = (req.query.q as string || '').replace(/^@/, '').toLowerCase().trim();
  if (!q) {
    return res.json({ success: true, data: [] });
  }

  const users = await prisma.user.findMany({
    where: {
      AND: [
        { id: { not: req.user!.id } },
        {
          OR: [
            { username: { contains: q, mode: 'insensitive' } },
            { displayName: { contains: q, mode: 'insensitive' } },
          ],
        },
      ],
    },
    take: 30,
    select: { id: true, username: true, displayName: true, avatarUrl: true, bio: true, isOnline: true },
  });

  res.json({ success: true, data: users });
});

router.get('/users/:id', authenticateToken, async (req: AuthRequest, res: Response) => {
  const user = await prisma.user.findUnique({
    where: { id: req.params.id },
    select: { id: true, username: true, displayName: true, avatarUrl: true, bio: true, isOnline: true, lastSeen: true },
  });

  if (!user) {
    return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'کاربر یافت نشد' } });
  }
  res.json({ success: true, data: user });
});

router.patch('/users/me', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { displayName, bio, avatarUrl } = req.body;
  const updated = await prisma.user.update({
    where: { id: req.user!.id },
    data: { displayName, bio, avatarUrl },
    select: { id: true, username: true, displayName: true, avatarUrl: true, bio: true, isOnline: true },
  });
  res.json({ success: true, data: updated });
});

// ==========================================
// 4. CONVERSATIONS
// ==========================================
router.get('/conversations', authenticateToken, async (req: AuthRequest, res: Response) => {
  const userId = req.user!.id;
  const members = await prisma.conversationMember.findMany({
    where: { userId },
    include: {
      conversation: {
        include: {
          messages: {
            orderBy: { createdAt: 'desc' },
            take: 1,
            include: { sender: { select: { displayName: true } } },
          },
        },
      },
    },
  });

  const list = members.map((m) => {
    const c = m.conversation;
    const lastMsg = c.messages[0];
    return {
      id: c.id,
      type: c.type,
      title: c.title,
      avatarUrl: c.avatarUrl || '',
      description: c.description || '',
      directUserId: c.directTargetUserId,
      unreadCount: 0,
      lastMessageText: lastMsg?.text || '',
      lastMessageSenderName: lastMsg?.sender?.displayName || '',
      lastMessageTimestamp: lastMsg ? new Date(lastMsg.createdAt).getTime() : new Date(c.createdAt).getTime(),
      createdAt: new Date(c.createdAt).getTime(),
      updatedAt: new Date(c.updatedAt).getTime(),
    };
  });

  res.json({ success: true, data: list });
});

router.post('/conversations/direct/:targetUserId', authenticateToken, async (req: AuthRequest, res: Response) => {
  const currentUserId = req.user!.id;
  const targetUserId = req.params.targetUserId;

  const targetUser = await prisma.user.findUnique({ where: { id: targetUserId } });
  if (!targetUser) {
    return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'کاربر مورد نظر یافت نشد' } });
  }

  // Find existing private conversation
  const existingMember = await prisma.conversationMember.findFirst({
    where: {
      userId: currentUserId,
      conversation: {
        type: 'PRIVATE',
        members: { some: { userId: targetUserId } },
      },
    },
    include: { conversation: true },
  });

  if (existingMember) {
    return res.json({
      success: true,
      data: {
        id: existingMember.conversation.id,
        type: 'PRIVATE',
        title: targetUser.displayName,
        avatarUrl: targetUser.avatarUrl || '',
        description: targetUser.bio || '',
        directUserId: targetUser.id,
        unreadCount: 0,
        lastMessageText: '',
        lastMessageSenderName: '',
        lastMessageTimestamp: Date.now(),
        createdAt: Date.now(),
        updatedAt: Date.now(),
      },
    });
  }

  // Create new conversation
  const newConv = await prisma.conversation.create({
    data: {
      type: 'PRIVATE',
      title: targetUser.displayName,
      avatarUrl: targetUser.avatarUrl,
      directTargetUserId: targetUser.id,
      members: {
        create: [
          { userId: currentUserId, role: 'MEMBER' },
          { userId: targetUserId, role: 'MEMBER' },
        ],
      },
    },
  });

  res.status(201).json({
    success: true,
    data: {
      id: newConv.id,
      type: 'PRIVATE',
      title: targetUser.displayName,
      avatarUrl: targetUser.avatarUrl || '',
      description: targetUser.bio || '',
      directUserId: targetUser.id,
      unreadCount: 0,
      lastMessageText: '',
      lastMessageSenderName: '',
      lastMessageTimestamp: Date.now(),
      createdAt: Date.now(),
      updatedAt: Date.now(),
    },
  });
});

// ==========================================
// 5. MESSAGES
// ==========================================
router.get('/conversations/:conversationId/messages', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { conversationId } = req.params;
  const before = req.query.before as string;
  const limit = Math.min(parseInt(req.query.limit as string) || 40, 100);

  const messages = await prisma.message.findMany({
    where: {
      conversationId,
      deletedAt: null,
      ...(before ? { createdAt: { lt: new Date(before) } } : {}),
    },
    orderBy: { createdAt: 'desc' },
    take: limit,
    include: { sender: { select: { displayName: true } } },
  });

  const dtoList = messages.reverse().map((m) => ({
    id: m.id,
    conversationId: m.conversationId,
    senderId: m.senderId,
    senderDisplayName: m.sender.displayName,
    text: m.text,
    messageType: m.messageType,
    deliveryStatus: m.status,
    clientRequestId: m.clientRequestId,
    replyToMessageId: m.replyToMessageId,
    aiActionPrompt: m.aiActionPrompt,
    aiTargetModule: m.aiTargetModule,
    editedAt: m.editedAt ? new Date(m.editedAt).getTime() : null,
    createdAt: new Date(m.createdAt).getTime(),
  }));

  res.json({ success: true, data: dtoList });
});

router.post('/conversations/:conversationId/messages', authenticateToken, messageLimiter, async (req: AuthRequest, res: Response) => {
  const { conversationId } = req.params;
  const senderId = req.user!.id;
  const { text, messageType = 'TEXT', clientRequestId, replyToMessageId } = req.body;

  if (!text || text.trim() === '') {
    return res.status(400).json({ success: false, error: { code: 'BAD_REQUEST', message: 'متن پیام الزامی است' } });
  }

  // Idempotency check with clientRequestId
  if (clientRequestId) {
    const existing = await prisma.message.findUnique({
      where: {
        conversationId_clientRequestId: { conversationId, clientRequestId },
      },
      include: { sender: { select: { displayName: true } } },
    });

    if (existing) {
      return res.json({
        success: true,
        data: {
          id: existing.id,
          conversationId: existing.conversationId,
          senderId: existing.senderId,
          senderDisplayName: existing.sender.displayName,
          text: existing.text,
          messageType: existing.messageType,
          deliveryStatus: existing.status,
          clientRequestId: existing.clientRequestId,
          replyToMessageId: existing.replyToMessageId,
          createdAt: new Date(existing.createdAt).getTime(),
        },
      });
    }
  }

  // Save new message
  const msg = await prisma.message.create({
    data: {
      conversationId,
      senderId,
      clientRequestId,
      text,
      messageType,
      status: 'SENT',
      replyToMessageId,
    },
    include: { sender: { select: { displayName: true } } },
  });

  const messageDto = {
    id: msg.id,
    conversationId: msg.conversationId,
    senderId: msg.senderId,
    senderDisplayName: msg.sender.displayName,
    text: msg.text,
    messageType: msg.messageType,
    deliveryStatus: 'SENT',
    clientRequestId: msg.clientRequestId,
    replyToMessageId: msg.replyToMessageId,
    createdAt: new Date(msg.createdAt).getTime(),
  };

  // Broadcast realtime event
  if (realtimeServerInstance) {
    realtimeServerInstance.broadcastToConversation(conversationId, {
      type: 'MESSAGE_CREATED',
      message: messageDto,
    });
  }

  // If user mentioned @TarhiNooAI, dispatch real AI response
  if (text.includes('@TarhiNooAI')) {
    setImmediate(async () => {
      try {
        const aiResult = await aiGateway.processAIMessage(text);
        const aiMsg = await prisma.message.create({
          data: {
            conversationId,
            senderId: 'ai-system-assistant',
            text: aiResult.text,
            messageType: 'AI_RESULT',
            status: 'SENT',
            isAiGenerated: true,
            aiActionPrompt: aiResult.actionPrompt,
            aiTargetModule: aiResult.targetModule,
          },
        });

        if (realtimeServerInstance) {
          realtimeServerInstance.broadcastToConversation(conversationId, {
            type: 'MESSAGE_CREATED',
            message: {
              id: aiMsg.id,
              conversationId: aiMsg.conversationId,
              senderId: 'ai-system-assistant',
              senderDisplayName: 'هوش مصنوعی طرحی نو',
              text: aiMsg.text,
              messageType: 'AI_RESULT',
              aiActionPrompt: aiResult.actionPrompt,
              aiTargetModule: aiResult.targetModule,
              createdAt: new Date(aiMsg.createdAt).getTime(),
            },
          });
        }
      } catch (err) {
        console.error('Failed to process AI response in background', err);
      }
    });
  }

  res.status(201).json({ success: true, data: messageDto });
});

router.patch('/messages/:messageId', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { messageId } = req.params;
  const { text } = req.body;

  const msg = await prisma.message.findUnique({ where: { id: messageId } });
  if (!msg) {
    return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'پیام یافت نشد' } });
  }

  if (msg.senderId !== req.user!.id && req.user!.role !== 'ADMIN') {
    return res.status(403).json({ success: false, error: { code: 'FORBIDDEN', message: 'شما مجاز به ویرایش این پیام نیستید' } });
  }

  const updated = await prisma.message.update({
    where: { id: messageId },
    data: { text, editedAt: new Date() },
    include: { sender: { select: { displayName: true } } },
  });

  if (realtimeServerInstance) {
    realtimeServerInstance.broadcastToConversation(msg.conversationId, {
      type: 'MESSAGE_UPDATED',
      message: {
        id: updated.id,
        conversationId: updated.conversationId,
        text: updated.text,
        editedAt: updated.editedAt ? new Date(updated.editedAt).getTime() : Date.now(),
      },
    });
  }

  res.json({ success: true, data: updated });
});

router.delete('/messages/:messageId', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { messageId } = req.params;
  const mode = req.query.mode || 'EVERYONE';

  const msg = await prisma.message.findUnique({ where: { id: messageId } });
  if (!msg) {
    return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'پیام یافت نشد' } });
  }

  if (mode === 'EVERYONE') {
    if (msg.senderId !== req.user!.id && req.user!.role !== 'ADMIN') {
      return res.status(403).json({ success: false, error: { code: 'FORBIDDEN', message: 'شما مجاز به حذف این پیام برای همه نیستید' } });
    }

    await prisma.message.update({
      where: { id: messageId },
      data: { deletedAt: new Date(), text: 'این پیام حذف شد' },
    });

    if (realtimeServerInstance) {
      realtimeServerInstance.broadcastToConversation(msg.conversationId, {
        type: 'MESSAGE_DELETED',
        conversationId: msg.conversationId,
        message: { id: messageId },
      });
    }
  }

  res.json({ success: true });
});

router.post('/conversations/:conversationId/read', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { conversationId } = req.params;
  const { lastReadMessageId } = req.body;

  await prisma.conversationMember.updateMany({
    where: { conversationId, userId: req.user!.id },
    data: { lastReadMessageId },
  });

  if (realtimeServerInstance) {
    realtimeServerInstance.broadcastToConversation(conversationId, {
      type: 'MESSAGE_READ',
      conversationId,
      message: { id: lastReadMessageId },
      timestamp: Date.now(),
    });
  }

  res.json({ success: true });
});

// ==========================================
// 6. REACTIONS
// ==========================================
router.post('/messages/:messageId/reactions', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { messageId } = req.params;
  const reaction = req.query.reaction as string;
  const userId = req.user!.id;

  if (!reaction) {
    return res.status(400).json({ success: false, error: { code: 'BAD_REQUEST', message: 'ایموجی واکنش مشخص نشده است' } });
  }

  const created = await prisma.messageReaction.upsert({
    where: { messageId_userId_reaction: { messageId, userId, reaction } },
    update: { createdAt: new Date() },
    create: { messageId, userId, reaction },
  });

  if (realtimeServerInstance) {
    const msg = await prisma.message.findUnique({ where: { id: messageId }, select: { conversationId: true } });
    if (msg) {
      realtimeServerInstance.broadcastToConversation(msg.conversationId, {
        type: 'REACTION_ADDED',
        reaction: { messageId, userId, reaction },
      });
    }
  }

  res.json({ success: true, data: { id: created.id, messageId, userId, reaction } });
});

router.delete('/messages/:messageId/reactions/:reaction', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { messageId, reaction } = req.params;
  const userId = req.user!.id;

  await prisma.messageReaction.deleteMany({
    where: { messageId, userId, reaction },
  });

  if (realtimeServerInstance) {
    const msg = await prisma.message.findUnique({ where: { id: messageId }, select: { conversationId: true } });
    if (msg) {
      realtimeServerInstance.broadcastToConversation(msg.conversationId, {
        type: 'REACTION_REMOVED',
        reaction: { messageId, userId, reaction },
      });
    }
  }

  res.json({ success: true });
});

// ==========================================
// 7. GROUPS
// ==========================================
router.post('/groups', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { name, description = '', memberUserIds = [] } = req.body;
  const ownerId = req.user!.id;

  const conv = await prisma.conversation.create({
    data: {
      type: 'GROUP',
      title: name,
      description,
      members: {
        create: [
          { userId: ownerId, role: 'OWNER' },
          ...memberUserIds.map((uId: string) => ({ userId: uId, role: 'MEMBER' })),
        ],
      },
      group: {
        create: {
          name,
          description,
          ownerId,
          permissions: { create: {} },
        },
      },
    },
    include: { group: true },
  });

  res.status(201).json({
    success: true,
    data: {
      id: conv.group!.id,
      conversationId: conv.id,
      name: conv.group!.name,
      description: conv.group!.description,
      ownerId: conv.group!.ownerId,
      memberCount: memberUserIds.length + 1,
    },
  });
});

// ==========================================
// 8. CHANNELS
// ==========================================
router.post('/channels', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { name, username, description = '' } = req.body;
  const ownerId = req.user!.id;
  const cleanUsername = username.replace(/^@/, '').toLowerCase().trim();

  const conv = await prisma.conversation.create({
    data: {
      type: 'CHANNEL',
      title: name,
      description,
      members: { create: [{ userId: ownerId, role: 'OWNER' }] },
      channel: {
        create: {
          name,
          username: cleanUsername,
          description,
          ownerId,
        },
      },
    },
    include: { channel: true },
  });

  res.status(201).json({
    success: true,
    data: {
      id: conv.channel!.id,
      conversationId: conv.id,
      name: conv.channel!.name,
      username: conv.channel!.username,
      description: conv.channel!.description,
      ownerId: conv.channel!.ownerId,
      subscriberCount: 1,
    },
  });
});

router.get('/channels/:channelId/posts', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { channelId } = req.params;
  const posts = await prisma.channelPost.findMany({
    where: { channelId },
    orderBy: { createdAt: 'desc' },
    take: 30,
    include: { author: { select: { displayName: true } } },
  });

  const dtoList = posts.map((p) => ({
    id: p.id,
    channelId: p.channelId,
    authorName: p.author.displayName,
    text: p.text,
    mediaUrl: p.mediaUrl || '',
    promptText: p.promptText || '',
    viewCount: p.viewCount,
    reactionCount: p.reactionCount,
    createdAt: new Date(p.createdAt).getTime(),
  }));

  res.json({ success: true, data: dtoList });
});

router.post('/channels/:channelId/posts', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { channelId } = req.params;
  const { text, mediaUrl = '', promptText = '' } = req.body;

  const post = await prisma.channelPost.create({
    data: {
      channelId,
      authorId: req.user!.id,
      text,
      mediaUrl,
      promptText,
    },
    include: { author: { select: { displayName: true } } },
  });

  const dto = {
    id: post.id,
    channelId: post.channelId,
    authorName: post.author.displayName,
    text: post.text,
    mediaUrl: post.mediaUrl || '',
    promptText: post.promptText || '',
    viewCount: 1,
    reactionCount: 0,
    createdAt: new Date(post.createdAt).getTime(),
  };

  if (realtimeServerInstance) {
    realtimeServerInstance.broadcastToChannel(channelId, {
      type: 'CHANNEL_POST',
      channelPost: dto,
    });
  }

  res.status(201).json({ success: true, data: dto });
});

// ==========================================
// 9. MEDIA UPLOADS & PRESIGN
// ==========================================
router.post('/uploads/presign', authenticateToken, async (req: AuthRequest, res: Response) => {
  const presignData = uploadsService.generatePresignedUpload(req.body, req.user!.id);
  res.json({ success: true, data: presignData });
});

// ==========================================
// 10. MODERATION & REPORTS
// ==========================================
router.post('/reports', authenticateToken, async (req: AuthRequest, res: Response) => {
  const { targetType, targetId, reasonCategory, details } = req.body;
  await prisma.report.create({
    data: {
      targetType: targetType || 'MESSAGE',
      targetId: String(targetId),
      reasonCategory: reasonCategory || 'SPAM',
      details: details || '',
      reportedById: req.user!.id,
    },
  });
  res.json({ success: true });
});

router.post('/users/block/:userId', authenticateToken, async (req: AuthRequest, res: Response) => {
  await prisma.blockedUser.upsert({
    where: { userId_blockedUserId: { userId: req.user!.id, blockedUserId: req.params.userId } },
    update: {},
    create: { userId: req.user!.id, blockedUserId: req.params.userId },
  });
  res.json({ success: true });
});

router.delete('/users/block/:userId', authenticateToken, async (req: AuthRequest, res: Response) => {
  await prisma.blockedUser.deleteMany({
    where: { userId: req.user!.id, blockedUserId: req.params.userId },
  });
  res.json({ success: true });
});

export default router;
