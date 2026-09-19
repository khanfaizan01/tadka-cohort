/**
 * Load client for rate limiter toy.
 * Sends rapid requests to demonstrate rate limiting.
 */
const axios = require('axios');
const BASE = 'http://localhost:3001';

async function burst(count) {
  console.log(`Sending ${count} rapid requests...`);
  const promises = [];
  for (let i = 0; i < count; i++) {
    promises.push(axios.get(`${BASE}/api/data`).then(r => ({ status: r.status }))
      .catch(e => ({ status: e.response?.status || 'error' })));
  }
  const results = await Promise.all(promises);
  const counts = {};
  results.forEach(r => { counts[r.status] = (counts[r.status] || 0) + 1; });
  console.log('Results:', counts);
}

burst(10);
