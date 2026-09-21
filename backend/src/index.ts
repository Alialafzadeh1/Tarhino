import http from 'http';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';

// Load local .env before validating configuration
dotenv.config();

import { env } from './config/env';
import apiRouter from './routes';
import { RealtimeServer, setRealtimeServer } from './realtime/websocket.server';

const app = express();
const PORT = env.PORT || 8080;
const HOST = env.HOST || '0.0.0.0';

app.use(helmet({ contentSecurityPolicy: false }));
app.use(
  cors({
    origin: env.ALLOWED_ORIGINS.includes('*') ? '*' : env.ALLOWED_ORIGINS,
    credentials: true,
  })
);
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Root welcome
app.get('/', (_req, res) => {
  res.json({
    name: 'Tarhi Noo Backend & Realtime Gateway',
    status: 'ONLINE',
    version: '1.0.0',
    environment: env.NODE_ENV,
    documentation: '/health',
  });
});

// API Routes
app.use('/api', apiRouter);
// Direct endpoint mapping
app.use('/', apiRouter);

const server = http.createServer(app);

// Initialize Realtime WebSocket Gateway
const realtimeServer = new RealtimeServer(server);
setRealtimeServer(realtimeServer);

if (process.env.NODE_ENV !== 'test') {
  server.listen(PORT, HOST, () => {
    console.log(`[Tarhi Noo Backend] HTTP & Realtime Gateway listening on http://${HOST}:${PORT}`);
  });
}

export { app, server, realtimeServer };
