/**
 * Rate limiter toy demo (Day 6, ADR-049).
 * Shows fixed-window vs sliding-window rate limiting behavior.
 *
 * Usage: node server.js
 * Then: node load-client.js
 */
const express = require('express');
const app = express();
const PORT = 3001;

// Simple in-memory rate limiter
const windowRequests = new Map();
const WINDOW_MS = 60000;
const LIMIT = 5;

app.use((req, res, next) => {
  const ip = req.ip || 'unknown';
  const now = Date.now();
  const windowStart = now - WINDOW_MS;

  if (!windowRequests.has(ip)) windowRequests.set(ip, []);
  const requests = windowRequests.get(ip);

  // Remove expired
  while (requests.length > 0 && requests[0] < windowStart) {
    requests.shift();
  }

  if (requests.length >= LIMIT) {
    const retryAfter = Math.ceil((requests[0] + WINDOW_MS - now) / 1000);
    res.set('Retry-After', String(retryAfter));
    return res.status(429).json({ error: 'Too Many Requests', retryAfter });
  }

  requests.push(now);
  next();
});

app.get('/api/data', (req, res) => {
  res.json({ data: 'success', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`Rate limiter toy running on port ${PORT}`);
  console.log(`Limit: ${LIMIT} requests per ${WINDOW_MS/1000}s`);
});
