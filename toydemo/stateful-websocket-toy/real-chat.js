/**
 * Stateful WebSocket chat — real Redis pub/sub backplane (Day 6, ADR-020).
 *
 * Demonstrates how Redis pub/sub allows multiple server instances to share
 * WebSocket connections, solving the stateful connection problem in scaled-out deployments.
 *
 * Run:
 *   cd toydemo/stateful-websocket-toy
 *   node real-chat.js &
 *   # Connect with: wscat -c ws://localhost:3005
 */

const { WebSocketServer } = require('ws');
const Redis = require('ioredis');
const wss = new WebSocketServer({ port: 3005 });

const redis = new Redis({ host: 'localhost', port: 6379 });
const clients = new Map();
const CHANNEL = 'tadka-chat';

// Subscribe to Redis channel so other server instances can broadcast to us
redis.subscribe(CHANNEL);
redis.on('message', (channel, message) => {
  if (channel === CHANNEL) {
    // Broadcast to all local clients
    clients.forEach((ws) => {
      if (ws.readyState === 1) ws.send(message);
    });
  }
});

wss.on('connection', (ws) => {
  const id = Date.now();
  clients.set(id, ws);
  console.log(`Client ${id} connected. Total: ${clients.size}`);

  ws.on('message', (msg) => {
    const data = JSON.stringify({ from: id, text: msg.toString(), ts: Date.now() });
    // Publish to Redis channel — all server instances receive this
    redis.publish(CHANNEL, data);
  });

  ws.on('close', () => {
    clients.delete(id);
    console.log(`Client ${id} disconnected. Total: ${clients.size}`);
  });
});

console.log(`Stateful WebSocket chat on ws://localhost:3005 (Redis backplane: ${CHANNEL})`);
