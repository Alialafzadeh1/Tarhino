import http from 'http';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';
import apiRouter from './routes';
import { RealtimeServer, setRealtimeServer } from './realtime/websocket.server';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 8080;

app.use(helmet({ contentSecurityPolicy: false }));
app.use(cors({ origin: process.env.ALLOWED_ORIGINS || '*' }));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Root welcome
app.get('/', (_req, res) => {
  res.json({
    name: 'Tarhi Noo Backend & Realtime Gateway',
    status: 'ONLINE',
    version: '1.0.0',
    documentation: '/health',
  });
});

// API Routes
app.use('/api', apiRouter);
// Fallback for direct endpoints
app.use('/', apiRouter);

const server = http.createServer(app);

// Initialize Realtime WebSocket Gateway
const realtimeServer = new RealtimeServer(server);
setRealtimeServer(realtimeServer);

server.listen(PORT, () => {
  console.log(`[Tarhi Noo Backend] HTTP & Realtime Gateway listening on port ${PORT}`);
});

export { app, server };
