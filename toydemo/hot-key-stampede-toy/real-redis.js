/**
 * Hot key stampede toy — real Redis edition (Day 6, ADR-019).
 *
 * Demonstrates the difference between:
 * - No protection: N concurrent requests all hit Redis + DB on cache miss
 * - SET NX single-flight: only ONE request refreshes, others wait for the result
 *
 * Run:
 *   cd toydemo/hot-key-stampede-toy
 *   node real-redis.js &
 *   # Then fire concurrent requests to http://localhost:3004/hot/:key
 */

const express = require('express');
const Redis = require('ioredis');
const app = express();
const PORT = 3004;

const redis = new Redis({ host: 'localhost', port: 6379 });
const CACHE_TTL_SECONDS = 10;
const HOT_KEY = 'popular-item';

async function fetchFromDB(key) {
  // Simulate slow DB query
  await new Promise(resolve => setTimeout(resolve, 500));
  return { id: key, data: `expensive-data-for-${key}`, computedAt: Date.now() };
}

// --- No protection: stampede ---
app.get('/nostampede/:key', async (req, res) => {
  const key = `${HOT_KEY}:${req.params.key}`;
  const cached = await redis.get(key);
  if (cached) return res.json(JSON.parse(cached));

  // EVERY concurrent request hits fetchFromDB — stampede!
  const result = await fetchFromDB(key);
  await redis.setex(key, CACHE_TTL_SECONDS, JSON.stringify(result));
  res.json(result);
});

// --- Single-flight: SET NX protection ---
app.get('/stampede/:key', async (req, res) => {
  const key = `${HOT_KEY}:${req.params.key}`;
  const lockKey = `${key}:lock`;

  const cached = await redis.get(key);
  if (cached) return res.json(JSON.parse(cached));

  // Try to acquire a lock with SET NX (only one request wins)
  const lockAcquired = await redis.set(lockKey, '1', 'EX', 5, 'NX');
  if (!lockAcquired) {
    // Another request is refreshing — poll until result is ready
    for (let i = 0; i < 10; i++) {
      await new Promise(r => setTimeout(r, 100));
      const fresh = await redis.get(key);
      if (fresh) return res.json(JSON.parse(fresh));
    }
    // Lock expired and no result — fall through to refresh
  }

  // This request refreshes the cache
  try {
    const result = await fetchFromDB(key);
    await redis.setex(key, CACHE_TTL_SECONDS, JSON.stringify(result));
    res.json(result);
  } finally {
    await redis.del(lockKey);
  }
});

app.listen(PORT, () => console.log(`Hot key stampede toy on http://localhost:${PORT}`));
