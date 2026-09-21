import { Request, Response, NextFunction } from 'express';

interface RateLimitRecord {
  count: number;
  resetTime: number;
}

const rateLimitStore = new Map<string, RateLimitRecord>();

// Periodic cleanup of stale rate-limit keys every 5 minutes (unref prevents hanging Jest tests)
if (process.env.NODE_ENV !== 'test') {
  const cleanupTimer = setInterval(() => {
    const now = Date.now();
    for (const [key, record] of rateLimitStore.entries()) {
      if (now > record.resetTime) {
        rateLimitStore.delete(key);
      }
    }
  }, 300000);
  cleanupTimer.unref();
}

export function createRateLimiter(options: { windowMs: number; max: number; message?: string }) {
  return (req: Request, res: Response, next: NextFunction) => {
    // Determine key: prioritize authenticated user id, then client IP
    const userIdentifier = (req as any).user?.id || req.ip || req.socket.remoteAddress || 'unknown';
    const key = `${userIdentifier}:${req.baseUrl || ''}${req.path}`;
    const now = Date.now();
    const record = rateLimitStore.get(key);

    if (!record || now > record.resetTime) {
      rateLimitStore.set(key, { count: 1, resetTime: now + options.windowMs });
      res.setHeader('X-RateLimit-Limit', options.max);
      res.setHeader('X-RateLimit-Remaining', options.max - 1);
      res.setHeader('X-RateLimit-Reset', Math.ceil((now + options.windowMs) / 1000));
      return next();
    }

    if (record.count >= options.max) {
      res.setHeader('Retry-After', Math.ceil((record.resetTime - now) / 1000));
      return res.status(429).json({
        success: false,
        error: {
          code: 'RATE_LIMITED',
          message: options.message || 'تعداد درخواست‌ها بیش از حد مجاز است. لطفاً کمی صبر کنید.',
        },
      });
    }

    record.count++;
    res.setHeader('X-RateLimit-Limit', options.max);
    res.setHeader('X-RateLimit-Remaining', Math.max(0, options.max - record.count));
    res.setHeader('X-RateLimit-Reset', Math.ceil(record.resetTime / 1000));
    next();
  };
}
