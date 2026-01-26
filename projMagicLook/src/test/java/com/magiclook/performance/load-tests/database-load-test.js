import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 10,
  duration: '2m',
  thresholds: {
    'http_req_duration{type:heavy_query}': ['p(95)<3000'],
    'http_req_duration{type:simple_query}': ['p(95)<500'],
  },
};

export default function () {
  // Testar queries pesadas
  const heavyQueries = [
    '/api/items?category=dresses&availability=true&size=M&sort=popular',
    '/api/bookings?status=active&from=2024-01-01&to=2024-12-31',
    '/api/users/search?q=a&limit=100', // LIKE queries
  ];
  
  // Queries simples
  const simpleQueries = [
    '/api/items/123',
    '/api/categories',
    '/api/stats/summary',
  ];
  
  const queryType = Math.random() > 0.7 ? 'heavy' : 'simple';
  const queries = queryType === 'heavy' ? heavyQueries : simpleQueries;
  const endpoint = queries[Math.floor(Math.random() * queries.length)];
  
  const res = http.get(`http://localhost:8080/magiclook${endpoint}`, {
    tags: { type: `${queryType}_query` },
  });
  
  check(res, {
    [`${queryType} query success`]: (r) => r.status === 200,
  });
}