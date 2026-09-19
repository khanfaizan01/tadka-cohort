import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 5 },
  ],
};

export default function () {
  // SSE connection (keep-alive)
  const res = http.get('http://localhost:5224/api/v1/orders/00000000-0000-0000-0000-000000000001/events', {
    headers: { 'Accept': 'text/event-stream' },
    timeout: '30s',
  });
  check(res, { 'SSE stream started': (r) => r.status === 200 });
}
