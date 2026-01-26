import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { randomIntBetween, randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = 'http://localhost:8080/magiclook';

export const options = {
  scenarios: {
    constant_load: {
      executor: 'constant-vus',
      vus: 100,  // 100 usuários simultâneos
      duration: '5m',  // 5 minutos
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.05'],
  },
};

const testItems = [
  { id: 1, size: 'S' },
  { id: 2, size: 'M' },
  { id: 3, size: 'L' },
  { id: 4, size: 'XL' },
  { id: 5, size: 'M' },
];

export default function () {
  const vu = __VU;
  const iter = __ITER;
  
  // Dados únicos por VU+iteração
  const userData = {
    username: `loadtest${vu}_${iter}_${Date.now()}`,
    firstName: `User${vu}`,
    lastName: `Test${iter}`,
    email: `load${vu}.${iter}.${Date.now()}@test.com`,
    phone: `91${randomIntBetween(1000000, 9999999)}`,
    password: 'Password123',
  };

  // 1. Registro
  group('register', function () {
    const registerRes = http.post(`${BASE_URL}/register`, {
      username: userData.username,
      firstName: userData.firstName,
      lastName: userData.lastName,
      email: userData.email,
      phone: userData.phone,
      password: userData.password,
      confirmPassword: userData.password,
    }, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });
    
    check(registerRes, {
      'register responded': (r) => r.status === 200 || r.status === 302,
    });
    
    sleep(randomIntBetween(1, 3));
  });

  // 2. Login
  let sessionCookie = null;
  group('login', function () {
    const loginRes = http.post(`${BASE_URL}/login`, {
      username: userData.username,
      password: userData.password,
    }, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      redirects: 0,
    });
    
    if (loginRes.cookies && loginRes.cookies.JSESSIONID) {
      sessionCookie = `JSESSIONID=${loginRes.cookies.JSESSIONID[0].value}`;
    }
    
    check(loginRes, {
      'login processed': (r) => r.status === 200 || r.status === 302,
    });
    
    sleep(randomIntBetween(1, 2));
  });

  // Se não conseguiu login, termina
  if (!sessionCookie) return;

  const authHeaders = {
    'Cookie': sessionCookie,
    'Content-Type': 'application/x-www-form-urlencoded',
  };

  // 3. Navegação
  group('navigation', function () {
    http.get(`${BASE_URL}/dashboard`, { headers: authHeaders });
    sleep(0.5);
    
    const gender = randomItem(['men', 'women']);
    http.get(`${BASE_URL}/items/${gender}`, { headers: authHeaders });
    
    sleep(randomIntBetween(1, 2));
  });

  // 4. Verificação de disponibilidade
  group('availability_check', function () {
    const item = randomItem(testItems);
    const startDate = getFutureDate(2 + randomIntBetween(0, 14));
    const endDate = getFutureDate(5 + randomIntBetween(0, 14));
    
    const checkRes = http.get(
      `${BASE_URL}/api/items/${item.id}/check?size=${item.size}&start=${startDate}&end=${endDate}`,
      { headers: authHeaders }
    );
    
    check(checkRes, {
      'availability checked': (r) => r.status === 200,
    });
    
    sleep(1);
  });

  // 5. Tentativa de reserva
  group('booking_attempt', function () {
    const item = randomItem(testItems);
    const startDate = getFutureDate(1 + randomIntBetween(0, 7));
    const endDate = getFutureDate(3 + randomIntBetween(0, 7));
    
    const bookingRes = http.post(`${BASE_URL}/booking/create`, {
      itemId: item.id.toString(),
      size: item.size,
      startUseDate: startDate,
      endUseDate: endDate,
    }, {
      headers: authHeaders,
    });
    
    check(bookingRes, {
      'booking processed': (r) => r.status === 200 || r.status === 302,
    });
    
    sleep(randomIntBetween(1, 3));
  });

  // 6. Listagem de reservas
  group('bookings_list', function () {
    http.get(`${BASE_URL}/my-bookings`, { headers: authHeaders });
    sleep(0.5);
    
    // Testar filtros
    const filter = randomItem(['active', 'past', null]);
    if (filter) {
      http.get(`${BASE_URL}/my-bookings?filter=${filter}`, { headers: authHeaders });
    }
    
    sleep(1);
  });

  // 7. Logout
  group('logout', function () {
    http.get(`${BASE_URL}/logout`, { headers: authHeaders });
  });
}

function getFutureDate(daysFromNow) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
}

export function setup() {
  // Teste básico de conectividade
  const res = http.get(`${BASE_URL}/login`);
  if (res.status !== 200) {
    throw new Error(`Application not accessible. Status: ${res.status}`);
  }
  
  return { startTime: new Date().toISOString() };
}

export function teardown(data) {
}