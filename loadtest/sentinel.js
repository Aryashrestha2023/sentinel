import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    steady_ingestion: {
      executor: 'constant-arrival-rate',
      rate: 100,
      timeUnit: '1s',
      duration: '60s',
      preAllocatedVUs: 20,
      maxVUs: 100,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<250'],
  },
};

export default function () {
  const body = JSON.stringify({
    eventId: `${__VU}-${__ITER}-${Date.now()}`,
    source: 'payment-gateway',
    entityId: `merchant-${__VU % 50}`,
    metric: 'authorization_latency_ms',
    value: 80 + Math.random() * 20,
    timestamp: new Date().toISOString(),
    attributes: { region: 'ap-south-1', channel: 'card' },
  });

  const res = http.post('http://localhost:8080/v1/events', body, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, { 'accepted': (r) => r.status === 202 });
  sleep(0.01);
}
