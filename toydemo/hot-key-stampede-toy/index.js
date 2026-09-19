/**
 * Hot key stampede toy (Day 6, ADR-019).
 * Demonstrates the difference between:
 * - No protection: N concurrent requests all hit the DB
 * - Single-flight (SET NX lock): only 1 refreshes, others wait
 */
const express = require('express');
const app = express();
const PORT = 3002;

// Simulated "cache" and "DB"
const cache = new Map();
const DB_LATENCY_MS = 1000; // Simulate slow DB

// Simulated DB query
function fetchFromDB(key) {
  return new Promise(resolve => {
    setTimeout(() => resolve({ data: `result-for-${key}`, from: 'db' }), DB_LATENCY_MS);
  });
}

// No-protection endpoint
app.get('/no-protection/:key', async (req, res) => {
  const key = req.params.key;
  const cached = cache.get(key);
  if (cached) return res.json(cached);
  // Every request hits the DB — stampede!
  const result = await fetchFromDB(key);
  cache.set(key, result);
  res.json(result);
});

// Single-flight endpoint
app.get('/single-flight/:key', async (req, res) => {
  const key = req.params.key;
  const cached = cache.get(key);
  if (cached) return res.json(cached);

  const lockKey = `lock:${key}`;
  if (!cache.has(lockKey)) {
    cache.set(lockKey, true); // Acquire lock
    try {
      const result = await fetchFromDB(key);
      cache.set(key, result);
    } finally {
      cache.delete(lockKey); // Release lock
    }
  } else {
    // Wait and retry (simplified)
    await new Promise(r => setTimeout(r, 200));
  }
  res.json(cache.get(key) || { data: 'fallback', from: 'db' });
});

app.listen(PORT, () => {
  console.log(`Hot key stampede toy running on port ${PORT}`);
  console.log(`Try: curl http://localhost:${PORT}/no-protection/popular-key`);
  console.log(`Try: curl http://localhost:${PORT}/single-flight/popular-key`);
});
