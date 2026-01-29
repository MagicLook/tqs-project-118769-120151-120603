// Teste de performance para funcionalidades de filtro e busca
import http from 'k6/http';
import { check, sleep, group } from 'k6';

function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

const BASE_URL = 'http://localhost:8080/magiclook';

export const options = {
  scenarios: {
    filter_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '2m', target: 50 },
        { duration: '30s', target: 20 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<3000'],
    http_req_failed: ['rate<0.05'],
  },
};

const USERS = Array.from({ length: 100 }, (_, i) => ({
  username: `filtertest${i}`,
  password: 'test123',
}));

export default function () {
  const user = randomItem(USERS);
  let authHeaders = null;

  // 1. Login
  group('login_for_filter_test', function () {
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

  // 2. Testar filtros de produtos
  group('product_filters', function () {
    const gender = randomItem(['men', 'women']);
    
    // Requisição base (sem filtro)
    const itemsRes = http.get(`${BASE_URL}/items/${gender}`, { 
      headers: authHeaders 
    });

    check(itemsRes, {
      'items page loads': (r) => r.status === 200,
    });

    sleep(0.5);

    // Com filtro aplicado
    const filterData = `gender=${gender}&minPrice=10&maxPrice=500`;
    const filteredRes = http.post(
      `${BASE_URL}/items/${gender}/filter`,
      filterData,
      {
        headers: authHeaders,
      }
    );

    check(filteredRes, {
      'filter works': (r) => r.status === 200,
      'returns results': (r) => r.body.length > 0,
    });

    sleep(1);
  });

  // 3. Teste de limpar filtros
  group('clear_filters', function () {
    const gender = randomItem(['men', 'women']);
    
    const clearRes = http.get(`${BASE_URL}/items/${gender}/clear`, { 
      headers: authHeaders 
    });

    check(clearRes, {
      'clear filters ok': (r) => r.status === 200 || r.status === 302,
    });

    sleep(0.5);
  });

  // 4. Teste de múltiplas consultas
  group('multiple_category_views', function () {
    const categories = ['men', 'women'];
    
    categories.forEach((category) => {
      const res = http.get(`${BASE_URL}/items/${category}`, { 
        headers: authHeaders 
      });

      check(res, {
        [`${category} category loads`]: (r) => r.status === 200,
      });

      sleep(0.3);
    });
  });
}

export function setup() {
  const res = http.get(`${BASE_URL}/login`);
  if (res.status !== 200) {
    throw new Error(`Application not accessible. Status: ${res.status}`);
  }
  return { timestamp: new Date().toISOString() };
}
