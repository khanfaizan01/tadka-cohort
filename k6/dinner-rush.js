import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
};

export default function () {
  const res = http.get('http://localhost:5224/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001');
  check(res, { 'status was 200': (r) => r.status === 200 });
  sleep(1);
}
