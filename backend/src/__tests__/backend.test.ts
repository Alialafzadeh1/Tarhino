import request from 'supertest';
import { app, realtimeServer } from '../index';

describe('Tarhi Noo Backend Health & Security Checks', () => {
  afterAll((done) => {
    realtimeServer.close();
    done();
  });

  it('GET / should return online service banner', async () => {
    const res = await request(app).get('/');
    expect(res.status).toBe(200);
    expect(res.body.name).toContain('Tarhi Noo Backend');
    expect(res.body.status).toBe('ONLINE');
  });

  it('GET /health should return system status', async () => {
    const res = await request(app).get('/health');
    expect([200, 503]).toContain(res.status);
    expect(res.body).toHaveProperty('components');
    expect(res.body.components).toHaveProperty('backend');
    expect(res.body.components.backend).toBe('CONNECTED');
  });

  it('Protected routes should reject unauthenticated requests with 401', async () => {
    const res = await request(app).get('/users/me');
    expect(res.status).toBe(401);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('UNAUTHORIZED');
  });

  it('Protected routes should reject forged token with 403', async () => {
    const res = await request(app)
      .get('/users/me')
      .set('Authorization', 'Bearer invalid_forged_jwt_token_payload');
    expect(res.status).toBe(403);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('FORBIDDEN');
  });

  it('POST /ai/chat should reject unauthenticated requests with 401', async () => {
    const res = await request(app)
      .post('/ai/chat')
      .send({ prompt: 'Create cinematic poster' });
    expect(res.status).toBe(401);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('UNAUTHORIZED');
  });

  it('POST /ai/chat should reject empty prompt with 400 when authorized', async () => {
    // Generate valid test JWT token
    const jwt = require('jsonwebtoken');
    const { env } = require('../config/env');
    const token = jwt.sign({ userId: 'test-user-id' }, env.JWT_SECRET, { expiresIn: '5m' });

    const res = await request(app)
      .post('/ai/chat')
      .set('Authorization', `Bearer ${token}`)
      .send({ prompt: '' });
    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe('AI_INVALID_PROMPT');
  });
});
