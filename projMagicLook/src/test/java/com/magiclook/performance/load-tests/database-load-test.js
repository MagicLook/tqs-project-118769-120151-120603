import http from 'k6/http';
import { check, sleep, group } from 'k6';

export const options = {
  scenarios: {
    db_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    'http_req_duration{type:heavy_query}': ['p(95)<3000'],
    'http_req_duration{type:simple_query}': ['p(95)<500'],
    'http_req_failed': ['rate<0.05'],
  },
};

export default function () {
  group('simple_db_queries', function () {
    // Queries simples (devem ser rápidas)
    const simpleQueries = [
      '/api/items/1/check?size=M&start=2024-12-20&end=2024-12-22',
      '/api/items/2/check?size=L&start=2024-12-21&end=2024-12-23',
    ];
    
    const simpleEndpoint = simpleQueries[Math.floor(Math.random() * simpleQueries.length)];
    
    const simpleRes = http.get(`http://localhost:8080/magiclook${simpleEndpoint}`, {
      tags: { type: 'simple_query' },
    });
    
    check(simpleRes, {
      'simple query success': (r) => r.status === 200 || r.status === 400,
      'simple query fast': (r) => r.timings.duration < 500,
    });

    sleep(0.5);
  });

  group('list_queries', function () {
    // Queries de listagem (podem ser mais pesadas)
    const listEndpoints = [
      '/items/men',
      '/items/women',
      '/my-bookings',
    ];
    
    const listEndpoint = listEndpoints[Math.floor(Math.random() * listEndpoints.length)];
    
    const listRes = http.get(`http://localhost:8080/magiclook${listEndpoint}`, {
      tags: { type: 'moderate_query' },
    });
    
    check(listRes, {
      'list query success': (r) => r.status === 200,
      'list query acceptable': (r) => r.timings.duration < 2000,
    });

    sleep(1);
  });

  // Ocasionalmente, fazer uma operação de escrita
  if (Math.random() < 0.2) {
    group('write_operations', function () {
      // Simular operações de escrita são testes mais específicos
      // Este teste apenas demonstra read-heavy loads
      
      const startDate = getFutureDate(2);
      const endDate = getFutureDate(4);
      
      const checkRes = http.get(
        `http://localhost:8080/magiclook/api/items/1/check?size=M&start=${startDate}&end=${endDate}`,
        { tags: { type: 'write_check' } }
      );

      check(checkRes, {
        'write check success': (r) => r.status === 200 || r.status === 400,
      });
    });
  }
}

function getFutureDate(daysFromNow) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
}

export function setup() {
  console.log('Iniciando teste de carga de base de dados...');
  console.log('Este teste valida performance de queries simples vs complexas');
  return { timestamp: new Date().toISOString() };
}

export function teardown(data) {
  console.log(`\n=== DATABASE LOAD TEST RESULTS ===`);
  console.log(`Timestamp: ${data.timestamp}`);
  console.log('Verifique as métricas acima para avaliar performance da BD');
}
