import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const availabilityTime = new Trend('availability_check_time');

export const options = {
  scenarios: {
    availability_load: {
      executor: 'constant-arrival-rate',
      rate: 50,           // 50 requisições por segundo
      timeUnit: '1s',
      duration: '3m',
      preAllocatedVUs: 20,
      maxVUs: 100,
    },
  },
};

export default function () {
  // Testar diferentes combinações
  const items = Array.from({length: 10}, (_, i) => i + 1);
  const sizes = ['XS', 'S', 'M', 'L', 'XL'];
  const daysFromNow = Math.floor(Math.random() * 30) + 1;
  
  const params = {
    itemId: items[Math.floor(Math.random() * items.length)],
    size: sizes[Math.floor(Math.random() * sizes.length)],
    start: getFutureDate(daysFromNow),
    end: getFutureDate(daysFromNow + Math.floor(Math.random() * 7) + 1),
  };
  
  const startTime = Date.now();
  
  const res = http.get(
    `http://localhost:8080/magiclook/api/items/${params.itemId}/check?size=${params.size}&start=${params.start}&end=${params.end}`
  );
  
  availabilityTime.add(Date.now() - startTime);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(0.1);
}

function getFutureDate(daysFromNow) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
}