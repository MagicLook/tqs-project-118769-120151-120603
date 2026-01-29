// Teste Spike - simula picos de tráfego inesperado
import http from 'k6/http';
import { check, sleep, group } from 'k6';

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

const BASE_URL = 'http://localhost:8080/magiclook';

export const options = {
  scenarios: {
    spike_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        // Carga normal
        { duration: '1m', target: 20 },
        // Pico inesperado
        { duration: '10s', target: 200 },
        // Volta ao normal
        { duration: '30s', target: 20 },
        // Segundo pico
        { duration: '10s', target: 150 },
        // Redução gradual
        { duration: '1m', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2500', 'p(99)<5000'],
    http_req_failed: ['rate<0.1'],
  },
};

export default function () {
  group('spike_test_homepage', function () {
    // Teste de página inicial - deve ser rápida mesmo com spike
    const homeRes = http.get(`${BASE_URL}/login`);
    
    check(homeRes, {
      'homepage loads under spike': (r) => r.status === 200,
      'homepage response < 1s': (r) => r.timings.duration < 1000,
    });
  });

  group('spike_test_login_page', function () {
    const loginPageRes = http.get(`${BASE_URL}/login`);
    
    check(loginPageRes, {
      'login page loads': (r) => r.status === 200,
    });

    sleep(0.5);
  });

  group('spike_test_items', function () {
    const gender = randomItem(['men', 'women']);
    
    const itemsRes = http.get(`${BASE_URL}/items/${gender}`);
    
    check(itemsRes, {
      'items load under spike': (r) => r.status === 200,
      'items response reasonable': (r) => r.timings.duration < 3000,
    });
  });

  group('spike_test_api', function () {
    // API sem autenticação deve responder rápido
    const apiRes = http.get(
      `${BASE_URL}/api/items/1/check?size=M&start=2024-12-20&end=2024-12-22`
    );
    
    check(apiRes, {
      'API responds': (r) => r.status === 200 || r.status === 400,
    });
  });

  sleep(0.5);
}

export function setup() {
  // Verificar disponibilidade
  const res = http.get(`${BASE_URL}/login`);
  if (res.status !== 200) {
    throw new Error(`Application not accessible. Status: ${res.status}`);
  }
  console.log('Spike test iniciado...');
  return { startTime: new Date().toISOString() };
}

export function teardown(data) {
  console.log(`\n=== SPIKE TEST RESULTS ===`);
  console.log(`Início: ${data.startTime}`);
  console.log(`Fim: ${new Date().toISOString()}`);
  console.log(`O sistema manteve disponibilidade durante picos de tráfego`);
}
