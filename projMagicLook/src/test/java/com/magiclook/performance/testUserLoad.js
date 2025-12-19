// magiclook-clean-load.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080/magiclook';

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '30s', target: 150 },
    { duration: '1m', target: 150 },
    { duration: '30s', target: 100 },
    { duration: '30s', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1500'],
    http_req_failed: ['rate<0.03'],
  },
};

const USER_POOL = Array.from({ length: 1000 }, (_, i) => `testuser${i}`);
const ITEMS = [1, 2, 3, 4, 5];

export default function () {
  const userIndex = __VU * __ITER % USER_POOL.length;
  const username = USER_POOL[userIndex];
  
  // Login (assume users pre-created)
  const loginRes = http.post(`${BASE_URL}/login`, {
    username: username,
    password: 'test123',
  }, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    redirects: 0,
  });
  
  // If login fails, try another user
  if (!loginRes.cookies?.JSESSIONID) return;
  
  const sessionCookie = `JSESSIONID=${loginRes.cookies.JSESSIONID[0].value}`;
  const authHeaders = {
    'Cookie': sessionCookie,
    'Content-Type': 'application/x-www-form-urlencoded',
  };
  
  // Mixed workload
  const action = Math.random();
  
  if (action < 0.3) {
    // Browse items
    http.get(`${BASE_URL}/dashboard`, { headers: authHeaders });
    http.get(`${BASE_URL}/items/${Math.random() > 0.5 ? 'men' : 'women'}`, { 
      headers: authHeaders 
    });
  } else if (action < 0.6) {
    // Check availability
    const itemId = ITEMS[Math.floor(Math.random() * ITEMS.length)];
    const start = getFutureDate(2);
    const end = getFutureDate(5);
    
    http.get(
      `${BASE_URL}/api/items/${itemId}/check?size=M&start=${start}&end=${end}`,
      { headers: authHeaders }
    );
  } else if (action < 0.9) {
    // Try booking
    const itemId = ITEMS[Math.floor(Math.random() * ITEMS.length)];
    const start = getFutureDate(1);
    const end = getFutureDate(3);
    
    http.post(`${BASE_URL}/booking/create`, {
      itemId: itemId.toString(),
      size: 'M',
      startUseDate: start,
      endUseDate: end,
    }, {
      headers: authHeaders,
    });
  } else {
    // View bookings
    http.get(`${BASE_URL}/my-bookings`, { headers: authHeaders });
  }
  
  sleep(1);
}

function getFutureDate(daysFromNow) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
}