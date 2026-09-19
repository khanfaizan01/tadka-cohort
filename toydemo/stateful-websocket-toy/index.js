/**
 * Stateful WebSocket toy (Day 6).
 * Demonstrates the problem with stateful connections in a scaled-out deployment.
 * Shows how Redis pub/sub can serve as a shared backplane.
 */
const { WebSocketServer } = require('ws');
const wss = new WebSocketServer({ port: 3003 });

const clients = new Map();

wss.on('connection', (ws) => {
  const id = Date.now();
  clients.set(id, ws);
  console.log(`Client ${id} connected. Total: ${clients.size}`);

  ws.on('message', (msg) => {
    const data = JSON.parse(msg);
    // Broadcast to all clients (simulating shared backplane)
    clients.forEach((client, cid) => {
      if (client.readyState === 1) {
        client.send(JSON.stringify({ from: id, ...data }));
      }
    });
  });

  ws.on('close', () => {
    clients.delete(id);
    console.log(`Client ${id} disconnected. Total: ${clients.size}`);
  });
});

console.log('Stateful WebSocket toy running on ws://localhost:3003');
