// Teste de performance para cancelamento de reservas
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter } from 'k6/metrics';

function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

const BASE_URL = 'http://localhost:8080/magiclook';

// Métricas customizadas
const cancellations = new Counter('booking_cancellations');
const cancellationErrors = new Counter('cancellation_errors');

export const options = {
  scenarios: {
    cancellation_load: {
      executor: 'constant-vus',
      vus: 30,
      duration: '3m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.1'],
  },
};

const USERS = Array.from({ length: 50 }, (_, i) => ({
  username: `testuser${i}`,
  password: 'test123',
}));

const TEST_ITEMS = [1, 2, 3, 4, 5];

export default function () {
  const user = randomItem(USERS);
  let authHeaders = null;
  let bookingId = null;

  // 1. Login
  group('login_for_cancellation', function () {
    const loginRes = http.post(
      `${BASE_URL}/login`,
      `username=${user.username}&password=${user.password}`,
      {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        redirects: 0,
      }
    );

    if (loginRes.cookies?.JSESSIONID && loginRes.cookies.JSESSIONID.length > 0) {
      authHeaders = {
        'Cookie': `JSESSIONID=${loginRes.cookies.JSESSIONID[0].value}`,
      };
    }

    check(loginRes, {
      'login ok': (r) => r.status === 200 || r.status === 302,
    });
  });

  if (!authHeaders) return;

  // 2. Criar uma reserva
  group('create_booking_for_cancel', function () {
    const item = randomItem(TEST_ITEMS);
    const startDate = getFutureDate(2 + randomIntBetween(0, 5));
    const endDate = getFutureDate(4 + randomIntBetween(0, 5));

    const bookingRes = http.post(
      `${BASE_URL}/booking/create`,
      `itemId=${item}&size=M&startUseDate=${startDate}&endUseDate=${endDate}`,
      {
        headers: { 
          ...authHeaders,
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      }
    );

    check(bookingRes, {
      'booking created': (r) => r.status === 200 || r.status === 302,
    });

    if (bookingRes.status === 200 || bookingRes.status === 302) {
      // Tentar extrair booking ID da resposta
      if (bookingRes.body.includes('booking')) {
        // Assumimos que o ID é formato e pode ser extraído
        const matches = bookingRes.body.match(/booking[\/-](\d+)/);
        if (matches && matches[1]) {
          bookingId = matches[1];
        }
      }
    }

    sleep(0.5);
  });

  // 3. Ver lista de reservas e extrair booking ID
  group('view_bookings_list', function () {
    const bookingsRes = http.get(`${BASE_URL}/my-bookings`, { 
      headers: authHeaders 
    });

    check(bookingsRes, {
      'bookings list ok': (r) => r.status === 200,
    });

    // Tentar extrair booking ID da página HTML
    if (bookingsRes.body) {
      const matches = bookingsRes.body.match(/my-bookings\/(\d+)/);
      if (matches && matches[1]) {
        bookingId = matches[1];
      }
    }

    sleep(1);
  });

  // 4. Tentar cancelar a reserva
  if (bookingId) {
    group('cancel_booking', function () {
      // Primeiro, obter informações do cancelamento
      const cancelInfoRes = http.get(
        `${BASE_URL}/my-bookings/${bookingId}/cancel-info`,
        { headers: authHeaders }
      );

      check(cancelInfoRes, {
        'cancel info loads': (r) => r.status === 200,
      });

      sleep(0.5);

      // Depois, executar o cancelamento
      const cancelRes = http.post(
        `${BASE_URL}/my-bookings/${bookingId}/cancel`,
        '',
        {
          headers: authHeaders,
        }
      );

      check(cancelRes, {
        'cancel successful': (r) => r.status === 200 || r.status === 302,
      });

      if (cancelRes.status === 200 || cancelRes.status === 302) {
        cancellations.add(1);
      } else {
        cancellationErrors.add(1);
      }

      sleep(1);
    });
  } else {
    group('skip_cancel_no_booking', function () {
      cancellationErrors.add(1);
      console.log(`Skipping cancellation test - no booking ID extracted for user ${user.username}`);
    });
  }

  // 5. Verificar reservas após cancelamento
  group('verify_after_cancel', function () {
    const bookingsRes = http.get(`${BASE_URL}/my-bookings`, { 
      headers: authHeaders 
    });

    check(bookingsRes, {
      'bookings list updated': (r) => r.status === 200,
    });
  });

  sleep(2);
}

function getFutureDate(daysFromNow) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
}

export function setup() {
  const res = http.get(`${BASE_URL}/login`);
  if (res.status !== 200) {
    throw new Error(`Application not accessible. Status: ${res.status}`);
  }
  return { timestamp: new Date().toISOString() };
}

export function teardown(data) {
  console.log(`\nCancellation Test Summary:`);
  console.log(`Started at: ${data.timestamp}`);
}
